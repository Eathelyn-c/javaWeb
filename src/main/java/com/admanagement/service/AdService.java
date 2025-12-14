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
 * Advertisement Service - handles both internal and external API operations
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

    /********** Internal Business Methods **********/

    // Create advertisement (internal use)
    public Map<String, Object> createAdvertisement(int userId, String title, String description,
                                                   String adType, String categoryName, String textContent,
                                                   String imageUrl, String videoUrl, String targetUrl,
                                                   Timestamp startDate, Timestamp endDate) {
        Map<String, Object> result = new HashMap<>();

        if (title == null || title.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Title is required");
            return result;
        }

        Category category = categoryDAO.getCategoryByName(categoryName);
        if (category == null) {
            result.put("success", false);
            result.put("message", "Invalid category");
            return result;
        }

        AdType type;
        try {
            type = AdType.fromValue(adType);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", "Invalid ad type");
            return result;
        }

        if ((type == AdType.TEXT && (textContent == null || textContent.trim().isEmpty()))
                || (type == AdType.IMAGE && (imageUrl == null || imageUrl.trim().isEmpty()))
                || (type == AdType.VIDEO && (videoUrl == null || videoUrl.trim().isEmpty()))
                || (type == AdType.TEXT_IMAGE && ((textContent == null || textContent.trim().isEmpty())
                || (imageUrl == null || imageUrl.trim().isEmpty())))) {
            result.put("success", false);
            result.put("message", "Required content missing for ad type");
            return result;
        }

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

    public Advertisement getAdvertisementById(int adId) {
        return adDAO.getAdvertisementById(adId);
    }

    public List<Advertisement> getUserAdvertisements(int userId) {
        return adDAO.getAdvertisementsByUserId(userId);
    }

    public List<Advertisement> getActiveAdvertisements() {
        return adDAO.getAdvertisementsByStatus(AdStatus.ACTIVE);
    }

    public List<Advertisement> getAdvertisementsByCategory(int categoryId) {
        return adDAO.getAdvertisementsByCategory(categoryId);
    }

    public Map<String, Object> updateAdvertisement(int adId, int userId, String title, String description,
                                                   String adType, String categoryName, String textContent,
                                                   String imageUrl, String videoUrl, String targetUrl,
                                                   Timestamp startDate, Timestamp endDate) {
        Map<String, Object> result = new HashMap<>();
        Advertisement ad = adDAO.getAdvertisementById(adId);

        if (ad == null || ad.getUserId() != userId) {
            result.put("success", false);
            result.put("message", "Advertisement not found or unauthorized");
            return result;
        }

        Category category = categoryDAO.getCategoryByName(categoryName);
        if (category == null) {
            result.put("success", false);
            result.put("message", "Invalid category");
            return result;
        }

        ad.setTitle(title);
        ad.setDescription(description);
        ad.setAdType(AdType.fromValue(adType));
        ad.setCategoryId(category.getCategoryId());
        ad.setTextContent(textContent);
        ad.setImageUrl(imageUrl);
        ad.setVideoUrl(videoUrl);
        ad.setTargetUrl(targetUrl);
        ad.setStartDate(startDate);
        ad.setEndDate(endDate);

        if (adDAO.updateAdvertisement(ad)) {
            result.put("success", true);
            result.put("message", "Advertisement updated successfully");
            result.put("data", ad);
        } else {
            result.put("success", false);
            result.put("message", "Failed to update advertisement");
        }

        return result;
    }

    public Map<String, Object> deleteAdvertisement(int adId, int userId) {
        Map<String, Object> result = new HashMap<>();
        Advertisement ad = adDAO.getAdvertisementById(adId);

        if (ad == null || ad.getUserId() != userId) {
            result.put("success", false);
            result.put("message", "Advertisement not found or unauthorized");
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

    public AdStatistics getAdStatistics(int adId, int userId) {
        Advertisement ad = adDAO.getAdvertisementById(adId);
        if (ad == null || ad.getUserId() != userId) {
            return null;
        }
        return statsDAO.getStatisticsByAdId(adId);
    }

    public boolean recordView(int adId) {
        return statsDAO.incrementViewCount(adId);
    }

    public boolean recordClick(int adId) {
        return statsDAO.incrementClickCount(adId);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    /********** External API Method **********/

    // External API can call this with Advertisement object directly
    public boolean createAdvertisement(Advertisement ad) {
        boolean created = adDAO.createAdvertisement(ad);
        if (created) {
            statsDAO.createStatistics(ad.getAdId());
        }
        return created;
    }

    // 获取活跃广告，带数量限制
    public List<Advertisement> getActiveAdvertisements(int limit) {
        List<Advertisement> allAds = getActiveAdvertisements();
        return allAds.size() > limit ? allAds.subList(0, limit) : allAds;
    }

    // 根据分类名称获取活跃广告，带数量限制
    public List<Advertisement> getAdvertisementsByCategoryName(String categoryName, int limit) {
        Category cat = categoryDAO.getCategoryByName(categoryName);
        if (cat == null) return List.of(); // 空列表
        List<Advertisement> ads = getAdvertisementsByCategory(cat.getCategoryId());
        return ads.size() > limit ? ads.subList(0, limit) : ads;
    }

    public List<Advertisement> getAdsByTagAndScore(String tag, int score, String platform, int limit) {
        // 假设 AdvertisementDAO 有对应方法
        return adDAO.getAdvertisementsByTagAndScore(tag, score, platform, limit);
    }


}
