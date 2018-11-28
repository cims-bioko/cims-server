package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.PrivilegeRepository;
import com.github.cimsbioko.server.dao.RoleRepository;
import com.github.cimsbioko.server.domain.Privilege;
import com.github.cimsbioko.server.domain.Role;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
public class RolesController {

    private RoleRepository roleRepo;
    private PrivilegeRepository privRepo;
    private MessageSource messages;

    public RolesController(RoleRepository roleRepo, PrivilegeRepository privRepo, MessageSource messages) {
        this.roleRepo = roleRepo;
        this.privRepo = privRepo;
        this.messages = messages;
    }

    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    @GetMapping("/roles")
    @ResponseBody
    public Page<Role> roles(@RequestParam(name = "p", defaultValue = "0") Integer page) {
        return roleRepo.findByDeletedIsNull(PageRequest.of(page, 10));
    }

    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    @GetMapping("/privileges")
    @ResponseBody
    public Iterable<Privilege> privileges() {
        return privRepo.findAll(Sort.by("privilege"));
    }

    @PreAuthorize("hasAuthority('CREATE_ROLES')")
    @PostMapping("/roles")
    @ResponseBody
    public ResponseEntity<AjaxResult> createRole(@Valid @RequestBody RoleForm form, Locale locale) {

        if (roleRepo.findByNameAndDeletedIsNull(form.getName()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("name",
                                    resolveMessage("roles.msg.exists", locale, form.getName())));
        }

        Role r = new Role();
        r.setName(form.getName());
        r.setDescription(form.getDescription());
        r.setPrivileges(privRepo.findByUuidIn(Arrays.stream(form.getPrivileges()).collect(Collectors.toSet())));
        r = roleRepo.save(r);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("roles.msg.created", locale, r.getName())));
    }

    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    @GetMapping("/role/{uuid}")
    @ResponseBody
    public RoleForm loadRole(@PathVariable("uuid") String uuid) {
        // FIXME: Use optional rather than null
        Role r = roleRepo.findById(uuid).orElse(null);
        RoleForm result = new RoleForm();
        result.setName(r.getName());
        result.setDescription(r.getDescription());
        result.setPrivileges(r.getPrivileges().stream().map(Privilege::getUuid).toArray(String[]::new));
        return result;
    }

    @PreAuthorize("hasAuthority('EDIT_ROLES')")
    @PutMapping("/role/{uuid}")
    @ResponseBody
    public ResponseEntity<?> updateRole(@PathVariable("uuid") String uuid, @Valid @RequestBody RoleForm form, Locale locale) {

        // FIXME: Use optional rather than null
        Role r = roleRepo.findById(uuid).orElse(null);

        if (r == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("roles.msg.existsnot", locale, form.getName())));
        }

        r.setName(form.getName());
        r.setDescription(form.getDescription());
        r.setPrivileges(privRepo.findByUuidIn(Arrays.stream(form.getPrivileges()).collect(Collectors.toSet())));
        roleRepo.save(r);
        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("roles.msg.updated", locale, r.getName())));
    }

    @PreAuthorize("hasAuthority('DELETE_ROLES')")
    @DeleteMapping("/role/{uuid}")
    @ResponseBody
    public ResponseEntity<?> deleteRole(@PathVariable("uuid") String uuid, Locale locale) {

        // FIXME: Use optional rather than null
        Role r = roleRepo.findById(uuid).orElse(null);

        if (r == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("roles.msg.existsnot", locale, uuid)));
        }

        r.setDeleted(Calendar.getInstance());
        roleRepo.save(r);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("roles.msg.deleted", locale, uuid)));
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


    static class RoleForm {

        @NotNull
        @Size(min = 1)
        private String name;

        private String description;

        @NotNull
        private String[] privileges;

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

        @NotNull
        public String[] getPrivileges() {
            return privileges;
        }

        public void setPrivileges(@NotNull String[] privileges) {
            this.privileges = privileges;
        }
    }
}
