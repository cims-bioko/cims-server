package com.github.cimsbioko.server.webapi.odk;

import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;

public interface EndpointHelper {

    String contextRelativeUrl(HttpServletRequest req, String... pathSegments);

    HttpHeaders openRosaHeaders();

}
