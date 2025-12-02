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

@WebServlet("/search")
public class SearchServlet extends HttpServlet {
    private final NewsDAO newsDAO = new NewsDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取搜索关键词并查询
        String keyword = request.getParameter("keyword");
        List<News> searchNewsList = newsDAO.searchNews(keyword);
        List<Category> categoryList = categoryDAO.findAllCategories();

        // 传递数据到页面
        request.setAttribute("categoryList", categoryList);
        request.setAttribute("searchNewsList", searchNewsList);
        request.setAttribute("keyword", keyword);

        // 转发到搜索页
        request.getRequestDispatcher("/pages/search.jsp").forward(request, response);
    }
}