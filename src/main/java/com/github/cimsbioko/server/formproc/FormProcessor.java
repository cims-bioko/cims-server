package com.github.cimsbioko.server.formproc;

import org.slf4j.Logger;
import org.springframework.context.ApplicationContextAware;

public interface FormProcessor extends ApplicationContextAware {
    void setLogger(Logger log);
    int processForms();
}
