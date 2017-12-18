package com.github.cimsbioko.server.dao.impl;

import com.github.cimsbioko.server.dao.FormSubmissionDao;
import com.github.cimsbioko.server.domain.model.FormSubmission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository("formSubmissionDao")
public class FormSubmissionDaoImpl extends NamedParameterJdbcTemplate implements FormSubmissionDao {

    private static final String COLUMNS = "instanceId, as_xml, as_json, form_id, form_version, form_binding," +
            " from_device, collected, submitted, processed, processed_ok";
    private static final String BASE_QUERY = "select " + COLUMNS + " from form_submission";

    private JdbcTemplate sql;

    private RowMapper<FormSubmission> mapper = (rs, rowNum) -> {
        String instanceId = rs.getString("instanceId"),
                xml = rs.getString("as_xml"),
                json = rs.getString("as_json"),
                formId = rs.getString("form_id"),
                formVersion = rs.getString("form_version"),
                formBinding = rs.getString("form_binding"),
                deviceId = rs.getString("from_device");
        Timestamp collected = rs.getTimestamp("collected"),
                submitted = rs.getTimestamp("submitted"),
                processed = rs.getTimestamp("processed");
        Boolean processedOk = (Boolean) rs.getObject("processed_ok");
        return new FormSubmission(instanceId, xml, json, formId, formVersion, formBinding, deviceId, collected,
                submitted, processed, processedOk);
    };

    @Autowired
    public FormSubmissionDaoImpl(DataSource ds) {
        super(ds);
        sql = new JdbcTemplate(ds);
    }

    @Override
    public void save(FormSubmission fs) {
        sql.update("insert into form_submission (" + COLUMNS +
                        ") values (?,cast(? as xml),cast(? as jsonb),?,?,?,?,?,coalesce(?,current_timestamp),?,?)",
                fs.getInstanceId(), fs.getXml(), fs.getJson(), fs.getFormId(), fs.getFormVersion(), fs.getFormBinding(),
                fs.getDeviceId(), fs.getCollected(), fs.getSubmitted(), fs.getProcessed(), fs.getProcessedOk());
    }

    @Override
    public void delete(String instanceId) {
        sql.update("delete from form_submission where instanceId = ?", instanceId);
    }

    @Override
    public FormSubmission findById(String instanceId) {
        return sql.queryForObject(BASE_QUERY + " where instanceId = ?", new Object[]{instanceId}, mapper);
    }

    @Override
    public List<FormSubmission> findByForm(String formId, String formVersion) {
        return sql.query(BASE_QUERY + " where form_id = ? and form_version = ?", mapper, formId, formVersion);
    }

    @Override
    public List<FormSubmission> findUnprocessed(int batchSize) {
        return sql.query(BASE_QUERY + " where processed is null order by collected asc limit ?", mapper, batchSize);
    }

    @Override
    public List<FormSubmission> findRecent(String form, String version, String binding, String device, Integer limit) {
        return find(form, version, binding, device, null, limit, true);
    }

    @Override
    public List<FormSubmission> find(String form, String version, String binding, String device, Timestamp submittedSince, Integer limit, boolean sortDesc) {
        StringBuilder where = new StringBuilder("1=1");
        Collection<Object> args = new ArrayList<>();
        if (form != null) {
            where.append(" and form_id = ?");
            args.add(form);
        }
        if (version != null) {
            where.append(" and form_version = ?");
            args.add(version);
        }
        if (binding != null) {
            where.append(" and form_binding = ?");
            args.add(binding);
        }
        if (device != null) {
            where.append(" and from_device = ?");
            args.add(device);
        }
        if (submittedSince != null) {
            where.append(" and submitted ");
            where.append(sortDesc ? " < " : " > ");
            where.append("?");
            args.add(submittedSince);
        }
        String whereClause = where.length() > 0 ? " where " + where : "";
        String orderClause = " order by submitted " + (sortDesc ? "desc" : "") + " limit ?";
        int limitArg = limit != null && limit > 0 && limit < 100 ? limit : 100;
        args.add(limitArg);
        return sql.query(BASE_QUERY + whereClause + orderClause, mapper, args.toArray());
    }

    @Override
    public void markProcessed(FormSubmission submission, Boolean processedOk) {
        String instanceId = submission.getInstanceId();
        if (instanceId == null)
            throw new IllegalArgumentException("submission must have an instance id");
        sql.update("update form_submission set processed = now(), processed_ok = ? where instanceId = ?", processedOk, instanceId);
    }

}
