package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.domain.FormSubmission;
import com.github.cimsbioko.server.security.RunAsUser;
import com.github.cimsbioko.server.service.ErrorService;
import com.github.cimsbioko.server.service.FormProcessorService;
import com.github.cimsbioko.server.service.FormSubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ScheduledFormProcessing {

    private static final float MILLIS_PER_SECOND = 1000f;
    private static final Logger log = LoggerFactory.getLogger(FormProcessorServiceImpl.class);

    private final EntityManager entityManager;
    private final FormSubmissionService formsService;
    private final FormProcessorService formProcessorService;
    private final ErrorService errorService;

    private int batchSize;

    public ScheduledFormProcessing(EntityManager entityManager, FormSubmissionService formsService,
                                   FormProcessorService formProcessorService, ErrorService errorService) {
        this.entityManager = entityManager;
        this.formsService = formsService;
        this.formProcessorService = formProcessorService;
        this.errorService = errorService;
    }

    public int getBatchSize() {
        return batchSize;
    }

    @Value("${app.formproc.batchSize:1000}")
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @RunAsUser("system")
    @Scheduled(fixedDelay = 30000)
    @Async
    @Transactional(readOnly = true)
    public void processForms() {
        long start = System.currentTimeMillis();
        try (Stream<FormSubmission> forms = formsService.getUnprocessed(batchSize)) {
            log.info("attempting to process submissions");
            AtomicLong totalProcessed = new AtomicLong(), totalFailures = new AtomicLong();
            forms.forEachOrdered(f -> {
                if (totalProcessed.get() == 0) {
                    log.info("time to first submission {}s", (System.currentTimeMillis() - start) / MILLIS_PER_SECOND);
                }
                boolean processedOk = false;
                try {
                    formProcessorService.process(f);
                    processedOk = true;
                } catch (Exception e) {
                    log.error("failed to process submission", e);
                    errorService.logError(f, e.toString());
                    totalFailures.incrementAndGet();
                }
                formsService.markProcessed(f, processedOk);
                totalProcessed.incrementAndGet();
                entityManager.detach(f);
            });
            float duration = (System.currentTimeMillis() - start) / MILLIS_PER_SECOND;
            String finalMessage = "processing completed: processed {} forms with {} failures ({}s)";
            long processed = totalProcessed.get(), failures = totalFailures.get();
            if (failures > 0) {
                log.warn(finalMessage, processed, failures, duration);
            } else {
                log.info(finalMessage, processed, failures, duration);
            }
        }
    }
}
