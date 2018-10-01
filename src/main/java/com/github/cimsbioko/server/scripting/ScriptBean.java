package com.github.cimsbioko.server.scripting;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.scripting.ScriptFactory;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.scripting.support.StandardScriptFactory;

public class ScriptBean<T> extends AbstractFactoryBean<T> {

    private String sourceLocator;
    private Class<T> interfaceType;

    public ScriptBean(String sourceLocator, Class<T> interfaceType) {
        this.sourceLocator = sourceLocator;
        this.interfaceType = interfaceType;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceType;
    }

    @Override
    protected T createInstance() throws Exception {
        Resource scriptResource = new DefaultResourceLoader().getResource(sourceLocator);
        ScriptFactory scriptFactory = new StandardScriptFactory(sourceLocator, interfaceType);
        return (T) scriptFactory.getScriptedObject(new ResourceScriptSource(scriptResource), interfaceType);
    }
}
