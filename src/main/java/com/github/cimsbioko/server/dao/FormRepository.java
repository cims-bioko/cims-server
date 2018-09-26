package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Form;
import com.github.cimsbioko.server.domain.FormId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FormRepository extends PagingAndSortingRepository<Form, FormId> {

    @Query("select f from #{#entityName} f where downloads = true")
    List<Form> findDownloadable();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update #{#entityName} f set f.downloads = false where f.formId.id = :formId and f.formId.version <> :version")
    void exclusiveDownload(String formId, String version);
}
