package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.FormSubmissionDao;
import com.github.cimsbioko.server.domain.FormSubmission;
import com.github.cimsbioko.server.exception.ExistingSubmissionException;
import com.github.cimsbioko.server.service.FormSubmissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FormSubmissionServiceImpl implements FormSubmissionService {

    private FormSubmissionDao submissionDao;

    public FormSubmissionServiceImpl(FormSubmissionDao submissionDao) {
        this.submissionDao = submissionDao;
    }

    @Override
    @Transactional
    public FormSubmission recordSubmission(FormSubmission submission) throws ExistingSubmissionException {
        String instanceId = submission.getInstanceId();
        FormSubmission existing = submissionDao.findById(instanceId);
        if (existing != null) {
            throw new ExistingSubmissionException("submission with id " + instanceId + " exists", existing);
        } else {
            submissionDao.save(submission);
            return submissionDao.findById(instanceId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormSubmission> getUnprocessed(int batchSize) {
        return submissionDao.findUnprocessed(batchSize);
    }

    @Override
    @Transactional
    public void markProcessed(FormSubmission submission, Boolean processedOk) {
        submissionDao.markProcessed(submission, processedOk);
    }

}
