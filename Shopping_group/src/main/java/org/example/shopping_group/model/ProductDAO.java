package org.example.shopping_group.model;

import org.example.shopping_group.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // 1. 查询所有商品（首页默认展示）
    public List<Product> findAllProducts() {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setCategory(rs.getString("category"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setDescription(rs.getString("description"));
                product.setImageUrl(rs.getString("image_url"));
                product.setStock(rs.getInt("stock"));
                productList.add(product);
            }

            System.out.println("DAO: 查询到 " + productList.size() + " 个商品");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("DAO: 查询所有商品时出错: " + e.getMessage());
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return productList;
    }

    // 2. 按类别查询商品（分类导航点击后）
    public List<Product> findProductsByCategory(String category) {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ? ORDER BY id DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setCategory(rs.getString("category"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setDescription(rs.getString("description"));
                product.setImageUrl(rs.getString("image_url"));
                product.setStock(rs.getInt("stock"));
                productList.add(product);
            }

            System.out.println("DAO: 按类别 '" + category + "' 查询到 " + productList.size() + " 个商品");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return productList;
    }

    // 3. 按ID查询商品（商品详情页）
    public Product findProductById(Integer id) {
        Product product = null;
        String sql = "SELECT * FROM products WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setCategory(rs.getString("category"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setDescription(rs.getString("description"));
                product.setImageUrl(rs.getString("image_url"));
                product.setStock(rs.getInt("stock"));
            }

            System.out.println("DAO: 按ID " + id + " 查询商品: " + (product != null ? "成功" : "未找到"));

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return product;
    }

    // 4. 按关键词搜索商品（搜索框提交后）
    public List<Product> searchProductsByKeyword(String keyword) {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ? ORDER BY id DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setCategory(rs.getString("category"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setDescription(rs.getString("description"));
                product.setImageUrl(rs.getString("image_url"));
                product.setStock(rs.getInt("stock"));
                productList.add(product);
            }

            System.out.println("DAO: 搜索关键词 '" + keyword + "' 找到 " + productList.size() + " 个商品");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return productList;
    }
}