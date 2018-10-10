package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.service.SyncService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SyncController {

    private SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/sync")
    public ModelAndView sync() {
        ModelAndView result = new ModelAndView("sync");
        result.addObject("nextRun", syncService.getMinutesToNextRun().orElse(null));
        result.addObject("task", syncService.getTask());
        return result;
    }
}
