package org.example.shopping_group_r1.controller;

import org.example.shopping_group_r1.model.Product;
import org.example.shopping_group_r1.model.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "ProductSearchServlet", value = "/product-search")
public class ProductSearchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String keyword = request.getParameter("keyword");

        if (keyword == null || keyword.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        ProductDAO productDAO = new ProductDAO();
        List<Product> searchResult = productDAO.searchProductsByKeyword(keyword.trim());

        request.setAttribute("searchResult", searchResult);
        request.setAttribute("keyword", keyword.trim());
        request.getRequestDispatcher("/search_result.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}