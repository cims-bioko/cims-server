package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Form;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FormSearch {
    Page<Form> findBySearch(String query, Pageable page);
}
