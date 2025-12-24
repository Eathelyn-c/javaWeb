<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>${video.title} - 视频播放</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/play.css">
</head>
<body>
<div class="play-container">
    <!-- 返回首页按钮 -->
    <a href="${pageContext.request.contextPath}/index" class="back-btn">← 返回首页</a>

    <!-- 视频标题 -->
    <h1 class="video-title">${video.title}</h1>

    <!-- 视频播放器容器 -->
    <div class="video-player-container">
        <!-- 原生video标签（隐藏默认控制栏） -->
        <video id="videoPlayer" src="${pageContext.request.contextPath}${video.videoPath}"
               poster="${pageContext.request.contextPath}${video.coverPath}"
               preload="auto" controlsList="nodownload" disablePictureInPicture>
            您的浏览器不支持HTML5视频播放，请更新浏览器！
        </video>

        <!-- 自定义控制栏（默认隐藏，鼠标悬停播放器显示） -->
        <div class="custom-controls">
            <!-- 播放/暂停按钮 -->
            <button id="playPauseBtn" class="control-btn">
                <i>▶</i>
            </button>

            <!-- 进度条 -->
            <div class="progress-container">
                <div class="progress-bg"></div>
                <div class="progress-played"></div>
                <div class="progress-handle"></div>
            </div>

            <!-- 播放时间 -->
            <div class="time-display">
                <span id="currentTime">00:00</span>
                <span>/</span>
                <span id="totalTime">00:00</span>
            </div>

            <!-- 音量控制 -->
            <div class="volume-container">
                <button id="muteBtn" class="control-btn">
                    <i>🔊</i>
                </button>
                <input type="range" id="volumeSlider" min="0" max="1" step="0.01" value="1">
            </div>

            <!-- 倍速切换 -->
            <select id="playbackRateSelect" class="control-select">
                <option value="0.5">0.5x</option>
                <option value="0.75">0.75x</option>
                <option value="1" selected>1.0x</option>
                <option value="1.25">1.25x</option>
                <option value="1.5">1.5x</option>
                <option value="2">2.0x</option>
            </select>

            <!-- 全屏按钮 -->
            <button id="fullscreenBtn" class="control-btn">
                <i>⛶</i>
            </button>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/play.js"></script>
</body>
</html>