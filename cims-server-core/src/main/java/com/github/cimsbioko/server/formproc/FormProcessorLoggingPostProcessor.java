package com.github.cimsbioko.server.formproc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class FormProcessorLoggingPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FormProcessor) {
            FormProcessor processor = (FormProcessor) bean;
            Logger processorLogger = LoggerFactory.getLogger(FormProcessor.class.getName() + "." + beanName);
            processor.setLogger(processorLogger);
        }
        return bean;
    }
}
