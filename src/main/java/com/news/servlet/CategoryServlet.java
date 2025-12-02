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

@WebServlet("/category")
public class CategoryServlet extends HttpServlet {
    private final NewsDAO newsDAO = new NewsDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取分类ID并查询数据
        int categoryId = Integer.parseInt(request.getParameter("categoryId"));
        Category category = categoryDAO.findCategoryById(categoryId);
        List<News> newsList = newsDAO.findByCategoryId(categoryId);
        List<Category> categoryList = categoryDAO.findAllCategories();

        // 生成随机广告位置
        int adPosition = 0;
        if (newsList.size() >= 5) {
            adPosition = new Random().nextInt(newsList.size() - 4) + 2;
        }

        // 传递数据到页面
        request.setAttribute("categoryList", categoryList);
        request.setAttribute("newsList", newsList);
        request.setAttribute("currentCategory", category);
        request.setAttribute("adPosition", adPosition);

        // 转发到分类页
        request.getRequestDispatcher("/pages/category.jsp").forward(request, response);
    }
}