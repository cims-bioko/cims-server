package com.github.cimsbioko.server.webapi.odk;

import com.github.cimsbioko.server.dao.FormRepository;
import com.github.cimsbioko.server.dao.FormSubmissionRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.cimsbioko.server.util.JDOMUtil.*;
import static com.github.cimsbioko.server.webapi.odk.Constants.*;
import static java.time.Instant.now;
import static org.json.XML.toJSONObject;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;
import static org.springframework.security.web.util.UrlUtils.buildFullRequestUrl;
import static org.springframework.util.StringUtils.isEmpty;

@Controller
@RequestMapping(ODK_API_PATH)
public class SubmissionResource {

    private static Logger log = LoggerFactory.getLogger(SubmissionResource.class);

    @Autowired
    SubmissionFileSystem submissionFileSystem;

    @Autowired
    private FormSubmissionService submissionService;

    @Autowired
    private FormSubmissionRepository submissionDao;

    @Autowired
    private FormRepository formDao;

    @Autowired
    private FileHasher hasher;

    @Autowired
    EndpointHelper helper;

    @Autowired
    private SubmissionIdGenerator idGenerator;

    @Autowired
    private OpenRosaResponseBuilder responseBuilder;

    @Autowired
    private DateFormatter dateFormatter;

    @RequestMapping(value = {"/submission"}, method = RequestMethod.HEAD)
    public ResponseEntity submissionPreAuth(HttpServletRequest req) {
        return ResponseEntity
                .noContent()
                .headers(helper.openRosaHeaders())
                .header(LOCATION, helper.contextRelativeUrl(req, "submission"))
                .build();
    }

    @GetMapping(value = "/submission/{instanceId}", produces = "application/xml")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getXMLInstance(@PathVariable String instanceId) throws IOException {
        return getInstanceEntity(APPLICATION_XML, instanceId);
    }

    @GetMapping(value = "/submission/{instanceId}", produces = "application/json")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getJSONInstance(@PathVariable String instanceId) throws IOException {
        return getInstanceEntity(APPLICATION_JSON, instanceId);
    }

    private ResponseEntity<?> getInstanceEntity(MediaType type, String instanceId)
            throws IOException {
        try {
            // FIXME: Use optional rather than null
            FormSubmission submission = submissionDao.findById(instanceId).orElse(null);
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
        String filePath = submissionFileSystem.getSubmissionFilePath(idScheme, instanceId, fileName, extension);
        org.springframework.core.io.Resource submissionResource = new FileSystemResource(filePath);
        return ResponseEntity
                .ok()
                .contentLength(submissionResource.contentLength())
                .contentType(APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(submissionResource.getInputStream()));
    }

    @GetMapping(path = "/view/submissionList")
    @Transactional(readOnly = true)
    public ResponseEntity<ByteArrayResource> submissionList(@RequestParam("formId") String form,
                                                            @RequestParam(value = "cursor", required = false) String cursor,
                                                            @RequestParam(value = "numEntries", required = false) Integer limit) {
        limit = limit == null || limit > 100 || limit <= 0 ? 100 : limit;
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "submitted"));
        List<FormSubmission> chunk;
        if (isEmpty(cursor)) {
            chunk = submissionDao.findByFormId(form, page);
        } else {
            chunk = submissionDao.findByFormIdAndSubmittedAfter(form, Timestamp.valueOf(cursor), page);
        }
        String chunkList = buildSubmissionChunk(chunk, cursor);
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_XML)
                .body(new ByteArrayResource(chunkList.getBytes()));
    }

    private String buildSubmissionChunk(List<FormSubmission> submissions, String cursor) {
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
        // FIXME: Use optional rather than null
        String contents = buildSubmissionDescriptor(submissionDao.findById(info[1]).orElse(null), submissionBaseUrl);
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_XML)
                .body(new ByteArrayResource(contents.getBytes()));
    }

    private String buildSubmissionDescriptor(FormSubmission submission, String submissionBaseUrl) throws IOException {
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
            root.setAttribute(SUBMISSION_DATE, dateFormatter.formatSubmitDate(submission.getSubmitted()));
        }
        b.append(new XMLOutputter(Format.getRawFormat().setOmitDeclaration(true)).outputString(doc));
        b.append("</data>");
        File submissionDir = submissionFileSystem.getSubmissionDir(instanceId);
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
                            b.append(hasher.hashFile(path.toFile()));
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
    public ResponseEntity<ByteArrayResource> handleSubmission(@RequestParam(value = DEVICE_ID, defaultValue = "unknown") String deviceId,
                                                              @RequestParam(XML_SUBMISSION_FILE) MultipartFile xmlFile,
                                                              MultipartHttpServletRequest req)
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
            collectionTimeElem.setText(dateFormatter.formatCollectionDate(collected));
            rootElem.addContent(collectionTimeElem);
        } else {
            collected = Timestamp.valueOf(collectionTimeElem.getText());
        }

        // retrieve or add instance id
        String instanceId = rootElem.getAttributeValue(INSTANCE_ID);
        if (instanceId == null) {
            Element instanceIdElem = metaElem.getChild(INSTANCE_ID, metaElemNs);
            if (instanceIdElem == null) {
                instanceId = idGenerator.generateId();
                instanceIdElem = new Element(INSTANCE_ID, metaElemNs);
                instanceIdElem.setText(instanceId);
                metaElem.addContent(instanceIdElem);
            } else if (instanceIdElem.getText().isEmpty()) {
                instanceId = idGenerator.generateId();
                instanceIdElem.setText(instanceId);
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

        // FIXME: Use optional rather than null
        Form form = formDao.findById(new FormId(id, version)).orElse(null);

        if (form == null) {
            log.warn("rejected {}, unknown form id={}, version={}", instanceId, id, version);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .headers(helper.openRosaHeaders())
                    .contentType(MediaType.TEXT_XML)
                    .body(new ByteArrayResource(
                            responseBuilder.response(String.format("form %s version %s doesn't exist", id, version)).getBytes()));
        } else if (!form.isSubmissions()) {
            log.warn("rejected {}, submissions disabled for form id={}, version={}", instanceId, id, version);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .headers(helper.openRosaHeaders())
                    .contentType(MediaType.TEXT_XML)
                    .body(new ByteArrayResource(
                            responseBuilder.response(String.format("form %s version %s submissions disabled", id, version)).getBytes()));
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
                processed = submitted = dateFormatter.parseSubmitDate(rootElem.getAttributeValue(SUBMISSION_DATE));
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
            File instanceDir = submissionFileSystem.getSubmissionDir(instanceId);
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

            URI submissionUri = new URI(helper.contextRelativeUrl(req, "submission"));

            return ResponseEntity
                    .created(submissionUri)
                    .headers(helper.openRosaHeaders())
                    .contentType(MediaType.TEXT_XML)
                    .body(new ByteArrayResource(responseBuilder.submissionResponse(submission).getBytes()));
        }
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
}