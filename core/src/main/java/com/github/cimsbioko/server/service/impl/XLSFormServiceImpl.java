package com.github.cimsbioko.server.service.impl;


import com.github.cimsbioko.server.service.XLSFormService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URI;
import java.util.zip.ZipFile;

import static org.springframework.util.StreamUtils.copy;

public class XLSFormServiceImpl implements XLSFormService {

    private static MediaType EXCEL = MediaType.parseMediaType("application/vnd.ms-excel");
    private static MediaType ZIP = MediaType.parseMediaType("application/zip");

    private RestTemplate template;

    @Value("${app.convert.url}")
    private URI convertUri;

    public XLSFormServiceImpl(RestTemplate restTemplate) {
        template = restTemplate;
    }

    @Override
    public ZipFile convertXLSForm(InputStream xlsxInput) throws IOException {
        ResponseEntity<Resource> result = template.exchange(
                RequestEntity.post(convertUri)
                        .accept(ZIP)
                        .contentType(EXCEL)
                        .body(new InputStreamResource(xlsxInput)),
                Resource.class);

        HttpStatus status = result.getStatusCode();

        if (status.is2xxSuccessful()) {
            if (result.hasBody()) {
                File temp = File.createTempFile("convertxls", ".zip");
                try (FileOutputStream out = new FileOutputStream(temp)) {
                    copy(result.getBody().getInputStream(), out);
                    return new ZipFile(temp);
                }
            } else {
                throw new RuntimeException("conversion service sent empty response body");
            }
        } else {
            throw new RuntimeException("conversion failed, received " + status);
        }
    }

}
