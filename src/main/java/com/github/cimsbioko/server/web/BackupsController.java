package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.BackupRepository;
import com.github.cimsbioko.server.domain.Backup;
import com.github.cimsbioko.server.service.BackupService;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Controller
public class BackupsController {

    private BackupService service;
    private BackupRepository repo;
    private MessageSource messages;

    BackupsController(BackupService service, BackupRepository repo, MessageSource messages) {
        this.service = service;
        this.repo = repo;
        this.messages = messages;
    }

    @GetMapping("/backups")
    public ModelAndView backups(@RequestParam(name = "p", defaultValue = "0") Integer page) {
        return new ModelAndView("backups", "backups", repo.findAll(new PageRequest(page, 10)));
    }

    @PostMapping("/backups")
    @ResponseBody
    public AjaxResult createBackup(@Valid @RequestBody Backup backup, Locale locale) {
        service.createBackup(backup.getName(), backup.getDescription());
        return new AjaxResult()
                .addMessage(resolveMessage("backups.msg.queued", locale));
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
