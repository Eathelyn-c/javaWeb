package com.videoweb.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

// HTTP请求工具类：用于调用广告投放系统的API接口
public class HttpUtil {
    // 广告投放系统的API地址
    private static final String AD_API_URL = "http://10.100.164.6:8080/ad-management/api/external/api/getAds";
    
    /**
     * 发送JSON格式的POST请求到广告投放系统（推送用户观看信息）
     * @param jsonData 要发送的JSON数据
     * @return 广告系统的响应结果
     */
    public static String sendPostJson(String jsonData) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();

        // ========== 日志：开始请求 ==========
        System.out.println("========================================");
        System.out.println("[HttpUtil] 开始请求广告平台API");
        System.out.println("[HttpUtil] API地址: " + AD_API_URL);
        System.out.println("[HttpUtil] 请求数据: " + jsonData);
        System.out.println("========================================");

        try {
            URL url = new URL(AD_API_URL);
            conn = (HttpURLConnection) url.openConnection();
            // 设置请求方式和头信息（按照API文档要求）
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(5000); // 连接超时5秒
            conn.setReadTimeout(5000);    // 读取超时5秒

            // 发送JSON数据
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            System.out.println("[HttpUtil] 数据已发送");

            // 读取响应
            int responseCode = conn.getResponseCode();
            System.out.println("[HttpUtil] 响应状态码: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                System.out.println("[HttpUtil] ✅ 请求成功！响应内容: " + response.toString());
            } else {
                // 非200响应，读取错误信息
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                System.out.println("[HttpUtil] ⚠️ 请求失败（状态码: " + responseCode + "），错误信息: " + response.toString());
            }
        } catch (IOException e) {
            System.out.println("[HttpUtil] ❌ 请求异常: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return "{\"code\":500,\"msg\":\"调用广告投放API失败：" + e.getMessage() + "\"}";
        } finally {
            try {
                if (reader != null) reader.close();
                if (conn != null) conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[HttpUtil] 请求完成");
            System.out.println("========================================");
        }
        return response.toString();
    }
    
    /**
     * 请求广告：调用广告投放系统的getAds接口获取广告
     * @param jsonData 请求参数JSON（包含tag, platform, anonymousUserId, score=0等）
     * @return 广告系统的响应结果（包含广告列表）
     */
    public static String requestAds(String jsonData) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();

        // ========== 日志：开始请求广告 ==========
        System.out.println("========================================");
        System.out.println("[HttpUtil] 开始请求广告");
        System.out.println("[HttpUtil] API地址: " + AD_API_URL);
        System.out.println("[HttpUtil] 请求数据: " + jsonData);
        System.out.println("========================================");

        try {
            URL url = new URL(AD_API_URL);
            conn = (HttpURLConnection) url.openConnection();
            // 设置请求方式和头信息（按照API文档要求）
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(3000); // 连接超时3秒（广告请求要快）
            conn.setReadTimeout(3000);    // 读取超时3秒

            // 发送JSON数据
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            System.out.println("[HttpUtil] 广告请求数据已发送");

            // 读取响应
            int responseCode = conn.getResponseCode();
            System.out.println("[HttpUtil] 广告请求响应状态码: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                System.out.println("[HttpUtil] ✅ 广告请求成功！响应内容: " + response.toString());
            } else {
                // 非200响应，读取错误信息
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                System.out.println("[HttpUtil] ⚠️ 广告请求失败（状态码: " + responseCode + "），错误信息: " + response.toString());
            }
        } catch (IOException e) {
            System.out.println("[HttpUtil] ❌ 广告请求异常: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return "{\"code\":500,\"msg\":\"请求广告API失败：" + e.getMessage() + "\"}";
        } finally {
            try {
                if (reader != null) reader.close();
                if (conn != null) conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[HttpUtil] 广告请求完成");
            System.out.println("========================================");
        }
        return response.toString();
    }
}