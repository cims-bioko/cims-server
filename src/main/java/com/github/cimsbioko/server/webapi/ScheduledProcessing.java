package com.github.cimsbioko.server.webapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledProcessing {

    private static final Logger log = LoggerFactory.getLogger(ScheduledProcessing.class);

    private FormProcessor processor;

    public ScheduledProcessing(FormProcessor processor) {
        this.processor = processor;
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void processForms() {
        long start = System.currentTimeMillis();
        int formCount = processor.processForms();
        long end = System.currentTimeMillis();
        log.info("processed {} forms in {}ms", formCount, end - start);
    }
}
