package com.github.cimsbioko.server.formproc;

import com.github.cimsbioko.server.security.RunAsUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class ScheduledProcessing {

    private static final Logger log = LoggerFactory.getLogger(ScheduledProcessing.class);

    private FormProcessor processor;

    public ScheduledProcessing(FormProcessor processor) {
        this.processor = processor;
    }

    @Scheduled(fixedDelay = 30000)
    @RunAsUser("system")
    public void processForms() {
        long start = System.currentTimeMillis();
        int formCount = processor.processForms();
        long end = System.currentTimeMillis();
        if (formCount > 0) {
            log.info("processed {} forms in {}ms", formCount, end - start);
        }
    }
}
