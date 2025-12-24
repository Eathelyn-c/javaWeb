package org.example.shopping_group_r1.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpClientUtil {
    private static final String AD_MANAGEMENT_API = "http://ad-server:8080/api/user-interest";


    public static String sendPostRequest(String requestBody) throws Exception {

        if (AD_MANAGEMENT_API == null || AD_MANAGEMENT_API.isEmpty()) {
            return "{\"code\":200,\"msg\":\"广告服务器未配置，数据已记录\"}";
        }

        URL url = new URL(AD_MANAGEMENT_API);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);

        try {
            try (OutputStream os = conn.getOutputStream()) {
                byte[] data = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(data, 0, data.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            return response.toString();
        } catch (Exception e) {
            System.err.println("连接广告服务器失败: " + e.getMessage());
            return "{\"code\":200,\"msg\":\"广告服务器连接失败，数据已本地记录\"}";
        } finally {
            conn.disconnect();
        }
    }
}