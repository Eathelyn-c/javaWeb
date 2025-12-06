package com.admanagement.dao;

import com.admanagement.model.UserBehavior;
import com.admanagement.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户行为数据访问层 - 处理数据库操作
 */
public class UserBehaviorDAO {

    /**
     * 保存用户行为记录
     */
    public boolean saveBehavior(UserBehavior behavior) {
        String sql = "INSERT INTO user_behaviors (anonymous_user_id, tag, score, platform, created_at) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, behavior.getAnonymousUserId());
            stmt.setString(2, behavior.getTag());
            stmt.setInt(3, behavior.getScore());
            stmt.setString(4, behavior.getPlatform());
            stmt.setTimestamp(5, behavior.getCreatedAt());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        behavior.setBehaviorId(rs.getInt(1));
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
     * 根据匿名用户ID获取行为记录
     */
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return behaviors;
    }

    /**
     * 根据标签获取行为记录
     */
    public List<UserBehavior> getBehaviorsByTag(String tag) {
        List<UserBehavior> behaviors = new ArrayList<>();
        String sql = "SELECT * FROM user_behaviors WHERE tag = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tag);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    behaviors.add(mapResultSetToBehavior(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return behaviors;
    }

    /**
     * 根据平台获取行为记录
     */
    public List<UserBehavior> getBehaviorsByPlatform(String platform) {
        List<UserBehavior> behaviors = new ArrayList<>();
        String sql = "SELECT * FROM user_behaviors WHERE platform = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, platform);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    behaviors.add(mapResultSetToBehavior(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return behaviors;
    }

    /**
     * 获取行为统计信息
     */
    public Map<String, Object> getBehaviorStatistics() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                     "COUNT(*) as total_records, " +
                     "COUNT(DISTINCT anonymous_user_id) as unique_users, " +
                     "AVG(score) as avg_score, " +
                     "MIN(score) as min_score, " +
                     "MAX(score) as max_score " +
                     "FROM user_behaviors";

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * 获取标签统计信息
     */
    public Map<String, Long> getTagStatistics() {
        Map<String, Long> tagStats = new HashMap<>();
        String sql = "SELECT tag, COUNT(*) as count FROM user_behaviors GROUP BY tag";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tagStats.put(rs.getString("tag"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tagStats;
    }

    /**
     * 获取平台统计信息
     */
    public Map<String, Long> getPlatformStatistics() {
        Map<String, Long> platformStats = new HashMap<>();
        String sql = "SELECT platform, COUNT(*) as count FROM user_behaviors GROUP BY platform";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                platformStats.put(rs.getString("platform"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return platformStats;
    }

    /**
     * 将ResultSet映射到UserBehavior对象
     */
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
}
