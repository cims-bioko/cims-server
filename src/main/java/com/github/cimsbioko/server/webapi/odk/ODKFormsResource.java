package com.github.cimsbioko.server.webapi.odk;

import com.github.cimsbioko.server.dao.FormDao;
import com.github.cimsbioko.server.dao.FormSubmissionDao;
import com.github.cimsbioko.server.domain.Form;
import com.github.cimsbioko.server.domain.FormId;
import com.github.cimsbioko.server.domain.FormSubmission;
import com.github.cimsbioko.server.exception.ExistingSubmissionException;
import com.github.cimsbioko.server.service.FormSubmissionService;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.cimsbioko.server.util.JDOMUtil.*;
import static com.github.cimsbioko.server.webapi.odk.ODKFormsResource.ODK_API_PATH;
import static java.time.Instant.now;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.json.XML.toJSONObject;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.web.util.UrlUtils.buildFullRequestUrl;
import static org.springframework.util.StringUtils.isEmpty;

@Controller
@RequestMapping(ODK_API_PATH)
public class ODKFormsResource {

    public static final String ODK_API_PATH = "/odk";

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
    private static final String SUBMISSION_DATE = "submissionDate";
    private static final String META = "meta";
    private static final String ID = "id";
    private static final String VERSION = "version";
    private static final String CIMS_BINDING = "cims-binding";
    private static final String XML_SUBMISSION_FILE = "xml_submission_file";
    private static final String FORM_DEF_FILE = "form_def_file";
    private static final String DEVICE_ID = "deviceID";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String ODK_SUBMIT_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    private static final String TITLE = "title";
    private static final String MD5_SCHEME = "md5:";
    private static final String MD5 = "MD5";
    private static final String DEFAULT_VERSION = "1";
    private static final String LOCATION = "Location";


    @Resource
    private File formsDir;

    @Resource
    private File submissionsDir;

    @Autowired
    private FormSubmissionService submissionService;

    @Autowired
    private FormSubmissionDao submissionDao;

    @Autowired
    private FormDao formDao;

    @RequestMapping(value = {"/upload", "/forms", "/formUpload"}, method = RequestMethod.HEAD)
    public void configPush(HttpServletRequest req, HttpServletResponse rsp) {
        addOpenRosaHeaders(rsp);
        rsp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        rsp.setHeader(LOCATION, contextRelativeUrl(req, "formUpload"));
    }

    private String contextRelativeUrl(HttpServletRequest req, String... pathSegments) {
        return UriComponentsBuilder
                .fromHttpUrl(buildFullRequestUrl(req))
                .replacePath(req.getContextPath() + req.getServletPath() + ODK_API_PATH)
                .pathSegment(pathSegments)
                .query(null)
                .toUriString();
    }

    @PostMapping(path = {"/forms", "/formUpload"})
    @ResponseBody
    public ResponseEntity<?> installForm(@RequestParam(FORM_DEF_FILE) MultipartFile formXml,
                                         MultipartHttpServletRequest req)
            throws JDOMException, IOException, NoSuchAlgorithmException, URISyntaxException {

        Document formDoc = getBuilder().build(formXml.getInputStream());
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
            version = DEFAULT_VERSION;
            firstInstance.setAttribute(VERSION, version);
        }

        FormId formId = new FormId(id,version);
        Form form = formDao.findById(formId);
        if (form == null) {
            log.info("uploading new form");
            form = new Form(formId, formDoc);
        } else {
            log.info("updating existing form");
            form.setXml(formDoc);
        }
        formDao.save(form);

        String formPath = String.format("%1$s/%2$s/%1$s.xml", id, version);
        log.info("storing form at {}", formPath);

        File formFile = new File(formsDir, formPath);
        File formDir = formFile.getParentFile();
        formDir.mkdirs();
        try (FileWriter writer = new FileWriter(formFile)) {
            getOutputter().output(formDoc, writer);
        }

        Document manifestDoc = new Document();
        Namespace manifestNs = Namespace.getNamespace("http://openrosa.org/xforms/xformsManifest");
        Element manifestElem = new Element(MANIFEST, manifestNs);
        manifestDoc.setRootElement(manifestElem);

        for (Map.Entry<String, List<MultipartFile>> fileEntry : req.getMultiFileMap().entrySet()) {
            if (FORM_DEF_FILE.equalsIgnoreCase(fileEntry.getKey())) {
                log.debug("skipping form file {}", FORM_DEF_FILE);
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
            getOutputter().output(manifestDoc, writer);
        }

        URI formUrl = new URI(contextRelativeUrl(req, "formUpload"));

        return ResponseEntity
                .created(formUrl)
                .contentType(MediaType.TEXT_XML)
                .body(new InputStreamReader(new ByteArrayInputStream(OR_SUCCESS_MSG)));
    }

    private static byte[] OR_SUCCESS_MSG = "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\"><message>Successful upload.</message></OpenRosaResponse>".getBytes();

    @GetMapping(path = {"/forms", "formList"}, produces = "text/xml")
    @ResponseBody
    public void formList(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        rsp.setContentType("text/xml;charset=UTF-8");
        addOpenRosaHeaders(rsp);
        Writer writer = rsp.getWriter();
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">\n");
        for (OpenRosaFormDef f : latestLoadedFormDefs()) {
            try {
                writer.write(f.toString(buildFullRequestUrl(req)));
            } catch (IOException e) {
                log.warn("failed to list form id={}, version={}: {}", f.id, f.version, e.toString());
            }
        }
        writer.write("</xforms>");
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
                .contentType(fileName.endsWith(".xml") ? MediaType.TEXT_XML : MediaType.APPLICATION_OCTET_STREAM)
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

        byte[] body = getOutputter().outputString(manifestDoc).getBytes();

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

    @RequestMapping(value = {"/submission"}, method = RequestMethod.HEAD)
    public void submissionPreAuth(HttpServletRequest req, HttpServletResponse rsp) {
        addOpenRosaHeaders(rsp);
        rsp.setHeader(LOCATION, contextRelativeUrl(req, "submission"));
        rsp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @GetMapping(value = "/submission/{instanceId}", produces = "application/xml")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getXMLInstance(@PathVariable String instanceId)
            throws IOException {
        return getInstanceEntity(APPLICATION_XML, instanceId);
    }

    @GetMapping(value = "/submission/{instanceId}", produces = "application/json")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getJSONInstance(@PathVariable String instanceId)
            throws IOException {
        return getInstanceEntity(APPLICATION_JSON, instanceId);
    }

    private ResponseEntity<?> getInstanceEntity(MediaType type, String instanceId)
            throws IOException {
        try {
            FormSubmission submission = submissionDao.findById(instanceId);
            String contentString = APPLICATION_JSON.equals(type) ? submission.getJson().toString() : stringFromDoc(submission.getXml());
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
        String submissionPath = String.format("%s/%s.%s", getSubmissionPath(idScheme, instanceId), fileName, extension);
        org.springframework.core.io.Resource submissionResource = new FileSystemResource(submissionPath);
        return ResponseEntity
                .ok()
                .contentLength(submissionResource.contentLength())
                .contentType(APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(submissionResource.getInputStream()));
    }

    private File getSubmissionDir(String instanceId) {
        return new File(submissionsDir, schemeSubPath(instanceId));
    }

    private String getSubmissionPath(String idScheme, String id) {
        return String.format("%s/%s/%s", submissionsDir, idScheme, id);
    }

    @GetMapping(value = "/submissions/recent", produces = "application/json")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<FormSubmission> recentSubmissions(
            @RequestParam(value = "formId", required = false) String form,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "formBinding", required = false) String binding,
            @RequestParam(value = "deviceId", required = false) String device,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return submissionDao.findRecent(form, version, binding, device, limit);
    }

    @GetMapping(value = "/submissions/unprocessed", produces = "application/json")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<FormSubmission> unprocessedSubmissions(
            @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit) {
        return submissionDao.findUnprocessed(limit);
    }

    @GetMapping(path = "/view/submissionList")
    @Transactional(readOnly = true)
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
        StringBuilder b = new StringBuilder("<idChunk xmlns=\"http://opendatakit.org/submissions\"><idList>");
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

    @GetMapping("/view/downloadSubmission")
    @Transactional(readOnly = true)
    public ResponseEntity<?> downloadSubmission(@RequestParam("formId") String submissionKey, HttpServletRequest req)
            throws JDOMException, IOException {
        String[] info = getInfoFromSubmissionKey(submissionKey);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        String submissionBaseUrl = String.format("%s/submission",
                buildFullRequestUrl(req).split("/view/downloadSubmission")[0]);
        String contents = getSubmissionDescriptor(submissionDao.findById(info[1]), submissionBaseUrl);
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_XML)
                .body(new InputStreamResource(new ByteArrayInputStream(contents.getBytes())));
    }

    private String formatODKSubmitDate(Timestamp t) {
        if (t != null) {
            return new SimpleDateFormat(ODK_SUBMIT_DATE_PATTERN).format(t);
        }
        return "";
    }

    private Timestamp parseODKSubmitDate(String s) throws ParseException {
        if (s != null) {
            return new Timestamp(new SimpleDateFormat(ODK_SUBMIT_DATE_PATTERN).parse(s).getTime());
        }
        return null;
    }

    private String getSubmissionDescriptor(FormSubmission submission, String submissionBaseUrl) throws IOException {
        StringBuilder b = new StringBuilder(
                "<submission xmlns=\"http://opendatakit.org/submissions\" " +
                        "xmlns:orx=\"http://openrosa.org/xforms\" ><data>");
        Document doc = submission.getXml();
        Element root = doc.getRootElement();
        String instanceId = submission.getInstanceId();
        if (root.getAttribute(ID) == null) {
            root.setAttribute(ID, submission.getFormId());
        }
        if (root.getAttribute(INSTANCE_ID) == null) {
            root.setAttribute(INSTANCE_ID, instanceId);
        }
        if (root.getAttribute(VERSION) == null) {
            root.setAttribute(VERSION, submission.getFormVersion());
        }
        if (root.getAttribute(SUBMISSION_DATE) == null) {
            root.setAttribute(SUBMISSION_DATE, formatODKSubmitDate(submission.getSubmitted()));
        }
        b.append(new XMLOutputter(Format.getRawFormat().setOmitDeclaration(true)).outputString(doc));
        b.append("</data>");
        File submissionDir = getSubmissionDir(instanceId);
        if (submissionDir.exists()) {
            log.debug("scanning {} for media files", submissionDir);
            Files.walk(submissionDir.toPath())
                    .filter(path -> !(path.toFile().isDirectory() || path.toString().endsWith(".xml")))
                    .forEach(path -> {
                        try {
                            b.append("<mediaFile>");
                            b.append("<fileName>");
                            b.append(path.getFileName());
                            b.append("</fileName>");
                            b.append("<hash>");
                            b.append(MD5_SCHEME);
                            b.append(getFileHash(path.toFile()));
                            b.append("</hash>");
                            String downloadUrl = String.format("%s/%s/%s",
                                    submissionBaseUrl, instanceId, path.getFileName());
                            b.append("<downloadUrl>");
                            b.append(downloadUrl);
                            b.append("</downloadUrl>");
                            b.append("</mediaFile>");
                            log.info("media file {}", path);
                        } catch (Exception e) {
                            log.error("failed to handle media file: " + path, e);
                        }
                    });
        }
        b.append("</submission>");
        return b.toString();
    }

    private static Pattern SUBMIT_KEY_PATTERN = Pattern.compile("/([^\\[]+)\\[@key=([^\\]]+)\\]$");

    private String[] getInfoFromSubmissionKey(String key) {
        Matcher m = SUBMIT_KEY_PATTERN.matcher(key);
        if (m.find()) {
            return new String[]{m.group(1), m.group(2)};
        }
        return null;
    }

    @PostMapping("/submission")
    public ResponseEntity<?> handleSubmission(@RequestParam(value = DEVICE_ID, defaultValue = "unknown") String deviceId,
                                              @RequestParam(XML_SUBMISSION_FILE) MultipartFile xmlFile,
                                              MultipartHttpServletRequest req, HttpServletResponse rsp)
            throws IOException, URISyntaxException, JDOMException {

        log.info("received submission from device '{}'", deviceId);

        Document xmlDoc = getBuilder().build(xmlFile.getInputStream());

        if (log.isDebugEnabled()) {
            log.debug("submitted form:\n{}", getOutputter().outputString(xmlDoc));
        }

        // retrieve or add meta element if it doesn't exist
        Element rootElem = xmlDoc.getRootElement();

        Namespace rootNs = rootElem.getNamespace(),
                metaNs = Namespace.getNamespace("jr", "http://openrosa.org/xforms");

        Element metaElem = getChild(rootElem, META, rootNs, metaNs);
        if (metaElem == null) {
            metaElem = new Element(META, metaNs);
            rootElem.addContent(metaElem);
        }

        Namespace metaElemNs = metaElem.getNamespace();

        // retrieve or add collection date/time if it doesn't exist
        Timestamp collected;
        Element collectionTimeElem = rootElem.getChild(COLLECTION_DATE_TIME, rootNs);
        if (collectionTimeElem == null || collectionTimeElem.getText().isEmpty()) {
            collected = Timestamp.from(now());
            collectionTimeElem = new Element(COLLECTION_DATE_TIME, rootNs);
            collectionTimeElem.setText(new SimpleDateFormat(DATE_TIME_PATTERN).format(collected));
            rootElem.addContent(collectionTimeElem);
        } else {
            collected = Timestamp.valueOf(collectionTimeElem.getText());
        }

        // retrieve or add instance id
        String instanceId = rootElem.getAttributeValue(INSTANCE_ID);
        if (instanceId == null) {
            Element instanceIdElem = metaElem.getChild(INSTANCE_ID, metaElemNs);
            if (instanceIdElem == null || instanceIdElem.getText().isEmpty()) {
                instanceId = generateInstanceId();
                instanceIdElem = new Element(INSTANCE_ID, metaElemNs);
                instanceIdElem.setText(instanceId);
                metaElem.addContent(instanceIdElem);
            } else {
                instanceId = instanceIdElem.getText();
            }
        }

        // Get required instance values
        String id = rootElem.getAttributeValue(ID), version = rootElem.getAttributeValue(VERSION);

        if (version == null) {
            version = DEFAULT_VERSION;
            rootElem.setAttribute(VERSION, version);
        }

        // Get CIMS-specific binding or fall back on form id
        String binding = rootElem.getAttributeValue(CIMS_BINDING);
        if (binding == null) {
            binding = id;
        }

        /*
           Get submission date if supplied. Its presence implies previously submitted/processed (briefcase upload).
           We can not infer whether the processing failed or succeeded because this CIMS concept is not present in
           ODK briefcase.
         */
        Timestamp submitted = null, processed = null;
        try {
            processed = submitted = parseODKSubmitDate(rootElem.getAttributeValue(SUBMISSION_DATE));
        } catch (ParseException e) {
            log.warn("failed to parse submission date", e);
        }

        JSONObject json = toJSONObject(stringFromDoc(xmlDoc));
        log.debug("converted json:\n{}", json);

        addOpenRosaHeaders(rsp);

        // Create a database record for the submission
        FormSubmission submission = new FormSubmission(instanceId, xmlDoc, json, id, version, binding,
                deviceId, collected, submitted, processed, null);
        boolean isDuplicateSubmission = false;
        try {
            submission = submissionService.recordSubmission(submission);
        } catch (ExistingSubmissionException e) {
            log.debug("duplicate submission, only uploading attachments");
            isDuplicateSubmission = true;
        }

        // Create directory to store submission
        File instanceDir = getSubmissionDir(instanceId);
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

        URI submissionUri = new URI(contextRelativeUrl(req, "submission"));

        return ResponseEntity
                .created(submissionUri)
                .contentType(MediaType.TEXT_XML)
                .body(new InputStreamResource(new ByteArrayInputStream(getSubmissionResult(submission).getBytes())));
    }

    private Element getChild(Element parent, String cname, Namespace... nses) {
        for (Namespace ns : nses) {
            Element child = parent.getChild(cname, ns);
            if (child != null) {
                return child;
            }
        }
        return null;
    }

    private String getSubmissionResult(FormSubmission fs) {
        return String.format("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" +
                "<message>full submission upload was successful!</message>" +
                "<submissionMetadata xmlns=\"http://www.opendatakit.org/xforms\"" +
                "id=\"%s\" instanceID=\"%s\" version=\"%s\" submissionDate=\"%s\"/>" +
                "</OpenRosaResponse>", fs.getFormId(), fs.getInstanceId(), fs.getFormVersion(), formatODKSubmitDate(fs.getSubmitted()));
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
                    "<hash>" + MD5_SCHEME + hash + "</hash>" +
                    "<downloadUrl>" + baseUrl + "/" + path + "</downloadUrl>\n" +
                    (manifestPath != null ? "<manifestUrl>" + baseUrl + "/" + manifestPath + "</manifestUrl>" : "") +
                    "</xform>\n";
        }
    }

    /**
     * Scans the application form directory and returns the list of detected (valid) form definitions found.
     */
    private Stream<Path> formPaths() throws IOException {
        if (formsDir.exists()) {
            Path formsPath = formsDir.toPath();
            return Files.walk(formsPath)
                    .filter(path -> {
                        Path rel = formsPath.relativize(path);
                        File f = path.toFile();
                        return f.exists()
                                && rel.getNameCount() == 3
                                && rel.getName(0).toString().matches("\\w+")
                                && rel.getName(1).toString().matches("\\d+")
                                && rel.getName(2).toString().matches("\\w+[.]xml");
                    });
        }
        return Stream.empty();
    }

    private Stream<OpenRosaFormDef> loadedFormDefs() throws IOException {
        Path formsPath = formsDir.toPath();
        return formPaths().map(p -> {
            try {
                return loadFormDef(formsPath, p);
            } catch (FileNotFoundException e) {
                log.warn("form not found for path '{}'", p);
            } catch (NoSuchAlgorithmException e) {
                log.warn("hash failed due to missing algorithm");
            } catch (IOException e) {
                log.warn("io failure loading definition for path '{}'", p);
            } catch (JDOMException e) {
                log.warn("failed to parse form definition for path '{}'", p);
            }
            return null;
        }).filter(Objects::nonNull);
    }

    private Collection<OpenRosaFormDef> latestLoadedFormDefs() throws IOException {
        Map<String, OpenRosaFormDef> latestVersions = new HashMap<>();
        loadedFormDefs().forEach(def -> {
            OpenRosaFormDef latest = latestVersions.get(def.id);
            if (latest == null || def.version.compareTo(latest.version) > 0) {
                latestVersions.put(def.id, def);
            }
        });
        return latestVersions.values();
    }

    private String getFileHash(File toHash) throws NoSuchAlgorithmException, IOException {
        try (DigestInputStream in = new DigestInputStream(new FileInputStream(toHash), MessageDigest.getInstance(MD5))) {
            byte[] buf = new byte[8096];
            while (in.read(buf) >= 0) {
                // reading only to compute hash
            }
            return MD5_SCHEME + encodeHexString(in.getMessageDigest().digest());
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
                     new DigestInputStream(new FileInputStream(formPath.toFile()), MessageDigest.getInstance(MD5))) {
            SAXBuilder sb = new SAXBuilder();
            Document formDoc = sb.build(digestIn);
            Namespace xhtmlNs = Namespace.getNamespace("http://www.w3.org/1999/xhtml");
            Element html = formDoc.getRootElement(),
                    head = html.getChild(HEAD, xhtmlNs);
            title = head.getChildText(TITLE, xhtmlNs);
            hash = encodeHexString(digestIn.getMessageDigest().digest()); // assumes doc build reads entire file content
        }
        boolean manifestExists = formPath.resolveSibling(MEDIA_MANIFEST).toFile().exists();
        return new OpenRosaFormDef(title, id, version, hash,
                id + "/" + version + "/" + fileName, manifestExists ? id + "/" + version + "/" + MANIFEST : null);
    }
}