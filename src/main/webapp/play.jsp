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
        <!-- 视频显示区域：视频 + 广告只覆盖该区域 -->
        <div class="video-screen">
            <!-- 原生video标签（使用浏览器自带控制条，上面这一层） -->
            <video id="videoPlayer" src="${pageContext.request.contextPath}${video.videoPath}"
                   poster="${pageContext.request.contextPath}${video.coverPath}"
                   preload="auto" playsinline
                   controls
                   controlsList="nodownload noremoteplayback"
                   disablePictureInPicture>
                您的浏览器不支持HTML5视频播放，请更新浏览器！
            </video>

            <!-- 广告容器（只覆盖视频区域） -->
            <div id="adContainer" class="ad-container" style="display: none;">
                <div class="ad-content">
                    <video id="adVideo" src="${pageContext.request.contextPath}/ads/sample-ad.mp4"></video>
                </div>
            </div>
        </div>

        <!-- 自定义控制栏（保持原样式，不被广告覆盖） -->
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

<!-- 引入jQuery -->
<script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
<script>
    // 全局变量初始化
    const videoPlayer = document.getElementById('videoPlayer');
    const videoId = Number("${video.id}");
    const videoType = "${video.type}";
    // 获取contextPath，如果为空则从当前URL中提取
    let contextPath = "${pageContext.request.contextPath}";
    if (!contextPath || contextPath === '') {
        // 从当前路径中提取contextPath（例如从 /video-website-1.0-SNAPSHOT/play?id=1 提取 /video-website-1.0-SNAPSHOT）
        const pathname = window.location.pathname;
        const pathParts = pathname.split('/').filter(p => p);
        if (pathParts.length > 0 && pathParts[0] !== 'play') {
            contextPath = '/' + pathParts[0];
        } else {
            contextPath = '';
        }
    }
    const origin = window.location.origin || "";
    console.log('[调试] contextPath:', contextPath);

    // 行为埋点核心变量
    let score = 0;
    let isSubmitted = false;
    let isHalfWatched = false;
    let isClicked = false;
    let isDraggingProgress = false;
    let anonymousUserId = "";

    // ===================== 广告相关变量 =====================
    const adContainer = document.getElementById('adContainer');
    const adVideo = document.getElementById('adVideo');

    let adIntervalTimer = null;  // 30秒间隔计时器
    let isAdPlaying = false;     // 广告播放状态
    let videoTimeBeforeAd = 0;   // 广告前视频位置
    let adStartTime = 0;         // 广告开始时间（防跳过）
    let cachedAdList = [];      // 缓存的广告列表
    let currentAdIndex = 0;     // 当前播放的广告索引
    let adClicksStats = [];     // 广告点击统计数组 [{adId: xxx, clicks: 0}, ...]
    let currentAdId = null;     // 当前正在播放的广告ID

    // 广告视频绝对地址（包含协议/域名/端口/上下文），避免 404（默认广告）
    const AD_VIDEO_URL = "${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/ads/sample-ad.mp4";

    // 预加载广告；声明类型
    adVideo.preload = 'auto';
    adVideo.setAttribute('type', 'video/mp4');
    adVideo.src = AD_VIDEO_URL;
    adVideo.load();

    // ===================== 广告加载容错 =====================
    let adRetry = 0;
    let adReady = false;

    // 元数据就绪视为成功，后续 error 不再重试
    const markAdReady = () => { adReady = true; };
    adVideo.addEventListener('loadedmetadata', markAdReady);
    adVideo.addEventListener('canplay', markAdReady);
    adVideo.addEventListener('canplaythrough', markAdReady);

    // 广告加载失败重试（最多2次）；若已 ready 则忽略 error
    adVideo.addEventListener('error', () => {
        if (adReady || adVideo.readyState >= 1) {
            console.warn('[广告] 已有元数据，忽略预加载 error');
            return;
        }
        adRetry += 1;
        console.error('[广告] 加载失败，重试次数：', adRetry, 'url:', AD_VIDEO_URL);
        if (adRetry <= 2) {
            adVideo.src = AD_VIDEO_URL + `?v=${Date.now()}`;
            adVideo.load();
        } else {
            console.warn('[广告] 多次加载失败，停止广告计时器');
            if (adIntervalTimer) {
                clearInterval(adIntervalTimer);
                adIntervalTimer = null;
            }
        }
    });

    // ===================== 隐私追踪判断 =====================
    let isRejectTrack = false;
    try {
        const privacyVal = localStorage.getItem("privacy_rejected");
        isRejectTrack = privacyVal === "true" || privacyVal === true;
    } catch (e) {
        console.warn('[隐私] 本地存储不可用，默认允许追踪：', e);
    }

    // ===================== 匿名ID生成 =====================
    window.getAnonymousUserId = function() {
        let uid = localStorage.getItem('anonymous_user_id');
        if (!uid) {
            uid = 'anon_' + Date.now() + '_' + Math.floor(Math.random() * 1000000);
            try {
                localStorage.setItem('anonymous_user_id', uid);
            } catch (e) {
                uid = 'temp_' + Date.now() + '_' + Math.floor(Math.random() * 1000000);
            }
        }
        return uid;
    };

    if (!isRejectTrack) {
        anonymousUserId = window.getAnonymousUserId();
    }

    // ===================== 进度条拖拽逻辑 =====================
    const progressContainer = document.querySelector('.progress-container');
    const playedBar = document.querySelector('.progress-played');
    const handle = document.querySelector('.progress-handle');

    // 进度条拖拽处理函数（按照原来的逻辑）
    function handleDrag(e) {
        if (!isDraggingProgress || isAdPlaying || !progressContainer || isNaN(videoPlayer.duration)) return;
        const rect = progressContainer.getBoundingClientRect();
        let dragPosition = (e.clientX - rect.left) / rect.width;
        // 限制拖拽范围在0-1之间
        dragPosition = Math.max(0, Math.min(1, dragPosition));
        videoPlayer.currentTime = dragPosition * videoPlayer.duration;
        updateProgressUI();
    }

    if (progressContainer) {
        // 点击进度条跳转（原来的逻辑）
        progressContainer.addEventListener('click', (e) => {
            if (isAdPlaying) return; // 广告播放时禁止点击跳转
            const rect = progressContainer.getBoundingClientRect();
            const clickPosition = (e.clientX - rect.left) / rect.width;
            videoPlayer.currentTime = clickPosition * videoPlayer.duration;
            updateProgressUI();
        });

        // 拖拽进度条手柄（按照原来的逻辑）
        if (handle) {
            handle.addEventListener('mousedown', () => {
                if (isAdPlaying) return; // 广告播放时禁止拖拽
                isDraggingProgress = true;
            });
            handle.addEventListener('touchstart', () => {
                if (isAdPlaying) return; // 广告播放时禁止拖拽
                isDraggingProgress = true;
            });
        }

        // 全局鼠标/触摸移动处理（按照原来的逻辑）
        document.addEventListener('mousemove', handleDrag);
        document.addEventListener('touchmove', (e) => {
            if (isDraggingProgress && !isAdPlaying && !isNaN(videoPlayer.duration)) {
                const rect = progressContainer.getBoundingClientRect();
                const clientX = e.touches ? e.touches[0].clientX : e.clientX;
                let dragPosition = (clientX - rect.left) / rect.width;
                dragPosition = Math.max(0, Math.min(1, dragPosition));
                videoPlayer.currentTime = dragPosition * videoPlayer.duration;
                updateProgressUI();
            }
        });
        document.addEventListener('mouseup', () => {
            isDraggingProgress = false;
        });
        document.addEventListener('touchend', () => {
            isDraggingProgress = false;
        });
    }

    // ===================== 格式化时间 & UI刷新 =====================
    function formatTime(seconds) {
        if (isNaN(seconds) || seconds < 0) return "00:00";
        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }

    function updateProgressUI() {
        if (isNaN(videoPlayer.duration)) return;
        const progress = (videoPlayer.currentTime / videoPlayer.duration) * 100;
        if (playedBar) playedBar.style.width = `${progress}%`;
        if (handle) handle.style.left = `${progress}%`;
        document.getElementById('currentTime').textContent = formatTime(videoPlayer.currentTime);
        document.getElementById('totalTime').textContent = formatTime(videoPlayer.duration);
    }

    // ===================== 广告核心逻辑 =====================

    // 请求广告（播放时调用，score=0）
    function requestAds() {
        console.log('[广告] 开始请求广告列表');
        console.log('[广告] contextPath变量值:', contextPath, '类型:', typeof contextPath);
        // 构建请求URL - 如果contextPath为空字符串，使用空字符串；否则使用contextPath
        // 注意：使用字符串拼接而不是模板字符串，避免JSP EL表达式冲突
        const basePath = (contextPath && contextPath.trim()) ? contextPath.trim() : '';
        const requestUrl = basePath ? (basePath + '/requestAd') : '/requestAd';
        console.log('[广告] basePath:', basePath);
        console.log('[广告] 最终请求URL:', requestUrl);
        const requestData = {
            tag: videoType,
            platform: "video",
            anonymousUserId: anonymousUserId || undefined,
            score: 0  // 固定为0，用于获取广告
        };

        // 移除undefined字段
        Object.keys(requestData).forEach(key => {
            if (requestData[key] === undefined) {
                delete requestData[key];
            }
        });

        const jsonData = JSON.stringify(requestData);
        console.log('[广告] 发送给API的JSON数据:', jsonData);
        console.log('[广告] 发送给API的请求对象:', requestData);

        fetch(requestUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json;charset=utf-8'
            },
            body: jsonData
        })
        .then(response => {
            // 检查响应状态
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            // 检查Content-Type是否为JSON
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                return response.text().then(text => {
                    throw new Error(`服务器返回非JSON格式: ${text.substring(0, 100)}`);
                });
            }
            return response.json();
        })
        .then(data => {
            console.log('[广告] 广告请求响应:', data);
            console.log('[广告] 响应数据类型:', typeof data);
            console.log('[广告] 响应数据code:', data.code);
            console.log('[广告] 响应数据success:', data.success);
            console.log('[广告] 响应数据ads:', data.ads);
            console.log('[广告] ads是否为数组:', Array.isArray(data.ads));
            console.log('[广告] ads数组长度:', data.ads ? data.ads.length : 'null/undefined');
            
            // 解析广告数据（数组格式：{code: 200, ads: [...]}）
            if (data.code === 200 || data.success === true) {
                if (data.ads && Array.isArray(data.ads)) {
                    if (data.ads.length > 0) {
                        // 遍历广告数组，添加到缓存列表并初始化点击统计
                        data.ads.forEach((ad, index) => {
                            console.log('[广告] 处理第 ' + (index + 1) + ' 个广告:', ad);
                            cachedAdList.push(ad);
                            // 如果有广告ID，初始化该广告的点击统计
                            const adIdValue = ad.id || ad.adId;
                            if (adIdValue) {
                                // 检查是否已存在该广告的统计，如果不存在则添加
                                const existingStat = adClicksStats.find(stat => stat.adId === adIdValue);
                                if (!existingStat) {
                                    adClicksStats.push({adId: adIdValue, clicks: 0});
                                    console.log('[广告] 初始化广告点击统计, adId: ' + adIdValue);
                                }
                            } else {
                                console.warn('[广告] 警告：广告对象缺少id和adId字段:', ad);
                            }
                        });
                        console.log('[广告] ✅ 获取到 ' + data.ads.length + ' 个广告，已添加到列表，当前列表长度: ' + cachedAdList.length);
                    } else {
                        console.warn('[广告] ⚠️ ads数组为空，长度: ' + data.ads.length);
                    }
                } else {
                    console.warn('[广告] ⚠️ ads字段不存在或不是数组:', typeof data.ads, data.ads);
                }
                
                // 如果ads数组为空，使用默认广告
                if (!data.ads || !Array.isArray(data.ads) || data.ads.length === 0) {
                    console.warn('[广告] ⚠️ 未获取到有效广告，将使用默认广告');
                }
            } else {
                console.warn('[广告] ⚠️ 广告请求失败，code: ' + data.code + ', msg: ' + (data.msg || data.message));
            }
        })
        .catch(err => {
            console.error('[广告] ❌ 请求广告失败，将使用默认广告:', err.message || err);
            cachedAdList = [];
        });
    }

    // 获取下一个广告URL（从缓存列表中选择，如果列表为空则使用默认广告）
    function getNextAdUrl() {
        if (cachedAdList.length > 0) {
            // 从缓存的广告列表中选择（循环使用）
            const ad = cachedAdList[currentAdIndex % cachedAdList.length];
            currentAdIndex++;
            
            // 根据实际API返回的字段名调整（可能是 videoUrl, url, video, src 等）
            const adUrl = ad.videoUrl || ad.url || ad.video || ad.src || ad.videoPath;
            if (adUrl) {
                console.log('[广告] 使用广告服务器返回的广告:', adUrl);
                return adUrl;
            }
        }
        
        // 如果列表为空或没有有效URL，使用默认广告
        console.log('[广告] 使用默认广告:', AD_VIDEO_URL);
        return AD_VIDEO_URL;
    }

    // 启动30秒广告计时器
    function startAdTimer() {
        if (adIntervalTimer) clearInterval(adIntervalTimer);

        console.log('[广告] 启动30秒计时器');
        adIntervalTimer = setInterval(() => {
            if (!isAdPlaying && !videoPlayer.paused) {
                console.log('[广告] 30秒到达，触发广告');
                showAd();
            }
        }, 30000);
    }

    // 显示广告
    function showAd() {
        if (isAdPlaying) {
            console.warn('[广告] 广告已在播放中');
            return;
        }

        try {
            console.log('[广告] 准备播放广告');

            // 1. 记录原视频状态并暂停
            videoTimeBeforeAd = videoPlayer.currentTime;
            videoPlayer.pause();

            // 2. 设置广告播放状态
            isAdPlaying = true;
            adStartTime = Date.now();

            // 标记容器正在播放广告（用于CSS控制进度条等）
            const rootContainer = document.querySelector('.video-player-container');
            if (rootContainer) {
                rootContainer.classList.add('ad-playing');
            }

            // 3. 显示广告容器并调整位置（覆盖视频区域，不覆盖控制栏）
            updateAdContainerPosition();

            // 4. 禁用倍速选择器和锁定时间显示
            const playbackRateSelect = document.getElementById('playbackRateSelect');
            playbackRateSelect.disabled = true;
            playbackRateSelect.style.opacity = '0.5';
            playbackRateSelect.style.cursor = 'not-allowed';

            // 锁定时间显示（视觉上）
            const timeDisplay = document.querySelector('.time-display');
            if (timeDisplay) {
                timeDisplay.style.opacity = '0.5';
            }

            // 4. 设置广告视频源并播放（从缓存的广告列表中选择，失败则使用默认广告）
            const adUrl = getNextAdUrl();
            const currentAd = cachedAdList[currentAdIndex - 1]; // 获取当前播放的广告对象
            // 设置当前播放的广告ID
            if (currentAd && (currentAd.id || currentAd.adId)) {
                currentAdId = currentAd.id || currentAd.adId;
                console.log('[广告] 当前播放广告ID:', currentAdId);
            } else {
                currentAdId = null; // 默认广告没有ID
            }
            adVideo.src = adUrl;
            adVideo.load();
            adVideo.currentTime = 0;
            adVideo.muted = false; // 确保有声音
            adVideo.volume = 1.0;

            // 5. 播放广告
            const playPromise = adVideo.play();
            if (playPromise !== undefined) {
                playPromise
                    .then(() => {
                        console.log('[广告] 广告播放成功');
                    })
                    .catch(err => {
                        console.error('[广告] 播放失败：', err);
                        closeAd();
                    });
            }

            // 6. 停止30秒计时器
            if (adIntervalTimer) {
                clearInterval(adIntervalTimer);
                adIntervalTimer = null;
            }

        } catch (e) {
            console.error('[广告] showAd失败：', e);
            closeAd();
        }
    }

    // 关闭广告
    function closeAd() {
        console.log('[广告] 广告播放完成，恢复视频');

        // 1. 隐藏广告容器
        adContainer.classList.remove('show');
        adContainer.style.display = 'none';

        // 2. 重置广告状态
        isAdPlaying = false;
        adVideo.pause();
        adVideo.currentTime = 0;
        adVideo.src = ''; // 清空src，防止后台播放

        // 3. 恢复倍速选择器和时间显示
        const playbackRateSelect = document.getElementById('playbackRateSelect');
        playbackRateSelect.disabled = false;
        playbackRateSelect.style.opacity = '1';
        playbackRateSelect.style.cursor = 'pointer';

        const timeDisplay = document.querySelector('.time-display');
        if (timeDisplay) {
            timeDisplay.style.opacity = '1';
        }

        // 移除容器上的广告状态标记
        const rootContainer = document.querySelector('.video-player-container');
        if (rootContainer) {
            rootContainer.classList.remove('ad-playing');
        }

        // 4. 恢复原视频播放
        videoPlayer.currentTime = videoTimeBeforeAd;
        videoPlayer.play();

        // 5. 重新启动30秒计时器
        startAdTimer();
    }

    // 根据全屏状态更新广告容器位置（容器全屏时，广告随容器放大，但只覆盖视频区域）
    function updateAdContainerPosition() {
        const container = document.querySelector('.video-screen');
        if (!isAdPlaying || !container) return;

        // 无论是否全屏，广告都覆盖整个视频容器；
        // 当容器进入全屏时，自然就是全屏广告
        adContainer.style.position = 'absolute';
        adContainer.style.top = '0';
        adContainer.style.left = '0';
        adContainer.style.width = '100%';
        adContainer.style.height = '100%';
        adContainer.style.zIndex = '20';

        if (isAdPlaying) {
            adContainer.classList.add('show');
            adContainer.style.display = 'flex';
        }
    }

    // 广告播放完成事件
    adVideo.addEventListener('ended', function() {
        console.log('[广告] 广告视频播放完毕');
        closeAd();
    });

    // 防止用户跳过广告
    adVideo.addEventListener('seeking', function(e) {
        if (isAdPlaying) {
            const elapsed = (Date.now() - adStartTime) / 1000;
            if (adVideo.currentTime > elapsed + 1) {
                console.warn('[广告] 禁止快进');
                adVideo.currentTime = elapsed;
            }
        }
    });

    // 禁止广告播放时操作原视频
    videoPlayer.addEventListener('play', function(e) {
        if (isAdPlaying) {
            e.preventDefault();
            videoPlayer.pause();
            console.warn('[广告] 广告播放中，禁止操作原视频');
        }
    });

    // 禁止广告播放时暂停广告
    adVideo.addEventListener('pause', function() {
        if (isAdPlaying && !adVideo.ended) {
            console.warn('[广告] 禁止暂停广告');
            adVideo.play();
        }
    });

    // 统计广告点击次数
    adContainer.addEventListener('click', function() {
        if (isAdPlaying && currentAdId) {
            // 找到当前广告的统计项并增加clicks
            const stat = adClicksStats.find(s => s.adId === currentAdId);
            if (stat) {
                stat.clicks++;
                console.log('[广告] 广告点击次数, adId: ' + currentAdId + ', clicks: ' + stat.clicks);
            }
        }
    });

    // ===================== 播放器基础控制 =====================
    document.getElementById('playPauseBtn').addEventListener('click', function() {
        if (isAdPlaying) {
            console.warn('[广告] 广告播放中，禁止操作');
            return;
        }

        if (videoPlayer.paused) {
            videoPlayer.play();
            this.innerHTML = '<i>❚❚</i>';
        } else {
            videoPlayer.pause();
            this.innerHTML = '<i>▶</i>';
        }
    });

    if (progressContainer) {
        progressContainer.addEventListener('click', function(e) {
            if (isAdPlaying) return; // 广告播放时禁止点击跳转
            seekVideo(e);
        });
    }

    document.getElementById('volumeSlider').addEventListener('input', function() {
        videoPlayer.volume = this.value;
        document.getElementById('muteBtn').innerHTML = this.value > 0 ? '<i>🔊</i>' : '<i>🔇</i>';
    });

    document.getElementById('muteBtn').addEventListener('click', function() {
        videoPlayer.muted = !videoPlayer.muted;
        this.innerHTML = videoPlayer.muted ? '<i>🔇</i>' : '<i>🔊</i>';
        document.getElementById('volumeSlider').value = videoPlayer.muted ? 0 : videoPlayer.volume;
    });

    document.getElementById('playbackRateSelect').addEventListener('change', function() {
        videoPlayer.playbackRate = this.value;
    });

    // 全屏功能 - 以视频容器为单位全屏，这样广告和控制栏都会一起进入全屏
    document.getElementById('fullscreenBtn').addEventListener('click', toggleFullscreen);

    function getFullscreenContainer() {
        return document.querySelector('.video-player-container');
    }

    function isContainerFullscreen() {
        const container = getFullscreenContainer();
        return document.fullscreenElement === container
            || document.webkitFullscreenElement === container
            || document.mozFullScreenElement === container
            || document.msFullscreenElement === container;
    }

    function toggleFullscreen() {
        const fullscreenBtn = document.getElementById('fullscreenBtn');
        const container = getFullscreenContainer();

        if (!isContainerFullscreen()) {
            // 进入全屏：让整个容器全屏（包含视频、广告、控制栏）
            if (container.requestFullscreen) {
                container.requestFullscreen();
            } else if (container.mozRequestFullScreen) { // Firefox
                container.mozRequestFullScreen();
            } else if (container.webkitRequestFullscreen) { // Chrome、Safari
                container.webkitRequestFullscreen();
            } else if (container.msRequestFullscreen) { // IE/Edge
                container.msRequestFullscreen();
            }
            fullscreenBtn.innerHTML = '<i>⛌</i>';
        } else {
            // 退出全屏
            if (document.exitFullscreen) {
                document.exitFullscreen();
            } else if (document.mozCancelFullScreen) {
                document.mozCancelFullScreen();
            } else if (document.webkitExitFullscreen) {
                document.webkitExitFullscreen();
            } else if (document.msExitFullscreen) {
                document.msExitFullscreen();
            }
            fullscreenBtn.innerHTML = '<i>⛶</i>';
        }
    }

    // 监听全屏状态变化，更新按钮图标并调整广告容器
    function updateFullscreenIcon() {
        const fullscreenBtn = document.getElementById('fullscreenBtn');
        const fullscreen = isContainerFullscreen();
        fullscreenBtn.innerHTML = fullscreen ? '<i>⛌</i>' : '<i>⛶</i>';

        // 如果广告正在播放，调整广告容器位置以适应全屏状态
        if (isAdPlaying) {
            updateAdContainerPosition();
        }
    }
    document.addEventListener('fullscreenchange', updateFullscreenIcon);
    document.addEventListener('mozfullscreenchange', updateFullscreenIcon);
    document.addEventListener('webkitfullscreenchange', updateFullscreenIcon);
    document.addEventListener('msfullscreenchange', updateFullscreenIcon);

    // ===================== 视频时间更新 =====================
    videoPlayer.addEventListener('loadedmetadata', updateProgressUI);
    videoPlayer.addEventListener('canplay', updateProgressUI);
    videoPlayer.addEventListener('durationchange', updateProgressUI);
    videoPlayer.addEventListener('timeupdate', function() {
        if (isDraggingProgress || isAdPlaying) return;
        updateProgressUI();

        if (!isRejectTrack && !isHalfWatched && !isNaN(videoPlayer.duration) && videoPlayer.duration > 0) {
            if (videoPlayer.currentTime / videoPlayer.duration >= 0.5) {
                score += 2;
                isHalfWatched = true;
                console.log(`[埋点] 观看过半，分数：${score}`);
            }
        }
    });

    // ===================== 埋点逻辑 =====================
    videoPlayer.addEventListener('play', function() {
        if (!isRejectTrack && !isClicked) {
            score += 1;
            isClicked = true;
            console.log(`[埋点] 点击播放，分数：${score}`);
        }
        document.getElementById('playPauseBtn').innerHTML = '<i>❚❚</i>';

        // 播放时立即请求广告（score=0）
        requestAds();

        if (!adIntervalTimer && !isAdPlaying) {
            startAdTimer();
        }
    });

    videoPlayer.addEventListener('pause', function() {
        if (!isAdPlaying) {
            document.getElementById('playPauseBtn').innerHTML = '<i>▶</i>';
        }
    });

    // 首次加载后强制刷新一次时间显示，避免出现仅“/”的情况
    document.addEventListener('DOMContentLoaded', () => {
        updateProgressUI();
    });

    videoPlayer.addEventListener('ended', function() {
        if (!isRejectTrack) {
            score = 3;
            console.log(`[埋点] 播放完成，分数：${score}`);
        }
        document.getElementById('playPauseBtn').innerHTML = '<i>▶</i>';

        if (adIntervalTimer) {
            clearInterval(adIntervalTimer);
            adIntervalTimer = null;
        }
    });

    window.addEventListener('beforeunload', submitScore);
    document.querySelector('.back-btn').addEventListener('click', submitScore);

    function submitScore() {
        if (isRejectTrack || score <= 0 || isSubmitted) {
            console.log('[埋点] 跳过提交 - isRejectTrack:', isRejectTrack, 'score:', score, 'isSubmitted:', isSubmitted);
            return;
        }

        const submitData = {
            tag: videoType,
            platform: "video",
            anonymousUserId: anonymousUserId,
            score: score,
            adClicks: adClicksStats.length > 0 ? adClicksStats : undefined  // 广告点击统计数组
        };

        // 移除undefined字段
        Object.keys(submitData).forEach(key => {
            if (submitData[key] === undefined) {
                delete submitData[key];
            }
        });

        // 构建提交URL - 如果contextPath为空字符串，使用空字符串；否则使用contextPath
        // 注意：使用字符串拼接而不是模板字符串，避免JSP EL表达式冲突
        const basePath2 = (contextPath && contextPath.trim()) ? contextPath.trim() : '';
        const submitUrl = basePath2 ? (basePath2 + '/submitScore') : '/submitScore';
        
        // 使用 fetch 发送 JSON（sendBeacon 不支持自定义 Content-Type）
        const jsonData = JSON.stringify(submitData);
        console.log('[埋点] ========== 提交用户行为数据 ==========');
        console.log('[埋点] 提交URL：', submitUrl);
        console.log('[埋点] 发送给API的JSON数据:', jsonData);
        console.log('[埋点] 发送给API的请求对象:', submitData);
        fetch(submitUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json;charset=utf-8'
            },
            body: jsonData,
            keepalive: true  // 页面关闭时也能发送
        })
        .then(response => {
            console.log('[埋点] 提交响应状态：', response.status);
            return response.text();
        })
        .then(data => {
            console.log('[埋点] 提交响应内容：', data);
            isSubmitted = true;
        })
        .catch(err => {
            console.error('[埋点] 提交失败：', err);
        });
    }

    window.addEventListener('beforeunload', () => {
        if (adIntervalTimer) clearInterval(adIntervalTimer);
    });
</script>
</body>
</html>