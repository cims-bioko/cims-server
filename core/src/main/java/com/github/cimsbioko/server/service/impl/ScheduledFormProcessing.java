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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
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
    @Scheduled(fixedDelayString = "${app.formproc.interval:PT1M}")
    @Async
    @Transactional(readOnly = true)
    public void processForms() {
        long start = System.currentTimeMillis();
        try (Stream<FormSubmission> forms = formsService.getUnprocessed(batchSize)) {
            log.info("attempting to process submissions");
            AtomicLong totalProcessed = new AtomicLong(), totalFailures = new AtomicLong();
            forms.forEachOrdered(form -> {
                if (totalProcessed.get() == 0) {
                    log.info("time to first submission {}s", (System.currentTimeMillis() - start) / MILLIS_PER_SECOND);
                }
                boolean processedOk = false;
                try {
                    formProcessorService.process(form);
                    processedOk = true;
                } catch (Exception e) {
                    log.error("failed to process submission {}: {}", form.getInstanceId(), e.getMessage());
                    errorService.logError(form, causes(e).stream().map(Throwable::getMessage).collect(Collectors.joining("\n")));
                    totalFailures.incrementAndGet();
                }
                formsService.markProcessed(form, processedOk);
                totalProcessed.incrementAndGet();
                entityManager.detach(form);
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

    private List<Throwable> causes(Throwable throwable) {
        List<Throwable> causes = new ArrayList<>();
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            causes.add(t);
        }
        return causes;
    }
}
