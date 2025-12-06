package com.admanagement.servlet;

import com.admanagement.service.UserBehaviorService;
import com.admanagement.util.ResponseUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户行为API接口 - 处理HTTP请求
 */
@WebServlet("/api/behavior/*")
public class UserBehaviorServlet extends HttpServlet {
    private UserBehaviorService userBehaviorService = new UserBehaviorService();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/track")) {
            trackBehavior(req, resp);
        } else {
            ResponseUtil.sendError(resp, 404, "接口不存在");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            ResponseUtil.sendError(resp, 400, "请指定查询操作");
            return;
        }

        if (pathInfo.startsWith("/stats/user/")) {
            String userId = pathInfo.substring("/stats/user/".length());
            getUserBehaviorStats(userId, req, resp);
        } else if (pathInfo.startsWith("/stats/tag/")) {
            String tag = pathInfo.substring("/stats/tag/".length());
            getTagStatistics(tag, req, resp);
        } else if (pathInfo.startsWith("/stats/platform/")) {
            String platform = pathInfo.substring("/stats/platform/".length());
            getPlatformStatistics(platform, req, resp);
        } else if (pathInfo.equals("/overall") || pathInfo.equals("/overall/")) {
            getOverallStatistics(req, resp);
        } else {
            ResponseUtil.sendError(resp, 404, "接口不存在");
        }
    }

    /**
     * 记录用户行为
     */
    private void trackBehavior(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 读取请求体
            String requestBody = req.getReader().lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual);

            // 解析JSON
            JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);

            String anonymousUserId = jsonObject.has("anonymousUserId") ? 
                    jsonObject.get("anonymousUserId").getAsString() : null;
            String tag = jsonObject.has("tag") ? 
                    jsonObject.get("tag").getAsString() : null;
            int score = jsonObject.has("score") ? 
                    jsonObject.get("score").getAsInt() : 0;
            String platform = jsonObject.has("platform") ? 
                    jsonObject.get("platform").getAsString() : null;

            // 调用服务层
            Map<String, Object> result = userBehaviorService.recordUserBehavior(
                    anonymousUserId, tag, score, platform);

            // 返回响应
            int code = (int) result.get("code");
            if (code == 200) {
                ResponseUtil.sendSuccess(resp, result.get("data"), (String) result.get("msg"));
            } else {
                ResponseUtil.sendError(resp, code, (String) result.get("msg"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(resp, 500, "处理请求失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户行为统计
     */
    private void getUserBehaviorStats(String userId, HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        try {
            Map<String, Object> result = userBehaviorService.getUserBehaviorStatistics(userId);
            int code = (int) result.get("code");
            if (code == 200) {
                ResponseUtil.sendSuccess(resp, result, (String) result.get("msg"));
            } else {
                ResponseUtil.sendError(resp, code, (String) result.get("msg"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(resp, 500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取标签统计
     */
    private void getTagStatistics(String tag, HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        try {
            Map<String, Object> result = userBehaviorService.getTagStatistics(tag);
            int code = (int) result.get("code");
            if (code == 200) {
                ResponseUtil.sendSuccess(resp, result, (String) result.get("msg"));
            } else {
                ResponseUtil.sendError(resp, code, (String) result.get("msg"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(resp, 500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取平台统计
     */
    private void getPlatformStatistics(String platform, HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        try {
            Map<String, Object> result = userBehaviorService.getPlatformStatistics(platform);
            int code = (int) result.get("code");
            if (code == 200) {
                ResponseUtil.sendSuccess(resp, result, (String) result.get("msg"));
            } else {
                ResponseUtil.sendError(resp, code, (String) result.get("msg"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(resp, 500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取整体统计
     */
    private void getOverallStatistics(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        try {
            Map<String, Object> result = userBehaviorService.getOverallStatistics();
            ResponseUtil.sendSuccess(resp, result, "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(resp, 500, "查询失败: " + e.getMessage());
        }
    }
}
