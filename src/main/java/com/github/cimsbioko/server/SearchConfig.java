package com.github.cimsbioko.server;

import com.github.cimsbioko.server.search.InitialIndexer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchConfig {
    @Bean
    InitialIndexer searchIndexer() {
        return new InitialIndexer();
    }
}
