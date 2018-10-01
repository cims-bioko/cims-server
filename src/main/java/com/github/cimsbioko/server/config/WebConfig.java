package com.github.cimsbioko.server.config;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;
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