package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Form;
import com.github.cimsbioko.server.domain.FormId;

import java.util.List;

public interface FormDao {
    void save(Form form);
    void delete(FormId id);
    Form findById(FormId id);
    void exclusiveDownload(Form form);
    List<Form> findDownloadable();
}
