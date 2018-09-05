package com.github.cimsbioko.server.webapi.odk;

import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

import static com.github.cimsbioko.server.webapi.odk.Constants.ODK_API_PATH;
import static org.springframework.security.web.util.UrlUtils.buildFullRequestUrl;

public class DefaultEndpointHelper implements EndpointHelper {

    public String contextRelativeUrl(HttpServletRequest req, String... pathSegments) {
        return UriComponentsBuilder
                .fromHttpUrl(buildFullRequestUrl(req))
                .replacePath(req.getContextPath() + req.getServletPath() + ODK_API_PATH)
                .pathSegment(pathSegments)
                .query(null)
                .toUriString();
    }

    public HttpHeaders openRosaHeaders() {
        HttpHeaders header = new HttpHeaders();
        header.add("X-OpenRosa-Version", "1.0");
        header.add("X-OpenRosa-Accept-Content-Length", "10485760");
        return header;
    }
}
