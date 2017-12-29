package com.github.cimsbioko.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;

@Configuration
@EnableAutoConfiguration
@ImportResource("classpath:/META-INF/spring/application-context.xml")
@Import({FileSystemConfig.class, SecurityConfig.class, WebConfig.class})
@PropertySource(value = "classpath:/META-INF/build-info.properties", ignoreResourceNotFound = true)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    String appVersion(@Value("${build.version}") String buildVersion) {
        return buildVersion != null ? buildVersion : "DEV";
    }
}