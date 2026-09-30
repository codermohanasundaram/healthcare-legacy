package com.healthcare.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Redirects to the login page when there is no logged in user in the session.
 * Role checks are done in the individual servlets.
 */
public class AuthenticationFilter implements Filter {

    private static Logger log = Logger.getLogger(AuthenticationFilter.class);

    public void init(FilterConfig config) throws ServletException {
        log.info("AuthenticationFilter initialised");
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String uri = request.getRequestURI();

        // public resources
        if (uri.endsWith("login.jsp") || uri.endsWith("/login")
                || uri.indexOf("/css/") != -1 || uri.indexOf("/images/") != -1
                || uri.endsWith(".js")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.debug("Unauthenticated request to " + uri + " - redirecting to login");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // prevent the browser caching patient pages
        response.setHeader("Cache-Control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");

        chain.doFilter(request, response);
    }

    public void destroy() {
    }
}
