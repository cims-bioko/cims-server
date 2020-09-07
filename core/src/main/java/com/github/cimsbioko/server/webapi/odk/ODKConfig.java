package com.github.cimsbioko.server.webapi.odk;

import com.github.cimsbioko.server.service.FormService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ODKConfig {

    @Bean
    FileHasher hasher() {
        return new DefaultFileHasher();
    }

    @Bean
    SubmissionIdGenerator submissionIdGenerator() {
        return new DefaultSubmissionIdGenerator();
    }

    @Bean
    SubmissionFileSystem submissionFileSystem() {
        return new DefaultSubmissionFileSystem();
    }

    @Bean
    EndpointHelper odkEndpointHelper() {
        return new DefaultEndpointHelper();
    }

    @Bean
    FormFileSystem formFileSystem() {
        return new DefaultFormFileSystem();
    }

    @Bean
    OpenRosaResponseBuilder openRosaResponseBuilder() {
        return new DefaultOpenRosaResponseBuilder();
    }

    @Bean
    DateFormatter odkDateFormatter() {
        return new DefaultDateFormatter();
    }

    @Bean
    SubmissionJSONConverter submissionJSONConverter(FormService formService) {
        return new DefaultSubmissionJSONConverter(formService);
    }

    @Bean
    RepeatExtractor repeatExtractor() {
        return new RepeatExtractorImpl();
    }
}
