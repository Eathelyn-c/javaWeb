package com.admanagement.dao;

import com.admanagement.model.UserBehavior;
import com.admanagement.model.AdStatistics;
import com.admanagement.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户行为数据访问层 - 完美适配结算与请求流程
 */
public class UserBehaviorDAO {

    /**
     * 从外部API访问记录获取广告统计数据
     * 浏览量 = score=0 的记录数（外部平台获取广告的总次数）
     * 点击量 = score>0 的记录数（外部用户点击广告的总次数）
     */
    public AdStatistics getStatisticsByAdId(int adId) {
        String sql = "SELECT " +
                "COUNT(CASE WHEN score = 0 THEN 1 END) as view_count, " +
                "COUNT(CASE WHEN score > 0 THEN 1 END) as click_count " +
                "FROM user_behaviors WHERE ad_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    AdStatistics stats = new AdStatistics();
                    stats.setAdId(adId);
                    stats.setViewCount(rs.getInt("view_count"));
                    stats.setClickCount(rs.getInt("click_count"));
                    return stats;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // 如果没有记录，返回0统计
        AdStatistics stats = new AdStatistics();
        stats.setAdId(adId);
        stats.setViewCount(0);
        stats.setClickCount(0);
        return stats;
    }

    /**
     * 核心方法：记录用户行为
     * 每次API调用都单独记录一条新记录
     */
    public boolean recordUserBehavior(UserBehavior behavior) {
        String sql = "INSERT INTO user_behaviors (anonymous_user_id, tag, ad_id, score, platform, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, behavior.getAnonymousUserId());
            stmt.setString(2, behavior.getTag());
            if (behavior.getAdId() != null) {
                stmt.setInt(3, behavior.getAdId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setInt(4, behavior.getScore());
            stmt.setString(5, behavior.getPlatform());
            stmt.setTimestamp(6, behavior.getCreatedAt());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 核心方法：获取用户最感兴趣的标签 (适配点击请求流程)
     * 用于 score=0 时寻找推荐依据
     */
    public String getMostInterestedTag(String userId) {
        String sql = "SELECT tag FROM user_behaviors WHERE anonymous_user_id = ? " +
                "ORDER BY score DESC, created_at DESC LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("tag");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<UserBehavior> getBehaviorsByUserId(String anonymousUserId) {
        List<UserBehavior> behaviors = new ArrayList<>();
        String sql = "SELECT * FROM user_behaviors WHERE anonymous_user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, anonymousUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    behaviors.add(mapResultSetToBehavior(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return behaviors;
    }

    public Map<String, Object> getBehaviorStatistics() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT COUNT(*) as total_records, COUNT(DISTINCT anonymous_user_id) as unique_users, " +
                "AVG(score) as avg_score, MIN(score) as min_score, MAX(score) as max_score FROM user_behaviors";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                stats.put("total_records", rs.getLong("total_records"));
                stats.put("unique_users", rs.getLong("unique_users"));
                stats.put("avg_score", rs.getDouble("avg_score"));
                stats.put("min_score", rs.getInt("min_score"));
                stats.put("max_score", rs.getInt("max_score"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }

    private UserBehavior mapResultSetToBehavior(ResultSet rs) throws SQLException {
        return new UserBehavior(
                rs.getInt("behavior_id"),
                rs.getString("anonymous_user_id"),
                rs.getString("tag"),
                rs.getInt("score"),
                rs.getString("platform"),
                rs.getTimestamp("created_at")
        );
    }

    public boolean saveBehavior(UserBehavior behavior) {
        return recordUserBehavior(behavior);
    }

    // 2. 根据标签查询
    public List<UserBehavior> getBehaviorsByTag(String tag) {
        List<UserBehavior> behaviors = new ArrayList<>();
        String sql = "SELECT * FROM user_behaviors WHERE tag = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tag);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    behaviors.add(mapResultSetToBehavior(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return behaviors;
    }

    // 3. 统计标签分布（用于管理后台图表）
    public Map<String, Long> getTagStatistics() {
        Map<String, Long> stats = new HashMap<>();
        String sql = "SELECT tag, COUNT(*) as count FROM user_behaviors GROUP BY tag";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("tag"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // 4. 根据平台查询
    public List<UserBehavior> getBehaviorsByPlatform(String platform) {
        List<UserBehavior> behaviors = new ArrayList<>();
        String sql = "SELECT * FROM user_behaviors WHERE platform = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, platform);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    behaviors.add(mapResultSetToBehavior(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return behaviors;
    }
    /**
     * 统计投放平台的分布情况
     */
    public Map<String, Long> getPlatformStatistics() {
        Map<String, Long> stats = new HashMap<>();
        String sql = "SELECT platform, COUNT(*) as count FROM user_behaviors GROUP BY platform";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("platform"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}