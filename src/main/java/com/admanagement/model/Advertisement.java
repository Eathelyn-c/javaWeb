package com.admanagement.model;

import java.sql.Timestamp;

/**
 * Advertisement entity
 */
public class Advertisement {
    private int adId;
    private int userId;
    private int categoryId;
    private String title;
    private String description;
    private AdType adType;
    private String textContent;
    private String imageUrl;
    private String videoUrl;
    private String targetUrl;
    private AdStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp startDate;
    private Timestamp endDate;

    // Additional fields for joined data
    private String categoryName;
    private String username;
    private String companyName;

    // Enums
    public enum AdType {
        TEXT("text"),
        IMAGE("image"),
        VIDEO("video"),
        TEXT_IMAGE("text_image");

        private final String value;

        AdType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static AdType fromValue(String value) {
            for (AdType type : AdType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid AdType: " + value);
        }
    }

    public enum AdStatus {
        ACTIVE("active"),
        PAUSED("paused"),
        DELETED("deleted");

        private final String value;

        AdStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static AdStatus fromValue(String value) {
            for (AdStatus status : AdStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid AdStatus: " + value);
        }
    }

    // Constructors
    public Advertisement() {
        this.status = AdStatus.ACTIVE;
    }

    // Getters and Setters
    public int getAdId() {
        return adId;
    }

    public void setAdId(int adId) {
        this.adId = adId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AdType getAdType() {
        return adType;
    }

    public void setAdType(AdType adType) {
        this.adType = adType;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public AdStatus getStatus() {
        return status;
    }

    public void setStatus(AdStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
