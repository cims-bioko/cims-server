package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.controller.exception.ExistingSubmissionException;
import com.github.cimsbioko.server.controller.service.FormSubmissionService;
import com.github.cimsbioko.server.dao.FormSubmissionDao;
import com.github.cimsbioko.server.domain.model.FormSubmission;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.time.Instant.now;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.json.XML.toJSONObject;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.web.util.UrlUtils.buildFullRequestUrl;
import static org.springframework.util.StringUtils.isEmpty;

@Controller
public class ODKFormsResource {

    private static Logger log = LoggerFactory.getLogger(ODKFormsResource.class);

    private static final String HEAD = "head";
    private static final String MODEL = "model";
    private static final String INSTANCE = "instance";
    private static final String MANIFEST = "manifest";
    private static final String MEDIA_FILE = "mediaFile";
    private static final String FILENAME = "filename";
    private static final String HASH = "hash";
    private static final String DOWNLOAD_URL = "downloadUrl";
    private static final String MEDIA_MANIFEST = ".media-manifest.xml";
    private static final String INSTANCE_ID = "instanceID";
    private static final String COLLECTION_DATE_TIME = "collectionDateTime";
    private static final String META = "meta";
    private static final String ID = "id";
    private static final String VERSION = "version";
    private static final String CIMS_BINDING = "cims-binding";
    private static final String XML_SUBMISSION_FILE = "xml_submission_file";
    private static final String XML_FORM_FILE = "xml_form_file";
    private static final String DEVICE_ID = "deviceID";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String UTF_8 = "UTF-8";

    @Resource
    private File formsDir;

    @Resource
    private File submissionsDir;

    @Autowired
    private FormSubmissionService submissionService;

    @Autowired
    private FormSubmissionDao submissionDao;


    @PostMapping(path = "/forms")
    @ResponseBody
    public ResponseEntity<?> installForm(@RequestParam(XML_FORM_FILE) MultipartFile formXml,
                                         MultipartHttpServletRequest req)
            throws JDOMException, IOException, NoSuchAlgorithmException {

        SAXBuilder builder = new SAXBuilder();
        Document formDoc = builder.build(formXml.getInputStream());
        Namespace xformsNs = Namespace.getNamespace("http://www.w3.org/2002/xforms"),
                xhtmlNs = Namespace.getNamespace("http://www.w3.org/1999/xhtml");

        Element firstInstance = formDoc.getRootElement()
                .getChild(HEAD, xhtmlNs)
                .getChild(MODEL, xformsNs)
                .getChild(INSTANCE, xformsNs)
                .getChildren().get(0);

        String id = firstInstance.getAttributeValue(ID),
                version = firstInstance.getAttributeValue(VERSION);

        if (version == null) {
            version = "1";
            firstInstance.setAttribute(VERSION, version);
        }

        XMLOutputter outputter = new XMLOutputter();
        String formPath = String.format("%1$s/%2$s/%1$s.xml", id, version);
        log.info("storing form at {}", formPath);

        File formFile = new File(formsDir, formPath);
        File formDir = formFile.getParentFile();
        formDir.mkdirs();
        try (FileWriter writer = new FileWriter(formFile)) {
            outputter.output(formDoc, writer);
        }

        Document manifestDoc = new Document();
        Namespace manifestNs = Namespace.getNamespace("http://openrosa.org/xforms/xformsManifest");
        Element manifestElem = new Element(MANIFEST, manifestNs);
        manifestDoc.setRootElement(manifestElem);

        for (Map.Entry<String, List<MultipartFile>> fileEntry : req.getMultiFileMap().entrySet()) {
            if (XML_FORM_FILE.equalsIgnoreCase(fileEntry.getKey())) {
                log.debug("skipping form file {}", XML_FORM_FILE);
            } else {
                for (MultipartFile file : fileEntry.getValue()) {

                    Element mediaFileElem = new Element(MEDIA_FILE, manifestNs);
                    manifestElem.addContent(mediaFileElem);

                    String fileName = file.getOriginalFilename();

                    File dest = new File(formDir, fileName);
                    file.transferTo(dest);

                    Element fileNameElem = new Element(FILENAME, manifestNs);
                    fileNameElem.setText(fileName);
                    mediaFileElem.addContent(fileNameElem);

                    Element hashElem = new Element(HASH, manifestNs);
                    hashElem.setText(getFileHash(dest));
                    mediaFileElem.addContent(hashElem);
                }
            }
        }

        File manifestFile = new File(formDir, MEDIA_MANIFEST);
        try (FileWriter writer = new FileWriter(manifestFile)) {
            outputter.output(manifestDoc, writer);
        }

        return ResponseEntity.noContent().build();
    }


    @GetMapping(path = {"/forms", "formList"}, produces = "text/xml")
    @ResponseBody
    public void formList(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        rsp.setContentType("text/xml;charset=UTF-8");
        addOpenRosaHeaders(rsp);
        Writer writer = rsp.getWriter();
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">\n");
        for (OpenRosaFormDef form : formScan()) {
            writer.write(form.toString(buildFullRequestUrl(req)));
        }
        writer.write("</xforms>");
    }

    @GetMapping(path = {"/forms/{formId:\\w+}/{formVersion:\\d+}/{fileName:\\w+[.]xml}",
            "/formList/{formId:\\w+}/{formVersion:\\d+}/{fileName:\\w+[.]xml}"}, produces = "text/xml")
    public ResponseEntity<InputStreamResource> form(@PathVariable String formId, @PathVariable String formVersion,
                                                    @PathVariable String fileName,
                                                    HttpServletResponse rsp) throws IOException {
        addOpenRosaHeaders(rsp);
        String formPath = String.format("%s/%s/%s/%s", formsDir, formId, formVersion, fileName);
        org.springframework.core.io.Resource formResource = new FileSystemResource(formPath);
        return ResponseEntity
                .ok()
                .contentLength(formResource.contentLength())
                .contentType(MediaType.TEXT_XML)
                .body(new InputStreamResource(formResource.getInputStream()));
    }

    @GetMapping(path = {"/forms/{formId:\\w+}/{formVersion:\\d+}/{fileName:[a-zA-Z0-9-_ ]+[.]\\w+}",
            "/formList/{formId:\\w+}/{formVersion:\\d+}/{fileName:[a-zA-Z0-9-_ ]+[.]\\w+}"})
    public ResponseEntity<InputStreamResource> formFile(@PathVariable String formId, @PathVariable String formVersion,
                                                        @PathVariable String fileName,
                                                        HttpServletResponse rsp) throws IOException {
        addOpenRosaHeaders(rsp);
        String formPath = String.format("%s/%s/%s/%s", formsDir, formId, formVersion, fileName);
        org.springframework.core.io.Resource formResource = new FileSystemResource(formPath);
        return ResponseEntity
                .ok()
                .contentLength(formResource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(formResource.getInputStream()));
    }

    @GetMapping(path = {"/forms/{formId:\\w+}/{formVersion:\\d+}/manifest",
            "/formList/{formId:\\w+}/{formVersion:\\d+}/manifest"}, produces = "text/xml")
    public ResponseEntity<InputStreamResource> form(@PathVariable String formId, @PathVariable String formVersion,
                                                    HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, JDOMException {

        String manifestPath = String.format("%s/%s/%s/%s", formsDir, formId, formVersion, MEDIA_MANIFEST);

        SAXBuilder builder = new SAXBuilder();
        org.springframework.core.io.Resource manifestResource = new FileSystemResource(manifestPath);
        Document manifestDoc = builder.build(manifestResource.getInputStream());

        String formBaseUrl = buildFullRequestUrl(req).replaceFirst("manifest$", "");

        // need to define a prefix for namespace for xpath to work
        Namespace nsWithPrefix = Namespace.getNamespace("mf", "http://openrosa.org/xforms/xformsManifest");
        Namespace nsNoPrefix = Namespace.getNamespace("http://openrosa.org/xforms/xformsManifest");

        // replace
        XPathFactory xpathFactory = XPathFactory.instance();
        XPathExpression<Element> xpathExpression = xpathFactory.compile("//mf:" + MEDIA_FILE, Filters.element(), null, nsWithPrefix);
        List<Element> files = xpathExpression.evaluate(manifestDoc);
        for (Element file : files) {
            Element filenameElem = file.getChild(FILENAME, nsNoPrefix), downloadUrlElem = new Element(DOWNLOAD_URL, nsNoPrefix);
            downloadUrlElem.setText(formBaseUrl + filenameElem.getText());
            file.addContent(downloadUrlElem);
        }

        XMLOutputter outputter = new XMLOutputter();
        byte[] body = outputter.outputString(manifestDoc).getBytes();

        addOpenRosaHeaders(rsp);
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_XML)
                .contentLength(body.length)
                .body(new InputStreamResource(new ByteArrayInputStream(body)));
    }

    @DeleteMapping("/forms/{formId:\\w+}/{formVersion:\\d+}")
    public ResponseEntity<?> deleteForm(@PathVariable String formId, @PathVariable String formVersion) throws IOException {
        String formPath = String.format("%s/%s", formId, formVersion);
        File formDir = new File(formsDir, formPath);
        if (formDir.exists() && FileSystemUtils.deleteRecursively(formDir)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping(value = "/submission", method = RequestMethod.HEAD)
    public void preAuthentication(HttpServletResponse rsp) {
        addOpenRosaHeaders(rsp);
        rsp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @GetMapping(value = "/submission/{instanceId}", produces = "application/xml")
    @ResponseBody
    public ResponseEntity<?> getXMLInstance(@PathVariable String instanceId)
            throws IOException {
        return getInstanceEntity(APPLICATION_XML, instanceId);
    }

    @GetMapping(value = "/submission/{instanceId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> getJSONInstance(@PathVariable String instanceId)
            throws IOException {
        return getInstanceEntity(APPLICATION_JSON, instanceId);
    }

    private ResponseEntity<?> getInstanceEntity(MediaType type, String instanceId)
            throws IOException {
        try {
            FormSubmission submission = submissionDao.findById(instanceId);
            String contentString = APPLICATION_JSON.equals(type) ? submission.getJson() : submission.getXml();
            org.springframework.core.io.Resource contents = new ByteArrayResource(contentString.getBytes());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(type);
            headers.setContentLength(contents.contentLength());
            return new ResponseEntity<>(contents, headers, OK);
        } catch (DataAccessException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/submission/{idScheme:\\w+}:{instanceId}/{fileName}.{extension}")
    public ResponseEntity<InputStreamResource> getSubmissionFile(@PathVariable String idScheme, @PathVariable String instanceId,
                                                                 @PathVariable String fileName, @PathVariable String extension) throws IOException {
        String submissionPath = String.format("%s/%s/%s/%s.%s", submissionsDir, idScheme, instanceId, fileName, extension);
        org.springframework.core.io.Resource submissionResource = new FileSystemResource(submissionPath);
        return ResponseEntity
                .ok()
                .contentLength(submissionResource.contentLength())
                .contentType(APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(submissionResource.getInputStream()));
    }

    @GetMapping(value = "/submissions/recent", produces = "application/json")
    @ResponseBody
    public List<FormSubmission> recentSubmissions(
            @RequestParam(value = "formId", required = false) String form,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "formBinding", required = false) String binding,
            @RequestParam(value = "deviceId", required = false) String device,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return submissionDao.findRecent(form, version, binding, device, limit);
    }

    @GetMapping(path = "/view/submissionList")
    public ResponseEntity<InputStreamResource> submissionList(@RequestParam("formId") String form,
                                                              @RequestParam(value = "cursor", required = false) String cursor,
                                                              @RequestParam(value = "numEntries", required = false) Integer limit) {
        Timestamp lastSeen = null;
        if (!isEmpty(cursor)) {
            lastSeen = Timestamp.valueOf(cursor);
        }
        String contents = getSubmissionIdList(submissionDao.find(form, null, null, null, lastSeen, limit, false), cursor);
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_XML)
                .body(new InputStreamResource(new ByteArrayInputStream(contents.getBytes())));
    }

    private String getSubmissionIdList(List<FormSubmission> submissions, String cursor) {
        StringBuffer b = new StringBuffer("<idChunk xmlns=\"http://opendatakit.org/submissions\"><idList>");
        for (FormSubmission s : submissions) {
            b.append("<id>");
            b.append(s.getInstanceId());
            b.append("</id>");
        }
        b.append("</idList>");
        b.append("<resumptionCursor>");
        if (submissions.size() > 0) {
            FormSubmission lastSubmission = submissions.get(submissions.size() - 1);
            b.append(lastSubmission.getSubmitted());
        } else {
            b.append(cursor);
        }
        b.append("</resumptionCursor>");
        b.append("</idChunk>");
        return b.toString();
    }


    @PostMapping("/submission")
    public void handleSubmission(@RequestParam(DEVICE_ID) String deviceId,
                                 @RequestParam(XML_SUBMISSION_FILE) MultipartFile xmlFile,
                                 MultipartHttpServletRequest req, HttpServletResponse rsp) throws IOException {

        log.info("received submission from device '{}'", deviceId);

        String xml = new String(xmlFile.getBytes(), UTF_8);
        log.debug("submitted form:\n{}", xml);

        JSONObject jsonObj = toJSONObject(xml);
        log.debug("converted json:\n{}", jsonObj);

        addOpenRosaHeaders(rsp);

        // Extract interesting values from form content
        String instanceName = jsonObj.names().getString(0);
        JSONObject instance = jsonObj.getJSONObject(instanceName);

        // Get metadata section, or create one
        JSONObject meta;
        try {
            meta = instance.getJSONObject(META);
        } catch (JSONException je) {
            meta = new JSONObject();
            instance.put(META, meta);
        }

        // Get collected time or add it
        Timestamp collected;
        try {
            collected = Timestamp.valueOf(instance.getString(COLLECTION_DATE_TIME));
        } catch (JSONException je) {
            collected = Timestamp.from(now());
            String formatted = new SimpleDateFormat(DATE_PATTERN).format(collected);
            instance.put(COLLECTION_DATE_TIME, formatted);
        }

        // Get required instance values
        String id = instance.getString(ID), version = instance.get(VERSION).toString();

        // Get CIMS-specific binding or fall back on form id
        String binding;
        try {
            binding = instance.getString(CIMS_BINDING);
        } catch (JSONException je) {
            binding = id;
        }

        // Get or add instance id
        String instanceId;
        try {
            instanceId = meta.getString(INSTANCE_ID);
        } catch (JSONException je) {
            instanceId = generateInstanceId();
            meta.put(INSTANCE_ID, instanceId);
        }

        // Create a database record for the submission
        boolean isDuplicateSubmission = false;
        try {
            FormSubmission submission = new FormSubmission(instanceId, xml, jsonObj.toString(), id, version, binding,
                    deviceId, collected, null);
            submissionService.recordSubmission(submission);
        } catch (ExistingSubmissionException e) {
            log.debug("duplicate submission, only uploading attachments");
            isDuplicateSubmission = true;
        }

        // Create directory to store submission
        File instanceDir = new File(submissionsDir, schemeSubPath(instanceId));
        instanceDir.mkdirs();

        // Save uploaded files to the submission directory
        for (Map.Entry<String, List<MultipartFile>> fileEntry : req.getMultiFileMap().entrySet()) {
            if (isDuplicateSubmission && XML_SUBMISSION_FILE.equalsIgnoreCase(fileEntry.getKey())) {
                log.debug("skipping multipart file {}", XML_SUBMISSION_FILE);
            } else {
                List<MultipartFile> files = fileEntry.getValue();
                if (files.size() == 1) {
                    MultipartFile file = files.get(0);
                    File dest = new File(instanceDir, file.getOriginalFilename());
                    file.transferTo(dest);
                } else {
                    log.warn("skipped multipart entry {}, had {} files", fileEntry.getKey(), files.size());
                }
            }
        }

        rsp.setStatus(HttpServletResponse.SC_CREATED);
    }

    private String generateInstanceId() {
        return String.format("uuid:%s", UUID.randomUUID());
    }

    private String schemeSubPath(String instanceId) {
        return instanceId.replaceFirst(":", "/");
    }

    private void addOpenRosaHeaders(HttpServletResponse rsp) {
        rsp.setHeader("X-OpenRosa-Version", "1.0");
        rsp.setIntHeader("X-OpenRosa-Accept-Content-Length", 10485760);
    }

    /**
     * Represents all of the required information for a form definition from the OpenRosa form list API.
     */
    private class OpenRosaFormDef {

        String id, name, version, hash, path, manifestPath;

        OpenRosaFormDef(String name, String id, String version, String hash, String path, String manifestPath) {
            this.id = id;
            this.name = name;
            this.version = version;
            this.hash = hash;
            this.path = path;
            this.manifestPath = manifestPath;
        }

        public String toString(String baseUrl) {
            return "<xform>\n" +
                    "<formID>" + id + "</formID>\n" +
                    "<name>" + name + "</name>\n" +
                    "<majorMinorVersion>" + version + "</majorMinorVersion>\n" +
                    "<version>" + version + "</version>\n" +
                    "<hash>md5:" + hash + "</hash>" +
                    "<downloadUrl>" + baseUrl + "/" + path + "</downloadUrl>\n" +
                    (manifestPath != null ? "<manifestUrl>" + baseUrl + "/" + manifestPath + "</manifestUrl>" : "") +
                    "</xform>\n";
        }
    }

    /**
     * Scans the application form directory and returns the list of detected (valid) form definitions found.
     */
    private List<OpenRosaFormDef> formScan() throws IOException {
        List<OpenRosaFormDef> results = new ArrayList<>();
        if (formsDir.exists()) {
            Path formsPath = formsDir.toPath();
            Files.walk(formsPath)
                    .filter(path -> {
                        Path rel = formsPath.relativize(path);
                        File f = path.toFile();
                        return f.exists()
                                && rel.getNameCount() == 3
                                && rel.getName(0).toString().matches("\\w+")
                                && rel.getName(1).toString().matches("\\d+")
                                && rel.getName(2).toString().matches("\\w+[.]xml");
                    })
                    .forEach(path -> {
                        try {
                            OpenRosaFormDef formDef = loadFormDef(formsPath, path);
                            results.add(formDef);
                        } catch (FileNotFoundException e) {
                            log.warn("form not found for path '{}'", path);
                        } catch (NoSuchAlgorithmException e) {
                            log.warn("hash failed due to missing algorithm");
                        } catch (IOException e) {
                            log.warn("io failure loading definition for path '{}'", path);
                        } catch (JDOMException e) {
                            log.warn("failed to parse form definition for path '{}'", path);
                        }
                    });
        }
        return results;
    }

    private String getFileHash(File toHash) throws NoSuchAlgorithmException, IOException {
        try (DigestInputStream in = new DigestInputStream(new FileInputStream(toHash), MessageDigest.getInstance("MD5"))) {
            byte[] buf = new byte[8096];
            while (in.read(buf) >= 0) {
                // reading only to compute hash
            }
            return "md5:" + encodeHexString(in.getMessageDigest().digest());
        }
    }

    /**
     * Loads a {@link OpenRosaFormDef} object from a form from its path within the application's forms directory.
     */
    private OpenRosaFormDef loadFormDef(Path formsPath, Path formPath)
            throws IOException, NoSuchAlgorithmException, JDOMException {
        Path rel = formsPath.relativize(formPath);
        String id = rel.getName(0).toString(),
                version = rel.getName(1).toString(),
                fileName = rel.getName(2).toString();
        String title, hash;
        try (DigestInputStream digestIn =
                     new DigestInputStream(new FileInputStream(formPath.toFile()), MessageDigest.getInstance("MD5"))) {
            SAXBuilder sb = new SAXBuilder();
            Document formDoc = sb.build(digestIn);
            Namespace xhtmlNs = Namespace.getNamespace("http://www.w3.org/1999/xhtml");
            Element html = formDoc.getRootElement(),
                    head = html.getChild("head", xhtmlNs);
            title = head.getChildText("title", xhtmlNs);
            hash = encodeHexString(digestIn.getMessageDigest().digest()); // assumes doc build reads entire file content
        }
        boolean manifestExists = formPath.resolveSibling(MEDIA_MANIFEST).toFile().exists();
        return new OpenRosaFormDef(title, id, version, hash,
                id + "/" + version + "/" + fileName, manifestExists ? id + "/" + version + "/" + MANIFEST : null);
    }
}