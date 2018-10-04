package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.BackupRepository;
import com.github.cimsbioko.server.domain.Backup;
import com.github.cimsbioko.server.service.BackupService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class BackupsController {

    private BackupService service;
    private BackupRepository repo;

    BackupsController(BackupService service, BackupRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @GetMapping("/backups")
    public String backups(ModelMap model, @RequestParam(name = "p", defaultValue = "0") Integer page) {
        if (!model.containsAttribute("backup")) {
            model.addAttribute("backup", new Backup());
        }
        model.addAttribute("backups", repo.findAll(new PageRequest(page, 10)));
        return "backups";
    }

    @PostMapping("/backups")
    public String createBackup(@Valid @ModelAttribute("backup") Backup backup, BindingResult bindingResult, ModelMap model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return backups(model, 0);
        } else {
            service.createBackup(backup.getName(), backup.getDescription());
            redirectAttributes.addFlashAttribute("queued", Boolean.TRUE);
            return "redirect:/backups";
        }
    }
}
