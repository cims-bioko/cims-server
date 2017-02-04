package org.openhds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.orm.hibernate4.support.OpenSessionInViewFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.faces.webapp.FacesServlet;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
@EnableAutoConfiguration
@ImportResource("classpath:/META-INF/spring/application-context.xml")
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ServletRegistrationBean jsfServletRegistration() {
        ServletRegistrationBean reg = new ServletRegistrationBean();
        reg.setServlet(new FacesServlet());
        reg.setLoadOnStartup(1);
        reg.addUrlMappings("*.faces");
        return reg;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    @Bean
    ServletRegistrationBean apiServletRegistration() {
        ServletRegistrationBean reg = new ServletRegistrationBean();
        reg.setName("webServices");
        reg.setServlet(new DispatcherServlet());
        reg.addInitParameter("contextConfigLocation", "classpath:/META-INF/spring/webserviceApplicationContext.xml");
        reg.setLoadOnStartup(1);
        reg.addUrlMappings("/api/rest/*");
        return reg;
    }

    @Bean
    FilterRegistrationBean transactionFilterRegistration() {
        FilterRegistrationBean reg = new FilterRegistrationBean();
        reg.setFilter(new OpenSessionInViewFilter());
        reg.addUrlPatterns("*.faces", "/loginProcess", "/api/rest/*");
        return reg;
    }

    @Bean
    ServletContextInitializer configureServletContext() {
        return ctx -> {
            ctx.setInitParameter("javax.faces.FACELETS_SKIP_COMMENTS", "true");
            ctx.setInitParameter("facelets.RECREATE_VALUE_EXPRESSION_ON_BUILD_BEFORE_RESTORE", "false");
            ctx.setInitParameter("javax.faces.STATE_SAVING_METHOD", "client");
            ctx.setInitParameter("javax.faces.FACELETS_LIBRARIES", "/WEB-INF/springsecurity.taglib.xml");
            ctx.setInitParameter("com.sun.faces.forceLoadConfiguration", "true");
            ctx.addListener(com.sun.faces.config.ConfigureListener.class);
        };
    }

    @Bean
    String appVersion() {
        InputStream infoStream = getClass().getResourceAsStream("/META-INF/build-info.properties");
        if (infoStream != null) {
            Properties buildInfo = new Properties();
            try {
                buildInfo.load(infoStream);
            } catch (IOException e) {
                /* ignore */
            }
            String buildVersion = buildInfo.getProperty("build.version");
            if (buildVersion != null) {
                return buildVersion;
            }
        }
        return "DEV";
    }
}