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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static com.github.cimsbioko.server.util.JDOMUtil.getBuilder;
import static com.github.cimsbioko.server.util.JDOMUtil.getOutputter;
import static com.github.cimsbioko.server.webapi.odk.Constants.*;
import static org.springframework.util.FileCopyUtils.copy;

public class FormServiceImpl implements FormService {

    private static final Logger log = LoggerFactory.getLogger(FormServiceImpl.class);
    public static final String CONVERTED_XML_NAME = "form.xml";

    private FormRepository formDao;

    private FileHasher hasher;

    private FormFileSystem formFileSystem;

    private XLSFormService xlsformService;

    public FormServiceImpl(FormRepository repo, FileHasher hasher, FormFileSystem fs, XLSFormService xlsformService) {
        this.formDao = repo;
        this.hasher = hasher;
        this.formFileSystem = fs;
        this.xlsformService = xlsformService;
    }

    @Override
    @Transactional
    public void uploadForm(MultipartFile formXml, MultipartFile xlsform, MultiValueMap<String, MultipartFile> uploadedFiles) throws JDOMException, IOException, NoSuchAlgorithmException {
        installFormWithMedia(xlsform, uploadedFiles, formXml.getInputStream());
    }

    @Override
    @Transactional
    public void uploadXlsform(MultipartFile xlsform, MultiValueMap<String, MultipartFile> uploadedFiles) throws JDOMException, IOException, NoSuchAlgorithmException {
        try (InputStream xlsInput = xlsform.getInputStream();
             ZipFile converted = xlsformService.convertXLSForm(xlsInput)) {
            installConvertedFormWithMedia(xlsform, uploadedFiles, converted);
        }
    }

    @Override
    public void exportToStream(String id, String version, OutputStream outputStream) throws IOException {
        File formDir = formFileSystem.getFormDirPath(id, version).toFile();
        if (formDir.exists() && formDir.isDirectory()) {
            try (ZipOutputStream zOut = new ZipOutputStream(outputStream)) {
                for (File file : formDir.listFiles((f) -> !f.isHidden())) {
                    try (FileInputStream fileIn = new FileInputStream(file)) {
                        ZipEntry e = new ZipEntry(file.getName());
                        e.setSize(file.length());
                        e.setTime(file.lastModified());
                        zOut.putNextEntry(e);
                        StreamUtils.copy(fileIn, zOut);
                    }
                }
                zOut.closeEntry();
                zOut.finish();
            }
        }
    }

    @Override
    @Transactional
    public void manageForm(String id, String version, boolean downloads, boolean submissions) {
        formDao.findById(new FormId(id, version)).ifPresent((form) -> {
            if (form.isSubmissions() != submissions) {
                form.setSubmissions(submissions);
            }
            if (form.isDownloads() != downloads) {
                if (downloads) {
                    form.setDownloads(true);
                    formDao.exclusiveDownload(id, version);
                } else {
                    form.setDownloads(false);
                }
            }
        });
    }

    private void installConvertedFormWithMedia(MultipartFile xlsform, MultiValueMap<String, MultipartFile> uploadedFiles, ZipFile converted)
            throws JDOMException, IOException, NoSuchAlgorithmException {
        try (InputStream xmlInput = converted.getInputStream(new ZipEntry(CONVERTED_XML_NAME))) {
            FormId id = createOrUpdateForm(xmlInput);
            Map<String, MultipartFile> uploadedMedia = extractMediaFromUploads(uploadedFiles);
            writeUploadedMediaFiles(id, uploadedMedia);
            List<String> writtenConverted = writeConvertedMediaFiles(id, converted);
            Collection<String> writtenMedia = Stream
                    .concat(uploadedMedia.keySet().stream(), writtenConverted.stream())
                    .collect(Collectors.toList());
            writeManifest(id, writtenMedia);
            writeXlsform(id, xlsform);
        }
    }

    private List<String> writeConvertedMediaFiles(FormId formId, ZipFile converted) {

        File formDir = formFileSystem.getFormDirPath(formId.getId(), formId.getVersion()).toFile();

        List<ZipEntry> media = converted
                .stream()
                .filter(e -> !e.isDirectory() && !CONVERTED_XML_NAME.equalsIgnoreCase(e.getName()))
                .collect(Collectors.toList());

        media.removeIf(e -> {
            File dest = new File(formDir, e.getName());
            try (FileOutputStream out = new FileOutputStream(dest)) {
                copy(converted.getInputStream(e), out);
                return false;
            } catch (IOException e1) {
                return true;
            }
        });

        return media.stream().map(ZipEntry::getName).collect(Collectors.toList());
    }

    private void installFormWithMedia(MultipartFile xlsform, MultiValueMap<String, MultipartFile> uploadedFiles, InputStream xmlInput) throws JDOMException, IOException, NoSuchAlgorithmException {
        FormId id = createOrUpdateForm(xmlInput);
        Map<String, MultipartFile> mediaFiles = extractMediaFromUploads(uploadedFiles);
        writeUploadedMediaFiles(id, mediaFiles);
        writeManifest(id, mediaFiles.keySet());
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
        // FIXME: Use optional rather than null
        Form form = formDao.findById(formId).orElse(null);

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

    private void writeManifest(FormId formId, Collection<String> mediaFileNames) throws IOException, NoSuchAlgorithmException {

        File formDir = formFileSystem.getFormDirPath(formId.getId(), formId.getVersion()).toFile();

        // create a new dom object for the form's media manifest
        Document manifestDoc = new Document();
        Namespace manifestNs = Namespace.getNamespace("http://openrosa.org/xforms/xformsManifest");
        Element manifestElem = new Element(MANIFEST, manifestNs);
        manifestDoc.setRootElement(manifestElem);

        for (String mediaFileName : mediaFileNames) {

            File mediaFile = new File(formDir, mediaFileName);

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

    private void writeUploadedMediaFiles(FormId formId, Map<String, MultipartFile> mediaFiles) {
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
