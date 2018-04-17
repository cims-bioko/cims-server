package com.github.cimsbioko.server.dao.impl;

import com.github.cimsbioko.server.dao.FormSubmissionDao;
import com.github.cimsbioko.server.domain.FormSubmission;
import org.apache.lucene.search.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository("formSubmissionDao")
public class FormSubmissionDaoImpl implements FormSubmissionDao {

    @Autowired
    private SessionFactory sf;

    @Override
    public void save(FormSubmission fs) {
        getCurrentSession().persist(fs);
    }

    private Session getCurrentSession() {
        return sf.getCurrentSession();
    }

    @Override
    public void delete(String instanceId) {
        getCurrentSession()
                .createQuery("delete from FormSubmission fs where instanceId = :id")
                .setParameter("id", instanceId)
                .executeUpdate();
    }

    @Override
    public FormSubmission findById(String instanceId) {
        return getCurrentSession().get(FormSubmission.class, instanceId);
    }

    @Override
    public List<FormSubmission> findUnprocessed(int batchSize) {
        return getCurrentSession()
                .createQuery("select fs from FormSubmission fs where processed is null order by collected asc")
                .setMaxResults(batchSize)
                .getResultList();
    }

    @Override
    public List<FormSubmission> findRecent(String form, String version, String binding, String device, Integer limit) {
        return find(form, version, binding, device, null, limit, true);
    }

    @Override
    public List<FormSubmission> find(String form, String version, String binding, String device, Timestamp submittedSince, Integer limit, boolean sortDesc) {

        int limitArg = limit != null && limit > 0 && limit < 100 ? limit : 100;

        CriteriaBuilder cb = sf.getCriteriaBuilder();
        CriteriaQuery<FormSubmission> cq = cb.createQuery(FormSubmission.class);
        Root<FormSubmission> r = cq.from(FormSubmission.class);
        List<Predicate> ps = new ArrayList<>(5);
        Map<String, Object> params = new LinkedHashMap<>();
        if (form != null) {
            ps.add(cb.equal(r.get("formId"), cb.parameter(String.class, "form")));
            params.put("form", form);
        }
        if (version != null) {
            ps.add(cb.equal(r.get("formVersion"), cb.parameter(String.class, "version")));
            params.put("version", version);
        }
        if (binding != null) {
            ps.add(cb.equal(r.get("formBinding"), cb.parameter(String.class, "binding")));
            params.put("binding", binding);
        }
        if (device != null) {
            ps.add(cb.equal(r.get("deviceId"), cb.parameter(String.class, "device")));
            params.put("device", device);
        }
        if (submittedSince != null) {
            if (sortDesc) {
                ps.add(cb.lessThan(r.get("submitted"), cb.parameter(Timestamp.class, "submitted")));
            } else {
                ps.add(cb.greaterThan(r.get("submitted"), cb.parameter(Timestamp.class, "submitted")));
            }
            params.put("submitted", submittedSince);
        }

        cq.where(ps.toArray(new Predicate[]{}))
                .orderBy(sortDesc ? cb.desc(r.get("submitted")) : cb.asc(r.get("submitted")));

        TypedQuery<FormSubmission> tq = getCurrentSession().createQuery(cq).setMaxResults(limitArg);

        for (Map.Entry<String, Object> e : params.entrySet()) {
            tq.setParameter(e.getKey(), e.getValue());
        }

        return tq.getResultList();
    }

    @Override
    public void markProcessed(FormSubmission submission, Boolean processedOk) {
        String instanceId = submission.getInstanceId();
        if (instanceId == null)
            throw new IllegalArgumentException("submission must have an instance id");
        getCurrentSession().createQuery("update FormSubmission set processed = CURRENT_TIMESTAMP, processed_ok = :processedOk where instanceId = :id")
                .setParameter("processedOk", processedOk)
                .setParameter("id", instanceId)
                .executeUpdate();
    }

    private FullTextSession getFullTextSession() {
        return Search.getFullTextSession(getCurrentSession());
    }

    private SearchFactory getSearchFactory() {
        return getFullTextSession().getSearchFactory();
    }

    private QueryBuilder getSearchQueryBuilder() {
        return getSearchFactory().buildQueryBuilder().forEntity(FormSubmission.class).get();
    }

    private String[] getSearchFields() {
        List<String> fieldNames = new ArrayList<>();
        for (Field field : FormSubmission.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
                fieldNames.add(field.getName());
            }
        }
        return fieldNames.toArray(new String[]{});
    }

    private Query getSearchQuery(String query) {
        return getSearchQueryBuilder()
                .keyword()
                .fuzzy()
                .onFields(getSearchFields())
                .matching(query)
                .createQuery();
    }

    private FullTextQuery getEntitySearchQuery(String query) {
        return getFullTextSession()
                .createFullTextQuery(getSearchQuery(query), FormSubmission.class);
    }


    public List<FormSubmission> findBySearch(String query, int first, int max) {
        return getEntitySearchQuery(query)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList();
    }
}
