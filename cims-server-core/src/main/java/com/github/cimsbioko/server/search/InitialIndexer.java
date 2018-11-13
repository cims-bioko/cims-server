package com.github.cimsbioko.server.search;

import org.hibernate.search.batchindexing.impl.SimpleIndexingProgressMonitor;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

public class InitialIndexer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(InitialIndexer.class);

    private boolean reindexOnStartup;

    private EntityManager em;

    public InitialIndexer(EntityManager em, boolean reindexOnStartup) {
        this.em = em;
        this.reindexOnStartup = reindexOnStartup;
    }

    @Override
    @Transactional
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (reindexOnStartup) {
            reindexAll();
        } else {
            log.info("not reindexing db, disabled by user settings");
        }
    }

    private void reindexAll() {
        log.info("building index for full-text search");
        try {
            Search.getFullTextEntityManager(em)
                    .createIndexer()
                    .progressMonitor(new SimpleIndexingProgressMonitor(10000))
                    .startAndWait();
        } catch (InterruptedException e) {
            log.error("interrupted while building search indexes", e);
        }
    }

}
