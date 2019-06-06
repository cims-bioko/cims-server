package com.github.cimsbioko.server.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Pattern AUTH_PATTERN = Pattern.compile("Bearer\\s+(\\S+)");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationValue = request.getHeader("Authorization");
        if (authorizationValue != null) {
            Matcher authMatcher = AUTH_PATTERN.matcher(authorizationValue);
            if (authMatcher.lookingAt()) {
                SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(authMatcher.group(1)));
            }
        }
        filterChain.doFilter(request, response);
    }
}
