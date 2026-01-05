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
        // 获取搜索关键词
        String keyword = request.getParameter("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = "";
        }
        // 分页参数：默认第1页，每页11条
        int pageNum = 1;
        int pageSize = 11;
        try {
            if (request.getParameter("pageNum") != null) {
                pageNum = Integer.parseInt(request.getParameter("pageNum"));
            }
            if (request.getParameter("pageSize") != null) {
                pageSize = Integer.parseInt(request.getParameter("pageSize"));
            }
        } catch (NumberFormatException e) {
            pageNum = 1;
            pageSize = 11;
        }

        // 查询分页搜索结果和总数
        List<News> searchNewsList = newsDAO.searchNewsByPage(keyword, pageNum, pageSize);
        int totalCount = newsDAO.searchNewsTotalCount(keyword);
        int totalPage = (totalCount + pageSize - 1) / pageSize;

        // 传递数据到页面
        request.setAttribute("categoryList", categoryDAO.findAllCategories());
        request.setAttribute("searchNewsList", searchNewsList);
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentCategoryId", -1);
        request.setAttribute("pageNum", pageNum);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("totalPage", totalPage);

        // 转发到搜索页
        request.getRequestDispatcher("/pages/search.jsp").forward(request, response);
    }
}