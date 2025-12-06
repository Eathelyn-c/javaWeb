package com.videoweb.servlet;

import com.videoweb.dao.VideoDao;
import com.videoweb.model.Video;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// 视频播放页入口：根据视频ID查询视频信息，传递给播放页JSP
@WebServlet("/play")
public class VideoPlayServlet extends HttpServlet {
    private VideoDao videoDao = new VideoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 获取视频ID（从URL参数中获取）
        String videoIdStr = request.getParameter("id");
        if (videoIdStr == null || videoIdStr.isEmpty()) {
            // 无ID参数，跳回首页
            response.sendRedirect(request.getContextPath() + "/index");
            return;
        }

        // 2. 解析ID并查询视频
        int videoId;
        try {
            videoId = Integer.parseInt(videoIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/index");
            return;
        }

        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            // 视频不存在，跳回首页
            response.sendRedirect(request.getContextPath() + "/index");
            return;
        }

        // 3. 将视频信息传递给播放页JSP
        request.setAttribute("video", video);
        request.getRequestDispatcher("/play.jsp").forward(request, response);
    }
}