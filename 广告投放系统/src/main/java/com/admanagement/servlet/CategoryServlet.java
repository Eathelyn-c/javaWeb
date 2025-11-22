package com.admanagement.servlet;

import com.admanagement.model.Category;
import com.admanagement.service.AdService;
import com.admanagement.util.ResponseUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Category Servlet - handles category operations
 */
@WebServlet("/api/categories/*")
public class CategoryServlet extends HttpServlet {
    private AdService adService;

    @Override
    public void init() throws ServletException {
        adService = new AdService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            List<Category> categories = adService.getAllCategories();
            ResponseUtil.sendSuccess(response, categories);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.sendError(response, "Failed to get categories: " + e.getMessage());
        }
    }
}
