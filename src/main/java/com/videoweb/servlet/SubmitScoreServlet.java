package com.videoweb.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.videoweb.util.HttpUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 接收前端分数并转发到广告投放系统的API接口
@WebServlet("/submitScore")
public class SubmitScoreServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 基础配置：响应格式 + 跨域头
        response.setContentType("application/json;charset=utf-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        request.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        try {
            // 2. 读取前端提交的JSON数据
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String jsonStr = sb.toString();
            // ========== 新增：打印前端原始数据 ==========
            System.out.println("===== 前端提交的原始JSON数据 =====");
            System.out.println(jsonStr);
            System.out.println("==================================");

            if (jsonStr.isEmpty()) {
                out.write("{\"code\":400,\"msg\":\"请求体不能为空\"}");
                return;
            }

            // 3. 解析前端数据，仅保留核心字段
            Gson gson = new Gson();
            @SuppressWarnings("unchecked")
            Map<String, Object> frontData = gson.fromJson(jsonStr, Map.class);

            // 提取核心字段：匿名用户ID、视频标签、分数、广告点击统计数组
            String anonymousUserId = (String) frontData.get("anonymousUserId");
            String tag = (String) frontData.get("tag");
            Integer score = (frontData.get("score") instanceof Double)
                    ? ((Double) frontData.get("score")).intValue()
                    : (Integer) frontData.get("score");
            List<Map<String, Object>> adClicks = null;
            if (frontData.get("adClicks") != null && frontData.get("adClicks") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> tempAdClicks = (List<Map<String, Object>>) frontData.get("adClicks");
                adClicks = tempAdClicks;
            }

            // 4. 校验核心字段
            if (anonymousUserId == null || tag == null || score == null) {
                out.write("{\"code\":400,\"msg\":\"参数缺失：anonymousUserId（匿名ID）/tag（标签）/score（分数）不能为空\"}");
                return;
            }

            // 5. 构造转发给广告投放系统的数据（按照API文档格式）
            Map<String, Object> adData = new HashMap<>();
            adData.put("tag", tag);                           // 视频标签
            String platform = (String) frontData.get("platform");
            if (platform != null && !platform.isEmpty()) {
                adData.put("platform", platform);             // 平台
            } else {
                adData.put("platform", "video");              // 如果前端未传，默认使用"video"
            }
            adData.put("anonymousUserId", anonymousUserId);  // 匿名用户ID（可选，但我们会传）
            adData.put("score", score);                       // 行为分数（1/2/3，可选，但我们会传）
            if (adClicks != null && !adClicks.isEmpty()) {
                adData.put("adClicks", adClicks);            // 广告点击统计数组（可选）
            }
            // 注意：不包括limit参数
            String adJsonData = gson.toJson(adData);
            
            System.out.println("===== 准备转发到广告平台 =====");
            System.out.println("转换后的数据: " + adJsonData);
            System.out.println("==============================");

            // 6. 调用广告投放系统API，转发数据
            String adResponse = HttpUtil.sendPostJson(adJsonData);
            
            System.out.println("===== 广告平台响应 =====");
            System.out.println(adResponse);
            System.out.println("========================");

            // 7. 将广告系统的响应返回给前端
            out.write(adResponse);

        } catch (JsonParseException e) {
            out.write("{\"code\":400,\"msg\":\"JSON格式错误：" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"code\":500,\"msg\":\"转发数据到广告系统失败：" + e.getMessage() + "\"}");
        }
    }

    // 处理跨域OPTIONS预检请求
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}