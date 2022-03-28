package com.github.cimsbioko.server.config;

import com.github.cimsbioko.server.search.InitialIndexer;
import com.github.cimsbioko.server.service.IndexingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;

@Configuration
public class SearchConfig {
    @Bean
    InitialIndexer searchIndexer(TaskScheduler scheduler, IndexingService indexingService,
                                 @Value("${app.reindexOnStartup}") boolean reindexOnStartup,
                                 @Value("${app.reindexDelayMinutes}") long delayInMinutes) {
        return new InitialIndexer(scheduler, indexingService, reindexOnStartup, delayInMinutes);
    }
}
