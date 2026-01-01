package com.admanagement.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON response utility for consistent API responses
 */
public class ResponseUtil {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    /**
     * Send success response
     */
    public static void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        sendSuccess(response, data, "Success");
    }

    /**
     * Send success response with custom message
     */
    public static void sendSuccess(HttpServletResponse response, Object data, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("data", data);
        
        sendJson(response, HttpServletResponse.SC_OK, result);
    }

    /**
     * Send error response
     */
    public static void sendError(HttpServletResponse response, String message) throws IOException {
        sendError(response, HttpServletResponse.SC_BAD_REQUEST, message);
    }

    /**
     * Send error response with custom status code
     */
    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("data", null);
        
        sendJson(response, statusCode, result);
    }

    /**
     * Send unauthorized error
     */
    public static void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        sendError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    /**
     * Send JSON response
     */
    private static void sendJson(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Add CORS headers
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    /**
     * Convert object to JSON string
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
}
