package com.github.cimsbioko.server.dao.finder;

import java.lang.reflect.Method;

/**
 * Used to locate a named query based on the called finder method
 */
public interface NamingStrategy {
    String queryNameFromMethod(Class<?> findTargetType,
                               Method finderMethod);
}
