package com.videoweb.servlet;

import com.videoweb.dao.VideoDao;
import com.videoweb.model.Video;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
@WebServlet("/videoList")
public class VideoListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=utf-8");

        VideoDao dao = new VideoDao();
        List<Video> list = dao.getVideoList(1, 1000); // 获取前1000条

        // 根据请求端 IP 拼接完整 URL
        String clientIP = request.getLocalAddr(); // 获取服务器 IP（Tomcat 所在主机）
        String baseURL = "http://" + clientIP + ":" + request.getServerPort() + "/video_website_war_exploded";

        for (Video v : list) {
            v.setCoverPath(baseURL + "/images/" + v.getCoverPath());
            v.setVideoPath(baseURL + "/videos/" + v.getVideoPath());
        }

        String json = new Gson().toJson(list);
        response.getWriter().write(json);
    }
}
