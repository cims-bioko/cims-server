package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.service.SyncService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class SyncController {

    private SyncService service;

    public SyncController(SyncService service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('VIEW_SYNC')")
    @GetMapping("/sync")
    public ModelAndView sync() {
        ModelAndView result = new ModelAndView("sync");
        result.addObject("nextRun", service.getMinutesToNextRun().orElse(null));
        result.addObject("task", service.getTask());
        result.addObject("scheduled", service.isTaskScheduled());
        result.addObject("schedule", service.getSchedule().map(String::trim).orElse(""));
        result.addObject("running", service.isTaskRunning());
        result.addObject("downloadable", service.getOutput().canRead());
        return result;
    }

    @PreAuthorize("hasAuthority('MANAGE_SYNC')")
    @GetMapping("/sync/pause")
    public String pause() {
        service.cancelTask();
        return "redirect:/sync";
    }

    @PreAuthorize("hasAuthority('MANAGE_SYNC')")
    @GetMapping("/sync/start")
    public String start() {
        service.resumeSchedule();
        return "redirect:/sync";
    }

    @PreAuthorize("hasAuthority('MANAGE_SYNC')")
    @GetMapping("/sync/run")
    public String run() {
        service.requestTaskRun();
        return "redirect:/sync";
    }

    private static final String INSTALLABLE_FILENAME = "openhds.db";

    @PreAuthorize("hasAuthority('EXPORT_SYNC')")
    @GetMapping("/sync/export")
    public void downloadDb(HttpServletResponse response) throws IOException {

        FileSystemResource dbFileRes = new FileSystemResource(service.getOutput());

        if (!dbFileRes.isReadable()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Unable to find mobiledb file. Try generating it from the tasks menu.");
        } else {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + INSTALLABLE_FILENAME + ".zip");
            try (ZipOutputStream zOut = new ZipOutputStream(response.getOutputStream())) {
                ZipEntry e = new ZipEntry(INSTALLABLE_FILENAME);
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
