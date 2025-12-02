package com.news.servlet;

import com.news.dao.NewsDAO;
import com.news.entity.News;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/detail")
public class NewsDetailServlet extends HttpServlet {
    private final NewsDAO newsDAO = new NewsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取新闻ID并查询详情
        int newsId = Integer.parseInt(request.getParameter("id"));
        News news = newsDAO.findNewsById(newsId);
        if (news == null) { // 新闻不存在时跳回首页
            response.sendRedirect(request.getContextPath() + "/index");
            return;
        }
        // 传递数据到页面
        request.setAttribute("news", news);

        // 转发到详情页
        request.getRequestDispatcher("/pages/detail.jsp").forward(request, response);
    }
}
