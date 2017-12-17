package com.github.cimsbioko.server.controller.aop;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import com.github.cimsbioko.server.controller.exception.AuthorizationException;
import com.github.cimsbioko.server.domain.annotations.Authorized;
import com.github.cimsbioko.server.domain.model.Privilege;
import com.github.cimsbioko.server.controller.service.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * Authorization Advice that is used before service level calls to ensure current user
 * has privileges to access those methods. Some of this code was lifted from the OpenMRS
 * AuthorizationAdvice class
 *
 * @author Dave Roberge
 */
public class AuthorizationAdvice implements MethodBeforeAdvice {

    CurrentUser currentUser;

    private final static Logger log = LoggerFactory.getLogger(AuthorizationAdvice.class);

    public void before(Method method, Object[] args, Object target) throws Throwable {

        Collection<String> privileges = getPrivileges(method);
        boolean requiredAll = getRequiredAll(method);

        if (privileges == null) {
            return;
        }

        Set<Privilege> userPrivileges = currentUser.getCurrentUserPrivileges();

        for (String privilege : privileges) {
            Privilege priv = new Privilege(privilege);
            if (userPrivileges.contains(priv)) {
                if (!requiredAll) { // only need 1 privilege
                    return;
                }
            } else if (requiredAll) { // user did not have at least 1 required method
                log.debug("user cannot access " + method.getName());
                throw new AuthorizationException("You do not have the privileges to complete this operation");
            }
        }

        if (!requiredAll) {
            log.debug("user cannot access " + method.getName());
            throw new AuthorizationException("You do not have the privileges to complete this operation");
        }
    }

    /**
     * Retrieve the privileges a method is annotated with
     *
     * @param method the method to retrieve the privileges from
     * @return a collection of privileges
     */
    private Collection<String> getPrivileges(Method method) {
        Authorized auth = method.getAnnotation(Authorized.class);

        if (auth == null) {
            return null;
        }

        return Arrays.asList(auth.value());
    }

    /**
     * Determine if the authorized requires all privileges
     *
     * @param method the method to check for privileges
     * @return true if the method requires all privileges, false otherwise
     */
    private boolean getRequiredAll(Method method) {
        Authorized auth = method.getAnnotation(Authorized.class);

        if (auth == null) {
            return false;
        }

        return auth.requireAll();
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }
}
