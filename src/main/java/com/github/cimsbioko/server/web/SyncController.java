package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.service.SyncService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SyncController {

    private SyncService service;

    public SyncController(SyncService service) {
        this.service = service;
    }

    @GetMapping("/sync")
    public ModelAndView sync() {
        ModelAndView result = new ModelAndView("sync");
        result.addObject("nextRun", service.getMinutesToNextRun().orElse(null));
        result.addObject("task", service.getTask());
        return result;
    }
}
