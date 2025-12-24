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

        response.setContentType("application/json;charset=UTF-8");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        reader.close();

        System.out.println("收到用户兴趣数据：" + requestBody.toString());

        try {
            String adApiResponse = HttpClientUtil.sendPostRequest(requestBody.toString());
            response.getWriter().write(adApiResponse);
            System.out.println("广告API响应：" + adApiResponse);
        } catch (Exception e) {
            e.printStackTrace();

            response.getWriter().write("{\"code\":200,\"msg\":\"数据已记录，广告服务器暂不可用\"}");
        }
    }
}