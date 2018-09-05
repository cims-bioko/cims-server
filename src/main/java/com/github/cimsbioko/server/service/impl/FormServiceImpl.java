package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.FormDao;
import com.github.cimsbioko.server.domain.Form;
import com.github.cimsbioko.server.domain.FormId;
import com.github.cimsbioko.server.service.FormService;
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

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    @Resource
    private File formsDir;

    @Autowired
    private FormDao formDao;

    @Autowired
    FileHasher hasher;

    @Autowired
    FormFileSystem formFileSystem;

    @Override
    @Transactional
    public void uploadForm(MultipartFile formXml, MultiValueMap<String, MultipartFile> uploadedFiles) throws JDOMException, IOException, NoSuchAlgorithmException {
        File formDir = createOrUpdateForm(formXml);
        Map<String, MultipartFile> mediaFiles = extractMediaFromUploads(uploadedFiles);
        writeMediaFiles(formDir, mediaFiles);
        writeManifest(formDir, mediaFiles);
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

    private File createOrUpdateForm(MultipartFile formXml) throws JDOMException, IOException {

        // make xml into dom object
        Document formDoc = getBuilder().build(formXml.getInputStream());

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
        Form form = formDao.findById(formId);

        // create or update the form in the database
        if (form == null) {
            log.info("uploading new form");
            form = new Form(formId, formDoc);
            formDao.save(form);
        } else {
            log.info("updating existing form");
            form.setXml(formDoc);
        }
        formDao.exclusiveDownload(form);

        // identify where to store the file on the file system
        String formFilePath = formFileSystem.getFormFilePath(id, version);
        log.info("storing form at {}", formFilePath);

        File formFile = new File(formsDir, formFilePath);
        File formDir = formFile.getParentFile();

        // ensure the directory exists prior to attempting to write the file
        formDir.mkdirs();

        // write the form definition file
        try (FileWriter writer = new FileWriter(formFile)) {
            getOutputter().output(formDoc, writer);
        }

        return formDir;
    }

    private void writeManifest(File formDir, Map<String, MultipartFile> mediaFiles) throws IOException, NoSuchAlgorithmException {
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

    private void writeMediaFiles(File formDir, Map<String, MultipartFile> mediaFiles) {
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
