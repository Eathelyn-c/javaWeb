package com.news.servlet;

import com.news.dao.CategoryDAO;
import com.news.dao.NewsDAO;
import com.news.entity.Category;
import com.news.entity.News;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@WebServlet("/index") // 注解配置访问路径
public class IndexServlet extends HttpServlet {
    private final NewsDAO newsDAO = new NewsDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 查询分类和新闻
        List<Category> categoryList = categoryDAO.findAllCategories();
        List<News> newsList = newsDAO.findIndexNews(10);

        // 生成随机广告位置（2~列表长度-2）
        int adPosition = 0;
        if (newsList.size() >= 5) {
            adPosition = new Random().nextInt(newsList.size() - 4) + 2;
        }

        // 传递数据到页面
        request.setAttribute("categoryList", categoryList);
        request.setAttribute("newsList", newsList);
        request.setAttribute("adPosition", adPosition);

        // 转发到首页
        request.getRequestDispatcher("/pages/index.jsp").forward(request, response);
    }
}