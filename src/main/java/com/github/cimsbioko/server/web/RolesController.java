package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.PrivilegeRepository;
import com.github.cimsbioko.server.dao.RoleRepository;
import com.github.cimsbioko.server.domain.Role;
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
import javax.validation.constraints.Size;
import java.util.Arrays;
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

    @GetMapping("/roles")
    public ModelAndView roles(@RequestParam(name = "p", defaultValue = "0") Integer page) {
        ModelAndView modelAndView = new ModelAndView("roles");
        modelAndView.addObject("roles", roleRepo.findByDeletedIsNull(new PageRequest(page, 10)));
        modelAndView.addObject("privileges", privRepo.findAll());
        return modelAndView;
    }

    @PostMapping("/roles")
    @ResponseBody
    public ResponseEntity<AjaxResult> createRole(@Valid @RequestBody RoleForm form, Locale locale) {

//        if (userRepo.findByUsernameAndDeletedIsNull(userForm.getUsername()) != null) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(new AjaxResult()
//                            .addError(resolveMessage("input.msg.errors", locale))
//                            .addFieldError("username",
//                                    resolveMessage("users.msg.exists", locale, userForm.getUsername())));
//        }

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
