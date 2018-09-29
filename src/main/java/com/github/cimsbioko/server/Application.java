package com.github.cimsbioko.server;

import com.github.cimsbioko.server.webapi.odk.ODKConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableAutoConfiguration
@ImportResource("classpath:/META-INF/spring/application-context.xml")
@Import({FileSystemConfig.class, FormProcConfig.class, SecurityConfig.class, WebConfig.class,
        AppVersionConfig.class, SearchConfig.class, ODKConfig.class, XLSFormConfig.class, SqliteExportConfig.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}