package com.github.cimsbioko.server.search;

import org.hibernate.SessionFactory;
import org.hibernate.search.Search;
import org.hibernate.search.batchindexing.impl.SimpleIndexingProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitialIndexer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(InitialIndexer.class);

    @Value("${app.reindexOnStartup}")
    private boolean reindexOnStartup;

    @Autowired
    private SessionFactory sf;

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
            Search.getFullTextSession(sf.getCurrentSession())
                    .createIndexer()
                    .progressMonitor(new SimpleIndexingProgressMonitor(10000))
                    .startAndWait();
        } catch (InterruptedException e) {
            log.error("interrupted while building search indexes", e);
        }
    }

}
