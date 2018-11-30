package com.github.cimsbioko.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AjaxAwareAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private ObjectMapper mapper;

    public AjaxAwareAuthenticationEntryPoint(String loginUrl, ObjectMapper mapper) {
        super(loginUrl);
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (isAjax(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (acceptsJson(request)) {
                sendJsonRedirect(request, response, authException);
            }
        } else {
            super.commence(request, response, authException);
        }
    }

    private Boolean acceptsJson(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.ACCEPT))
                .map(s -> s.contains(MediaType.APPLICATION_JSON_VALUE))
                .orElse(false);
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private void sendJsonRedirect(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        Map<String, Object> body = new HashMap<>();
        body.put("location", buildRedirectUrlToLoginPage(request, response, authException));
        mapper.writeValue(response.getWriter(), body);
    }
}
