package com.github.cimsbioko.server.web;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream;
import static java.net.URLEncoder.encode;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.security.web.util.UrlUtils.buildFullRequestUrl;

@Controller
public class MobileConfigController {

    @PreAuthorize("hasAuthority('VIEW_MOBILE_CONFIG_CODES')")
    @GetMapping(value = "/mcfg")
    public void generateConfigCode(HttpServletRequest req, HttpServletResponse res,
                                   @RequestParam String name, @RequestParam String secret,
                                   @RequestParam(defaultValue = "320") int dim) throws IOException, WriterException {
        String serverUrl = buildFullRequestUrl(req.getScheme(), req.getServerName(), req.getServerPort(), "", null);
        String content = String.format("cimsmcfg://%s?d=%s&s=%s", urlEncode(serverUrl), urlEncode(name), urlEncode(secret));
        BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, dim, dim);
        res.setContentType(IMAGE_PNG_VALUE);
        writeToStream(bitMatrix, "PNG", res.getOutputStream());
    }

    private String urlEncode(String value) throws UnsupportedEncodingException {
        return encode(value, StandardCharsets.UTF_8.toString());
    }

}