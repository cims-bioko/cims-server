package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.RoleRepository;
import com.github.cimsbioko.server.dao.UserRepository;
import com.github.cimsbioko.server.domain.Role;
import com.github.cimsbioko.server.domain.User;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
public class UsersController {

    private UserRepository userRepo;
    private RoleRepository roleRepo;
    private PasswordEncoder encoder;
    private MessageSource messages;

    public UsersController(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder, MessageSource messages) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        this.messages = messages;
    }

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @GetMapping("/users")
    public ModelAndView users(@RequestParam(name = "p", defaultValue = "0") Integer page) {
        ModelAndView modelAndView = new ModelAndView("users");
        modelAndView.addObject("users", userRepo.findByDeletedIsNull(PageRequest.of(page, 10)));
        modelAndView.addObject("roles", roleRepo.findByDeletedIsNull());
        return modelAndView;
    }

    @PreAuthorize("hasAuthority('CREATE_USERS')")
    @PostMapping("/users")
    @ResponseBody
    public ResponseEntity<AjaxResult> createUser(@Valid @RequestBody UserForm userForm, Locale locale) {

        if (userRepo.findByUsernameAndDeletedIsNull(userForm.getUsername()) != null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("username",
                                    resolveMessage("users.msg.exists", locale, userForm.getUsername())));
        }

        User u = new User();
        u.setFirstName(userForm.getFirstName());
        u.setLastName(userForm.getLastName());
        u.setDescription(userForm.getDescription());
        u.setUsername(userForm.getUsername());
        u.setPassword(encoder.encode(userForm.getPassword()));
        u.setRoles(roleRepo.findByUuidIn(Arrays.stream(userForm.getRoles()).collect(Collectors.toSet())));
        u = userRepo.save(u);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("users.msg.created", locale, u.getUsername())));
    }

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @GetMapping("/user/{uuid}")
    @ResponseBody
    public UserForm loadUser(@PathVariable("uuid") String uuid) {
        // FIXME: Use optional rather than null
        User u = userRepo.findById(uuid).orElse(null);
        UserForm result = new UserForm();
        result.setFirstName(u.getFirstName());
        result.setLastName(u.getLastName());
        result.setDescription(u.getDescription());
        result.setUsername(u.getUsername());
        result.setRoles(u.getRoles().stream().map(Role::getUuid).toArray(String[]::new));
        return result;
    }

    @PreAuthorize("hasAuthority('EDIT_USERS')")
    @PutMapping("/user/{uuid}")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable("uuid") String uuid, @Valid @RequestBody UserForm form, Locale locale) {

        // FIXME: Use optional rather than null
        User u = userRepo.findById(uuid).orElse(null);

        if (u == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("users.msg.existsnot", locale, form.getUsername())));
        }

        u.setFirstName(form.getFirstName());
        u.setLastName(form.getLastName());
        u.setDescription(form.getDescription());
        u.setUsername(form.getUsername());
        if (form.getPassword() != null) {
            u.setPassword(encoder.encode(form.getPassword()));
        }
        u.setRoles(roleRepo.findByUuidIn(Arrays.stream(form.getRoles()).collect(Collectors.toSet())));
        userRepo.save(u);
        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("users.msg.updated", locale, u.getUsername())));
    }

    @PreAuthorize("hasAuthority('DELETE_USERS')")
    @DeleteMapping("/user/{uuid}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable("uuid") String uuid, Locale locale) {

        // FIXME: Use optional rather than null
        User u = userRepo.findById(uuid).orElse(null);

        if (u == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addError(resolveMessage("input.msg.errors", locale))
                            .addFieldError("uuid",
                                    resolveMessage("users.msg.existsnot", locale, uuid)));
        }

        u.setDeleted(Calendar.getInstance());
        userRepo.save(u);

        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("users.msg.deleted", locale, uuid)));
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

    static class UserForm {

        private String firstName;
        private String lastName;
        private String description;
        @NotNull
        @Size(max = 255)
        private String username;
        @Size(min = 8, max = 255)
        private String password;
        @NotNull
        private String[] roles;

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @NotNull
        public String getUsername() {
            return username;
        }

        public void setUsername(@NotNull String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @NotNull
        public String[] getRoles() {
            return roles;
        }

        public void setRoles(@NotNull String[] roles) {
            this.roles = roles;
        }
    }
}
