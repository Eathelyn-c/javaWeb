package com.videoweb.model;

import java.util.Date;

// 视频实体类：映射数据库 video 表
public class Video {
    private Integer id;         // 视频ID
    private String title;       // 视频标题
    private String coverPath;   // 封面路径
    private String videoPath;   // 视频路径
    private Date createTime;    // 添加时间
    private String type;

    // 构造方法、getter、setter
    public Video() {}

    public Video(String title, String coverPath, String videoPath , String type) {
        this.title = title;
        this.coverPath = coverPath;
        this.videoPath = videoPath;
        this.type = type;
    }

    // getter 和 setter 方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }
    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}