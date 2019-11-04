package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.CampaignRepository;
import com.github.cimsbioko.server.domain.Campaign;
import com.github.cimsbioko.server.service.CampaignService;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

@Controller
public class CampaignsController {

    private final CampaignRepository repo;
    private final CampaignService service;
    private final MessageSource messages;

    public CampaignsController(CampaignRepository repo, CampaignService service, MessageSource messages) {
        this.repo = repo;
        this.service = service;
        this.messages = messages;
    }

    @PreAuthorize("hasAuthority('VIEW_CAMPAIGNS')")
    @GetMapping("/campaigns")
    @ResponseBody
    public Page<Campaign> campaigns(@RequestParam(name = "p", defaultValue = "0") Integer page) {
        return repo.findAll(PageRequest.of(page, 10, Sort.by("name")));
    }

    @PreAuthorize("hasAuthority('CREATE_CAMPAIGNS')")
    @PostMapping("/campaigns")
    @ResponseBody
    public ResponseEntity<AjaxResult> createDevice(@Valid @RequestBody CampaignForm form, Locale locale) {

        if (repo.findByName(form.getName()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("name",
                                    resolveMessage("campaigns.msg.exists", locale, form.getName())));
        }

        Timestamp now = Timestamp.from(Instant.now());
        final Campaign initial = new Campaign();
        initial.setName(form.getName());
        initial.setDescription(form.getDescription());
        initial.setCreated(now);
        Optional.ofNullable(form.getStart()).map(s -> new Timestamp(s.getTime())).ifPresent(initial::setStart);
        Optional.ofNullable(form.getEnd()).map(e -> new Timestamp(e.getTime())).ifPresent(initial::setEnd);
        if (form.isDisabled()) {
            initial.setDisabled(now);
        }
        Campaign saved = repo.save(initial);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("campaigns.msg.created", locale, saved.getName())));
    }

    private String resolveMessage(String key, Locale locale, Object... args) {
        return messages.getMessage(key, args, locale);
    }

    @PreAuthorize("hasAuthority('UPLOAD_CAMPAIGNS')")
    @PostMapping("/campaign/{name}")
    @ResponseBody
    public ResponseEntity uploadForm(@PathVariable(value = "name") String name, @RequestParam("campaign_file") MultipartFile file) throws IOException {
        service.uploadCampaignFile(name, file);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('DOWNLOAD_CAMPAIGNS')")
    @GetMapping("/campaign/export/{name}")
    public ResponseEntity downloadForm(@PathVariable("name") String name, HttpServletResponse response) throws IOException {
        Optional<File> maybeCampaign = service.getCampaignFile(name);
        if (maybeCampaign.isPresent() && maybeCampaign.get().canRead()) {
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

    static class CampaignForm {

        @NotNull
        String name;
        String description;
        Date start, end;
        boolean disabled;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public Date getEnd() {
            return end;
        }

        public void setEnd(Date end) {
            this.end = end;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }
}
