package com.admanagement.servlet;

import com.admanagement.model.Advertisement;
import com.admanagement.model.UserBehavior;
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
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * 外部平台广告获取接口
 * 工作流：
 * 1. score = 0 -> 获取广告列表（精准推荐 + 历史偏好 + 热门兜底）
 * 2. score > 0 -> 记录用户交互行为（结算模式，不返回广告）
 */
@WebServlet("/api/external/api/getAds")
public class ExternalAPIServlet extends HttpServlet {

    private AdService adService;
    private String apiKey;

    @Override
    public void init() throws ServletException {
        adService = new AdService();

        // 加载 API Key 验证
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                this.apiKey = prop.getProperty("api.key");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 2. 解析 JSON 请求体
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
            ResponseUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
            return;
        }

        // 提取核心参数
        String anonymousUserId = json.has("anonymousUserId") ? json.get("anonymousUserId").getAsString() : null;
        String tag = json.has("tag") ? json.get("tag").getAsString() : null;
        int score = json.has("score") ? json.get("score").getAsInt() : 0;
        String platform = json.has("platform") ? json.get("platform").getAsString() : "external";
        int limit = json.has("limit") ? json.get("limit").getAsInt() : 5;

        // --- 核心工作流处理 ---

        // 场景 A: 行为结算模式 (score > 0)
        if (score > 0 && anonymousUserId != null && tag != null) {
            // 构造行为对象并记录
            UserBehavior behavior = new UserBehavior(anonymousUserId, tag, score, platform);
            boolean recorded = adService.recordUserBehavior(behavior);

            JsonObject result = new JsonObject();
            result.addProperty("success", recorded);
            result.addProperty("message", "Settlement completed. Behavior recorded.");
            ResponseUtil.sendSuccess(response, result);
            return; // 结算模式不执行后续广告查询，直接返回
        }

// 场景 B: 广告拉取模式 (score == 0)
        List<Advertisement> ads = adService.getAdsByWorkFlow(anonymousUserId, tag, score, limit);

// 根据平台类型调整响应
        JsonArray adArray = new JsonArray();
        if (ads != null) {
            for (Advertisement ad : ads) {
                JsonObject adJson = new JsonObject();
                if ("news".equalsIgnoreCase(platform)) { // 如果是新闻平台，只返回图片 URL
                    adJson.addProperty("imageUrl", ad.getImageUrl());
                } else if ("video".equalsIgnoreCase(platform)) { // 如果是视频平台，只返回视频 URL
                    adJson.addProperty("videoUrl", ad.getVideoUrl());
                } else { // 默认返回所有字段
                    adJson.addProperty("adId", ad.getAdId());
                    adJson.addProperty("title", ad.getTitle());
                    adJson.addProperty("description", ad.getDescription());
                    adJson.addProperty("textContent", ad.getTextContent());
                    adJson.addProperty("imageUrl", ad.getImageUrl());
                    adJson.addProperty("videoUrl", ad.getVideoUrl());
                    adJson.addProperty("targetUrl", ad.getTargetUrl());
                    adJson.addProperty("category", ad.getCategoryName());
                }
                adArray.add(adJson);
            }
        }

        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.addProperty("count", adArray.size());
        result.add("ads", adArray);

        ResponseUtil.sendSuccess(response, result);
    }
}