package com.admanagement.dao;

import com.admanagement.model.Advertisement;
import com.admanagement.model.Advertisement.AdStatus;
import com.admanagement.model.Advertisement.AdType;
import com.admanagement.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Advertisement Data Access Object
 */
public class AdvertisementDAO {

    /**
     * Create a new advertisement
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
     * Get advertisement by ID
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
     * Get all advertisements
     */
    public List<Advertisement> getAllAdvertisements() {
        return getAdvertisementsByStatus(null);
    }

    /**
     * Get advertisements by user ID
     */
    public List<Advertisement> getAdvertisementsByUserId(int userId) {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username, u.company_name " +
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
     * Get advertisements by category
     */
    public List<Advertisement> getAdvertisementsByCategory(int categoryId) {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username, u.company_name " +
                     "FROM advertisements a " +
                     "JOIN categories c ON a.category_id = c.category_id " +
                     "JOIN users u ON a.user_id = u.user_id " +
                     "WHERE a.category_id = ? AND a.status = 'active' " +
                     "ORDER BY a.created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
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
     * Get advertisements by status
     */
    public List<Advertisement> getAdvertisementsByStatus(AdStatus status) {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username, u.company_name " +
                     "FROM advertisements a " +
                     "JOIN categories c ON a.category_id = c.category_id " +
                     "JOIN users u ON a.user_id = u.user_id ";
        
        if (status != null) {
            sql += "WHERE a.status = ? ";
        } else {
            sql += "WHERE a.status != 'deleted' ";
        }
        
        sql += "ORDER BY a.created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (status != null) {
                stmt.setString(1, status.getValue());
            }
            
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
     * Update advertisement
     */
    public boolean updateAdvertisement(Advertisement ad) {
        String sql = "UPDATE advertisements SET category_id = ?, title = ?, description = ?, " +
                     "ad_type = ?, text_content = ?, image_url = ?, video_url = ?, target_url = ?, " +
                     "status = ?, start_date = ?, end_date = ? WHERE ad_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, ad.getCategoryId());
            stmt.setString(2, ad.getTitle());
            stmt.setString(3, ad.getDescription());
            stmt.setString(4, ad.getAdType().getValue());
            stmt.setString(5, ad.getTextContent());
            stmt.setString(6, ad.getImageUrl());
            stmt.setString(7, ad.getVideoUrl());
            stmt.setString(8, ad.getTargetUrl());
            stmt.setString(9, ad.getStatus().getValue());
            stmt.setTimestamp(10, ad.getStartDate());
            stmt.setTimestamp(11, ad.getEndDate());
            stmt.setInt(12, ad.getAdId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete advertisement (soft delete)
     */
    public boolean deleteAdvertisement(int adId) {
        String sql = "UPDATE advertisements SET status = 'deleted' WHERE ad_id = ?";
        
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
     * Update advertisement status
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
     * Extract Advertisement object from ResultSet
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
        ad.setUpdatedAt(rs.getTimestamp("updated_at"));
        ad.setStartDate(rs.getTimestamp("start_date"));
        ad.setEndDate(rs.getTimestamp("end_date"));
        
        // Set joined data if available
        try {
            ad.setCategoryName(rs.getString("category_name"));
            ad.setUsername(rs.getString("username"));
            ad.setCompanyName(rs.getString("company_name"));
        } catch (SQLException e) {
            // Ignore if these columns don't exist
        }
        
        return ad;
    }

    /**
     * Get advertisements by category name (for recommendation)
     */
    public List<Advertisement> getAdvertisementsByCategoryName(String categoryName) {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username, u.company_name " +
                "FROM advertisements a " +
                "JOIN categories c ON a.category_id = c.category_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "WHERE c.category_name = ? AND a.status = 'active' " +
                "ORDER BY a.created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
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

    public List<Advertisement> getAdvertisementsByTagAndScore(String tag, int score, String platform, int limit) {
        List<Advertisement> ads = new ArrayList<>();
        String sql = "SELECT a.*, c.category_name, u.username, u.company_name " +
                "FROM advertisements a " +
                "JOIN categories c ON a.category_id = c.category_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "WHERE a.tag = ? AND a.score >= ? AND a.platform = ? AND a.status = 'active' " +
                "ORDER BY a.score DESC, a.created_at DESC " +
                "LIMIT ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tag);
            stmt.setInt(2, score);
            stmt.setString(3, platform);
            stmt.setInt(4, limit);
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

}
