package com.admanagement.dao;

import com.admanagement.model.AdStatistics;
import com.admanagement.util.DatabaseUtil;

import java.sql.*;

/**
 * Statistics Data Access Object
 */
public class StatisticsDAO {

    /**
     * Create initial statistics record for new advertisement
     */
    public boolean createStatistics(int adId) {
        String sql = "INSERT INTO ad_statistics (ad_id, view_count, click_count) VALUES (?, 0, 0)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get statistics by advertisement ID
     */
    public AdStatistics getStatisticsByAdId(int adId) {
        String sql = "SELECT * FROM ad_statistics WHERE ad_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractStatisticsFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Increment view count
     */
    public boolean incrementViewCount(int adId) {
        String sql = "UPDATE ad_statistics SET view_count = view_count + 1, last_viewed = CURRENT_TIMESTAMP " +
                     "WHERE ad_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adId);
            int rows = stmt.executeUpdate();
            
            // If no statistics record exists, create one
            if (rows == 0) {
                createStatistics(adId);
                return incrementViewCount(adId);
            }
            
            // Log the view
            logView(adId, conn);
            
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Increment click count
     */
    public boolean incrementClickCount(int adId) {
        String sql = "UPDATE ad_statistics SET click_count = click_count + 1, last_clicked = CURRENT_TIMESTAMP " +
                     "WHERE ad_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adId);
            int rows = stmt.executeUpdate();
            
            // If no statistics record exists, create one
            if (rows == 0) {
                createStatistics(adId);
                return incrementClickCount(adId);
            }
            
            // Log the click
            logClick(adId, conn);
            
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Log a view
     */
    private void logView(int adId, Connection conn) {
        String sql = "INSERT INTO view_logs (ad_id, viewer_ip, user_agent) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adId);
            stmt.setString(2, ""); // IP address would be set by servlet
            stmt.setString(3, ""); // User agent would be set by servlet
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Log error but don't fail the main operation
            e.printStackTrace();
        }
    }

    /**
     * Log a click
     */
    private void logClick(int adId, Connection conn) {
        String sql = "INSERT INTO click_logs (ad_id, clicker_ip, user_agent) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adId);
            stmt.setString(2, ""); // IP address would be set by servlet
            stmt.setString(3, ""); // User agent would be set by servlet
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Log error but don't fail the main operation
            e.printStackTrace();
        }
    }

    /**
     * Get total views for user's advertisements
     */
    public int getTotalViewsByUserId(int userId) {
        String sql = "SELECT SUM(s.view_count) as total_views " +
                     "FROM ad_statistics s " +
                     "JOIN advertisements a ON s.ad_id = a.ad_id " +
                     "WHERE a.user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_views");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get total clicks for user's advertisements
     */
    public int getTotalClicksByUserId(int userId) {
        String sql = "SELECT SUM(s.click_count) as total_clicks " +
                     "FROM ad_statistics s " +
                     "JOIN advertisements a ON s.ad_id = a.ad_id " +
                     "WHERE a.user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_clicks");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Extract AdStatistics object from ResultSet
     */
    private AdStatistics extractStatisticsFromResultSet(ResultSet rs) throws SQLException {
        AdStatistics stats = new AdStatistics();
        stats.setStatId(rs.getInt("stat_id"));
        stats.setAdId(rs.getInt("ad_id"));
        stats.setViewCount(rs.getInt("view_count"));
        stats.setClickCount(rs.getInt("click_count"));
        stats.setLastViewed(rs.getTimestamp("last_viewed"));
        stats.setLastClicked(rs.getTimestamp("last_clicked"));
        stats.setCreatedAt(rs.getTimestamp("created_at"));
        stats.setUpdatedAt(rs.getTimestamp("updated_at"));
        return stats;
    }
}
