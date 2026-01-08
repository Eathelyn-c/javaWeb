package com.news.dao;

import com.news.entity.News;
import com.news.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NewsDAO {
    // 首页新闻（分页版+恢复按时间排序，移除随机）核心修改
    public List<News> findIndexNewsByPage(int pageNum, int pageSize) {
        List<News> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;
        // 核心修改：删除 ORDER BY RAND()，恢复 ORDER BY n.create_time DESC（按发布时间倒序）
        String sql = "SELECT n.*, c.category_name FROM news_info n " +
                "JOIN news_category c ON n.category_id = c.category_id " +
                "ORDER BY n.create_time DESC LIMIT ?, ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, offset);
            stmt.setInt(2, pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToNews(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询首页新闻失败", e);
        }
        return list;
    }

    // 以下所有方法保持不变（完整保留）
    public int findIndexNewsTotalCount() {
        String sql = "SELECT COUNT(*) FROM news_info";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询首页新闻总数失败", e);
        }
        return 0;
    }

    public List<News> findByCategoryIdByPage(int categoryId, int pageNum, int pageSize) {
        List<News> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT n.*, c.category_name FROM news_info n " +
                "JOIN news_category c ON n.category_id = c.category_id " +
                "WHERE n.category_id = ? ORDER BY n.create_time DESC LIMIT ?, ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            stmt.setInt(2, offset);
            stmt.setInt(3, pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToNews(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询分类新闻失败", e);
        }
        return list;
    }

    public int findCategoryNewsTotalCount(int categoryId) {
        String sql = "SELECT COUNT(*) FROM news_info WHERE category_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询分类新闻总数失败", e);
        }
        return 0;
    }

    public List<News> searchNewsByPage(String keyword, int pageNum, int pageSize) {
        List<News> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT n.*, c.category_name FROM news_info n " +
                "JOIN news_category c ON n.category_id = c.category_id " +
                "WHERE n.title LIKE ? OR n.content LIKE ? ORDER BY n.create_time DESC LIMIT ?, ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeKeyword = "%" + keyword + "%";
            stmt.setString(1, likeKeyword);
            stmt.setString(2, likeKeyword);
            stmt.setInt(3, offset);
            stmt.setInt(4, pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToNews(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("搜索新闻失败", e);
        }
        return list;
    }

    public int searchNewsTotalCount(String keyword) {
        String sql = "SELECT COUNT(*) FROM news_info WHERE title LIKE ? OR content LIKE ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String likeKeyword = "%" + keyword + "%";
            stmt.setString(1, likeKeyword);
            stmt.setString(2, likeKeyword);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询搜索新闻总数失败", e);
        }
        return 0;
    }

    public List<News> findIndexNews(int limit) {
        List<News> list = new ArrayList<>();
        String sql = "SELECT n.*, c.category_name FROM news_info n " +
                "JOIN news_category c ON n.category_id = c.category_id " +
                "ORDER BY n.create_time DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToNews(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询首页新闻失败", e);
        }
        return list;
    }

    public List<News> findByCategoryId(int categoryId) {
        List<News> list = new ArrayList<>();
        String sql = "SELECT n.*, c.category_name FROM news_info n " +
                "JOIN news_category c ON n.category_id = c.category_id " +
                "WHERE n.category_id = ? ORDER BY n.create_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToNews(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询分类新闻失败", e);
        }
        return list;
    }

    public List<News> searchNews(String keyword) {
        List<News> list = new ArrayList<>();
        String sql = "SELECT n.*, c.category_name FROM news_info n " +
                "JOIN news_category c ON n.category_id = c.category_id " +
                "WHERE n.title LIKE ? OR n.content LIKE ? ORDER BY n.create_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeKeyword = "%" + keyword + "%";
            stmt.setString(1, likeKeyword);
            stmt.setString(2, likeKeyword);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToNews(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("搜索新闻失败", e);
        }
        return list;
    }

    public News findNewsById(int newsId) {
        String sql = "SELECT n.*, c.category_name FROM news_info n " +
                "JOIN news_category c ON n.category_id = c.category_id " +
                "WHERE n.news_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newsId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    News news = mapToNews(rs);
                    List<String> paragraphs = new ArrayList<>();
                    for (String para : news.getContent().split("。")) {
                        if (!para.isEmpty()) paragraphs.add(para + "。");
                    }
                    news.setContentParagraphs(paragraphs);
                    return news;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询新闻详情失败", e);
        }
        return null;
    }

    private News mapToNews(ResultSet rs) throws SQLException {
        News news = new News();
        news.setNewsId(rs.getInt("news_id"));
        news.setTitle(rs.getString("title"));
        news.setCoverImg(rs.getString("cover_img"));
        news.setContent(rs.getString("content"));
        news.setCategoryId(rs.getInt("category_id"));
        news.setCreateTime(rs.getTimestamp("create_time"));
        news.setCategoryName(rs.getString("category_name"));
        return news;
    }
}