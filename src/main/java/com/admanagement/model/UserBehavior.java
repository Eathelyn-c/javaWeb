package com.admanagement.model;

import java.sql.Timestamp;

/**
 * 用户行为模型类 - 记录匿名用户与广告的交互行为
 */
public class UserBehavior {
    private int behaviorId;
    private String anonymousUserId;  // 匿名用户ID
    private String tag;              // 广告标签 (food/makeup/digital/sport/clothes/book/others)
    private Integer adId;            // 广告ID - 记录具体是哪个广告
    private int score;               // 交互评分 (1/2/3) 或 0 (浏览)
    private String platform;         // 投放平台 (video/text/image/text+image)
    private Timestamp createdAt;

    /**
     * 默认构造函数
     */
    public UserBehavior() {
    }

    /**
     * 完整构造函数
     */
    public UserBehavior(String anonymousUserId, String tag, int score, String platform) {
        this.anonymousUserId = anonymousUserId;
        this.tag = tag;
        this.score = score;
        this.platform = platform;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    /**
     * 完整构造函数（包含ID和时间戳）
     */
    public UserBehavior(int behaviorId, String anonymousUserId, String tag, int score, String platform, Timestamp createdAt) {
        this.behaviorId = behaviorId;
        this.anonymousUserId = anonymousUserId;
        this.tag = tag;
        this.score = score;
        this.platform = platform;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getBehaviorId() {
        return behaviorId;
    }

    public void setBehaviorId(int behaviorId) {
        this.behaviorId = behaviorId;
    }

    public String getAnonymousUserId() {
        return anonymousUserId;
    }

    public void setAnonymousUserId(String anonymousUserId) {
        this.anonymousUserId = anonymousUserId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Integer getAdId() {
        return adId;
    }

    public void setAdId(Integer adId) {
        this.adId = adId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserBehavior{" +
                "behaviorId=" + behaviorId +
                ", anonymousUserId='" + anonymousUserId + '\'' +
                ", tag='" + tag + '\'' +
                ", adId=" + adId +
                ", score=" + score +
                ", platform='" + platform + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
