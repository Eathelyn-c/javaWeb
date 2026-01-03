package org.example.shopping_group_r1.controller;

import org.example.shopping_group_r1.model.Product;
import org.example.shopping_group_r1.model.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ProductDetailServlet", value = "/product-detail")
public class ProductDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        Integer productId;
        try {
            productId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        ProductDAO productDAO = new ProductDAO();
        Product product = productDAO.findProductById(productId);

        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        request.setAttribute("product", product);

        request.getRequestDispatcher("/product_detail.jsp").forward(request, response);
    }
}
