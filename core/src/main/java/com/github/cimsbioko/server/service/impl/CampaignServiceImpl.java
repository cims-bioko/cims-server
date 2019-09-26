package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.service.CampaignService;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class CampaignServiceImpl implements CampaignService {

    @Resource
    private File campaignsDir;

    @Override
    public void uploadCampaignFile(String name, MultipartFile file) throws IOException {
        file.transferTo(getCampaignFilePath(name));
    }

    @Override
    public Optional<File> getCampaignFile(String name) {
        File archive = getCampaignFilePath(name).toFile();
        return archive.canRead() ? Optional.of(archive) : Optional.empty();
    }

    private Path getCampaignFilePath(String name) {
        String fileName = Optional.ofNullable(name).map(n -> n + ".zip").orElse("default.zip");
        return campaignsDir.toPath().resolve(Paths.get(fileName));
    }
}
