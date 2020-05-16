package com.github.cimsbioko.server.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * An aspect that enables running methods as the named user. Simply add the {@link RunAsUser} annotation to the method
 * and it should automatically be proxied and invoked with this behavior when called.
 */
@Aspect
public class RunAsUserAspect {

    private static final Logger log = LoggerFactory.getLogger(RunAsUserAspect.class);

    private final UserDetailsService userDetailsService;
    private final UserCache userCache;

    public RunAsUserAspect(UserDetailsService service, UserCache userCache) {
        this.userDetailsService = service;
        this.userCache = userCache;
    }

    @Around("@annotation(annotation))")
    Object runAsUser(ProceedingJoinPoint joinPoint, RunAsUser annotation) throws Throwable {
        String username = annotation.value();
        SecurityContext origContext = SecurityContextHolder.getContext();
        try {
            SecurityContextHolder.setContext(getAuthenticatedContext(username));
            log.debug("running {} as {}", joinPoint.getSignature(), username);
            return joinPoint.proceed();
        } finally {
            SecurityContextHolder.setContext(origContext);
        }
    }

    private Authentication getUserAuthentication(String username) {
        UserDetails user = userCache.getUserFromCache(username);
        if (user == null) {
            user = userDetailsService.loadUserByUsername(username);
            if (user != null) {
                userCache.putUserInCache(user);
            } else {
                throw new SecurityException("could not load user " + username);
            }
        }
        return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
    }

    private SecurityContext getAuthenticatedContext(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(getUserAuthentication(username));
        return context;
    }
}
