package com.admanagement.service;

import com.admanagement.dao.AdvertisementDAO;
import com.admanagement.dao.CategoryDAO;
import com.admanagement.dao.StatisticsDAO;
import com.admanagement.model.Advertisement;
import com.admanagement.model.Advertisement.AdStatus;
import com.admanagement.model.Advertisement.AdType;
import com.admanagement.model.AdStatistics;
import com.admanagement.model.Category;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advertisement Service
 */
public class AdService {
    private final AdvertisementDAO adDAO;
    private final CategoryDAO categoryDAO;
    private final StatisticsDAO statsDAO;

    public AdService() {
        this.adDAO = new AdvertisementDAO();
        this.categoryDAO = new CategoryDAO();
        this.statsDAO = new StatisticsDAO();
    }

    /**
     * Create a new advertisement
     */
    public Map<String, Object> createAdvertisement(int userId, String title, String description,
                                                   String adType, String categoryName, String textContent,
                                                   String imageUrl, String videoUrl, String targetUrl,
                                                   Timestamp startDate, Timestamp endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // Validate input
        if (title == null || title.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Title is required");
            return result;
        }
        
        // Get category
        Category category = categoryDAO.getCategoryByName(categoryName);
        if (category == null) {
            result.put("success", false);
            result.put("message", "Invalid category");
            return result;
        }
        
        // Validate ad type and content
        AdType type;
        try {
            type = AdType.fromValue(adType);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", "Invalid ad type");
            return result;
        }
        
        // Validate content based on ad type
        if (type == AdType.TEXT && (textContent == null || textContent.trim().isEmpty())) {
            result.put("success", false);
            result.put("message", "Text content is required for text ads");
            return result;
        }
        
        if (type == AdType.IMAGE && (imageUrl == null || imageUrl.trim().isEmpty())) {
            result.put("success", false);
            result.put("message", "Image is required for image ads");
            return result;
        }
        
        if (type == AdType.VIDEO && (videoUrl == null || videoUrl.trim().isEmpty())) {
            result.put("success", false);
            result.put("message", "Video is required for video ads");
            return result;
        }
        
        if (type == AdType.TEXT_IMAGE && 
            (textContent == null || textContent.trim().isEmpty() || 
             imageUrl == null || imageUrl.trim().isEmpty())) {
            result.put("success", false);
            result.put("message", "Both text and image are required for text+image ads");
            return result;
        }
        
        // Create advertisement
        Advertisement ad = new Advertisement();
        ad.setUserId(userId);
        ad.setCategoryId(category.getCategoryId());
        ad.setTitle(title);
        ad.setDescription(description);
        ad.setAdType(type);
        ad.setTextContent(textContent);
        ad.setImageUrl(imageUrl);
        ad.setVideoUrl(videoUrl);
        ad.setTargetUrl(targetUrl);
        ad.setStatus(AdStatus.ACTIVE);
        ad.setStartDate(startDate);
        ad.setEndDate(endDate);
        
        if (adDAO.createAdvertisement(ad)) {
            // Create statistics record
            statsDAO.createStatistics(ad.getAdId());
            
            result.put("success", true);
            result.put("message", "Advertisement created successfully");
            result.put("data", ad);
        } else {
            result.put("success", false);
            result.put("message", "Failed to create advertisement");
        }
        
        return result;
    }

    /**
     * Get advertisement by ID
     */
    public Advertisement getAdvertisementById(int adId) {
        return adDAO.getAdvertisementById(adId);
    }

    /**
     * Get all advertisements for a user
     */
    public List<Advertisement> getUserAdvertisements(int userId) {
        return adDAO.getAdvertisementsByUserId(userId);
    }

    /**
     * Get all active advertisements
     */
    public List<Advertisement> getActiveAdvertisements() {
        return adDAO.getAdvertisementsByStatus(AdStatus.ACTIVE);
    }

    /**
     * Get advertisements by category
     */
    public List<Advertisement> getAdvertisementsByCategory(int categoryId) {
        return adDAO.getAdvertisementsByCategory(categoryId);
    }

    /**
     * Update advertisement
     */
    public Map<String, Object> updateAdvertisement(int adId, int userId, String title, String description,
                                                   String adType, String categoryName, String textContent,
                                                   String imageUrl, String videoUrl, String targetUrl,
                                                   Timestamp startDate, Timestamp endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // Check if ad exists and belongs to user
        Advertisement existingAd = adDAO.getAdvertisementById(adId);
        if (existingAd == null) {
            result.put("success", false);
            result.put("message", "Advertisement not found");
            return result;
        }
        
        if (existingAd.getUserId() != userId) {
            result.put("success", false);
            result.put("message", "Unauthorized to update this advertisement");
            return result;
        }
        
        // Get category
        Category category = categoryDAO.getCategoryByName(categoryName);
        if (category == null) {
            result.put("success", false);
            result.put("message", "Invalid category");
            return result;
        }
        
        // Update advertisement
        existingAd.setTitle(title);
        existingAd.setDescription(description);
        existingAd.setAdType(AdType.fromValue(adType));
        existingAd.setCategoryId(category.getCategoryId());
        existingAd.setTextContent(textContent);
        existingAd.setImageUrl(imageUrl);
        existingAd.setVideoUrl(videoUrl);
        existingAd.setTargetUrl(targetUrl);
        existingAd.setStartDate(startDate);
        existingAd.setEndDate(endDate);
        
        if (adDAO.updateAdvertisement(existingAd)) {
            result.put("success", true);
            result.put("message", "Advertisement updated successfully");
            result.put("data", existingAd);
        } else {
            result.put("success", false);
            result.put("message", "Failed to update advertisement");
        }
        
        return result;
    }

    /**
     * Delete advertisement
     */
    public Map<String, Object> deleteAdvertisement(int adId, int userId) {
        Map<String, Object> result = new HashMap<>();
        
        // Check if ad exists and belongs to user
        Advertisement ad = adDAO.getAdvertisementById(adId);
        if (ad == null) {
            result.put("success", false);
            result.put("message", "Advertisement not found");
            return result;
        }
        
        if (ad.getUserId() != userId) {
            result.put("success", false);
            result.put("message", "Unauthorized to delete this advertisement");
            return result;
        }
        
        if (adDAO.deleteAdvertisement(adId)) {
            result.put("success", true);
            result.put("message", "Advertisement deleted successfully");
        } else {
            result.put("success", false);
            result.put("message", "Failed to delete advertisement");
        }
        
        return result;
    }

    /**
     * Pause/resume advertisement
     */
    public Map<String, Object> toggleAdvertisementStatus(int adId, int userId) {
        Map<String, Object> result = new HashMap<>();
        
        Advertisement ad = adDAO.getAdvertisementById(adId);
        if (ad == null || ad.getUserId() != userId) {
            result.put("success", false);
            result.put("message", "Advertisement not found or unauthorized");
            return result;
        }
        
        AdStatus newStatus = (ad.getStatus() == AdStatus.ACTIVE) ? AdStatus.PAUSED : AdStatus.ACTIVE;
        
        if (adDAO.updateAdvertisementStatus(adId, newStatus)) {
            result.put("success", true);
            result.put("message", "Advertisement status updated");
            result.put("data", newStatus.getValue());
        } else {
            result.put("success", false);
            result.put("message", "Failed to update status");
        }
        
        return result;
    }

    /**
     * Get advertisement statistics
     */
    public AdStatistics getAdStatistics(int adId, int userId) {
        // Verify ownership
        Advertisement ad = adDAO.getAdvertisementById(adId);
        if (ad == null || ad.getUserId() != userId) {
            return null;
        }
        
        return statsDAO.getStatisticsByAdId(adId);
    }

    /**
     * Record view
     */
    public boolean recordView(int adId) {
        return statsDAO.incrementViewCount(adId);
    }

    /**
     * Record click
     */
    public boolean recordClick(int adId) {
        return statsDAO.incrementClickCount(adId);
    }

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }
}
