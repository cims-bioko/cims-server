package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Form;
import com.github.cimsbioko.server.domain.FormId;

public interface FormDao {
    void save(Form form);
    void delete(FormId id);
    Form findById(FormId id);
}
