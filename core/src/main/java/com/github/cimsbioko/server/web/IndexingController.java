package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.service.IndexingService;
import com.github.cimsbioko.server.service.impl.indexing.IndexingEvent;
import com.github.cimsbioko.server.service.impl.sync.ExportFinishedEvent;
import com.github.cimsbioko.server.service.impl.sync.ExportStatusEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Controller
public class IndexingController {

    private final SimpMessagingTemplate simpMsgTemplate;
    private final IndexingService service;

    public IndexingController(SimpMessagingTemplate simpMsgTemplate, IndexingService service) {
        this.simpMsgTemplate = simpMsgTemplate;
        this.service = service;
    }

    @PreAuthorize("hasAuthority('REBUILD_INDEX')")
    @GetMapping("/indexing")
    @ResponseBody
    public Map<String, Object> status() {
        return getStatusMap();
    }

    @PreAuthorize("hasAuthority('REBUILD_INDEX')")
    @PostMapping("/indexing")
    @ResponseBody
    public void request() {
        service.requestRebuild();
    }

    @EventListener
    @Order
    @Transactional(propagation = REQUIRES_NEW)
    public void publishSyncStatus(IndexingEvent event) {
        simpMsgTemplate.convertAndSend("/topic/indexing", getStatusMap());
    }

    @NotNull
    private Map<String, Object> getStatusMap() {
        IndexingService.Status status = service.getStatus();
        Map<String, Object> data = new HashMap<>();
        data.put("running", status.isRunning());
        data.put("processed", status.getProcessed());
        data.put("todo", status.getToDo());
        data.put("elapsed", status.getElapsed().toMinutes());
        data.put("dps", status.getDocsPerSecond());
        data.put("pctComplete", status.getPercentComplete());
        data.put("completedAt", status.getCompletedAt());
        return data;
    }
}
