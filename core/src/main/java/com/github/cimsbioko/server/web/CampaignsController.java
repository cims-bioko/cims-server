package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.CampaignRepository;
import com.github.cimsbioko.server.dao.DeviceRepository;
import com.github.cimsbioko.server.dao.FormRepository;
import com.github.cimsbioko.server.dao.UserRepository;
import com.github.cimsbioko.server.domain.*;
import com.github.cimsbioko.server.service.CampaignService;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class CampaignsController {

    private final CampaignRepository repo;
    private final FormRepository formRepo;
    private final DeviceRepository deviceRepo;
    private final UserRepository userRepo;
    private final CampaignService service;
    private final MessageSource messages;

    public CampaignsController(CampaignRepository repo, FormRepository formRepo, DeviceRepository deviceRepo,
                               UserRepository userRepo, CampaignService service, MessageSource messages) {
        this.repo = repo;
        this.formRepo = formRepo;
        this.deviceRepo = deviceRepo;
        this.userRepo = userRepo;
        this.service = service;
        this.messages = messages;
    }

    @PreAuthorize("hasAuthority('VIEW_CAMPAIGNS')")
    @GetMapping("/campaigns")
    @ResponseBody
    public Page<Campaign> campaigns(@RequestParam(name = "p", defaultValue = "0") Integer page,
                                    @RequestParam(name = "q", defaultValue = "") String query) {
        return query.isEmpty() ?
                repo.findByDeletedIsNull(PageRequest.of(page, 10, Sort.by("name"))) :
                repo.findBySearch(query, PageRequest.of(page, 10));
    }

    @PreAuthorize("hasAuthority('VIEW_CAMPAIGNS')")
    @GetMapping("/campaign/{uuid}")
    @ResponseBody
    @Transactional
    public CampaignForm loadCampaign(@PathVariable String uuid) {
        Campaign c = repo.findById(uuid).orElseThrow(ResourceNotFoundException::new);
        CampaignForm form = new CampaignForm();
        form.setName(c.getName());
        form.setDescription(c.getDescription());
        form.setStart(c.getStart());
        form.setEnd(c.getEnd());
        form.setDisabled(c.getDisabled() != null);
        form.setForms(c.getForms().stream().map(Form::getFormId).collect(Collectors.toList()));
        form.setDevices(c.getDevices().stream().map(Device::getUuid).collect(Collectors.toList()));
        form.setUsers(c.getUsers().stream().map(User::getUuid).collect(Collectors.toList()));
        return form;
    }

    @PreAuthorize("hasAuthority('SET_DEFAULT_CAMPAIGN')")
    @PutMapping("/campaign/{uuid}/default")
    @ResponseBody
    @Transactional
    public ResponseEntity<AjaxResult> setDefaultCampaign(@PathVariable String uuid, Locale locale) {
        Optional<Campaign> maybeNewDefault = repo.findById(uuid);
        if (!maybeNewDefault.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("name",
                                    resolveMessage("campaigns.msg.existsnot", locale, uuid)));
        } else {
            Campaign newDefault = maybeNewDefault.get(), oldDefault = repo.findDefault().orElse(null);
            if (oldDefault != null && !oldDefault.equals(newDefault)) {
                oldDefault.setDefaultCampaign(false);
                repo.flush(); // ordering not guaranteed, flush to avoid unique constraint violation
            }
            newDefault.setDefaultCampaign(true);
            return ResponseEntity
                    .ok(new AjaxResult()
                            .addMessage(
                                    resolveMessage("campaigns.msg.setdefault", locale, newDefault.getName())));
        }
    }

    @PreAuthorize("hasAuthority('CREATE_CAMPAIGNS')")
    @PostMapping("/campaigns")
    @ResponseBody
    @Transactional
    public ResponseEntity<AjaxResult> createCampaign(@Valid @RequestBody CampaignForm form, Locale locale) {

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

        Set<Form> associatedForms = new HashSet<>();
        Optional.ofNullable(form.getForms())
                .map(formRepo::findAllById)
                .orElse(Collections.emptyList())
                .forEach(associatedForms::add);
        initial.setForms(associatedForms);

        Set<User> associatedUsers = new HashSet<>();
        Optional.ofNullable(form.getUsers())
                .map(userRepo::findAllById)
                .orElse(Collections.emptyList())
                .forEach(associatedUsers::add);
        initial.setUsers(associatedUsers);

        Campaign saved = repo.save(initial);

        Optional.ofNullable(form.getDevices())
                .map(deviceRepo::findAllById)
                .orElse(Collections.emptyList())
                .forEach(d -> d.setCampaign(saved));

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("campaigns.msg.created", locale, saved.getName())));
    }

    private String resolveMessage(String key, Locale locale, Object... args) {
        return messages.getMessage(key, args, locale);
    }

    @PreAuthorize("hasAuthority('EDIT_CAMPAIGNS')")
    @PutMapping("/campaign/{uuid}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> updateCampaign(@PathVariable String uuid, @Valid @RequestBody CampaignForm form, Locale locale) {

        Campaign existing = repo.findById(uuid).orElseThrow(ResourceNotFoundException::new);

        existing.setName(form.getName());
        existing.setDescription(form.getDescription());
        existing.setStart(Optional.ofNullable(form.getStart()).map(s -> new Timestamp(s.getTime())).orElse(null));
        existing.setEnd(Optional.ofNullable(form.getEnd()).map(e -> new Timestamp(e.getTime())).orElse(null));

        if (form.isDisabled() && existing.getDisabled() == null) {
            Timestamp now = Timestamp.from(Instant.now());
            existing.setDisabled(now);
        } else if (!form.isDisabled()){
            existing.setDisabled(null);
        }

        Set<Form> associatedForms = new HashSet<>();
        Optional.ofNullable(form.getForms())
                .map(formRepo::findAllById)
                .orElse(Collections.emptyList())
                .forEach(associatedForms::add);
        existing.setForms(associatedForms);

        Set<Device> associatedDevices = new HashSet<>();
        Optional.ofNullable(form.getDevices())
                .map(deviceRepo::findAllById)
                .orElse(Collections.emptyList())
                .forEach(associatedDevices::add);
        Set<Device> disassociatedDevices = new HashSet<>(existing.getDevices());
        disassociatedDevices.removeAll(associatedDevices);
        disassociatedDevices.forEach(device -> device.setCampaign(null));
        final Campaign saved = repo.save(existing);
        associatedDevices.forEach(device -> device.setCampaign(saved));

        Set<User> associatedUsers = new HashSet<>();
        Optional.ofNullable(form.getUsers())
                .map(userRepo::findAllById)
                .orElse(Collections.emptyList())
                .forEach(associatedUsers::add);
        existing.setUsers(associatedUsers);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("campaigns.msg.updated", locale, existing.getName())));
    }

    @PreAuthorize("hasAuthority('DELETE_CAMPAIGNS')")
    @DeleteMapping("/campaign/{uuid}")
    @ResponseBody
    public ResponseEntity<?> deleteCampaign(@PathVariable("uuid") String uuid, Locale locale) {

        // FIXME: Use optional rather than null
        Campaign c = repo.findById(uuid).orElse(null);

        if (c == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("campaigns.msg.existsnot", locale, uuid)));
        }

        c.setDeleted(Timestamp.from(Instant.now()));
        repo.save(c);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("campaigns.msg.deleted", locale, c.getName())));
    }

    @PreAuthorize("hasAuthority('RESTORE_CAMPAIGNS')")
    @PutMapping("/campaign/restore/{uuid}")
    @ResponseBody
    public ResponseEntity<?> restoreCampaign(@PathVariable("uuid") String uuid, Locale locale) {

        // FIXME: Use optional rather than null
        Campaign c = repo.findById(uuid).orElse(null);

        if (c == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("campaigns.msg.existsnot", locale, uuid)));
        }

        c.setDeleted(null);
        repo.save(c);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("campaigns.msg.restored", locale, c.getName())));
    }

    @PreAuthorize("hasAuthority('UPLOAD_CAMPAIGNS')")
    @PostMapping("/campaign/{uuid}")
    @ResponseBody
    public ResponseEntity<?> uploadCampaignFile(@PathVariable String uuid, @RequestParam("campaign_file") MultipartFile file) throws IOException {
        service.uploadCampaignFile(uuid, file);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('DOWNLOAD_CAMPAIGNS')")
    @GetMapping("/campaign/export/{uuid}")
    public ResponseEntity<?> downloadCampaignFile(@PathVariable String uuid) throws IOException {
        Optional<File> maybeCampaign = service.getCampaignFile(uuid);
        if (maybeCampaign.isPresent() && maybeCampaign.get().canRead()) {
            Resource res = new FileSystemResource(maybeCampaign.get());
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .contentLength(res.contentLength())
                    .header("Content-Disposition", String.format("attachment; filename=%s", res.getFilename()))
                    .body(res);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAnyAuthority('EDIT_CAMPAIGNS', 'CREATE_CAMPAIGNS')")
    @GetMapping("/campaign/availableForms")
    @ResponseBody
    public List<FormId> availableForms() {
        return StreamSupport
                .stream(formRepo.findAll().spliterator(), false)
                .map(Form::getFormId)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyAuthority('EDIT_CAMPAIGNS', 'CREATE_CAMPAIGNS')")
    @GetMapping("/campaign/{uuid}/availableDevices")
    @ResponseBody
    public List<Device> availableDevicesForEdit(@PathVariable String uuid) {
        return deviceRepo.findSelectableForCampaign(uuid);
    }

    @PreAuthorize("hasAnyAuthority('EDIT_CAMPAIGNS', 'CREATE_CAMPAIGNS')")
    @GetMapping("/campaign/availableUsers")
    @ResponseBody
    public List<User> availableUsers() {
        return userRepo.findSelectableForCampaign();
    }

    @PreAuthorize("hasAnyAuthority('EDIT_CAMPAIGNS', 'CREATE_CAMPAIGNS')")
    @GetMapping("/campaign/availableDevices")
    @ResponseBody
    public List<Device> availableDevicesForCreate() {
        return deviceRepo.findSelectableForCampaign("");
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> uploadFailed() {
        return ResponseEntity.badRequest().build();
    }

    static class CampaignForm {

        @NotNull
        String name;
        String description;
        Date start, end;
        boolean disabled;
        List<FormId> forms = Collections.emptyList();
        List<String> devices = Collections.emptyList();
        List<String> users = Collections.emptyList();

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

        public List<FormId> getForms() {
            return forms;
        }

        public void setForms(List<FormId> forms) {
            this.forms = forms;
        }

        public List<String> getDevices() {
            return devices;
        }

        public void setDevices(List<String> devices) {
            this.devices = devices;
        }

        public List<String> getUsers() {
            return users;
        }

        public void setUsers(List<String> users) {
            this.users = users;
        }
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException extends RuntimeException {

}