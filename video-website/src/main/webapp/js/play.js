// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const video = document.getElementById('videoPlayer');
    const playPauseBtn = document.getElementById('playPauseBtn');
    const progressContainer = document.querySelector('.progress-container');
    const progressPlayed = document.querySelector('.progress-played');
    const progressHandle = document.querySelector('.progress-handle');
    const currentTimeEl = document.getElementById('currentTime');
    const totalTimeEl = document.getElementById('totalTime');
    const muteBtn = document.getElementById('muteBtn');
    const volumeSlider = document.getElementById('volumeSlider');
    const playbackRateSelect = document.getElementById('playbackRateSelect');
    const fullscreenBtn = document.getElementById('fullscreenBtn');

    // 1. 播放/暂停功能
    playPauseBtn.addEventListener('click', togglePlayPause);
    video.addEventListener('click', togglePlayPause);

    function togglePlayPause() {
        if (video.paused) {
            video.play();
            playPauseBtn.querySelector('i').textContent = '❚❚';
        } else {
            video.pause();
            playPauseBtn.querySelector('i').textContent = '▶';
        }
    }

    // 视频播放状态变化时更新按钮
    video.addEventListener('play', () => {
        playPauseBtn.querySelector('i').textContent = '❚❚';
    });
    video.addEventListener('pause', () => {
        playPauseBtn.querySelector('i').textContent = '▶';
    });

    // 2. 进度条功能（显示进度、拖拽调整）
    let isDragging = false;

    // 视频加载完成后设置总时长
    video.addEventListener('loadedmetadata', updateTotalTime);

    // 视频播放时更新进度条和当前时间
    video.addEventListener('timeupdate', updateProgress);

    // 点击进度条跳转
    progressContainer.addEventListener('click', (e) => {
        const rect = progressContainer.getBoundingClientRect();
        const clickPosition = (e.clientX - rect.left) / rect.width;
        video.currentTime = clickPosition * video.duration;
    });

    // 拖拽进度条手柄
    progressHandle.addEventListener('mousedown', () => isDragging = true);
    document.addEventListener('mousemove', handleDrag);
    document.addEventListener('mouseup', () => isDragging = false);

    function handleDrag(e) {
        if (!isDragging) return;
        const rect = progressContainer.getBoundingClientRect();
        let dragPosition = (e.clientX - rect.left) / rect.width;
        // 限制拖拽范围在0-1之间
        dragPosition = Math.max(0, Math.min(1, dragPosition));
        video.currentTime = dragPosition * video.duration;
        updateProgress();
    }

    // 更新进度条和当前时间
    function updateProgress() {
        const progress = (video.currentTime / video.duration) * 100;
        progressPlayed.style.width = `${progress}%`;
        progressHandle.style.left = `${progress}%`;
        currentTimeEl.textContent = formatTime(video.currentTime);
    }

    // 格式化时间（秒 → 分:秒，如 125 → 02:05）
    function formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = Math.floor(seconds % 60);
        return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
    }

    // 更新总时长
    function updateTotalTime() {
        totalTimeEl.textContent = formatTime(video.duration);
    }

    // 3. 音量控制
    // 静音切换
    muteBtn.addEventListener('click', toggleMute);

    function toggleMute() {
        video.muted = !video.muted;
        volumeSlider.value = video.muted ? 0 : video.volume;
        muteBtn.querySelector('i').textContent = video.muted ? '🔇' : '🔊';
    }

    // 音量滑块调整
    volumeSlider.addEventListener('input', (e) => {
        const volume = parseFloat(e.target.value);
        video.volume = volume;
        video.muted = volume === 0;
        muteBtn.querySelector('i').textContent = video.muted ? '🔇' : '🔊';
    });

    // 4. 倍速切换
    playbackRateSelect.addEventListener('change', (e) => {
        video.playbackRate = parseFloat(e.target.value);
    });

    // 5. 全屏功能
    fullscreenBtn.addEventListener('click', toggleFullscreen);

    function toggleFullscreen() {
        if (!document.fullscreenElement) {
            // 进入全屏
            if (video.requestFullscreen) {
                video.requestFullscreen();
            } else if (video.mozRequestFullScreen) { // Firefox
                video.mozRequestFullScreen();
            } else if (video.webkitRequestFullscreen) { // Chrome、Safari
                video.webkitRequestFullscreen();
            } else if (video.msRequestFullscreen) { // IE/Edge
                video.msRequestFullscreen();
            }
            fullscreenBtn.querySelector('i').textContent = '⛌';
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
            fullscreenBtn.querySelector('i').textContent = '⛶';
        }
    }

    // 监听全屏状态变化，更新按钮图标
    document.addEventListener('fullscreenchange', updateFullscreenIcon);
    document.addEventListener('mozfullscreenchange', updateFullscreenIcon);
    document.addEventListener('webkitfullscreenchange', updateFullscreenIcon);
    document.addEventListener('msfullscreenchange', updateFullscreenIcon);

    function updateFullscreenIcon() {
        fullscreenBtn.querySelector('i').textContent = document.fullscreenElement ? '⛌' : '⛶';
    }
});