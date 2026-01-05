package org.example.shopping_group_r1.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.shopping_group_r1.util.HttpClientUtil;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "AdFetchServlet", value = "/ad-fetch")
public class AdFetchServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            System.out.println("=== AdFetchServlet 开始获取广告 ===");

            // 调用广告API获取广告数据
            String adApiResponse = HttpClientUtil.fetchAds();

            System.out.println("广告API原始响应: " + adApiResponse);

            // 直接返回广告API的响应
            out.write(adApiResponse);

            System.out.println("=== AdFetchServlet 处理完成 ===\n");

        } catch (Exception e) {
            System.err.println("获取广告失败: " + e.getMessage());
            e.printStackTrace();

            // 返回错误信息
            out.write("{\"code\":500,\"msg\":\"获取广告失败\",\"ads\":[]}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 允许GET请求，调用POST处理
        doPost(request, response);
    }
}