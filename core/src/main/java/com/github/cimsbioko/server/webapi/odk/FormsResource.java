package com.github.cimsbioko.server.webapi.odk;

import com.github.cimsbioko.server.dao.FormRepository;
import com.github.cimsbioko.server.service.FormService;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.cimsbioko.server.util.JDOMUtil.getOutputter;
import static com.github.cimsbioko.server.webapi.odk.Constants.*;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.MD5;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.security.web.util.UrlUtils.buildFullRequestUrl;

@Controller
@RequestMapping(ODK_API_PATH)
public class FormsResource {

    private static Logger log = LoggerFactory.getLogger(FormsResource.class);

    @Resource
    private File formsDir;

    @Autowired
    private FormRepository formDao;

    @Autowired
    private EndpointHelper helper;

    @Autowired
    private FormFileSystem formFileSystem;

    @Autowired
    private OpenRosaResponseBuilder responseBuilder;

    @Autowired
    private FormService formService;

    @RequestMapping(value = {"/upload", "/forms", "/formUpload"}, method = RequestMethod.HEAD)
    public ResponseEntity headForBriefcasePush(HttpServletRequest req) {
        return ResponseEntity
                .noContent()
                .headers(helper.openRosaHeaders())
                .header(LOCATION, helper.contextRelativeUrl(req, "formUpload"))
                .build();
    }

    @PostMapping(path = {"/forms", "/formUpload"})
    @PreAuthorize("hasAuthority('ODK_FORM_UPLOAD')")
    public ResponseEntity<ByteArrayResource> uploadForm(@RequestParam(FORM_DEF_FILE) MultipartFile formXml,
                                                        MultipartHttpServletRequest req)
            throws JDOMException, IOException, NoSuchAlgorithmException, URISyntaxException {
        formService.uploadForm(formXml, null, req.getMultiFileMap());
        return ResponseEntity
                .created(new URI(helper.contextRelativeUrl(req, "formUpload")))
                .contentType(MediaType.TEXT_XML)
                .body(new ByteArrayResource(responseBuilder.response("Successful upload").getBytes()));
    }

    @GetMapping(path = {"/forms", "/formList"})
    @PreAuthorize("hasAuthority('ODK_FORM_LIST')")
    public ResponseEntity<ByteArrayResource> formList(@RequestParam(value = "campaign", required = false) String campaign, HttpServletRequest req) {
        String reqUrlNoQuery = buildFullRequestUrl(req.getScheme(), req.getServerName(), req.getServerPort(), req.getRequestURI(),  null);
        String formList = buildFormList(downloadableForms(campaign), reqUrlNoQuery);
        return ResponseEntity
                .ok()
                .headers(helper.openRosaHeaders())
                .header(CONTENT_TYPE, "text/xml;charset=UTF-8")
                .body(new ByteArrayResource(formList.getBytes()));
    }


    @GetMapping(path = {"/forms/{formId:\\w+}/{formVersion:\\d+}/{fileName:[a-zA-Z0-9-_ ]+[.]\\w+}",
            "/formList/{formId:\\w+}/{formVersion:\\d+}/{fileName:[a-zA-Z0-9-_ ]+[.]\\w+}"})
    @PreAuthorize("hasAuthority('ODK_FORM_DOWNLOAD')")
    public ResponseEntity<InputStreamResource> formFile(@PathVariable String formId, @PathVariable String formVersion,
                                                        @PathVariable String fileName) throws IOException {
        File formFile = formFileSystem.getFormFilePath(formId, formVersion, fileName).toFile();
        org.springframework.core.io.Resource formResource = new FileSystemResource(formFile);
        return ResponseEntity
                .ok()
                .headers(helper.openRosaHeaders())
                .contentLength(formResource.contentLength())
                .contentType(fileName.endsWith(".xml") ? MediaType.TEXT_XML : MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(formResource.getInputStream()));
    }

    @GetMapping(path = {"/forms/{formId:\\w+}/{formVersion:\\d+}/manifest",
            "/formList/{formId:\\w+}/{formVersion:\\d+}/manifest"}, produces = "text/xml")
    @PreAuthorize("hasAuthority('ODK_FORM_DOWNLOAD')")
    public ResponseEntity<ByteArrayResource> manifest(@PathVariable String formId, @PathVariable String formVersion,
                                                      HttpServletRequest req) throws IOException, JDOMException {

        File manifestFile = formFileSystem.getFormFilePath(formId, formVersion, MEDIA_MANIFEST).toFile();

        SAXBuilder builder = new SAXBuilder();
        org.springframework.core.io.Resource manifestResource = new FileSystemResource(manifestFile);
        Document manifestDoc = builder.build(manifestResource.getInputStream());

        String formBaseUrl = buildFullRequestUrl(req).replaceFirst("manifest$", "");

        // need to define a prefix for namespace for xpath to work
        Namespace nsWithPrefix = Namespace.getNamespace("mf", "http://openrosa.org/xforms/xformsManifest");
        Namespace nsNoPrefix = Namespace.getNamespace("http://openrosa.org/xforms/xformsManifest");

        // replace
        XPathFactory xpathFactory = XPathFactory.instance();
        XPathExpression<Element> xpathExpression = xpathFactory
                .compile("//mf:" + MEDIA_FILE, Filters.element(), null, nsWithPrefix);
        List<Element> files = xpathExpression.evaluate(manifestDoc);
        for (Element file : files) {
            Element filenameElem = file.getChild(FILENAME, nsNoPrefix),
                    downloadUrlElem = new Element(DOWNLOAD_URL, nsNoPrefix);
            downloadUrlElem.setText(formBaseUrl + filenameElem.getText());
            file.addContent(downloadUrlElem);
        }

        byte[] body = getOutputter().outputString(manifestDoc).getBytes();

        return ResponseEntity
                .ok()
                .headers(helper.openRosaHeaders())
                .contentType(MediaType.TEXT_XML)
                .contentLength(body.length)
                .body(new ByteArrayResource(body));
    }

    private String buildFormList(Stream<OpenRosaFormDef> forms, String formListUrl) {
        StringBuilder b = new StringBuilder();
        b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xforms xmlns=\"http://openrosa.org/xforms/xformsList\">\n");
        forms.forEach(f -> b.append(f.toString(formListUrl)));
        b.append("</xforms>");
        return b.toString();
    }

    /**
     * Represents all of the required information for a form definition from the OpenRosa form list API.
     */
    private static class OpenRosaFormDef {

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
                    "<hash>" + Constants.MD5_SCHEME + hash + "</hash>" +
                    "<downloadUrl>" + baseUrl + "/" + path + "</downloadUrl>\n" +
                    (manifestPath != null ? "<manifestUrl>" + baseUrl + "/" + manifestPath + "</manifestUrl>" : "") +
                    "</xform>\n";
        }
    }

    /**
     * Scans the application form directory and returns the list of detected (valid) form definitions found.
     */
    private Stream<OpenRosaFormDef> downloadableForms(String campaign) {
        if (formsDir.exists()) {
            Path formsPath = formsDir.toPath();
            return Optional.ofNullable(campaign)
                    .map(formDao::findDownloadableByCampaign)
                    .orElseGet(formDao::findDownloadable)
                    .stream()
                    .map(form -> formFileSystem.getXmlFormPath(form.getFormId().getId(), form.getFormId().getVersion()))
                    .map(formsPath::resolve)
                    .filter(path -> path.toFile().exists())
                    .map(path -> {
                        try {
                            return loadFormDef(formsPath, path);
                        } catch (FileNotFoundException e) {
                            log.warn("form not found for path '{}'", path);
                        } catch (NoSuchAlgorithmException e) {
                            log.warn("hash failed due to missing algorithm");
                        } catch (IOException e) {
                            log.warn("io failure loading definition for path '{}'", path);
                        } catch (JDOMException e) {
                            log.warn("failed to parse form definition for path '{}'", path);
                        }
                        return null;
                    }).filter(Objects::nonNull);
        }
        return Stream.empty();
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