package com.github.cimsbioko.server.webapi;

import org.springframework.context.ApplicationContextAware;

public interface FormProcessor extends ApplicationContextAware {
    int processForms();
}
