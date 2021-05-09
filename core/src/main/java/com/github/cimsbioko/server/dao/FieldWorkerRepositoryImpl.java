package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.FieldWorker;
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

public class FieldWorkerRepositoryImpl implements FieldworkerSearch {

    private final EntityManager em;

    public FieldWorkerRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    private FullTextEntityManager getFullTextEntityManager() {
        return Search.getFullTextEntityManager(em);
    }

    private QueryBuilder getQueryBuilder() {
        return getFullTextEntityManager()
                .getSearchFactory()
                .buildQueryBuilder()
                .forEntity(FieldWorker.class)
                .get();
    }

    private Query getSearchQuery(String query) {
        return getQueryBuilder()
                .bool()
                .should(getQueryBuilder()
                        .keyword()
                        .fuzzy()
                        .withEditDistanceUpTo(2)
                        .onFields("firstName", "lastName")
                        .matching(query)
                        .createQuery())
                .should(getQueryBuilder()
                        .keyword()
                        .onField("extId")
                        .matching(query)
                        .createQuery())
                .createQuery();
    }

    private FullTextQuery getFullTextQuery(String query) {
        return getFullTextEntityManager().createFullTextQuery(getSearchQuery(query), FieldWorker.class);
    }

    @SuppressWarnings("unchecked")
    private PageImpl<FieldWorker> getSearchPage(FullTextQuery query, Pageable page) {
        FullTextQuery pagedQuery = query
                .setFirstResult((int) page.getOffset())
                .setMaxResults(page.getPageSize());
        return new PageImpl<>((List<FieldWorker>) pagedQuery.getResultList(), page, pagedQuery.getResultSize());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FieldWorker> findBySearch(String query, Pageable page) {
        return getSearchPage(getFullTextQuery(query), page);
    }
}
