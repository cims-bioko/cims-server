package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.FormRepository;
import com.github.cimsbioko.server.domain.Form;
import com.github.cimsbioko.server.domain.FormId;
import com.github.cimsbioko.server.service.FormService;
import com.github.cimsbioko.server.service.XLSFormService;
import com.github.cimsbioko.server.webapi.odk.FileHasher;
import com.github.cimsbioko.server.webapi.odk.FormFileSystem;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.cimsbioko.server.util.JDOMUtil.getBuilder;
import static com.github.cimsbioko.server.util.JDOMUtil.getOutputter;
import static com.github.cimsbioko.server.webapi.odk.Constants.*;

@Service
public class FormServiceImpl implements FormService {

    private static final Logger log = LoggerFactory.getLogger(FormServiceImpl.class);

    @Autowired
    private FormRepository formDao;

    @Autowired
    private FileHasher hasher;

    @Autowired
    private FormFileSystem formFileSystem;

    @Autowired
    private XLSFormService xlsformService;

    @Override
    @Transactional
    public void uploadForm(MultipartFile formXml, MultipartFile xlsform, MultiValueMap<String, MultipartFile> uploadedFiles) throws JDOMException, IOException, NoSuchAlgorithmException {
        installFormWithMedia(xlsform, uploadedFiles, formXml.getInputStream());
    }

    @Override
    public void uploadXlsform(MultipartFile xlsform, MultiValueMap<String, MultipartFile> uploadedFiles) throws JDOMException, IOException, NoSuchAlgorithmException {
        try (InputStream xmlInput = new FileInputStream(xlsformService.generateXForm(xlsform.getInputStream()))) {
            installFormWithMedia(xlsform, uploadedFiles, xmlInput);
        }
    }

    private void installFormWithMedia(MultipartFile xlsform, MultiValueMap<String, MultipartFile> uploadedFiles, InputStream xmlInput) throws JDOMException, IOException, NoSuchAlgorithmException {
        FormId id = createOrUpdateForm(xmlInput);
        Map<String, MultipartFile> mediaFiles = extractMediaFromUploads(uploadedFiles);
        writeMediaFiles(id, mediaFiles);
        writeManifest(id, mediaFiles);
        writeXlsform(id, xlsform);
    }

    private void writeXlsform(FormId id, MultipartFile xlsform) throws IOException {
        if (!(xlsform == null || xlsform.getOriginalFilename().isEmpty())) {
            xlsform.transferTo(formFileSystem.getXlsformPath(id.getId(), id.getVersion()).toFile());
        }
    }

    private Map<String, MultipartFile> extractMediaFromUploads(MultiValueMap<String, MultipartFile> uploadedFiles) {
        return uploadedFiles.entrySet()
                .stream()
                .filter(e -> !FORM_DEF_FILE.equalsIgnoreCase(e.getKey()))
                .filter(e -> !XLSFORM_DEF_FILE.equalsIgnoreCase(e.getKey()))
                .flatMap(e -> e.getValue().stream())
                .filter(f -> !f.getOriginalFilename().isEmpty())
                .filter(f -> !MEDIA_MANIFEST.equalsIgnoreCase(f.getOriginalFilename()))
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, Function.identity()));
    }

    private FormId createOrUpdateForm(InputStream xmlStream) throws JDOMException, IOException {

        // make xml into dom object
        Document formDoc = getBuilder().build(xmlStream);

        // create namespaces to we can unambiguously reference elements using dom api
        Namespace xformsNs = Namespace.getNamespace("http://www.w3.org/2002/xforms"),
                xhtmlNs = Namespace.getNamespace("http://www.w3.org/1999/xhtml");

        // grab the first data instance document (our data)
        Element firstInstance = formDoc.getRootElement()
                .getChild(HEAD, xhtmlNs)
                .getChild(MODEL, xformsNs)
                .getChild(INSTANCE, xformsNs)
                .getChildren()
                .get(0);

        // extract the id (required) and version (optional)
        String id = firstInstance.getAttributeValue(ID),
                version = firstInstance.getAttributeValue(VERSION);

        // assign a default version if none was specified
        if (version == null) {
            version = DEFAULT_VERSION;
            firstInstance.setAttribute(VERSION, version);
        }

        // try to lookup form based on form id and version
        FormId formId = new FormId(id, version);
        Form form = formDao.findOne(formId);

        // create or update the form in the database
        if (form == null) {
            log.info("uploading new form");
            form = new Form(formId, formDoc);
            formDao.save(form);
        } else {
            log.info("updating existing form");
            form.setXml(formDoc);
        }
        formDao.exclusiveDownload(id, version);

        // identify where to store the file on the file system
        Path formFilePath = formFileSystem.getXmlFormPath(id, version);
        log.info("storing form at {}", formFilePath);

        File formFile = formFilePath.toFile();
        File formDir = formFile.getParentFile();

        // ensure the directory exists prior to attempting to write the file
        formDir.mkdirs();

        // write the form definition file
        try (FileWriter writer = new FileWriter(formFile)) {
            getOutputter().output(formDoc, writer);
        }

        return formId;
    }

    private void writeManifest(FormId formId, Map<String, MultipartFile> mediaFiles) throws IOException, NoSuchAlgorithmException {

        File formDir = formFileSystem.getFormDirPath(formId.getId(), formId.getVersion()).toFile();

        // create a new dom object for the form's media manifest
        Document manifestDoc = new Document();
        Namespace manifestNs = Namespace.getNamespace("http://openrosa.org/xforms/xformsManifest");
        Element manifestElem = new Element(MANIFEST, manifestNs);
        manifestDoc.setRootElement(manifestElem);

        for (Map.Entry<String, MultipartFile> mediaFileEntry : mediaFiles.entrySet()) {

            File mediaFile = new File(formDir, mediaFileEntry.getKey());

            // add mediaFile element to the manifest for the file
            Element mediaFileElem = new Element(MEDIA_FILE, manifestNs);
            manifestElem.addContent(mediaFileElem);

            // add filename element to mediaFile element
            Element fileNameElem = new Element(FILENAME, manifestNs);
            fileNameElem.setText(mediaFile.getName());
            mediaFileElem.addContent(fileNameElem);

            // add hash element to the mediafile element
            Element hashElem = new Element(HASH, manifestNs);
            hashElem.setText(hasher.hashFile(mediaFile));
            mediaFileElem.addContent(hashElem);
        }

        // write the manifest file to the file system
        File manifestFile = new File(formDir, MEDIA_MANIFEST);
        try (FileWriter writer = new FileWriter(manifestFile)) {
            getOutputter().output(manifestDoc, writer);
        }
    }

    private void writeMediaFiles(FormId formId, Map<String, MultipartFile> mediaFiles) {
        File formDir = formFileSystem.getFormDirPath(formId.getId(), formId.getVersion()).toFile();
        mediaFiles.entrySet().removeIf(e -> {
            File dest = new File(formDir, e.getKey());
            try {
                e.getValue().transferTo(dest);
                return false;
            } catch (IOException e1) {
                return true;
            }
        });
    }
}
