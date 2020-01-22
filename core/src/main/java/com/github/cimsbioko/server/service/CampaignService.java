package com.github.cimsbioko.server.service;

import com.github.cimsbioko.server.domain.Campaign;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CampaignService {
    void uploadCampaignFile(String uuid, MultipartFile file) throws IOException;
    Optional<File> getCampaignFile(String uuid);
    boolean isMember(String uuid, Authentication auth);
    List<Campaign> getMyCampaigns(Authentication auth);
}
