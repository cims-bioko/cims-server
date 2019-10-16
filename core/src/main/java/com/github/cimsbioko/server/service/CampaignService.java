package com.github.cimsbioko.server.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface CampaignService {
    void uploadCampaignFile(String name, MultipartFile file) throws IOException;
    Optional<File> getCampaignFile(String name);
}
