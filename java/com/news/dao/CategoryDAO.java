package com.news.dao;

import com.news.entity.Category;
import com.news.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    // 查询所有分类
    public List<Category> findAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM news_category ORDER BY category_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setCategoryCode(rs.getString("category_code"));
                category.setCategoryName(rs.getString("category_name"));
                list.add(category);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询分类失败", e);
        }
        return list;
    }

    // 根据ID查询分类
    public Category findCategoryById(int categoryId) {
        String sql = "SELECT * FROM news_category WHERE category_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category();
                    category.setCategoryId(rs.getInt("category_id"));
                    category.setCategoryCode(rs.getString("category_code"));
                    category.setCategoryName(rs.getString("category_name"));
                    return category;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询分类失败", e);
        }
        return null;
    }
}