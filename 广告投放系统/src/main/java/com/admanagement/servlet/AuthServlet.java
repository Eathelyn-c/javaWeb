package com.admanagement.servlet;

import com.admanagement.service.AuthService;
import com.admanagement.util.ResponseUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/**
 * Authentication Servlet - handles login and registration
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            ResponseUtil.sendError(response, "Invalid endpoint");
            return;
        }
        
        switch (pathInfo) {
            case "/register":
                handleRegister(request, response);
                break;
            case "/login":
                handleLogin(request, response);
                break;
            default:
                ResponseUtil.sendError(response, "Invalid endpoint");
        }
    }

    /**
     * Handle user registration
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            // Parse JSON request
            JsonObject json = parseJsonRequest(request);
            
            String username = json.has("username") ? json.get("username").getAsString() : null;
            String email = json.has("email") ? json.get("email").getAsString() : null;
            String password = json.has("password") ? json.get("password").getAsString() : null;
            String fullName = json.has("fullName") ? json.get("fullName").getAsString() : null;
            String companyName = json.has("companyName") ? json.get("companyName").getAsString() : null;
            String phone = json.has("phone") ? json.get("phone").getAsString() : null;
            
            Map<String, Object> result = authService.register(username, email, password, 
                                                             fullName, companyName, phone);
            
            if ((Boolean) result.get("success")) {
                ResponseUtil.sendSuccess(response, result.get("data"), result.get("message").toString());
            } else {
                ResponseUtil.sendError(response, result.get("message").toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Registration failed: " + e.getMessage());
        }
    }

    /**
     * Handle user login
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            // Parse JSON request
            JsonObject json = parseJsonRequest(request);
            
            String username = json.has("username") ? json.get("username").getAsString() : null;
            String password = json.has("password") ? json.get("password").getAsString() : null;
            
            Map<String, Object> result = authService.login(username, password);
            
            if ((Boolean) result.get("success")) {
                ResponseUtil.sendSuccess(response, result.get("data"), result.get("message").toString());
            } else {
                ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, 
                                     result.get("message").toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Login failed: " + e.getMessage());
        }
    }

    /**
     * Parse JSON from request body
     */
    private JsonObject parseJsonRequest(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }
}
