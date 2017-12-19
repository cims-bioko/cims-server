package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.controller.exception.ExistingSubmissionException;
import com.github.cimsbioko.server.service.FormSubmissionService;
import com.github.cimsbioko.server.dao.FormSubmissionDao;
import com.github.cimsbioko.server.domain.model.FormSubmission;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormSubmissionServiceImpl implements FormSubmissionService {

    FormSubmissionDao formDao;

    public FormSubmissionServiceImpl(FormSubmissionDao formDao) {
        this.formDao = formDao;
    }

    @Override
    public FormSubmission recordSubmission(FormSubmission submission) throws ExistingSubmissionException {
        String instanceId = submission.getInstanceId();
        try {
            FormSubmission existing = formDao.findById(instanceId);
            throw new ExistingSubmissionException("submission with id " + instanceId + " exists", existing);
        } catch (EmptyResultDataAccessException e) {
            formDao.save(submission);
            return formDao.findById(instanceId);
        }
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
