package com.admanagement.service;

import com.admanagement.dao.AdvertisementDAO;
import com.admanagement.dao.CategoryDAO;
import com.admanagement.dao.StatisticsDAO;
import com.admanagement.dao.UserBehaviorDAO;
import com.admanagement.model.UserBehavior;
import com.admanagement.model.Advertisement;
import com.admanagement.model.Advertisement.AdStatus;
import com.admanagement.model.Advertisement.AdType;
import com.admanagement.model.AdStatistics;
import com.admanagement.model.Category;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 完整广告业务服务类
 * 适配流程：点击播放(score=0) -> 请求广告列表 -> 缓存播放 -> 退出视频(score>0) -> 结算数据
 */
public class AdService {
    private final AdvertisementDAO adDAO;
    private final CategoryDAO categoryDAO;
    private final StatisticsDAO statsDAO;
    private final UserBehaviorDAO behaviorDAO;

    public AdService() {
        this.adDAO = new AdvertisementDAO();
        this.categoryDAO = new CategoryDAO();
        this.statsDAO = new StatisticsDAO();
        this.behaviorDAO = new UserBehaviorDAO();
    }

    /* ============================================================
       1. 外部 API 核心工作流 (解决流程对接)
       ============================================================ */

    /**
     * 根据工作流获取广告
     * @param userId 匿名用户ID
     * @param tag 标签
     * @param score 用于排序的分数参数（不影响返回逻辑）
     * @param limit 数量
     */
    public List<Advertisement> getAdsByWorkFlow(String userId, String tag, int score, int limit) {
        // score参数现在仅用于推荐算法排序，不影响是否返回广告
        String targetTag = tag;
        // 记忆功能：如果没传标签，查历史最高分标签
        if (targetTag == null || targetTag.trim().isEmpty()) {
            targetTag = behaviorDAO.getMostInterestedTag(userId);
            System.out.println("[AdService] 标签为空，查询历史标签结果: " + targetTag);
        }

        System.out.println("[AdService] 开始获取广告。userId=" + userId + ", targetTag=" + targetTag + ", score=" + score + ", limit=" + limit);
        List<Advertisement> finalAds = new ArrayList<>();
        
        // 优先精准匹配
        if (targetTag != null && !targetTag.trim().isEmpty()) {
            List<Advertisement> matched = adDAO.getAdvertisementsByTagAndScore(targetTag, limit);
            System.out.println("[AdService] 标签匹配查询结果数量: " + (matched != null ? matched.size() : 0));
            if (matched != null && !matched.isEmpty()) {
                finalAds.addAll(matched);
            }
        }

        // 数量不足则热门补齐（确保至少返回一些广告）
        if (finalAds.size() < limit) {
            List<Advertisement> topAds = adDAO.getTopPerformantAds(limit - finalAds.size());
            System.out.println("[AdService] 热门广告查询结果数量: " + (topAds != null ? topAds.size() : 0));
            if (topAds != null && !topAds.isEmpty()) {
                // 避免重复添加
                for (Advertisement ad : topAds) {
                    boolean exists = false;
                    for (Advertisement existing : finalAds) {
                        if (existing.getAdId() == ad.getAdId()) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        finalAds.add(ad);
                        if (finalAds.size() >= limit) {
                            break;
                        }
                    }
                }
            }
        }

        System.out.println("[AdService] 最终返回广告数量: " + finalAds.size());
        return finalAds;
    }

    /* ============================================================
       2. 基础管理逻辑
       ============================================================ */

    public List<Advertisement> getActiveAdvertisements() {
        return adDAO.getAdvertisementsByStatus(AdStatus.ACTIVE);
    }

    public List<Advertisement> getAdvertisementsByCategory(int categoryId) {
        return adDAO.getAdvertisementsByCategory(categoryId);
    }

    public boolean updateAdvertisement(Advertisement ad) {
        return adDAO.updateAdvertisement(ad);
    }

    public boolean deleteAdvertisement(int adId) {
        return adDAO.deleteAdvertisement(adId);
    }

    public boolean toggleAdvertisementStatus(int adId) {
        Advertisement ad = adDAO.getAdvertisementById(adId);
        if (ad != null) {
            AdStatus nextStatus = (ad.getStatus() == AdStatus.ACTIVE) ? AdStatus.PAUSED : AdStatus.ACTIVE;
            return adDAO.updateAdvertisementStatus(adId, nextStatus);
        }
        return false;
    }

    // 基础 ID 获取
    public Advertisement getAdvertisementById(int adId) {
        return adDAO.getAdvertisementById(adId);
    }

    // 获取特定用户的广告
    public List<Advertisement> getAdvertisementsByUserId(int userId) {
        return adDAO.getAdvertisementsByUserId(userId);
    }

    /* ============================================================
       3. 统计与创建逻辑
       ============================================================ */

    public AdStatistics getAdStatistics(int adId) {
        return statsDAO.getStatisticsByAdId(adId);
    }

    // 供统计页面使用的重载方法
    public AdStatistics getAdStatistics(int adId, int platformType) {
        // 这里可以根据需求扩展 DAO 查询
        return statsDAO.getStatisticsByAdId(adId);
    }
    
    /**
     * 从外部API访问记录获取统计数据
     * 浏览量 = 外部平台通过API获取该广告的次数 (score=0时返回的广告)
     * 点击量 = 外部用户点击后报告的次数 (score>0时的记录)
     */
    public AdStatistics getExternalApiStatistics(int adId) {
        return behaviorDAO.getStatisticsByAdId(adId);
    }

    // 增加计数：曝光
    public boolean recordView(int adId) {
        return statsDAO.incrementViewCount(adId);
    }

    // 增加计数：点击
    public boolean recordClick(int adId) {
        return statsDAO.incrementClickCount(adId);
    }

    /**
     * 创建广告逻辑 (含初始化统计)
     */
    public boolean createAdvertisement(Advertisement ad) {
        if (adDAO.createAdvertisement(ad)) {
            // 关键：新广告必须初始化统计行，否则 JOIN 查询会查不到此广告
            statsDAO.createStatistics(ad.getAdId());
            return true;
        }
        return false;
    }
    public boolean recordUserBehavior(UserBehavior behavior) {
        return behaviorDAO.recordUserBehavior(behavior);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

}