package com.admanagement.servlet;

import com.admanagement.model.Advertisement;
import com.admanagement.model.AdStatistics;
import com.admanagement.service.AdService;
import com.admanagement.util.FileUploadUtil;
import com.admanagement.util.ResponseUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@WebServlet("/api/ads/*")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 20
)
public class AdServlet extends HttpServlet {

    private AdService adService;

    @Override
    public void init() {
        adService = new AdService();
    }

    /* ===================== GET ===================== */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String path = request.getPathInfo();

        if (path == null || path.equals("/")) {
            handleGetUserAds(request, response);
        } else if (path.equals("/active")) {
            handleGetActiveAds(response);
        } else if (path.startsWith("/view/")) {
            handleViewAd(request, response, path.substring(6));
        } else if (path.startsWith("/category/")) {
            handleGetAdsByCategory(response, path.substring(10));
        } else if (path.startsWith("/stats/")) {
            handleGetAdStats(response, path.substring(7));
        } else {
            ResponseUtil.sendError(response, "Invalid endpoint");
        }
    }

    /* ===================== POST ===================== */

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String path = request.getPathInfo();

        if (path == null || path.equals("/")) {
            handleCreateAd(request, response);
        } else if (path.equals("/click")) {
            handleClickAd(request, response);
        } else {
            ResponseUtil.sendError(response, "Invalid endpoint");
        }
    }

    /* ===================== PUT ===================== */

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String path = request.getPathInfo();

        if (path != null && path.matches("/\\d+")) {
            handleUpdateAd(request, response, path.substring(1));
        } else {
            ResponseUtil.sendError(response, "Invalid endpoint");
        }
    }

    /* ===================== DELETE ===================== */

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String path = request.getPathInfo();

        if (path != null && path.matches("/\\d+")) {
            handleDeleteAd(response, path.substring(1));
        } else {
            ResponseUtil.sendError(response, "Invalid endpoint");
        }
    }

    /* ===================== Handlers ===================== */

    private void handleCreateAd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            Advertisement ad = new Advertisement();
            int userId = (Integer) request.getAttribute("userId");
            ad.setUserId(userId);

            Map<String, Object> parsed =
                    FileUploadUtil.parseMultipartRequest(request);

            @SuppressWarnings("unchecked")
            Map<String, String> fields =
                    (Map<String, String>) parsed.get("formFields");

            @SuppressWarnings("unchecked")
            Map<String, String> files =
                    (Map<String, String>) parsed.get("uploadedFiles");

            // Set basic fields
            ad.setTitle(fields.get("title"));
            ad.setDescription(fields.get("description"));
            
            // Set category (field name is 'category' from form)
            String categoryIdStr = fields.get("category");
            if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
                ad.setCategoryId(Integer.parseInt(categoryIdStr));
            } else {
                throw new IllegalArgumentException("Category is required");
            }
            
            // Set ad type
            String adTypeStr = fields.get("adType");
            if (adTypeStr != null && !adTypeStr.isEmpty()) {
                ad.setAdType(Advertisement.AdType.fromValue(adTypeStr));
            } else {
                // Default to TEXT if not specified
                ad.setAdType(Advertisement.AdType.TEXT);
            }
            
            // Set content based on type
            ad.setTextContent(fields.get("textContent"));
            ad.setTargetUrl(fields.get("targetUrl"));
            
            // Set uploaded files
            if (files != null) {
                if (files.containsKey("image")) {
                    ad.setImageUrl(files.get("image"));
                }
                if (files.containsKey("video")) {
                    ad.setVideoUrl(files.get("video"));
                }
            }
            
            // Set status (default to ACTIVE)
            ad.setStatus(Advertisement.AdStatus.ACTIVE);

            boolean ok = adService.createAdvertisement(ad);
            if (ok) {
                ResponseUtil.sendSuccess(response, ad, "Created");
            } else {
                ResponseUtil.sendError(response, "Create failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Upload failed: " + e.getMessage());
        }
    }


    private void handleGetUserAds(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int userId = (Integer) request.getAttribute("userId");
        List<Advertisement> ads = adService.getAdvertisementsByUserId(userId);
        ResponseUtil.sendSuccess(response, ads);
    }

    private void handleGetActiveAds(HttpServletResponse response)
            throws IOException {

        ResponseUtil.sendSuccess(response, adService.getActiveAdvertisements());
    }

    private void handleGetAdsByCategory(HttpServletResponse response, String cid)
            throws IOException {

        ResponseUtil.sendSuccess(
                response,
                adService.getAdvertisementsByCategory(Integer.parseInt(cid))
        );
    }

    private void handleViewAd(HttpServletRequest request, HttpServletResponse response, String adIdStr)
            throws IOException {

        int adId = Integer.parseInt(adIdStr);
        Advertisement ad = adService.getAdvertisementById(adId);

        if (ad == null) {
            ResponseUtil.sendError(response, 404, "Advertisement not found");
            return;
        }

        // 不再记录内部查看次数，统计数据仅来自外部API访问
        // adService.recordView(adId); // 已删除
        
        // 从外部API访问记录获取统计数据
        AdStatistics stats = adService.getExternalApiStatistics(adId);
        
        // 创建响应对象，包含广告详情和统计信息
        JsonObject result = new JsonObject();
        result.addProperty("adId", ad.getAdId());
        result.addProperty("userId", ad.getUserId());
        result.addProperty("categoryId", ad.getCategoryId());
        result.addProperty("categoryName", ad.getCategoryName());
        result.addProperty("title", ad.getTitle());
        result.addProperty("description", ad.getDescription());
        result.addProperty("adType", ad.getAdType().getValue());
        result.addProperty("textContent", ad.getTextContent());
        result.addProperty("imageUrl", ad.getImageUrl());
        result.addProperty("videoUrl", ad.getVideoUrl());
        result.addProperty("targetUrl", ad.getTargetUrl());
        result.addProperty("status", ad.getStatus().getValue());
        result.addProperty("createdAt", ad.getCreatedAt() != null ? ad.getCreatedAt().toString() : null);
        result.addProperty("updatedAt", ad.getUpdatedAt() != null ? ad.getUpdatedAt().toString() : null);
        result.addProperty("startDate", ad.getStartDate() != null ? ad.getStartDate().toString() : null);
        result.addProperty("endDate", ad.getEndDate() != null ? ad.getEndDate().toString() : null);
        result.addProperty("username", ad.getUsername());
        result.addProperty("companyName", ad.getCompanyName());
        
        // 添加统计信息（来自外部API访问记录）
        if (stats != null) {
            JsonObject statsObj = new JsonObject();
            statsObj.addProperty("viewCount", stats.getViewCount());
            statsObj.addProperty("clickCount", stats.getClickCount());
            // 计算CTR (点击率)
            double ctr = stats.getViewCount() > 0 ? 
                (double) stats.getClickCount() / stats.getViewCount() * 100 : 0.0;
            statsObj.addProperty("ctr", String.format("%.2f%%", ctr));
            result.add("statistics", statsObj);
        }
        
        ResponseUtil.sendSuccess(response, result, "Advertisement details retrieved successfully");
    }

    private void handleClickAd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        JsonObject json = parseJson(request);
        boolean ok = adService.recordClick(json.get("adId").getAsInt());
        if (ok) ResponseUtil.sendSuccess(response, null, "Clicked");
        else ResponseUtil.sendError(response, "Click failed");
    }

    private void handleGetAdStats(HttpServletResponse response, String adIdStr)
            throws IOException {

        // 从外部API访问记录获取统计数据
        AdStatistics stats = adService.getExternalApiStatistics(Integer.parseInt(adIdStr));
        ResponseUtil.sendSuccess(response, stats);
    }

    private void handleUpdateAd(HttpServletRequest request, HttpServletResponse response, String adIdStr)
            throws IOException {

        int adId = Integer.parseInt(adIdStr);
        Advertisement ad = adService.getAdvertisementById(adId);
        if (ad == null) {
            ResponseUtil.sendError(response, 404, "Advertisement not found");
            return;
        }

        JsonObject json = parseJson(request);
        
        // 更新标题
        if (json.has("title") && !json.get("title").isJsonNull()) {
            ad.setTitle(json.get("title").getAsString());
        }
        
        // 更新描述
        if (json.has("description") && !json.get("description").isJsonNull()) {
            ad.setDescription(json.get("description").getAsString());
        }
        
        // 更新文本内容
        if (json.has("textContent") && !json.get("textContent").isJsonNull()) {
            ad.setTextContent(json.get("textContent").getAsString());
        }
        
        // 更新图片URL
        if (json.has("imageUrl") && !json.get("imageUrl").isJsonNull()) {
            ad.setImageUrl(json.get("imageUrl").getAsString());
        }
        
        // 更新视频URL
        if (json.has("videoUrl") && !json.get("videoUrl").isJsonNull()) {
            ad.setVideoUrl(json.get("videoUrl").getAsString());
        }
        
        // 更新目标URL
        if (json.has("targetUrl") && !json.get("targetUrl").isJsonNull()) {
            ad.setTargetUrl(json.get("targetUrl").getAsString());
        }
        
        // 更新分类
        if (json.has("categoryId") && !json.get("categoryId").isJsonNull()) {
            ad.setCategoryId(json.get("categoryId").getAsInt());
        }
        
        // 更新广告类型
        if (json.has("adType") && !json.get("adType").isJsonNull()) {
            String adTypeStr = json.get("adType").getAsString();
            ad.setAdType(Advertisement.AdType.fromValue(adTypeStr));
        }

        boolean ok = adService.updateAdvertisement(ad);
        if (ok) {
            // 返回更新后的完整广告信息
            Advertisement updatedAd = adService.getAdvertisementById(adId);
            ResponseUtil.sendSuccess(response, updatedAd, "Advertisement updated successfully");
        } else {
            ResponseUtil.sendError(response, "Failed to update advertisement");
        }
    }

    private void handleDeleteAd(HttpServletResponse response, String adIdStr)
            throws IOException {

        boolean ok = adService.deleteAdvertisement(Integer.parseInt(adIdStr));
        if (ok) ResponseUtil.sendSuccess(response, null, "Deleted");
        else ResponseUtil.sendError(response, "Delete failed");
    }

    private JsonObject parseJson(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = request.getReader();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }
}
