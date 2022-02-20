package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Form;
import com.github.cimsbioko.server.domain.FormId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FormRepository extends PagingAndSortingRepository<Form, FormId>, FormSearch {

    @Query("select f.id from #{#entityName} f where downloads = true or submissions = true order by f.id")
    List<FormId> findUsableFormIds();

    @Query("select f.id from #{#entityName} f where downloads = true")
    List<FormId> findDownloadableFormIds();

    @Query("select f.id from #{#entityName} f where exists (select c from Campaign c where c.uuid = :campaign and f member of c.forms)")
    List<FormId> findDownloadableFormIdsByCampaign(@Param("campaign") String campaign);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update #{#entityName} f set f.downloads = false where f.formId.id = :formId and f.formId.version <> :version")
    void exclusiveDownload(@Param("formId") String formId, @Param("version") String version);
}
