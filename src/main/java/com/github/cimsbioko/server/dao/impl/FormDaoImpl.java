package com.github.cimsbioko.server.dao.impl;

import com.github.cimsbioko.server.dao.FormDao;
import com.github.cimsbioko.server.domain.Form;
import com.github.cimsbioko.server.domain.FormId;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("formDao")
public class FormDaoImpl implements FormDao {

    @Autowired
    private SessionFactory sf;

    private Session getCurrentSession() {
        return sf.getCurrentSession();
    }

    @Override
    @Transactional
    public void save(Form form) {
        getCurrentSession().persist(form);
    }

    @Override
    @Transactional
    public void delete(FormId id) {
        getCurrentSession()
                .createQuery("delete from Form f where formId = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public Form findById(FormId id) {
        return getCurrentSession().find(Form.class, id);
    }

    @Override
    @Transactional
    public void exclusiveDownload(Form form) {
        getCurrentSession()
                .createQuery("update Form f set f.downloads = false where formId.id = :id and formId.version <> :version")
                .setParameter("id", form.getFormId().getId())
                .setParameter("version", form.getFormId().getVersion())
                .executeUpdate();
    }
}
