package org.openhds.web.util;

import org.openfaces.util.ResourceFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by motech on 2/5/15.
 */
public class OpenFacesResourceFilter extends ResourceFilter {

    private String exludeSuffixPattern;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        this.exludeSuffixPattern = filterConfig.getInitParameter("excludeSuffix");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if (servletRequest instanceof HttpServletRequest) {
            String url = ((HttpServletRequest)servletRequest).getRequestURL().toString();
            if (shouldExlude(url)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }

        super.doFilter(servletRequest, servletResponse, filterChain);

    }

    private boolean shouldExlude(String urlPattern) {
        return urlPattern.endsWith(exludeSuffixPattern);
    }
}
