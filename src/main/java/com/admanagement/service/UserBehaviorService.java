package com.admanagement.service;

import com.admanagement.dao.UserBehaviorDAO;
import com.admanagement.model.UserBehavior;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户行为业务逻辑层 - 处理验证和业务规则
 */
public class UserBehaviorService {
    private UserBehaviorDAO userBehaviorDAO = new UserBehaviorDAO();

    // 有效的标签列表
    private static final String[] VALID_TAGS = {"food", "makeup", "digital", "sport", "clothes", "book", "others"};
    // 有效的分数值
    private static final int[] VALID_SCORES = {1, 2, 3};
    // 有效的平台列表
    private static final String[] VALID_PLATFORMS = {"video", "text", "image", "text+image"};

    /**
     * 记录用户行为 - 包含验证
     */
    public Map<String, Object> recordUserBehavior(String anonymousUserId, String tag, int score, String platform) {
        Map<String, Object> response = new HashMap<>();

        // 验证参数
        if (anonymousUserId == null || anonymousUserId.trim().isEmpty()) {
            response.put("code", 400);
            response.put("msg", "匿名用户ID不能为空");
            return response;
        }

        if (tag == null || tag.trim().isEmpty()) {
            response.put("code", 400);
            response.put("msg", "广告标签不能为空");
            return response;
        }

        if (!isValidTag(tag)) {
            response.put("code", 400);
            response.put("msg", "无效的广告标签，有效值为: food, makeup, digital, sport, clothes, book, others");
            return response;
        }

        if (!isValidScore(score)) {
            response.put("code", 400);
            response.put("msg", "无效的交互评分，有效值为: 1, 2, 3");
            return response;
        }

        if (platform == null || platform.trim().isEmpty()) {
            response.put("code", 400);
            response.put("msg", "投放平台不能为空");
            return response;
        }

        if (!isValidPlatform(platform)) {
            response.put("code", 400);
            response.put("msg", "无效的投放平台，有效值为: video, text, image, text+image");
            return response;
        }

        // 创建行为对象并保存
        UserBehavior behavior = new UserBehavior(anonymousUserId, tag, score, platform);
        if (userBehaviorDAO.saveBehavior(behavior)) {
            response.put("code", 200);
            response.put("msg", "行为记录成功");
            response.put("data", behavior);
            return response;
        } else {
            response.put("code", 500);
            response.put("msg", "保存行为记录失败");
            return response;
        }
    }

    /**
     * 获取用户行为统计信息
     */
    public Map<String, Object> getUserBehaviorStatistics(String anonymousUserId) {
        Map<String, Object> response = new HashMap<>();

        if (anonymousUserId == null || anonymousUserId.trim().isEmpty()) {
            response.put("code", 400);
            response.put("msg", "匿名用户ID不能为空");
            return response;
        }

        List<UserBehavior> behaviors = userBehaviorDAO.getBehaviorsByUserId(anonymousUserId);
        response.put("code", 200);
        response.put("msg", "查询成功");
        response.put("data", behaviors);
        response.put("total", behaviors.size());
        return response;
    }

    /**
     * 获取标签统计信息
     */
    public Map<String, Object> getTagStatistics(String tag) {
        Map<String, Object> response = new HashMap<>();

        if (tag == null || tag.trim().isEmpty()) {
            response.put("code", 400);
            response.put("msg", "标签不能为空");
            return response;
        }

        if (!isValidTag(tag)) {
            response.put("code", 400);
            response.put("msg", "无效的广告标签");
            return response;
        }

        List<UserBehavior> behaviors = userBehaviorDAO.getBehaviorsByTag(tag);
        response.put("code", 200);
        response.put("msg", "查询成功");
        response.put("tag", tag);
        response.put("data", behaviors);
        response.put("total", behaviors.size());
        return response;
    }

    /**
     * 获取平台统计信息
     */
    public Map<String, Object> getPlatformStatistics(String platform) {
        Map<String, Object> response = new HashMap<>();

        if (platform == null || platform.trim().isEmpty()) {
            response.put("code", 400);
            response.put("msg", "平台不能为空");
            return response;
        }

        if (!isValidPlatform(platform)) {
            response.put("code", 400);
            response.put("msg", "无效的投放平台");
            return response;
        }

        List<UserBehavior> behaviors = userBehaviorDAO.getBehaviorsByPlatform(platform);
        response.put("code", 200);
        response.put("msg", "查询成功");
        response.put("platform", platform);
        response.put("data", behaviors);
        response.put("total", behaviors.size());
        return response;
    }

    /**
     * 获取整体统计信息
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> overallStats = userBehaviorDAO.getBehaviorStatistics();
        Map<String, Long> tagStats = userBehaviorDAO.getTagStatistics();
        Map<String, Long> platformStats = userBehaviorDAO.getPlatformStatistics();

        response.put("code", 200);
        response.put("msg", "查询成功");
        response.put("overall", overallStats);
        response.put("by_tag", tagStats);
        response.put("by_platform", platformStats);

        return response;
    }

    /**
     * 验证标签是否有效
     */
    private boolean isValidTag(String tag) {
        for (String validTag : VALID_TAGS) {
            if (validTag.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证分数是否有效
     */
    private boolean isValidScore(int score) {
        for (int validScore : VALID_SCORES) {
            if (validScore == score) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证平台是否有效
     */
    private boolean isValidPlatform(String platform) {
        for (String validPlatform : VALID_PLATFORMS) {
            if (validPlatform.equals(platform)) {
                return true;
            }
        }
        return false;
    }
}
