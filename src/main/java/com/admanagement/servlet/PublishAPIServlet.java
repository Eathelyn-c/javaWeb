package com.admanagement.servlet;

import com.admanagement.model.Advertisement;
import com.admanagement.service.AdService;
import com.admanagement.service.APIPublishService;
import com.admanagement.util.ResponseUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API Publishing Servlet - handles publishing ads to external platforms
 */
@WebServlet("/api/publish/*")
public class PublishAPIServlet extends HttpServlet {
    private APIPublishService publishService;
    private AdService adService;

    @Override
    public void init() throws ServletException {
        publishService = new APIPublishService();
        adService = new AdService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            ResponseUtil.sendError(response, "Invalid endpoint");
            return;
        }
        
        try {
            int userId = (Integer) request.getAttribute("userId");
            JsonObject json = parseJsonRequest(request);
            
            int adId = json.get("adId").getAsInt();
            
            // Verify ad ownership
            Advertisement ad = adService.getAdvertisementById(adId);
            if (ad == null) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, 
                                     "Advertisement not found");
                return;
            }
            
            if (ad.getUserId() != userId) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, 
                                     "Unauthorized to publish this advertisement");
                return;
            }
            
            Map<String, Object> result;
            
            switch (pathInfo) {
                case "/news":
                    result = publishService.publishToNews(ad);
                    break;
                case "/video":
                    result = publishService.publishToVideo(ad);
                    break;
                case "/shopping":
                    result = publishService.publishToShopping(ad);
                    break;
                case "/multiple":
                    // Publish to multiple platforms
                    JsonArray platformsArray = json.getAsJsonArray("platforms");
                    List<String> platforms = new ArrayList<>();
                    for (int i = 0; i < platformsArray.size(); i++) {
                        platforms.add(platformsArray.get(i).getAsString());
                    }
                    result = publishService.publishToMultiplePlatforms(ad, 
                                                                      platforms.toArray(new String[0]));
                    break;
                default:
                    ResponseUtil.sendError(response, "Invalid platform");
                    return;
            }
            
            if ((Boolean) result.getOrDefault("success", false) || 
                (Boolean) result.getOrDefault("allSuccessful", false)) {
                ResponseUtil.sendSuccess(response, result);
            } else {
                ResponseUtil.sendError(response, result.get("message").toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to publish advertisement: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.equals("/test")) {
            // Test API connection
            String platform = request.getParameter("platform");
            
            if (platform == null) {
                ResponseUtil.sendError(response, "Platform parameter required");
                return;
            }
            
            try {
                Map<String, Object> result = publishService.testAPIConnection(platform);
                ResponseUtil.sendSuccess(response, result);
            } catch (Exception e) {
                e.printStackTrace();
                ResponseUtil.sendError(response, "Failed to test connection: " + e.getMessage());
            }
        } else {
            ResponseUtil.sendError(response, "Invalid endpoint");
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
