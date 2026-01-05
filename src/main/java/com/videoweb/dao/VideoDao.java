package com.videoweb.dao;

import com.videoweb.model.Video;
import com.videoweb.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// 视频数据访问层：操作数据库 video 表
public class VideoDao {
    // 1. 分页查询视频列表（每页30条）
    public List<Video> getVideoList(int pageNum, int pageSize) {
        List<Video> videoList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // 补充type字段查询（广告投放核心）
        String sql = "SELECT id, title, cover_path, video_path, create_time ,type FROM video ORDER BY create_time DESC LIMIT ?, ?";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, (pageNum - 1) * pageSize);  // 偏移量
            pstmt.setInt(2, pageSize);                  // 每页条数

            rs = pstmt.executeQuery();
            while (rs.next()) {
                Video video = new Video();
                video.setId(rs.getInt("id"));
                video.setTitle(rs.getString("title"));
                video.setCoverPath(rs.getString("cover_path"));
                video.setVideoPath(rs.getString("video_path"));
                video.setCreateTime(rs.getDate("create_time"));
                video.setType(rs.getString("type")); // 映射视频标签
                videoList.add(video);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return videoList;
    }

    // 2. 查询视频总条数（用于计算总页数）
    public int getTotalVideoCount() {
        int totalCount = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT COUNT(*) AS total FROM video";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalCount = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return totalCount;
    }

    // 3. 根据ID查询单个视频（用于播放页，补充type字段）
    public Video getVideoById(int id) {
        Video video = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // 补充type字段查询
        String sql = "SELECT id, title, cover_path, video_path, type FROM video WHERE id = ?";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                video = new Video();
                video.setId(rs.getInt("id"));
                video.setTitle(rs.getString("title"));
                video.setCoverPath(rs.getString("cover_path"));
                video.setVideoPath(rs.getString("video_path"));
                video.setType(rs.getString("type")); // 映射视频标签
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return video;
    }
}