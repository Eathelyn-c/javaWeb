package com.admanagement.service;

import com.admanagement.dao.AdvertisementDAO;
import com.admanagement.dao.UserBehaviorDAO;
import com.admanagement.model.Advertisement;
import com.admanagement.model.UserBehavior;

import java.util.*;
import java.util.stream.Collectors;

public class RecommendAdService {
    private UserBehaviorDAO userBehaviorDAO = new UserBehaviorDAO();
    private AdvertisementDAO advertisementDAO = new AdvertisementDAO();

    /**
     * 根据用户行为推荐广告
     * @param anonymousUserId 匿名用户ID
     * @param maxResults 最大返回广告数量
     * @return 推荐广告列表
     */
    public List<Advertisement> recommendAds(String anonymousUserId, int maxResults) {
        // 1. 获取用户行为
        List<UserBehavior> behaviors = userBehaviorDAO.getBehaviorsByUserId(anonymousUserId);
        if (behaviors.isEmpty()) {
            // 没有行为就随机返回一些广告
            return advertisementDAO.getAllAdvertisements()
                    .stream()
                    .limit(maxResults)
                    .collect(Collectors.toList());
        }

        // 2. 统计每个标签的权重（分数总和）
        Map<String, Integer> tagScores = new HashMap<>();
        for (UserBehavior b : behaviors) {
            tagScores.put(b.getTag(), tagScores.getOrDefault(b.getTag(), 0) + b.getScore());
        }

        // 3. 根据权重排序标签
        List<String> sortedTags = tagScores.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 4. 根据标签获取广告，按权重顺序合并
        List<Advertisement> recommendedAds = new ArrayList<>();
        for (String tag : sortedTags) {
            List<Advertisement> ads = advertisementDAO.getAdvertisementsByCategoryName(tag); // 你可以在 DAO 里写按标签查广告
            for (Advertisement ad : ads) {
                if (!recommendedAds.contains(ad)) {
                    recommendedAds.add(ad);
                    if (recommendedAds.size() >= maxResults) break;
                }
            }
            if (recommendedAds.size() >= maxResults) break;
        }

        // 5. 如果还不够数量，补充随机广告
        if (recommendedAds.size() < maxResults) {
            List<Advertisement> allAds = advertisementDAO.getAllAdvertisements();
            for (Advertisement ad : allAds) {
                if (!recommendedAds.contains(ad)) {
                    recommendedAds.add(ad);
                    if (recommendedAds.size() >= maxResults) break;
                }
            }
        }

        return recommendedAds;
    }
}
