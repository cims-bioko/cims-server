package com.github.cimsbioko.server.web;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.MimeMappings;
import org.springframework.stereotype.Component;

/**
 * This adds additional media types to the embedded container so that resource content types can be properly identified.
 */
@Component
public class MimeTypeCustomizer implements EmbeddedServletContainerCustomizer {

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
        mappings.add("db", "application/x-sqlite3");
        mappings.add("jrsmd", "application/vnd.jrsync+jrsmd");
        container.setMimeMappings(mappings);
    }
}