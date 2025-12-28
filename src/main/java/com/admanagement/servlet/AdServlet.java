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
        } else if (path != null && path.matches("/\\d+/toggle")) {
            handleToggleStatus(response, path.substring(1, path.indexOf("/toggle")));
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
            ResponseUtil.sendError(response, 404, "Not found");
            return;
        }

        adService.recordView(adId);
        ResponseUtil.sendSuccess(response, ad);
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

        AdStatistics stats = adService.getAdStatistics(Integer.parseInt(adIdStr));
        ResponseUtil.sendSuccess(response, stats);
    }

    private void handleUpdateAd(HttpServletRequest request, HttpServletResponse response, String adIdStr)
            throws IOException {

        Advertisement ad = adService.getAdvertisementById(Integer.parseInt(adIdStr));
        if (ad == null) {
            ResponseUtil.sendError(response, "Not found");
            return;
        }

        JsonObject json = parseJson(request);
        ad.setTitle(json.get("title").getAsString());
        ad.setDescription(json.has("description") ? json.get("description").getAsString() : null);

        boolean ok = adService.updateAdvertisement(ad);
        if (ok) ResponseUtil.sendSuccess(response, ad, "Updated");
        else ResponseUtil.sendError(response, "Update failed");
    }

    private void handleDeleteAd(HttpServletResponse response, String adIdStr)
            throws IOException {

        boolean ok = adService.deleteAdvertisement(Integer.parseInt(adIdStr));
        if (ok) ResponseUtil.sendSuccess(response, null, "Deleted");
        else ResponseUtil.sendError(response, "Delete failed");
    }

    private void handleToggleStatus(HttpServletResponse response, String adIdStr)
            throws IOException {

        boolean ok = adService.toggleAdvertisementStatus(Integer.parseInt(adIdStr));
        if (ok) ResponseUtil.sendSuccess(response, null, "Toggled");
        else ResponseUtil.sendError(response, "Toggle failed");
    }

    private JsonObject parseJson(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = request.getReader();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }
}
