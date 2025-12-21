package com.admanagement.servlet;

import com.admanagement.model.Advertisement;
import com.admanagement.service.AdService;
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
import java.util.List;

/**
 * External API Servlet - 外部平台获取广告
 */
@WebServlet("/external/api/getAds")
public class ExternalAPIServlet extends HttpServlet {

    private AdService adService;
    private String apiKey;

    @Override
    public void init() throws ServletException {
        adService = new AdService();
        apiKey = getServletContext().getInitParameter("apiKey"); // 从 web.xml 读取
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. 验证 API Key
        String key = request.getHeader("X-API-Key");
        if (key == null || !key.equals(apiKey)) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
            return;
        }

        // 2. 解析请求体 JSON
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        JsonObject json;
        try {
            json = JsonParser.parseString(sb.toString()).getAsJsonObject();
        } catch (Exception e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        String anonymousUserId = json.has("anonymousUserId") ? json.get("anonymousUserId").getAsString() : null;
        String tag = json.has("tag") ? json.get("tag").getAsString() : null;
        int score = json.has("score") ? json.get("score").getAsInt() : 0;
        String platform = json.has("platform") ? json.get("platform").getAsString() : null;
        int limit = 10;
        if (json.has("limit")) {
            limit = json.get("limit").getAsInt();
        }

        if (tag == null || platform == null) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Tag and platform are required");
            return;
        }

        // 3. 调用 Service 获取广告列表（按 score 排序）
        List<Advertisement> ads = adService.getAdsByTagAndScore(tag, score, platform, limit);

        // 4. 构建 JSON 响应
        JsonArray adArray = new JsonArray();
        for (Advertisement ad : ads) {
            JsonObject adJson = new JsonObject();
            adJson.addProperty("adId", ad.getAdId());
            adJson.addProperty("title", ad.getTitle());
            adJson.addProperty("description", ad.getDescription());
            adJson.addProperty("textContent", ad.getTextContent());
            adJson.addProperty("imageUrl", ad.getImageUrl());
            adJson.addProperty("videoUrl", ad.getVideoUrl());
            adJson.addProperty("targetUrl", ad.getTargetUrl());
            adJson.addProperty("category", ad.getCategoryName());
            adArray.add(adJson);
        }

        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.add("ads", adArray);

        ResponseUtil.sendSuccess(response, result);
    }
}
