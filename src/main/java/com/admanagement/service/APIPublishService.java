package com.admanagement.service;

import com.admanagement.model.Advertisement;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * API Publishing Service for external platforms
 */
public class APIPublishService {
    private static String NEWS_API_ENDPOINT;
    private static String VIDEO_API_ENDPOINT;
    private static String SHOPPING_API_ENDPOINT;

    static {
        try {
            Properties props = new Properties();
            InputStream input = APIPublishService.class.getClassLoader()
                    .getResourceAsStream("config.properties");
            props.load(input);
            
            NEWS_API_ENDPOINT = props.getProperty("api.news.endpoint");
            VIDEO_API_ENDPOINT = props.getProperty("api.video.endpoint");
            SHOPPING_API_ENDPOINT = props.getProperty("api.shopping.endpoint");
            
            input.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize API configuration", e);
        }
    }

    /**
     * Publish advertisement to news platform
     */
    public Map<String, Object> publishToNews(Advertisement ad) {
        return publishToAPI(NEWS_API_ENDPOINT, ad, "news");
    }

    /**
     * Publish advertisement to video platform
     */
    public Map<String, Object> publishToVideo(Advertisement ad) {
        // Only publish if ad contains video
        if (ad.getVideoUrl() == null || ad.getVideoUrl().trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Advertisement must contain video for video platform");
            return result;
        }
        return publishToAPI(VIDEO_API_ENDPOINT, ad, "video");
    }

    /**
     * Publish advertisement to shopping platform
     */
    public Map<String, Object> publishToShopping(Advertisement ad) {
        return publishToAPI(SHOPPING_API_ENDPOINT, ad, "shopping");
    }

    /**
     * Generic method to publish to any API
     */
    private Map<String, Object> publishToAPI(String endpoint, Advertisement ad, String platformType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("adId", ad.getAdId());
            payload.put("title", ad.getTitle());
            payload.put("description", ad.getDescription());
            payload.put("adType", ad.getAdType().getValue());
            payload.put("category", ad.getCategoryName());
            payload.put("textContent", ad.getTextContent());
            payload.put("imageUrl", ad.getImageUrl());
            payload.put("videoUrl", ad.getVideoUrl());
            payload.put("targetUrl", ad.getTargetUrl());
            payload.put("companyName", ad.getCompanyName());
            
            // Convert to JSON
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String json = gson.toJson(payload);
            
            // Create HTTP client and post request
            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(endpoint);
            
            // Set headers
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");
            
            // Set entity
            post.setEntity(new StringEntity(json, "UTF-8"));
            
            // Execute request
            HttpResponse response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            
            if (statusCode >= 200 && statusCode < 300) {
                result.put("success", true);
                result.put("message", "Successfully published to " + platformType + " platform");
                result.put("platform", platformType);
                result.put("response", responseBody);
            } else {
                result.put("success", false);
                result.put("message", "Failed to publish to " + platformType + " platform");
                result.put("platform", platformType);
                result.put("statusCode", statusCode);
                result.put("error", responseBody);
            }
            
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "Error publishing to " + platformType + " platform: " + e.getMessage());
            result.put("platform", platformType);
            e.printStackTrace();
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Unexpected error: " + e.getMessage());
            result.put("platform", platformType);
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * Publish to multiple platforms
     */
    public Map<String, Object> publishToMultiplePlatforms(Advertisement ad, String[] platforms) {
        Map<String, Object> results = new HashMap<>();
        results.put("adId", ad.getAdId());
        results.put("title", ad.getTitle());
        
        Map<String, Object> platformResults = new HashMap<>();
        
        for (String platform : platforms) {
            Map<String, Object> platformResult;
            
            switch (platform.toLowerCase()) {
                case "news":
                    platformResult = publishToNews(ad);
                    break;
                case "video":
                    platformResult = publishToVideo(ad);
                    break;
                case "shopping":
                    platformResult = publishToShopping(ad);
                    break;
                default:
                    platformResult = new HashMap<>();
                    platformResult.put("success", false);
                    platformResult.put("message", "Unknown platform: " + platform);
            }
            
            platformResults.put(platform, platformResult);
        }
        
        results.put("results", platformResults);
        
        // Check if all successful
        boolean allSuccessful = true;
        for (Object value : platformResults.values()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> platformResult = (Map<String, Object>) value;
            if (!Boolean.TRUE.equals(platformResult.get("success"))) {
                allSuccessful = false;
                break;
            }
        }
        
        results.put("allSuccessful", allSuccessful);
        
        return results;
    }

    /**
     * Test API connection
     */
    public Map<String, Object> testAPIConnection(String platformType) {
        Map<String, Object> result = new HashMap<>();
        
        String endpoint;
        switch (platformType.toLowerCase()) {
            case "news":
                endpoint = NEWS_API_ENDPOINT;
                break;
            case "video":
                endpoint = VIDEO_API_ENDPOINT;
                break;
            case "shopping":
                endpoint = SHOPPING_API_ENDPOINT;
                break;
            default:
                result.put("success", false);
                result.put("message", "Unknown platform type");
                return result;
        }
        
        result.put("platform", platformType);
        result.put("endpoint", endpoint);
        result.put("success", true);
        result.put("message", "API endpoint configured");
        
        return result;
    }
}
