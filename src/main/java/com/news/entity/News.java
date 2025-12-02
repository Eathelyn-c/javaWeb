package com.news.entity;

import java.util.Date;
import java.util.List;

public class News {
    private int newsId;
    private String title;
    private String coverImg;
    private String content;
    private int categoryId;
    private Date createTime;
    private String categoryName;
    private List<String> contentParagraphs; // 正文段落拆分（用于插入广告）

    // 无参构造
    public News() {}

    // Getter和Setter
    public int getNewsId() { return newsId; }
    public void setNewsId(int newsId) { this.newsId = newsId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverImg() { return coverImg; }
    public void setCoverImg(String coverImg) { this.coverImg = coverImg; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public List<String> getContentParagraphs() { return contentParagraphs; }
    public void setContentParagraphs(List<String> contentParagraphs) { this.contentParagraphs = contentParagraphs; }
}