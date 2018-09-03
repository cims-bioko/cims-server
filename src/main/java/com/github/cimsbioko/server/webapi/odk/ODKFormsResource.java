package com.github.cimsbioko.server.webapi.odk;

import com.github.cimsbioko.server.dao.FormDao;
import com.github.cimsbioko.server.domain.Form;
import com.github.cimsbioko.server.domain.FormId;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.cimsbioko.server.util.JDOMUtil.getBuilder;
import static com.github.cimsbioko.server.util.JDOMUtil.getOutputter;
import static com.github.cimsbioko.server.webapi.odk.Constants.DEFAULT_VERSION;
import static com.github.cimsbioko.server.webapi.odk.Constants.ODK_API_PATH;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.MD5;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.security.web.util.UrlUtils.buildFullRequestUrl;

@Controller
@RequestMapping(ODK_API_PATH)
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
    private static final String FORM_DEF_FILE = "form_def_file";
    private static final String TITLE = "title";

    @Resource
    private File formsDir;

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

        String id = firstInstance.getAttributeValue(Constants.ID),
                version = firstInstance.getAttributeValue(Constants.VERSION);

        if (version == null) {
            version = DEFAULT_VERSION;
            firstInstance.setAttribute(Constants.VERSION, version);
        }

        FormId formId = new FormId(id, version);
        Form form = formDao.findById(formId);
        if (form == null) {
            log.info("uploading new form");
            form = new Form(formId, formDoc);
        } else {
            log.info("updating existing form");
            form.setXml(formDoc);
        }
        formDao.save(form);
        formDao.exclusiveDownload(form);

        String formFilePath = getFormFilePath(id, version);
        log.info("storing form at {}", formFilePath);

        File formFile = new File(formsDir, formFilePath);
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

                    String fileName = file.getOriginalFilename();

                    if (fileName.isEmpty()) {
                        continue;
                    }

                    File dest = new File(formDir, fileName);
                    file.transferTo(dest);

                    Element mediaFileElem = new Element(MEDIA_FILE, manifestNs);
                    manifestElem.addContent(mediaFileElem);

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

    private String getFormFilePath(String id, String version) {
        return String.format("%1s/%2$s.xml", getFormDirPath(id, version), id);
    }

    private String getFormDirPath(String id, String version) {
        return String.format("%1$s/%2$s", id, version);
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
        downloadableForms().forEach(f -> {
            try {
                writer.write(f.toString(buildFullRequestUrl(req)));
            } catch (IOException e) {
                log.warn("failed to list form id={}, version={}: {}", f.id, f.version, e.toString());
            }
        });
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
    public ResponseEntity<InputStreamResource> manifest(@PathVariable String formId, @PathVariable String formVersion,
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
    public ResponseEntity<?> deleteForm(@PathVariable String formId, @PathVariable String formVersion) {
        String formPath = String.format("%s/%s", formId, formVersion);
        File formDir = new File(formsDir, formPath);
        if (formDir.exists() && FileSystemUtils.deleteRecursively(formDir)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
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
                    "<hash>" + Constants.MD5_SCHEME + hash + "</hash>" +
                    "<downloadUrl>" + baseUrl + "/" + path + "</downloadUrl>\n" +
                    (manifestPath != null ? "<manifestUrl>" + baseUrl + "/" + manifestPath + "</manifestUrl>" : "") +
                    "</xform>\n";
        }
    }

    /**
     * Scans the application form directory and returns the list of detected (valid) form definitions found.
     */
    private Stream<OpenRosaFormDef> downloadableForms() throws IOException {
        if (formsDir.exists()) {
            Path formsPath = formsDir.toPath();
            return formDao.findDownloadable()
                    .stream()
                    .map(form -> getFormFilePath(form.getFormId().getId(), form.getFormId().getVersion()))
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

    private String getFileHash(File toHash) throws NoSuchAlgorithmException, IOException {
        try (DigestInputStream in = new DigestInputStream(new FileInputStream(toHash), MessageDigest.getInstance(MD5))) {
            byte[] buf = new byte[8096];
            while (in.read(buf) >= 0) {
                // reading only to compute hash
            }
            return Constants.MD5_SCHEME + encodeHexString(in.getMessageDigest().digest());
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