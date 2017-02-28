package com.github.cimsbioko.server.dao.impl;

import com.github.cimsbioko.server.dao.FormSubmissionDao;
import com.github.cimsbioko.server.domain.model.FormSubmission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Component
public class FormSubmissionDaoImpl extends NamedParameterJdbcTemplate implements FormSubmissionDao {

    private JdbcTemplate sql;

    private RowMapper<FormSubmission> mapper = new RowMapper<FormSubmission>() {
        @Override
        public FormSubmission mapRow(ResultSet rs, int rowNum) throws SQLException {
            String instanceId = rs.getString("instanceId"),
                    xml = rs.getString("as_xml"),
                    json = rs.getString("as_json"),
                    formId = rs.getString("form_id"),
                    formVersion = rs.getString("form_version"),
                    formBinding = rs.getString("form_binding"),
                    deviceId = rs.getString("from_device");
            Timestamp submitDate = rs.getTimestamp("submitted");
            return new FormSubmission(instanceId, xml, json, formId, formVersion, formBinding, deviceId, submitDate);
        }
    };

    @Autowired
    public FormSubmissionDaoImpl(DataSource ds) {
        super(ds);
        sql = new JdbcTemplate(ds);
    }

    @Override
    public void save(FormSubmission fs) {
        sql.update("insert into form_submission " +
                        "(instanceId, as_xml, as_json, form_id, form_version, form_binding, from_device) values (?,cast(? as xml),cast(? as jsonb),?,?,?,?)",
                fs.getInstanceId(), fs.getXml(), fs.getJson(), fs.getFormId(), fs.getFormVersion(), fs.getFormBinding(), fs.getDeviceId());
    }

    @Override
    public void delete(String instanceId) {
        sql.update("delete from form_submission where instanceId = ?", instanceId);
    }

    private static final String baseQuery = "select instanceId, as_xml, as_json, form_id, form_version, form_binding, from_device, submitted" +
            " from form_submission";

    @Override
    public FormSubmission findById(String instanceId) {
        return sql.queryForObject(baseQuery + " where instanceId = ?", new Object[]{instanceId}, mapper);
    }

    @Override
    public List<FormSubmission> findByForm(String formId, String formVersion) {
        return sql.query(baseQuery + " where form_id = ? and form_version = ?", mapper, formId, formVersion);
    }
}
