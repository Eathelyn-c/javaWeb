package com.admanagement.filter;

import com.admanagement.util.JWTUtil;
import com.admanagement.util.ResponseUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Authentication filter to validate JWT tokens
 */
@WebFilter("/*")
public class AuthFilter implements Filter {
    
    // Paths that don't require authentication
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/ads/view",
        "/api/ads/click",
        "/api/ads/active",
        "/api/categories"
    ));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Add CORS headers
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        // Handle preflight requests
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        String path = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String relativePath = path.substring(contextPath.length());
        
        // Check if path is public or is a static resource
        if (isPublicPath(relativePath) || isStaticResource(relativePath)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Get token from Authorization header
        String authHeader = httpRequest.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.sendUnauthorized(httpResponse, "Missing or invalid authorization token");
            return;
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        // Validate token
        if (!JWTUtil.validateToken(token)) {
            ResponseUtil.sendUnauthorized(httpResponse, "Invalid or expired token");
            return;
        }
        
        // Add user ID to request attribute
        try {
            int userId = JWTUtil.getUserIdFromToken(token);
            httpRequest.setAttribute("userId", userId);
            httpRequest.setAttribute("username", JWTUtil.getUsernameFromToken(token));
        } catch (Exception e) {
            ResponseUtil.sendUnauthorized(httpResponse, "Invalid token");
            return;
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }

    /**
     * Check if path is public (doesn't require authentication)
     */
    private boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if path is a static resource
     */
    private boolean isStaticResource(String path) {
        return path.startsWith("/frontend/") || 
               path.startsWith("/uploads/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.endsWith(".html") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".jpeg") ||
               path.endsWith(".gif") ||
               path.endsWith(".ico");
    }
}
