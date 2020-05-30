package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.service.SyncService;
import com.github.cimsbioko.server.service.impl.MobileDbGeneratorEvent;
import com.github.cimsbioko.server.service.impl.sync.ExportFinishedEvent;
import com.github.cimsbioko.server.service.impl.sync.ExportStatusEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Controller
public class SyncController {

    private final SimpMessagingTemplate simpMsgTemplate;
    private final SyncService service;

    public SyncController(SimpMessagingTemplate simpMsgTemplate, SyncService service) {
        this.simpMsgTemplate = simpMsgTemplate;
        this.service = service;
    }

    @PreAuthorize("hasAuthority('VIEW_SYNC')")
    @GetMapping("/sync/{campaign}")
    @ResponseBody
    public ResponseEntity<?> loadSync(@PathVariable("campaign") String campaign) {
        return getSyncData(campaign)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private Optional<Map<String, String>> getSyncData(String campaign) {
        SyncService.Status status = service.getStatus(campaign);
        return service.getTask(campaign).map(task -> {
            final Map<String, String> data = new HashMap<>();
            data.put("status", status.name());
            data.put("contentHash", task.getContentHash());
            data.put("nextRun", Long.toString(task.getNextRunMinutes()));
            data.put("progress", Integer.toString(task.getPercentComplete()));
            return data;
        });
    }

    @EventListener(classes = {ExportStatusEvent.class, ExportFinishedEvent.class})
    @Transactional(propagation = REQUIRES_NEW)
    public void publishSyncStatus(MobileDbGeneratorEvent event) {
        String campaign = event.getCampaignUuid();
        getSyncData(campaign).ifPresent(data -> {
            simpMsgTemplate.convertAndSend(String.format("/topic/sync/%s", campaign), data);
        });
    }

    @PreAuthorize("hasAuthority('MANAGE_SYNC')")
    @GetMapping("/sync/{campaign}/pause")
    @ResponseBody
    public ResponseEntity<?> pause(@PathVariable("campaign") String campaign) {
        service.pauseSync(campaign);
        return getSyncData(campaign)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('MANAGE_SYNC')")
    @GetMapping("/sync/{campaign}/resume")
    @ResponseBody
    public ResponseEntity<?> resume(@PathVariable("campaign") String campaign) {
        service.resumeSync(campaign);
        return getSyncData(campaign)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('MANAGE_SYNC')")
    @GetMapping("/sync/{campaign}/run")
    @ResponseBody
    public ResponseEntity<?> run(@PathVariable("campaign") String campaign) {
        service.requestExport(campaign);
        return getSyncData(campaign)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('EXPORT_SYNC')")
    @GetMapping("/sync/{campaign}/export")
    public void downloadDb(@PathVariable("campaign") String campaign, HttpServletResponse response) throws IOException {
        String filename = "cims.db";
        FileSystemResource dbFileRes = new FileSystemResource(service.getOutput(campaign));
        if (!dbFileRes.isReadable()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Unable to find mobiledb file. Try generating it from the tasks menu.");
        } else {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".zip\"");
            try (ZipOutputStream zOut = new ZipOutputStream(response.getOutputStream())) {
                ZipEntry e = new ZipEntry(filename);
                e.setSize(dbFileRes.contentLength());
                e.setTime(System.currentTimeMillis());
                zOut.putNextEntry(e);
                StreamUtils.copy(dbFileRes.getInputStream(), zOut);
                zOut.closeEntry();
                zOut.finish();
            }
        }
    }
}
