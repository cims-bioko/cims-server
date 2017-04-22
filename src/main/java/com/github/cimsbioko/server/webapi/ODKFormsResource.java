package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.controller.exception.ExistingSubmissionException;
import com.github.cimsbioko.server.controller.service.FormSubmissionService;
import com.github.cimsbioko.server.domain.model.FormSubmission;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

import static com.github.cimsbioko.server.Application.WebConfig.FORMS_PATH;
import static com.github.cimsbioko.server.Application.WebConfig.SUBMISSIONS_PATH;
import static java.time.Instant.now;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.json.XML.toJSONObject;
import static org.springframework.security.web.util.UrlUtils.buildFullRequestUrl;

@Controller
public class ODKFormsResource {

    private static Logger log = LoggerFactory.getLogger(ODKFormsResource.class);

    private static final String INSTANCE_ID = "instanceID";
    private static final String COLLECTION_DATE_TIME = "collectionDateTime";
    private static final String META = "meta";
    private static final String ID = "id";
    private static final String VERSION = "version";
    private static final String CIMS_BINDING = "cims-binding";
    private static final String XML_SUBMISSION_FILE = "xml_submission_file";
    private static final String DEVICE_ID = "deviceID";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String UTF_8 = "UTF-8";

    @Resource
    private File formsDir;

    @Resource
    private File submissionsDir;

    @GetMapping(path = "/forms", produces = {"text/xml"})
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

    @GetMapping(path = "/forms/{formId:\\w+}/{formVersion:\\d+}/{fileName:\\w+[.]xml}", produces = {"text/xml"})
    public String form(@PathVariable String formId, @PathVariable String formVersion, @PathVariable String fileName,
                       HttpServletResponse rsp) throws IOException {
        rsp.setContentType("text/xml;charset=UTF-8");
        addOpenRosaHeaders(rsp);
        return "forward:" + FORMS_PATH + "/" + formId + "/" + formVersion + "/" + fileName;
    }

    @Autowired
    private FormSubmissionService submissionService;

    @RequestMapping(value = "/submission", method = RequestMethod.HEAD)
    public void handleHead(HttpServletResponse rsp) {
        addOpenRosaHeaders(rsp);
        rsp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @GetMapping(value = "/submission/{idScheme:\\w+}:{instanceId}/{fileName}.{extension}")
    public String getSubmissionFile(@PathVariable String idScheme, @PathVariable String instanceId,
                                    @PathVariable String fileName, @PathVariable String extension) throws UnsupportedEncodingException {
        return String.format("forward:%s/%s/%s/%s.%s", SUBMISSIONS_PATH, idScheme, instanceId, fileName, extension);
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
                continue; // Don't allow updating the original form instance
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

        String id, name, version, hash, path;

        OpenRosaFormDef(String name, String id, String version, String hash, String path) {
            this.id = id;
            this.name = name;
            this.version = version;
            this.hash = hash;
            this.path = path;
        }

        public String toString(String baseUrl) {
            return "<xform>\n" +
                    "<formID>" + id + "</formID>\n" +
                    "<name>" + name + "</name>\n" +
                    "<majorMinorVersion>" + version + "</majorMinorVersion>\n" +
                    "<version>" + version + "</version>\n" +
                    "<hash>md5:" + hash + "</hash>" +
                    "<downloadUrl>" + baseUrl + "/" + path + "</downloadUrl>\n" +
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
        return new OpenRosaFormDef(title, id, version, hash, id + "/" + version + "/" + fileName);
    }
}