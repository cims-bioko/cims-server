package com.github.cimsbioko.server.scripting;

import org.mozilla.javascript.Context;

import static org.mozilla.javascript.Context.VERSION_ES6;

/**
 * Custom context factory for consistent creation of contexts when entering js, including through the vm bridge.
 */
public class ContextFactory extends org.mozilla.javascript.ContextFactory {

    ContextFactory() {
        super();
    }

    @Override
    protected Context makeContext() {
        Context ctx = super.makeContext();
        ctx.setOptimizationLevel(-1);
        ctx.setLanguageVersion(VERSION_ES6);
        return ctx;
    }

    public static void register() {
        ContextFactory ctxFactory = new ContextFactory();
        if (!hasExplicitGlobal()) {
            initGlobal(ctxFactory);
        } else {
            getGlobalSetter().setContextFactoryGlobal(ctxFactory);
        }
    }

    public static void unregister() {
        if (hasExplicitGlobal()) {
            ContextFactory
                    .getGlobalSetter()
                    .setContextFactoryGlobal(new org.mozilla.javascript.ContextFactory());
        }
    }
}