package com.github.cimsbioko.server.webapi;

import com.github.cimsbioko.server.dao.FormSubmissionDao;
import com.github.cimsbioko.server.domain.model.FormSubmission;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
import java.util.ArrayList;
import java.util.List;

import static com.github.cimsbioko.server.Application.WebConfig.FORMS_PATH;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.json.XML.toJSONObject;
import static org.springframework.security.web.util.UrlUtils.buildFullRequestUrl;

@Controller
public class ODKFormsResource {

    private static Logger log = LoggerFactory.getLogger(ODKFormsResource.class);

    @Resource
    File formsDir;

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
    private FormSubmissionDao submitDao;

    @RequestMapping(value = "/submission", method = RequestMethod.HEAD)
    public void handleHead(HttpServletResponse rsp) {
        addOpenRosaHeaders(rsp);
        rsp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @PostMapping("/submission")
    public void handle(@RequestParam("xml_submission_file") MultipartFile formFile,
                       @RequestParam("deviceID") String deviceId, HttpServletResponse rsp) throws IOException {
        String formContent = new String(StreamUtils.copyToByteArray(formFile.getInputStream()), "UTF-8");
        JSONObject jsonContent = toJSONObject(formContent);
        log.info("received submission from device '{}': {}\n{}", deviceId, formContent, jsonContent);
        addOpenRosaHeaders(rsp);
        String instanceName = jsonContent.names().getString(0);
        JSONObject instance = jsonContent.getJSONObject(instanceName), meta = instance.getJSONObject("meta");
        Timestamp collected = Timestamp.valueOf(instance.getString("collectionDateTime"));
        FormSubmission submission = new FormSubmission(meta.getString("instanceID"), formContent, jsonContent.toString(),
                instance.getString("id"), instance.get("version").toString(), instance.getString("cims-binding"),
                deviceId, collected, null);
        submitDao.save(submission);
        rsp.setStatus(HttpServletResponse.SC_CREATED);
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