package com.admanagement.servlet;

import com.admanagement.model.Advertisement;
import com.admanagement.model.AdStatistics;
import com.admanagement.model.Category;
import com.admanagement.service.AdService;
import com.admanagement.util.FileUploadUtil;
import com.admanagement.util.ResponseUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Advertisement Servlet - handles ad CRUD operations
 */
@WebServlet("/api/ads/*")
@jakarta.servlet.annotation.MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1 MB
    maxFileSize = 1024 * 1024 * 10,      // 10 MB
    maxRequestSize = 1024 * 1024 * 20    // 20 MB
)
public class AdServlet extends HttpServlet {
    private AdService adService;

    @Override
    public void init() throws ServletException {
        adService = new AdService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // Get all ads for the user
            handleGetUserAds(request, response);
        } else if (pathInfo.startsWith("/view/")) {
            // Get single ad and record view
            String adIdStr = pathInfo.substring(6);
            handleViewAd(request, response, adIdStr);
        } else if (pathInfo.equals("/active")) {
            // Get all active ads (public)
            handleGetActiveAds(request, response);
        } else if (pathInfo.startsWith("/category/")) {
            // Get ads by category
            String categoryIdStr = pathInfo.substring(10);
            handleGetAdsByCategory(request, response, categoryIdStr);
        } else if (pathInfo.startsWith("/stats/")) {
            // Get ad statistics
            String adIdStr = pathInfo.substring(7);
            handleGetAdStats(request, response, adIdStr);
        } else {
            ResponseUtil.sendError(response, "Invalid endpoint");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // Create new ad
            handleCreateAd(request, response);
        } else if (pathInfo.equals("/click")) {
            // Record click
            handleClickAd(request, response);
        } else {
            ResponseUtil.sendError(response, "Invalid endpoint");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            // Update ad
            String adIdStr = pathInfo.substring(1);
            handleUpdateAd(request, response, adIdStr);
        } else if (pathInfo != null && pathInfo.matches("/\\d+/toggle")) {
            // Toggle ad status
            String adIdStr = pathInfo.substring(1, pathInfo.indexOf("/toggle"));
            handleToggleAdStatus(request, response, adIdStr);
        } else {
            ResponseUtil.sendError(response, "Invalid endpoint");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            // Delete ad
            String adIdStr = pathInfo.substring(1);
            handleDeleteAd(request, response, adIdStr);
        } else {
            ResponseUtil.sendError(response, "Invalid endpoint");
        }
    }

    /**
     * Create new advertisement
     */
    private void handleCreateAd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            int userId = (Integer) request.getAttribute("userId");
            
            // Handle multipart form data (for file uploads)
            if (FileUploadUtil.isMultipartRequest(request)) {
                Map<String, Object> parsed = FileUploadUtil.parseMultipartRequest(request);
                
                @SuppressWarnings("unchecked")
                Map<String, String> formFields = (Map<String, String>) parsed.get("formFields");
                @SuppressWarnings("unchecked")
                Map<String, String> uploadedFiles = (Map<String, String>) parsed.get("uploadedFiles");
                
                String title = formFields.get("title");
                String description = formFields.get("description");
                String adType = formFields.get("adType");
                String categoryName = formFields.get("category");
                String textContent = formFields.get("textContent");
                String targetUrl = formFields.get("targetUrl");
                
                String imageUrl = uploadedFiles.get("image");
                String videoUrl = uploadedFiles.get("video");
                
                Timestamp startDate = formFields.containsKey("startDate") ? 
                    Timestamp.valueOf(formFields.get("startDate")) : null;
                Timestamp endDate = formFields.containsKey("endDate") ? 
                    Timestamp.valueOf(formFields.get("endDate")) : null;
                
                Map<String, Object> result = adService.createAdvertisement(userId, title, description,
                    adType, categoryName, textContent, imageUrl, videoUrl, targetUrl, startDate, endDate);
                
                if ((Boolean) result.get("success")) {
                    ResponseUtil.sendSuccess(response, result.get("data"), result.get("message").toString());
                } else {
                    ResponseUtil.sendError(response, result.get("message").toString());
                }
            } else {
                // Handle JSON request
                JsonObject json = parseJsonRequest(request);
                
                String title = json.get("title").getAsString();
                String description = json.has("description") ? json.get("description").getAsString() : null;
                String adType = json.get("adType").getAsString();
                String categoryName = json.get("category").getAsString();
                String textContent = json.has("textContent") ? json.get("textContent").getAsString() : null;
                String imageUrl = json.has("imageUrl") ? json.get("imageUrl").getAsString() : null;
                String videoUrl = json.has("videoUrl") ? json.get("videoUrl").getAsString() : null;
                String targetUrl = json.has("targetUrl") ? json.get("targetUrl").getAsString() : null;
                
                Timestamp startDate = json.has("startDate") ? 
                    Timestamp.valueOf(json.get("startDate").getAsString()) : null;
                Timestamp endDate = json.has("endDate") ? 
                    Timestamp.valueOf(json.get("endDate").getAsString()) : null;
                
                Map<String, Object> result = adService.createAdvertisement(userId, title, description,
                    adType, categoryName, textContent, imageUrl, videoUrl, targetUrl, startDate, endDate);
                
                if ((Boolean) result.get("success")) {
                    ResponseUtil.sendSuccess(response, result.get("data"), result.get("message").toString());
                } else {
                    ResponseUtil.sendError(response, result.get("message").toString());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to create advertisement: " + e.getMessage());
        }
    }

    /**
     * Get user's advertisements
     */
    private void handleGetUserAds(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            int userId = (Integer) request.getAttribute("userId");
            List<Advertisement> ads = adService.getUserAdvertisements(userId);
            
            ResponseUtil.sendSuccess(response, ads);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to get advertisements: " + e.getMessage());
        }
    }

    /**
     * Get active advertisements (public)
     */
    private void handleGetActiveAds(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            List<Advertisement> ads = adService.getActiveAdvertisements();
            ResponseUtil.sendSuccess(response, ads);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to get advertisements: " + e.getMessage());
        }
    }

    /**
     * Get advertisements by category
     */
    private void handleGetAdsByCategory(HttpServletRequest request, HttpServletResponse response, 
                                       String categoryIdStr) throws IOException {
        
        try {
            int categoryId = Integer.parseInt(categoryIdStr);
            List<Advertisement> ads = adService.getAdvertisementsByCategory(categoryId);
            ResponseUtil.sendSuccess(response, ads);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to get advertisements: " + e.getMessage());
        }
    }

    /**
     * View advertisement and record view
     */
    private void handleViewAd(HttpServletRequest request, HttpServletResponse response, String adIdStr)
            throws IOException {
        
        try {
            int adId = Integer.parseInt(adIdStr);
            Advertisement ad = adService.getAdvertisementById(adId);
            
            if (ad == null) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Advertisement not found");
                return;
            }
            
            // Record view
            adService.recordView(adId);
            
            ResponseUtil.sendSuccess(response, ad);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to get advertisement: " + e.getMessage());
        }
    }

    /**
     * Record click on advertisement
     */
    private void handleClickAd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            JsonObject json = parseJsonRequest(request);
            int adId = json.get("adId").getAsInt();
            
            boolean success = adService.recordClick(adId);
            
            if (success) {
                ResponseUtil.sendSuccess(response, null, "Click recorded");
            } else {
                ResponseUtil.sendError(response, "Failed to record click");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to record click: " + e.getMessage());
        }
    }

    /**
     * Get advertisement statistics
     */
    private void handleGetAdStats(HttpServletRequest request, HttpServletResponse response, 
                                  String adIdStr) throws IOException {
        
        try {
            int userId = (Integer) request.getAttribute("userId");
            int adId = Integer.parseInt(adIdStr);
            
            AdStatistics stats = adService.getAdStatistics(adId, userId);
            
            if (stats == null) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, 
                                     "Statistics not found or unauthorized");
                return;
            }
            
            ResponseUtil.sendSuccess(response, stats);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to get statistics: " + e.getMessage());
        }
    }

    /**
     * Update advertisement
     */
    private void handleUpdateAd(HttpServletRequest request, HttpServletResponse response, 
                               String adIdStr) throws IOException {
        
        try {
            int userId = (Integer) request.getAttribute("userId");
            int adId = Integer.parseInt(adIdStr);
            
            JsonObject json = parseJsonRequest(request);
            
            String title = json.get("title").getAsString();
            String description = json.has("description") ? json.get("description").getAsString() : null;
            String adType = json.get("adType").getAsString();
            String categoryName = json.get("category").getAsString();
            String textContent = json.has("textContent") ? json.get("textContent").getAsString() : null;
            String imageUrl = json.has("imageUrl") ? json.get("imageUrl").getAsString() : null;
            String videoUrl = json.has("videoUrl") ? json.get("videoUrl").getAsString() : null;
            String targetUrl = json.has("targetUrl") ? json.get("targetUrl").getAsString() : null;
            
            Timestamp startDate = json.has("startDate") ? 
                Timestamp.valueOf(json.get("startDate").getAsString()) : null;
            Timestamp endDate = json.has("endDate") ? 
                Timestamp.valueOf(json.get("endDate").getAsString()) : null;
            
            Map<String, Object> result = adService.updateAdvertisement(adId, userId, title, description,
                adType, categoryName, textContent, imageUrl, videoUrl, targetUrl, startDate, endDate);
            
            if ((Boolean) result.get("success")) {
                ResponseUtil.sendSuccess(response, result.get("data"), result.get("message").toString());
            } else {
                ResponseUtil.sendError(response, result.get("message").toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to update advertisement: " + e.getMessage());
        }
    }

    /**
     * Delete advertisement
     */
    private void handleDeleteAd(HttpServletRequest request, HttpServletResponse response, 
                               String adIdStr) throws IOException {
        
        try {
            int userId = (Integer) request.getAttribute("userId");
            int adId = Integer.parseInt(adIdStr);
            
            Map<String, Object> result = adService.deleteAdvertisement(adId, userId);
            
            if ((Boolean) result.get("success")) {
                ResponseUtil.sendSuccess(response, null, result.get("message").toString());
            } else {
                ResponseUtil.sendError(response, result.get("message").toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to delete advertisement: " + e.getMessage());
        }
    }

    /**
     * Toggle advertisement status (active/paused)
     */
    private void handleToggleAdStatus(HttpServletRequest request, HttpServletResponse response, 
                                     String adIdStr) throws IOException {
        
        try {
            int userId = (Integer) request.getAttribute("userId");
            int adId = Integer.parseInt(adIdStr);
            
            Map<String, Object> result = adService.toggleAdvertisementStatus(adId, userId);
            
            if ((Boolean) result.get("success")) {
                ResponseUtil.sendSuccess(response, result.get("data"), result.get("message").toString());
            } else {
                ResponseUtil.sendError(response, result.get("message").toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to toggle status: " + e.getMessage());
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
