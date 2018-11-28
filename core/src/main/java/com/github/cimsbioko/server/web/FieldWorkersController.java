package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.FieldWorkerRepository;
import com.github.cimsbioko.server.domain.FieldWorker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Calendar;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
public class FieldWorkersController {

    private FieldWorkerRepository repo;
    private MessageSource messages;
    private PasswordEncoder encoder;

    public FieldWorkersController(FieldWorkerRepository repo, @Qualifier("fieldworkerPasswordEncoder") PasswordEncoder encoder, MessageSource messages) {
        this.repo = repo;
        this.encoder = encoder;
        this.messages = messages;
    }

    @PreAuthorize("hasAuthority('VIEW_FIELDWORKERS')")
    @GetMapping("/fieldworkers")
    @ResponseBody
    public Page<FieldWorker> fieldworkers(@RequestParam(name = "p", defaultValue = "0") Integer page,
                                          @RequestParam(name = "q", defaultValue = "") String query) {
        PageRequest pageObj = PageRequest.of(page, 10);
        return query.isEmpty() ? repo.findByDeletedIsNull(pageObj) : repo.findBySearch(query, pageObj);
    }

    @PreAuthorize("hasAuthority('CREATE_FIELDWORKERS')")
    @PostMapping("/fieldworkers")
    @ResponseBody
    public ResponseEntity<AjaxResult> createFieldworker(@Valid @RequestBody FieldWorkerForm form, Locale locale) {
        if (repo.idExists(form.getExtId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("extId", resolveMessage("fieldworkers.msg.idexists", locale)));
        }
        FieldWorker fw = new FieldWorker();
        fw.setExtId(form.getExtId());
        fw.setFirstName(form.getFirstName());
        fw.setLastName(form.getLastName());
        fw.setPasswordHash(encoder.encode(form.getPassword()));
        fw = repo.save(fw);
        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(resolveMessage("fieldworkers.msg.created", locale, fw.getExtId())));
    }

    @PreAuthorize("hasAuthority('VIEW_FIELDWORKERS')")
    @GetMapping("/fieldworker/{uuid}")
    @ResponseBody
    public FieldWorkerForm loadFieldworker(@PathVariable("uuid") String uuid) {
        // FIXME: Use optional rather than null
        FieldWorker f = repo.findById(uuid).orElse(null);
        FieldWorkerForm result = new FieldWorkerForm();
        result.setExtId(f.getExtId());
        result.setFirstName(f.getFirstName());
        result.setLastName(f.getLastName());
        return result;
    }

    @PreAuthorize("hasAuthority('EDIT_FIELDWORKERS')")
    @PutMapping("/fieldworker/{uuid}")
    @ResponseBody
    public ResponseEntity<?> updateFieldworker(@PathVariable("uuid") String uuid, @Valid @RequestBody FieldWorkerForm form, Locale locale) {

        // FIXME: Use optional rather than null
        FieldWorker f = repo.findById(uuid).orElse(null);

        if (f == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("fieldworkers.msg.existsnot", locale, form.getExtId())));
        }

        if (!f.getExtId().equals(form.getExtId()) && repo.idExists(form.getExtId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("extId", resolveMessage("fieldworkers.msg.idexists", locale)));
        }

        f.setExtId(form.getExtId());
        f.setFirstName(form.getFirstName());
        f.setLastName(form.getLastName());
        if (form.getPassword() != null) {
            f.setPasswordHash(encoder.encode(form.getPassword()));
        }
        repo.save(f);
        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("fieldworkers.msg.updated", locale, f.getExtId())));
    }

    @PreAuthorize("hasAuthority('DELETE_FIELDWORKERS')")
    @DeleteMapping("/fieldworker/{uuid}")
    @ResponseBody
    public ResponseEntity<?> deleteFieldworker(@PathVariable("uuid") String uuid, Locale locale) {

        // FIXME: Use optional rather than null
        FieldWorker f = repo.findById(uuid).orElse(null);

        if (f == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("fieldworkers.msg.existsnot", locale, uuid)));
        }

        f.setDeleted(Calendar.getInstance());
        repo.save(f);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("fieldworkers.msg.deleted", locale, uuid)));
    }

    @PreAuthorize("hasAuthority('RESTORE_FIELDWORKERS')")
    @PutMapping("/fieldworker/restore/{uuid}")
    @ResponseBody
    public ResponseEntity<?> restoreFieldworker(@PathVariable("uuid") String uuid, Locale locale) {

        // FIXME: Use optional rather than null
        FieldWorker f = repo.findById(uuid).orElse(null);

        if (f == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("fieldworkers.msg.existsnot", locale, uuid)));
        }

        f.setDeleted(null);
        repo.save(f);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("fieldworkers.msg.restored", locale, uuid)));
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


    static class FieldWorkerForm {

        @NotNull
        @Pattern(regexp = "FW[A-Z][A-Z][1-9][0-9]*")
        private String extId;
        @NotNull
        @Size(min = 1)
        private String firstName;
        @NotNull
        @Size(min = 1)
        private String lastName;
        @Size(min = 8, max = 255)
        private String password;

        public String getExtId() {
            return extId;
        }

        public void setExtId(String extId) {
            this.extId = extId;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
