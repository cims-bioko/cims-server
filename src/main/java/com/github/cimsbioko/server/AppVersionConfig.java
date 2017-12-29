package com.github.cimsbioko.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:/META-INF/build-info.properties", ignoreResourceNotFound = true)
public class AppVersionConfig {
    @Bean
    String appVersion(@Value("${build.version}") String buildVersion) {
        return buildVersion != null ? buildVersion : "DEV";
    }
}
