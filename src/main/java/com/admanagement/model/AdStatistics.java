package com.admanagement.model;

import java.sql.Timestamp;

/**
 * AdStatistics entity for tracking views and clicks
 */
public class AdStatistics {
    private int statId;
    private int adId;
    private int viewCount;
    private int clickCount;
    private Timestamp lastViewed;
    private Timestamp lastClicked;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructors
    public AdStatistics() {
        this.viewCount = 0;
        this.clickCount = 0;
    }

    public AdStatistics(int adId) {
        this.adId = adId;
        this.viewCount = 0;
        this.clickCount = 0;
    }

    // Getters and Setters
    public int getStatId() {
        return statId;
    }

    public void setStatId(int statId) {
        this.statId = statId;
    }

    public int getAdId() {
        return adId;
    }

    public void setAdId(int adId) {
        this.adId = adId;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    public Timestamp getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(Timestamp lastViewed) {
        this.lastViewed = lastViewed;
    }

    public Timestamp getLastClicked() {
        return lastClicked;
    }

    public void setLastClicked(Timestamp lastClicked) {
        this.lastClicked = lastClicked;
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

    @Override
    public String toString() {
        return "AdStatistics{" +
                "statId=" + statId +
                ", adId=" + adId +
                ", viewCount=" + viewCount +
                ", clickCount=" + clickCount +
                '}';
    }
}
