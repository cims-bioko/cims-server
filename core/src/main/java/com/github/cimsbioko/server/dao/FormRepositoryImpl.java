package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Form;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

public class FormRepositoryImpl implements FormSearch {

    private final EntityManager em;

    public FormRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    private FullTextEntityManager getFullTextEntityManager() {
        return Search.getFullTextEntityManager(em);
    }

    private QueryBuilder getQueryBuilder() {
        return getFullTextEntityManager()
                .getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Form.class)
                .get();
    }

    private Query getSearchQuery(String query) {
        return getQueryBuilder()
                .keyword()
                .wildcard()
                .onFields("formId.id", "formId.version")
                .matching("*" + query + "*")
                .createQuery();
    }

    private FullTextQuery getFullTextQuery(String query) {
        return getFullTextEntityManager().createFullTextQuery(getSearchQuery(query), Form.class);
    }

    @SuppressWarnings("unchecked")
    private PageImpl<Form> getSearchPage(FullTextQuery query, Pageable page) {
        FullTextQuery pagedQuery = query.setFirstResult((int) page.getOffset())
                .setMaxResults(page.getPageSize());
        return new PageImpl<>((List<Form>) pagedQuery.getResultList(), page, pagedQuery.getResultSize());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Form> findBySearch(String query, Pageable page) {
        return getSearchPage(getFullTextQuery(query), page);
    }
}
