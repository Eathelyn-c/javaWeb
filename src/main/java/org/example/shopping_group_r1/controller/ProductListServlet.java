
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

@WebServlet(name = "ProductListServlet", value = "/product-list")
public class ProductListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        String category = request.getParameter("category");
        ProductDAO productDAO = new ProductDAO();
        List<Product> productList;

        if (category == null || category.isEmpty()) {
            productList = productDAO.findAllProducts();
        } else {
            productList = productDAO.findProductsByCategory(category);
        }

        request.setAttribute("productList", productList);
        request.setAttribute("currentCategory", category);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}