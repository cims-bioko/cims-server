package com.github.cimsbioko.server;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;
import javax.faces.webapp.FacesServlet;
import javax.servlet.MultipartConfigElement;
import java.io.File;

@Configuration
public class WebConfig {

    public static final String CACHED_FILES_PATH = "/WEB-INF/cached-files";

    @Resource
    File dataDir;

    @Bean
    WebMvcConfigurerAdapter webMvcConfigurerAdapter() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler(CACHED_FILES_PATH + "/**")
                        .addResourceLocations(dataDir.toURI().toString());
            }
        };
    }

    @Bean
    ServletRegistrationBean jsfServletRegistration() {
        ServletRegistrationBean reg = new ServletRegistrationBean();
        reg.setServlet(new FacesServlet());
        reg.setLoadOnStartup(1);
        reg.addUrlMappings("*.faces");
        return reg;
    }

    @Bean
    ServletRegistrationBean apiServletRegistration() {
        ServletRegistrationBean reg = new ServletRegistrationBean();
        reg.setName("webApis");
        reg.setServlet(new DispatcherServlet());
        reg.addInitParameter("contextConfigLocation", "classpath:/META-INF/spring/webapi-application-context.xml");
        reg.setLoadOnStartup(1);
        reg.addUrlMappings("/api/*");
        reg.setMultipartConfig(new MultipartConfigElement(""));
        return reg;
    }

    @Bean
    FilterRegistrationBean transactionFilterRegistration() {
        FilterRegistrationBean reg = new FilterRegistrationBean();
        reg.setFilter(new OpenSessionInViewFilter());
        reg.addUrlPatterns("*.faces", "/loginProcess", "/api/*");
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
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        return new TomcatEmbeddedServletContainerFactory() {
            @Override
            protected void postProcessContext(Context ctx) {
                final int sizeInKB = 32 * 1024;  // default is 10MiB, increase to 32
                WebResourceRoot resourceRoot = new StandardRoot(ctx);
                resourceRoot.setCacheMaxSize(sizeInKB);
                ctx.setResources(resourceRoot);
            }
        };
    }
}