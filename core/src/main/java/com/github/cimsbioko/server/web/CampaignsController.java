package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.service.CampaignService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Controller
public class CampaignsController {

    private final CampaignService service;

    public CampaignsController(CampaignService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('UPLOAD_CAMPAIGNS')")
    @PostMapping("/campaign")
    @ResponseBody
    public ResponseEntity uploadForm(@RequestParam("campaign_file") MultipartFile file) throws IOException {
        service.uploadCampaignFile(null, file);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('DOWNLOAD_CAMPAIGNS')")
    @GetMapping("/campaign/export/{name}")
    public ResponseEntity downloadForm(@PathVariable("name") String name, HttpServletResponse response) throws IOException {
        Optional<File> maybeCampaign = service.getCampaignFile(name);
        if (maybeCampaign.isPresent()) {
            Resource res = new FileSystemResource(maybeCampaign.get());
            response.setHeader("Content-Disposition", String.format("attachment; filename=%s.zip", name));
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .contentLength(res.contentLength())
                    .header("Content-Disposition", String.format("attachment; filename=%s.zip", name))
                    .body(res);
        }
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity uploadFailed() {
        return ResponseEntity.badRequest().build();
    }
}
