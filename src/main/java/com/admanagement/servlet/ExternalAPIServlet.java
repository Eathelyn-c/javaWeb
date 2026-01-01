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

        // 读取请求体
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        JsonObject json;
        try {
            json = JsonParser.parseString(sb.toString()).getAsJsonObject();
        } catch (Exception e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
            return;
        }

        // 参数解析
        String anonymousUserId = json.has("anonymousUserId") ? json.get("anonymousUserId").getAsString() : null;
        String tag = json.has("tag") ? json.get("tag").getAsString() : null;
        int score = json.has("score") ? json.get("score").getAsInt() : 0;
        String platform = json.has("platform") ? json.get("platform").getAsString() : "external";
        int limit = json.has("limit") ? json.get("limit").getAsInt() : 5;

        // 平台限制 video / news / shopping
        if(!platform.equals("video") && !platform.equals("news") && !platform.equals("shopping")) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid platform. Allowed: video, news, shopping");
            return;
        }

        // score > 0 行为结算模式
        if (score > 0 && anonymousUserId != null && tag != null) {
            UserBehavior behavior = new UserBehavior(anonymousUserId, tag, score, platform);
            boolean recorded = adService.recordUserBehavior(behavior);

            JsonObject result = new JsonObject();
            result.addProperty("success", recorded);
            result.addProperty("message", "Settlement completed. Behavior recorded.");
            ResponseUtil.sendSuccess(response, result);
            return;
        }

        // score = 0 拉取广告
        List<Advertisement> ads = adService.getAdsByWorkFlow(anonymousUserId, tag, score, limit);

        JsonArray adArray = new JsonArray();
        if (ads != null) {
            for (Advertisement ad : ads) {
                JsonObject adJson = new JsonObject();

                // 平台决定返回资源类型
                String url = null;
                if(platform.equals("video")) {
                    url = ad.getVideoUrl();       // 只传视频
                } else {
                    url = ad.getImageUrl();       // 只传图片
                }

                if (url != null && !url.trim().isEmpty()) {
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        String baseUrl = request.getScheme() + "://" + request.getServerName();
                        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                            baseUrl += ":" + request.getServerPort();
                        }
                        String contextPath = request.getContextPath();
                        url = baseUrl + (url.startsWith("/") ? contextPath + url : contextPath + "/" + url);
                    }
                    adJson.addProperty("url", url);
                    adArray.add(adJson);
                }
            }
        }

        // 返回内容
        JsonObject result = new JsonObject();
        result.addProperty("code", 200);
        result.add("ads", adArray);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(result.toString());
    }
}
