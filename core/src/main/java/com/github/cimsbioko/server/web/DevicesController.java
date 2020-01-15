package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.DeviceRepository;
import com.github.cimsbioko.server.dao.RoleRepository;
import com.github.cimsbioko.server.dao.UserRepository;
import com.github.cimsbioko.server.domain.Device;
import com.github.cimsbioko.server.security.TokenGenerator;
import com.github.cimsbioko.server.security.TokenHasher;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
public class DevicesController {

    private final DeviceRepository deviceRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final TokenGenerator tokenGen;
    private final TokenHasher hasher;
    private final MessageSource messages;

    public DevicesController(DeviceRepository deviceRepo, UserRepository userRepo, RoleRepository roleRepo,
                             TokenGenerator tokenGen, TokenHasher hasher, MessageSource messages) {
        this.deviceRepo = deviceRepo;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.tokenGen = tokenGen;
        this.hasher = hasher;
        this.messages = messages;
    }

    @PreAuthorize("hasAuthority('VIEW_DEVICES')")
    @GetMapping("/devices")
    @ResponseBody
    public Page<Device> devices(@RequestParam(name = "p", defaultValue = "0") Integer page,
                                @RequestParam(name = "q", defaultValue = "") String query) {
        PageRequest pageObj = PageRequest.of(page, 10);
        return query.isEmpty() ? deviceRepo.findByDeletedIsNull(pageObj) : deviceRepo.findBySearch(query, pageObj);
    }

    @PreAuthorize("hasAuthority('CREATE_DEVICES')")
    @PostMapping("/devices")
    @ResponseBody
    public ResponseEntity<AjaxResult> createDevice(@Valid @RequestBody DeviceForm deviceForm, Locale locale, @AuthenticationPrincipal UserDetails user) {

        if (deviceRepo.findByNameAndDeletedIsNull(deviceForm.getName()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("name",
                                    resolveMessage("devices.msg.exists", locale, deviceForm.getName())));
        }

        final Device initial = new Device();
        String initialSecret = tokenGen.generate();
        initial.setName(deviceForm.getName());
        initial.setDescription(deviceForm.getDescription());
        initial.setSecret(hasher.hash(initialSecret));
        initial.setCreator(userRepo.findByUsernameAndDeletedIsNull(user.getUsername()));
        roleRepo.findByNameAndDeletedIsNull("DEVICE").ifPresent(deviceRole -> initial.getRoles().add(deviceRole));
        Device saved = deviceRepo.save(initial);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addData("device", saved.getName())
                        .addData("secret", initialSecret)
                        .addMessage(
                                resolveMessage("devices.msg.created", locale, saved.getName(), initialSecret)));
    }

    @PreAuthorize("hasAuthority('VIEW_DEVICES')")
    @GetMapping("/device/{uuid}")
    @ResponseBody
    @Transactional
    public DeviceForm loadDevice(@PathVariable("uuid") String uuid) {
        // FIXME: Use optional rather than null
        Device d = deviceRepo.findById(uuid).orElse(null);
        DeviceForm result = new DeviceForm();
        result.setName(d.getName());
        result.setDescription(d.getDescription());
        return result;
    }

    @PreAuthorize("hasAuthority('EDIT_DEVICES')")
    @PutMapping("/device/{uuid}")
    @ResponseBody
    public ResponseEntity<?> updateDevice(@PathVariable("uuid") String uuid, @Valid @RequestBody DeviceForm form, Locale locale) {

        // FIXME: Use optional rather than null
        Device d = deviceRepo.findById(uuid).orElse(null);

        if (d == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("devices.msg.existsnot", locale, form.getName())));
        }

        if (!d.getName().equals(form.getName()) && deviceRepo.findByNameAndDeletedIsNull(form.getName()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("name",
                                    resolveMessage("devices.msg.exists", locale, form.getName())));
        }


        final AjaxResult result = new AjaxResult();

        d.setName(form.getName());
        d.setDescription(form.getDescription());

        if (form.getResetSecret()) {
            String initialSecret = tokenGen.generate();
            d.setSecret(hasher.hash(initialSecret));
            result
                    .addData("device", d.getName())
                    .addData("secret", initialSecret)
                    .addMessage(resolveMessage("devices.msg.updatedWithSecret", locale, d.getName(), initialSecret));
        } else {
            result.addMessage(resolveMessage("devices.msg.updated", locale, d.getName()));
        }

        deviceRepo.save(d);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('DELETE_DEVICES')")
    @DeleteMapping("/device/{uuid}")
    @ResponseBody
    public ResponseEntity<?> deleteDevice(@PathVariable("uuid") String uuid, Locale locale) {

        // FIXME: Use optional rather than null
        Device d = deviceRepo.findById(uuid).orElse(null);

        if (d == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("devices.msg.existsnot", locale, uuid)));
        }

        d.setDeleted(Timestamp.from(Instant.now()));
        deviceRepo.save(d);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("devices.msg.deleted", locale, d.getName())));
    }

    @PreAuthorize("hasAuthority('RESTORE_DEVICES')")
    @PutMapping("/device/restore/{uuid}")
    @ResponseBody
    public ResponseEntity<?> restoreDevice(@PathVariable("uuid") String uuid, Locale locale) {

        // FIXME: Use optional rather than null
        Device u = deviceRepo.findById(uuid).orElse(null);

        if (u == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("devices.msg.existsnot", locale, uuid)));
        }

        u.setDeleted(null);
        deviceRepo.save(u);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("devices.msg.restored", locale, u.getName())));
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public AjaxResult handleInvalidArgument(MethodArgumentNotValidException e, Locale locale) {
        AjaxResult result = new AjaxResult();
        BindingResult bindResult = e.getBindingResult();
        result.addError(resolveMessage("input.msg.errors", locale));
        bindResult.getGlobalErrors()
                .stream()
                .map(oe -> messages.getMessage(oe, locale))
                .forEach(result::addError);
        bindResult.getFieldErrors()
                .forEach(fe -> result.addFieldError(fe.getField(), messages.getMessage(fe, locale)));
        return result;
    }

    private String resolveMessage(String key, Locale locale, Object... args) {
        return messages.getMessage(key, args, locale);
    }

    static class DeviceForm {

        @NotNull
        private String name;
        @NotNull
        private String description;
        private Boolean resetSecret;

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

        public Boolean getResetSecret() {
            return Optional.ofNullable(resetSecret).orElse(FALSE);
        }

        public void setResetSecret(Boolean resetSecret) {
            this.resetSecret = resetSecret;
        }
    }
}