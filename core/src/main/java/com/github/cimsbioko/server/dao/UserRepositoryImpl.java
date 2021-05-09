package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.User;
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

public class UserRepositoryImpl implements UserSearch {

    private final EntityManager em;

    public UserRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    private FullTextEntityManager getFullTextEntityManager() {
        return Search.getFullTextEntityManager(em);
    }

    private QueryBuilder getQueryBuilder() {
        return getFullTextEntityManager()
                .getSearchFactory()
                .buildQueryBuilder()
                .forEntity(User.class)
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
                        .onField("username")
                        .matching(query)
                        .createQuery())
                .createQuery();
    }

    private FullTextQuery getFullTextQuery(String query) {
        return getFullTextEntityManager().createFullTextQuery(getSearchQuery(query), User.class);
    }

    @SuppressWarnings("unchecked")
    private PageImpl<User> getSearchPage(FullTextQuery query, Pageable page) {
        FullTextQuery pagedQuery = query
                .setFirstResult((int) page.getOffset())
                .setMaxResults(page.getPageSize());
        return new PageImpl<>((List<User>) pagedQuery.getResultList(), page, pagedQuery.getResultSize());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findBySearch(String query, Pageable page) {
        return getSearchPage(getFullTextQuery(query), page);
    }
}
