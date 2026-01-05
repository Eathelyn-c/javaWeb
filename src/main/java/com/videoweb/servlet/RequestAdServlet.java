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
import java.util.Map;

// 接收前端广告请求并转发到广告投放系统的API接口（播放时获取广告）
@WebServlet("/requestAd")
public class RequestAdServlet extends HttpServlet {
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

            if (jsonStr.isEmpty()) {
                out.write("{\"code\":400,\"msg\":\"请求体不能为空\"}");
                return;
            }

            // 3. 解析前端数据
            Gson gson = new Gson();
            @SuppressWarnings("unchecked")
            Map<String, Object> frontData = gson.fromJson(jsonStr, Map.class);

            // 提取字段：tag, platform, anonymousUserId, score（固定为0）
            String tag = (String) frontData.get("tag");
            String platform = (String) frontData.get("platform");
            String anonymousUserId = (String) frontData.get("anonymousUserId");
            
            // 4. 校验必需字段
            if (tag == null || platform == null) {
                out.write("{\"code\":400,\"msg\":\"参数缺失：tag（标签）和platform（平台）不能为空\"}");
                return;
            }

            // 5. 构造转发给广告投放系统的数据（按照API文档格式，score固定为0）
            Map<String, Object> adRequestData = new HashMap<>();
            adRequestData.put("tag", tag);                    // 视频标签
            adRequestData.put("platform", platform);         // 平台（固定为"video"）
            if (anonymousUserId != null && !anonymousUserId.isEmpty()) {
                adRequestData.put("anonymousUserId", anonymousUserId); // 匿名用户ID（可选）
            }
            adRequestData.put("score", 0);                   // 固定为0（获取广告时）
            // 注意：不包括limit参数

            String adRequestJson = gson.toJson(adRequestData);
            
            System.out.println("===== 准备请求广告（播放时）=====");
            System.out.println("转换后的数据: " + adRequestJson);
            System.out.println("==============================");

            // 6. 调用广告投放系统API，请求广告
            String adResponse = HttpUtil.requestAds(adRequestJson);
            
            System.out.println("===== 广告平台响应（广告列表）=====");
            System.out.println(adResponse);
            System.out.println("================================");

            // 7. 将广告系统的响应原样返回给前端
            out.write(adResponse);

        } catch (JsonParseException e) {
            out.write("{\"code\":400,\"msg\":\"JSON格式错误：" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            out.write("{\"code\":500,\"msg\":\"请求广告失败：" + e.getMessage() + "\"}");
        }
    }

    // 处理GET请求（用于测试，返回提示信息）
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().write("{\"code\":400,\"msg\":\"此接口仅支持POST请求，请使用POST方法提交数据\"}");
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

