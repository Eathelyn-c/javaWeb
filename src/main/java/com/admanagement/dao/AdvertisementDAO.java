package com.admanagement.dao;

import com.admanagement.model.Advertisement;
import com.admanagement.model.Advertisement.AdStatus;
import com.admanagement.model.Advertisement.AdType;
import com.admanagement.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 广告数据访问对象 (DAO)
 * 集成了基础 CRUD、热门权重排序及标签推荐逻辑
 */
public class AdvertisementDAO {

    /**
     * 获取全平台表现最好的广告（冷启动/兜底逻辑）
     * 逻辑：关联统计表，按点击量 (clicks) 降序排列
     */
    public List<Advertisement> getTopPerformantAds(int limit) {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username, s.clicks " +
                "FROM advertisements a " +
                "JOIN categories c ON a.category_id = c.category_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "LEFT JOIN ad_statistics s ON a.ad_id = s.ad_id " +
                "WHERE a.status = 'active' " +
                "ORDER BY s.clicks DESC, a.created_at DESC " +
                "LIMIT ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ads.add(extractAdvertisementFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ads;
    }

    /**
     * 根据标签（分类名）进行精准匹配
     * 逻辑：匹配 category_name，并按该分类下的点击量排序
     */
    public List<Advertisement> getAdvertisementsByTagAndScore(String tag, int limit) {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username, s.clicks " +
                "FROM advertisements a " +
                "JOIN categories c ON a.category_id = c.category_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "LEFT JOIN ad_statistics s ON a.ad_id = s.ad_id " +
                "WHERE c.category_name = ? AND a.status = 'active' " +
                "ORDER BY s.clicks DESC, a.created_at DESC " +
                "LIMIT ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tag);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ads.add(extractAdvertisementFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ads;
    }

    /**
     * 创建新广告
     */
    public boolean createAdvertisement(Advertisement ad) {
        String sql = "INSERT INTO advertisements (user_id, category_id, title, description, ad_type, " +
                "text_content, image_url, video_url, target_url, status, start_date, end_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, ad.getUserId());
            stmt.setInt(2, ad.getCategoryId());
            stmt.setString(3, ad.getTitle());
            stmt.setString(4, ad.getDescription());
            stmt.setString(5, ad.getAdType().getValue());
            stmt.setString(6, ad.getTextContent());
            stmt.setString(7, ad.getImageUrl());
            stmt.setString(8, ad.getVideoUrl());
            stmt.setString(9, ad.getTargetUrl());
            stmt.setString(10, ad.getStatus().getValue());
            stmt.setTimestamp(11, ad.getStartDate());
            stmt.setTimestamp(12, ad.getEndDate());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        ad.setAdId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据 ID 获取广告详情
     */
    public Advertisement getAdvertisementById(int adId) {
        String sql = "SELECT a.*, c.category_name, u.username, u.company_name " +
                "FROM advertisements a " +
                "JOIN categories c ON a.category_id = c.category_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "WHERE a.ad_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAdvertisementFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定用户的广告列表
     */
    public List<Advertisement> getAdvertisementsByUserId(int userId) {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username " +
                "FROM advertisements a " +
                "JOIN categories c ON a.category_id = c.category_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "WHERE a.user_id = ? AND a.status != 'deleted' " +
                "ORDER BY a.created_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ads.add(extractAdvertisementFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ads;
    }

    /**
     * 更新广告状态
     */
    public boolean updateAdvertisementStatus(int adId, AdStatus status) {
        String sql = "UPDATE advertisements SET status = ? WHERE ad_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.getValue());
            stmt.setInt(2, adId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据状态获取广告列表
     */
    public List<Advertisement> getAdvertisementsByStatus(AdStatus status) {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username FROM advertisements a " +
                "JOIN categories c ON a.category_id = c.category_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "WHERE a.status = ? ORDER BY a.created_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.getValue());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ads.add(extractAdvertisementFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ads;
    }

    /**
     * 辅助方法：从 ResultSet 提取对象
     */
    private Advertisement extractAdvertisementFromResultSet(ResultSet rs) throws SQLException {
        Advertisement ad = new Advertisement();
        ad.setAdId(rs.getInt("ad_id"));
        ad.setUserId(rs.getInt("user_id"));
        ad.setCategoryId(rs.getInt("category_id"));
        ad.setTitle(rs.getString("title"));
        ad.setDescription(rs.getString("description"));
        ad.setAdType(AdType.fromValue(rs.getString("ad_type")));
        ad.setTextContent(rs.getString("text_content"));
        ad.setImageUrl(rs.getString("image_url"));
        ad.setVideoUrl(rs.getString("video_url"));
        ad.setTargetUrl(rs.getString("target_url"));
        ad.setStatus(AdStatus.fromValue(rs.getString("status")));
        ad.setCreatedAt(rs.getTimestamp("created_at"));
        ad.setStartDate(rs.getTimestamp("start_date"));
        ad.setEndDate(rs.getTimestamp("end_date"));

        // 尝试获取关联字段
        try {
            ad.setCategoryName(rs.getString("category_name"));
            ad.setUsername(rs.getString("username"));
        } catch (SQLException ignored) {}

        return ad;
    }
    // 1. 根据分类获取广告
    public List<Advertisement> getAdvertisementsByCategory(int categoryId) {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username FROM advertisements a " +
                "JOIN categories c ON a.category_id = c.category_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "WHERE a.category_id = ? AND a.status != 'deleted'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ads.add(extractAdvertisementFromResultSet(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ads;
    }

    // 2. 更新广告信息
    public boolean updateAdvertisement(Advertisement ad) {
        String sql = "UPDATE advertisements SET title=?, description=?, ad_type=?, " +
                "text_content=?, image_url=?, video_url=?, target_url=?, " +
                "start_date=?, end_date=? WHERE ad_id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ad.getTitle());
            stmt.setString(2, ad.getDescription());
            stmt.setString(3, ad.getAdType().getValue());
            stmt.setString(4, ad.getTextContent());
            stmt.setString(5, ad.getImageUrl());
            stmt.setString(6, ad.getVideoUrl());
            stmt.setString(7, ad.getTargetUrl());
            stmt.setTimestamp(8, ad.getStartDate());
            stmt.setTimestamp(9, ad.getEndDate());
            stmt.setInt(10, ad.getAdId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 3. 删除广告 (软删除逻辑)
    public boolean deleteAdvertisement(int adId) {
        String sql = "UPDATE advertisements SET status = 'deleted' WHERE ad_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * 1. 获取所有活跃广告 (供 RecommendAdService 兜底使用)
     */
    public List<Advertisement> getAllAdvertisements() {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username FROM advertisements a " +
                "JOIN categories c ON a.category_id = c.category_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "WHERE a.status = 'active' ORDER BY a.created_at DESC";
        try (Connection conn = com.admanagement.util.DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ads.add(extractAdvertisementFromResultSet(rs));
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return ads;
    }

    /**
     * 2. 根据标签名称查询广告 (供 RecommendAdService 精准匹配使用)
     */
    public List<Advertisement> getAdvertisementsByCategoryName(String categoryName) {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username FROM advertisements a " +
                "JOIN categories c ON a.category_id = c.category_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "WHERE c.category_name = ? AND a.status = 'active'";
        try (Connection conn = com.admanagement.util.DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ads.add(extractAdvertisementFromResultSet(rs));
                }
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return ads;
    }
}