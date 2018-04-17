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
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.cimsbioko.server.util.JDOMUtil.*;
import static com.github.cimsbioko.server.webapi.odk.Constants.*;
import static java.time.Instant.now;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.MD5;
import static org.json.XML.toJSONObject;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.web.util.UrlUtils.buildFullRequestUrl;
import static org.springframework.util.StringUtils.isEmpty;

@Controller
@RequestMapping(ODK_API_PATH)
public class ODKSubmissionResource {

    private static Logger log = LoggerFactory.getLogger(ODKSubmissionResource.class);

    private static final String INSTANCE_ID = "instanceID";
    private static final String COLLECTION_DATE_TIME = "collectionDateTime";
    private static final String SUBMISSION_DATE = "submissionDate";
    private static final String META = "meta";
    private static final String CIMS_BINDING = "cims-binding";
    private static final String XML_SUBMISSION_FILE = "xml_submission_file";
    private static final String DEVICE_ID = "deviceID";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String ODK_SUBMIT_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    @Resource
    private File submissionsDir;

    @Autowired
    private FormSubmissionService submissionService;

    @Autowired
    private FormSubmissionDao submissionDao;

    @Autowired
    private FormDao formDao;

    private String contextRelativeUrl(HttpServletRequest req, String... pathSegments) {
        return UriComponentsBuilder
                .fromHttpUrl(buildFullRequestUrl(req))
                .replacePath(req.getContextPath() + req.getServletPath() + ODK_API_PATH)
                .pathSegment(pathSegments)
                .query(null)
                .toUriString();
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
    public ResponseEntity<?> getXMLInstance(@PathVariable String instanceId) throws IOException {
        return getInstanceEntity(APPLICATION_XML, instanceId);
    }

    @GetMapping(value = "/submission/{instanceId}", produces = "application/json")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getJSONInstance(@PathVariable String instanceId) throws IOException {
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

    @GetMapping(value = "/submissions/search", produces = "application/json")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<FormSubmission> recentSubmissions(String query,
                                                  @RequestParam(value="start", defaultValue = "0") int start,
                                                  @RequestParam(value="end", defaultValue = "100") int max) {
        return submissionDao.findBySearch(query, start, max);
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
            throws IOException {
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
            version = Constants.DEFAULT_VERSION;
            rootElem.setAttribute(VERSION, version);
        }

        Form form = formDao.findById(new FormId(id, version));

        addOpenRosaHeaders(rsp);

        if (form == null) {
            log.warn("rejected {}, unknown form id={}, version={}", instanceId, id, version);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_XML)
                    .body(new InputStreamResource(new ByteArrayInputStream(
                            getFailedSubmissionResponse(String.format("form %s version %s doesn't exist", id, version)).getBytes())));
        } else if (!form.isSubmissions()) {
            log.warn("rejected {}, submissions disabled for form id={}, version={}", instanceId, id, version);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.TEXT_XML)
                    .body(new InputStreamResource(new ByteArrayInputStream(
                            getFailedSubmissionResponse(String.format("form %s version %s submissions disabled", id, version)).getBytes())));
        } else {

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
                    .body(new InputStreamResource(new ByteArrayInputStream(getSuccessfulSubmissionResponse(submission).getBytes())));
        }
    }

    private String getFailedSubmissionResponse(String message) {
        return "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" +
                "<message>" + message + "</message>" +
                "</OpenRosaResponse>";
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

    private String getSuccessfulSubmissionResponse(FormSubmission fs) {
        return String.format("<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" +
                        "<message>full submission upload was successful!</message>" +
                        "<submissionMetadata xmlns=\"http://www.opendatakit.org/xforms\"" +
                        "id=\"%s\" instanceID=\"%s\" version=\"%s\" submissionDate=\"%s\"/>" +
                        "</OpenRosaResponse>",
                fs.getFormId(), fs.getInstanceId(), fs.getFormVersion(), formatODKSubmitDate(fs.getSubmitted()));
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

    private String getFileHash(File toHash) throws NoSuchAlgorithmException, IOException {
        try (DigestInputStream in = new DigestInputStream(new FileInputStream(toHash), MessageDigest.getInstance(MD5))) {
            byte[] buf = new byte[8096];
            while (in.read(buf) >= 0) {
                // reading only to compute hash
            }
            return MD5_SCHEME + encodeHexString(in.getMessageDigest().digest());
        }
    }
}