package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.domain.FormSubmission;
import com.github.cimsbioko.server.security.RunAsUser;
import com.github.cimsbioko.server.service.ErrorService;
import com.github.cimsbioko.server.service.FormProcessorService;
import com.github.cimsbioko.server.service.FormSubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public class ScheduledFormProcessing {

    private static final Logger log = LoggerFactory.getLogger(FormProcessorServiceImpl.class);

    private final FormSubmissionService formsService;
    private final FormProcessorService formProcessorService;
    private final ErrorService errorService;

    public ScheduledFormProcessing(FormSubmissionService formsService, FormProcessorService formProcessorService,
                                   ErrorService errorService) {
        this.formsService = formsService;
        this.formProcessorService = formProcessorService;
        this.errorService = errorService;
    }

    @RunAsUser("system")
    @Scheduled(fixedDelay = 30000)
    @Async
    public void processForms() {
        List<FormSubmission> forms = formsService.getUnprocessed(300);
        if (forms.size() > 0) {
            log.info("processing {} submissions", forms.size());
            int failures = 0;
            for (FormSubmission f : forms) {
                boolean processedOk = false;
                try {
                    formProcessorService.process(f);
                    processedOk = true;
                } catch (Exception e) {
                    log.error("failed to process submission", e);
                    errorService.logError(f, e.toString());
                    failures++;
                }
                formsService.markProcessed(f, processedOk);
            }
            if (failures > 0) {
                log.warn("processing completed with {} failures", failures);
            }
        }
    }

}
