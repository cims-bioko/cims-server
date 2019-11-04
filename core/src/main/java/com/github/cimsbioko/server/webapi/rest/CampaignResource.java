package com.github.cimsbioko.server.webapi.rest;

import com.github.cimsbioko.server.service.CampaignService;
import com.github.cimsbioko.server.webapi.odk.FileHasher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.WebRequest;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Controller
public class CampaignResource {

    private final FileHasher fileHasher;
    private final CampaignService service;

    public CampaignResource(FileHasher fileHasher, CampaignService service) {
        this.fileHasher = fileHasher;
        this.service = service;
    }

    @PreAuthorize("hasAuthority('DOWNLOAD_CAMPAIGNS' and @campaignService.isMember('default', #auth))")
    @GetMapping("/api/rest/campaign")
    public ResponseEntity<InputStreamResource> downloadCampaign(WebRequest request, Authentication auth) throws IOException, NoSuchAlgorithmException {
        return downloadCampaign(request, "default");
    }

    @PreAuthorize("hasAuthority('DOWNLOAD_CAMPAIGNS' and @campaignService.isMember(#name, #auth))")
    @GetMapping("/api/rest/campaign/{name}")
    public ResponseEntity<InputStreamResource> downloadCampaign(WebRequest request, @PathVariable String name, Authentication auth) throws IOException, NoSuchAlgorithmException {
        String campaign = Optional.ofNullable(name).orElse("default");
        return downloadCampaign(request, campaign);
    }

    private ResponseEntity<InputStreamResource> downloadCampaign(WebRequest request, String campaign) throws IOException, NoSuchAlgorithmException {
        Optional<File> maybeCampaignFile = service.getCampaignFile(campaign);
        if (maybeCampaignFile.isPresent()) {
            File file = maybeCampaignFile.get();
            String descriptor = fileHasher.hashFile(file);
            if (request.checkNotModified(descriptor)) {
                return null;
            } else {
                Resource resource = new FileSystemResource(file);
                return ResponseEntity
                        .ok()
                        .eTag(descriptor)
                        .contentLength(resource.contentLength())
                        .contentType(MediaType.parseMediaType("application/zip"))
                        .body(new InputStreamResource(resource.getInputStream()));
            }
        }
        return ResponseEntity.notFound().build();
    }
}
