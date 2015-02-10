package org.openhds.web.util;

import org.openfaces.util.ResourceFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by motech on 2/5/15.
 */
public class OpenFacesResourceFilter extends ResourceFilter {

    private String excludeSuffixPattern;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        this.excludeSuffixPattern = filterConfig.getInitParameter("excludeSuffix");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if (servletRequest instanceof HttpServletRequest) {
            String url = ((HttpServletRequest)servletRequest).getRequestURL().toString();
            if (shouldExclude(url)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }

        super.doFilter(servletRequest, servletResponse, filterChain);

    }

    private boolean shouldExclude(String urlPattern) {
        if (null == excludeSuffixPattern) {
            return false;
        }
        return urlPattern.endsWith(excludeSuffixPattern);
    }
}
