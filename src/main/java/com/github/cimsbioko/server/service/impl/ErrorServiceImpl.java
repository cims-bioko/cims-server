package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.ErrorRepository;
import com.github.cimsbioko.server.domain.Error;
import com.github.cimsbioko.server.domain.FormSubmission;
import com.github.cimsbioko.server.service.ErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ErrorServiceImpl implements ErrorService {

    @Autowired
    private ErrorRepository errorDao;

    @Override
    @Transactional
    public void logError(FormSubmission submission, String message) {
        Error e = new Error();
        e.setSubmission(submission);
        e.setMessage(message);
        errorDao.save(e);
    }
}
