package org.example.shopping_group_r1.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.shopping_group_r1.util.HttpClientUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "AdDataForwardServlet", value = "/ad-data-forward")
public class AdDataForwardServlet extends HttpServlet {

    @Override

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. 设置响应头
        response.setContentType("application/json;charset=UTF-8");

        // 【新增】输出请求来源信息
        System.out.println("=== AdDataForwardServlet 收到请求 ===");
        System.out.println("请求方法:  " + request.getMethod());
        System.out.println("请求URL: " + request.getRequestURL());
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Content-Length: " + request.getContentLength());

        // 2. 读取前端发送的JSON数据
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(request. getInputStream(), StandardCharsets.UTF_8));
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        reader.close();

        String requestData = requestBody.toString();
        System.out.println("收到用户兴趣数据：" + requestData);
        System.out.println("数据长度：" + requestData.length() + " 字节");

        // 【新增】验证JSON格式
        if (requestData.isEmpty()) {
            System.err.println("错误：请求体为空！");
            response.getWriter().write("{\"code\": 400,\"msg\":\"请求体为空\"}");
            return;
        }

        // 3. 转发数据到广告管理API并返回结果
        try {
            System.out.println("准备转发到广告API...");
            String adApiResponse = HttpClientUtil.sendPostRequest(requestData);
            System.out. println("广告API响应成功：" + adApiResponse);
            response.getWriter().write(adApiResponse);
        } catch (Exception e) {
            System.err.println("广告API调用失败：");
            e.printStackTrace();
            response.getWriter().write("{\"code\":200,\"msg\":\"数据已记录，广告服务器暂不可用\"}");
        }
        System.out.println("=== 请求处理完成 ===\n");
    }
}
