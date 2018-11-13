package com.github.cimsbioko.server.config;

import com.github.cimsbioko.server.search.InitialIndexer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class SearchConfig {
    @Bean
    InitialIndexer searchIndexer(EntityManager em, @Value("${app.reindexOnStartup}") boolean reindexOnStartup) {
        return new InitialIndexer(em, reindexOnStartup);
    }
}
