package com.github.cimsbioko.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class FileSystemConfig {

    @Value("${app.data.dir}")
    File dataDir;

    @Value("${app.forms.dir}")
    File formsDir;

    @Value("${app.submissions.dir}")
    File submissionsDir;

    @Value("${app.campaigns.dir}")
    File campaignsDir;

    @Bean
    File dataDir() {
        dataDir.mkdirs();
        return dataDir;
    }

    @Bean
    File formsDir() {
        formsDir.mkdirs();
        return formsDir;
    }

    @Bean
    File submissionsDir() {
        submissionsDir.mkdirs();
        return submissionsDir;
    }

    @Bean
    File campaignsDir() {
        campaignsDir.mkdirs();
        return campaignsDir;
    }
}
