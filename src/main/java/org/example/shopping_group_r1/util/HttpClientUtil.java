package org.example.shopping_group_r1. util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net. URL;
import java.nio. charset.StandardCharsets;

public class HttpClientUtil {
    // 广告服务器API地址
    private static final String AD_MANAGEMENT_API = "http://10.100.164.6:8080/ad-management/api/external/api/getAds";

    // API Key
    private static final String API_KEY = "123456789abcdef";

    //发送POST请求到广告服务器（用户兴趣数据 + 广告点击数据）
    public static String sendPostRequest(String requestBody) throws Exception {
        System.out.println("\n========== 开始转发数据到广告服务器 ==========");
        System.out.println("目标API:  " + AD_MANAGEMENT_API);
        System.out.println("发送的JSON数据: " + requestBody);

        if (AD_MANAGEMENT_API == null || AD_MANAGEMENT_API.isEmpty()) {
            System.err.println(" 广告服务器未配置");
            return "{\"code\": 200,\"msg\": \"广告服务器未配置\"}";
        }

        URL url = new URL(AD_MANAGEMENT_API);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // 设置请求方法和请求头
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        // 添加 X-API-Key 请求头
        // conn.setRequestProperty("X-API-Key", API_KEY);

        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try {
            // 发送请求体
            try (OutputStream os = conn.getOutputStream()) {
                byte[] data = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(data, 0, data.length);
                os.flush();
                System.out.println("✓ 数据已发送，字节数: " + data.length);
            }

            // 获取响应状态码
            int responseCode = conn. getResponseCode();
            String responseMessage = conn.getResponseMessage();
            System.out.println("响应状态:  " + responseCode + " " + responseMessage);

            // 如果是错误响应
            if (responseCode >= 400) {
                StringBuilder errorResponse = new StringBuilder();

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                } catch (Exception e) {
                    errorResponse.append("无法读取错误响应:  " + e.getMessage());
                }

                String errorDetail = errorResponse.toString();
                System.err.println("广告服务器返回错误:");
                System.err.println("   HTTP状态码: " + responseCode);
                System.err.println("   错误详情: " + errorDetail);
                System.err.println("============================================\n");

                return "{\"code\":" + responseCode +
                        ",\"msg\":\"广告服务器错误(" + responseCode + "): " +
                        errorDetail. replace("\"", "'") + "\"}";
            }

            // 读取正常响应
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            String result = response.toString();
            System.out.println("广告服务器响应成功:  " + result);
            System.out.println("============================================\n");
            return result;

        } catch (Exception e) {
            System.err.println("连接广告服务器异常:");
            System.err.println("   异常类型: " + e. getClass().getName());
            System.err.println("   异常信息: " + e. getMessage());
            e.printStackTrace();
            System.err.println("============================================\n");
            return "{\"code\": 500,\"msg\":\"连接失败: " + e.getMessage() + "\"}";
        } finally {
            conn.disconnect();
        }
    }

    //获取广告数据
    public static String fetchAds() throws Exception {
        System. out.println("\n========== 开始获取广告数据 ==========");

        URL url = new URL(AD_MANAGEMENT_API);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try {
            // 发送包含 platform 参数的 JSON
            String requestBody = "{\"platform\":\"shopping\"}";
            System.out.println("发送请求体: " + requestBody);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 读取响应
            int code = conn.getResponseCode();
            System.out.println("响应状态码: " + code);

            InputStream is = (code < 400) ? conn.getInputStream() : conn.getErrorStream();

            if (is == null) {
                return "{\"code\":500,\"msg\":\"无响应\",\"ads\":[]}";
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            br.close();

            String response = result.toString();
            System.out.println("广告服务器响应: " + response);

            return response. isEmpty() ? "{\"code\":500,\"msg\":\"空响应\",\"ads\": []}" : response;

        } catch (Exception e) {
            System.err.println("获取广告异常:  " + e.getMessage());
            e.printStackTrace();
            return "{\"code\":500,\"msg\": \"连接失败\",\"ads\":[]}";
        } finally {
            conn. disconnect();
            System.out.println("========== 广告数据获取流程结束 ==========\n");
        }
    }
}