package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.BackupRepository;
import com.github.cimsbioko.server.domain.Backup;
import com.github.cimsbioko.server.service.BackupService;
import com.github.cimsbioko.server.service.impl.BackupCreatedEvent;
import com.github.cimsbioko.server.service.impl.BackupFailedEvent;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Controller
public class BackupsController {

    private BackupService service;
    private BackupRepository repo;
    private SimpMessagingTemplate simpMsgTemplate;
    private MessageSource messages;

    BackupsController(BackupService service, BackupRepository repo, SimpMessagingTemplate simpTemplate, MessageSource messages) {
        this.service = service;
        this.repo = repo;
        this.simpMsgTemplate = simpTemplate;
        this.messages = messages;
    }

    @EventListener
    @Transactional(propagation = REQUIRES_NEW)
    public void status(BackupCreatedEvent event) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("backup", event.getBackupName());
        msg.put("status", "created");
        simpMsgTemplate.convertAndSend("/topic/backups", msg);
    }

    @EventListener
    @Transactional(propagation = REQUIRES_NEW)
    public void status(BackupFailedEvent event) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("backup", event.getBackupName());
        msg.put("status", "error");
        msg.put("errorMessage", event.getCause().getCause().getMessage());
        simpMsgTemplate.convertAndSend("/topic/backups", msg);
    }

    @PreAuthorize("hasAuthority('VIEW_BACKUPS')")
    @GetMapping("/backups")
    @ResponseBody
    public Page<Backup> backups(@RequestParam(name = "p", defaultValue = "0") Integer page) {
        return repo.findAll(PageRequest.of(page, 10));
    }

    @PreAuthorize("hasAuthority('CREATE_BACKUPS')")
    @PostMapping("/backups")
    @ResponseBody
    public ResponseEntity<AjaxResult> createBackup(@Valid @RequestBody Backup backup, Locale locale) {
        if (repo.existsById(backup.getName())) {
            return ResponseEntity.badRequest().body(new AjaxResult()
                    .addFieldError("name", resolveMessage("backups.msg.exists", locale, backup.getName())));
        }
        service.createBackup(backup.getName(), backup.getDescription());
        return ResponseEntity.ok(new AjaxResult()
                .addMessage(resolveMessage("backups.msg.queued", locale)));
    }

    @PreAuthorize("hasAuthority('VIEW_BACKUPS')")
    @GetMapping("/backup/{name}")
    @ResponseBody
    public Backup loadBackup(@PathVariable("name") String name) {
        // FIXME: Use optional rather than null
        return repo.findById(name).orElse(null);
    }

    @PreAuthorize("hasAuthority('EDIT_BACKUPS')")
    @PutMapping("/backup/{name}")
    @ResponseBody
    public ResponseEntity<?> updateBackup(@PathVariable("name") String name, @Valid @RequestBody Backup backup, Locale locale) {
        if (!name.equals(backup.getName()) && repo.existsById(backup.getName())) {
            return ResponseEntity
                    .badRequest()
                    .body(new AjaxResult()
                            .addFieldError("name", resolveMessage("backups.msg.exists", locale, backup.getName())));
        }
        service.updateBackup(name, backup);
        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("backups.msg.updated", locale, name)));
    }

    @PreAuthorize("hasAuthority('DELETE_BACKUPS')")
    @DeleteMapping("/backup/{name}")
    @ResponseBody
    public ResponseEntity<?> deleteBackup(@PathVariable("name") String name, Locale locale) {
        service.deleteBackup(name);
        return ResponseEntity
                .ok(new AjaxResult()
                        .addMessage(
                                resolveMessage("backups.msg.deleted", locale, name)));
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
}
