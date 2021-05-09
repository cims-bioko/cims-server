package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.FormSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FormSubmissionSearch {
    Page<FormSubmission> findBySearch(String query, Pageable page);
}
