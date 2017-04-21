package com.github.cimsbioko.server.controller.service.impl;

import com.github.cimsbioko.server.controller.service.FormSubmissionService;
import com.github.cimsbioko.server.dao.FormSubmissionDao;
import com.github.cimsbioko.server.domain.model.FormSubmission;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormSubmissionServiceImpl implements FormSubmissionService {

    FormSubmissionDao formDao;

    public FormSubmissionServiceImpl(FormSubmissionDao formDao) {
        this.formDao = formDao;
    }

    @Override
    public List<FormSubmission> getUnprocessed(int batchSize) {
        return formDao.findUnprocessed(batchSize);
    }

    @Override
    public void markProcessed(FormSubmission submission, Boolean processedOk) {
        formDao.markProcessed(submission, processedOk);
    }

}
