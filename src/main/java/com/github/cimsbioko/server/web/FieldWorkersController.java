package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.FieldWorkerRepository;
import com.github.cimsbioko.server.domain.FieldWorker;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
public class FieldWorkersController {

    private FieldWorkerRepository repo;
    private MessageSource messages;

    public FieldWorkersController(FieldWorkerRepository repo, MessageSource messages) {
        this.repo = repo;
        this.messages = messages;
    }

    @GetMapping("/fieldworkers")
    public ModelAndView fieldworkers(@RequestParam(name = "p", defaultValue = "0") Integer page) {
        return new ModelAndView("fieldworkers",
                "fieldworkers", repo.findByDeletedIsNull(new PageRequest(page, 10)));
    }

    @PostMapping("/fieldworkers")
    @ResponseBody
    public ResponseEntity<AjaxResult> createFieldworker(@Valid @RequestBody FieldWorkerForm form, Locale locale) {
        if (repo.idExists(form.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("id", resolveMessage("fieldworkers.msg.idexists", locale)));
        }
        FieldWorker fw = new FieldWorker();
        fw.setExtId(form.getId());
        fw.setFirstName(form.getFirstName());
        fw.setLastName(form.getLastName());
        fw.setPassword(form.getPassword());
        fw = repo.save(fw);
        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(resolveMessage("fieldworkers.msg.created", locale, fw.getExtId())));
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
        private String id;
        @NotNull
        @Size(min = 1)
        private String firstName;
        @NotNull
        @Size(min = 1)
        private String lastName;
        @NotNull
        @Size(min = 8, max = 255)
        private String password;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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