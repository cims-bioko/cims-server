package com.github.cimsbioko.server.scripting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * Responsible for registering a custom rhino context factory global on application startup.
 */
public class ContextFactoryRegistrar implements ApplicationListener<ApplicationPreparedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ContextFactoryRegistrar.class);

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        log.info("registering custom js context factory");
        ContextFactory.register();
    }
}
