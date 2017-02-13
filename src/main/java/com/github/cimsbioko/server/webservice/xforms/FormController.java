package com.github.cimsbioko.server.webservice.xforms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.springframework.security.web.util.UrlUtils.buildFullRequestUrl;

@Controller
public class FormController {

    private static Logger log = LoggerFactory.getLogger(FormController.class);

    @GetMapping(path = "/forms", produces = {"text/xml"})
    @ResponseBody
    public void formList(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        rsp.setContentType("text/xml;charset=UTF-8");
        rsp.setHeader("X-OpenRosa-Version", "1.0");
        rsp.setIntHeader("X-OpenRosa-Accept-Content-Length", 10485760);
        rsp.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">\n" +
                "<xform>\n" +
                "<formID>test</formID>\n" +
                "<name>Test</name>\n" +
                "<majorMinorVersion>1</majorMinorVersion>\n" +
                "<version>1</version>\n" +
                "<hash>md5:a293aeb1c461aa8ca5991cbaab089932</hash>" +
                "<downloadUrl>" + buildFullRequestUrl(req) + "/test</downloadUrl>\n" +
                "</xform>\n" +
                "</xforms>");
    }

    @GetMapping(path = "/forms/{formId}", produces = {"text/xml"})
    @ResponseBody
    public void form(@PathVariable String formId, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        rsp.setContentType("text/xml;charset=UTF-8");
        rsp.setHeader("X-OpenRosa-Version", "1.0");
        rsp.setIntHeader("X-OpenRosa-Accept-Content-Length", 10485760);
        rsp.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<h:html \n" +
                "   xmlns=\"http://www.w3.org/2002/xforms\"\n" +
                "   xmlns:h=\"http://www.w3.org/1999/xhtml\"\n" +
                "   xmlns:jr=\"http://openrosa.org/javarosa\">\n" +
                "  <h:head>\n" +
                "    <h:title>Test</h:title>\n" +
                "    <model>\n" +
                "      <instance>\n" +
                "        <data id=\"" + formId + "\" version=\"1\">\n" +
                "          <meta>\n" +
                "            <instanceID/>\n" +
                "          </meta>\n" +
                "          <now/>\n" +
                "        </data>\n" +
                "      </instance>\n" +
                "      <bind nodeset=\"/data/meta/instanceID\" type=\"string\" readonly=\"true()\" calculate=\"concat('uuid:', uuid())\"/>\n" +
                "      <bind nodeset=\"/data/now\" type=\"dateTime\" readonly=\"true\"/>\n" +
                "    </model>\n" +
                "  </h:head>\n" +
                "  <h:body>\n" +
                "    <group appearance=\"field-list\">\n" +
                "      <input ref=\"/data/meta/instanceID\" />\n" +
                "      <input ref=\"/data/now\"/>\n" +
                "    </group>\n" +
                "  </h:body>\n" +
                "</h:html>\n");
    }

    @PostMapping("/submission")
    public void handle(@RequestParam("xml_submission_file") MultipartFile formFile,
                       @RequestParam("deviceID") String deviceId, HttpServletResponse rsp) throws IOException {
        String formContent = new String(StreamUtils.copyToByteArray(formFile.getInputStream()), "UTF-8");
        log.info("received submission from device '{}': {}", deviceId, formContent);
        rsp.setHeader("X-OpenRosa-Version", "1.0");
        rsp.setIntHeader("X-OpenRosa-Accept-Content-Length", 10485760);
        rsp.setStatus(HttpServletResponse.SC_CREATED);
    }
}