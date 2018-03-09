package com.github.cimsbioko.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;

@Configuration
@EnableAutoConfiguration
@ImportResource("classpath:/META-INF/spring/application-context.xml")
@Import({FileSystemConfig.class, PersistenceConfig.class, FormProcConfig.class, SecurityConfig.class, WebConfig.class,
        AppVersionConfig.class, SearchConfig.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}