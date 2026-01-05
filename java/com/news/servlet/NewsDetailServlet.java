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

@WebServlet("/detail")
public class NewsDetailServlet extends HttpServlet {
    private final NewsDAO newsDAO = new NewsDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO(); // 新增：注入CategoryDAO

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取新闻ID并查询详情
        int newsId = Integer.parseInt(request.getParameter("id"));
        News news = newsDAO.findNewsById(newsId);
        if (news == null) {
            response.sendRedirect(request.getContextPath() + "/index");
            return;
        }

        // 新增：查询所有分类（导航栏需要）
        List<Category> categoryList = categoryDAO.findAllCategories();
        // 新增：传递当前新闻所属分类ID（详情页高亮对应分类）
        request.setAttribute("currentCategoryId", news.getCategoryId());
        request.setAttribute("categoryList", categoryList); // 导航栏需要分类列表
        request.setAttribute("news", news);

        // 转发到详情页
        request.getRequestDispatcher("/pages/detail.jsp").forward(request, response);
    }
}