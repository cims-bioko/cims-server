package com.github.cimsbioko.server.service;

import org.jdom2.JDOMException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

public interface FormService {
    void uploadForm(MultipartFile formXml, MultipartFile xlsform, MultiValueMap<String, MultipartFile> uploadedFiles)
            throws JDOMException, IOException, NoSuchAlgorithmException;
    void uploadXlsform(MultipartFile xlsform, MultiValueMap<String,MultipartFile> multiFileMap) throws IOException, NoSuchAlgorithmException, JDOMException;
    void exportToStream(String id, String version, OutputStream outputStream) throws IOException;
    void manageForm(String id, String version, boolean downloads, boolean submissions);
    void wipeSubmissions(String id, String version);
    void deleteForm(String id, String version);
}
