// 广告系统API配置
const API_CONFIG = {
    getAds: "http://10.100.164.6:8080/ad-management/api/external/api/getAds"
};

// 新增：动态获取项目上下文路径（适配任意环境，不硬编码）
function getContextPath() {
    const pathname = window.location.pathname;
    const contextPath = pathname.split('/')[1];
    return contextPath ? `/${contextPath}` : '';
}

// 1. 生成唯一设备ID（保留，作为用户唯一标识）
function getDeviceId() {
    let deviceId = localStorage.getItem('news_device_id');
    if (!deviceId) {
        deviceId = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            const r = Math.random() * 16 | 0;
            const v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
        localStorage.setItem('news_device_id', deviceId);
    }
    return `user ${deviceId}`;
}

// 2. 上报用户行为（新增：上报成功后刷新广告）
function reportUserBehavior(tag, score) {
    const deviceId = getDeviceId();
    const requestData = {
        anonymousUserId: deviceId,
        tag: tag,
        score: score,
        platform: "news" // 标记为新闻平台
    };

    return fetch(API_CONFIG.getAds, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(requestData)
    })
        .then(res => {
            console.log('权重上报响应状态：', res.status);
            return res.json().catch(() => ({code: res.status, msg: '非JSON响应'}));
        })
        .then(data => {
            console.log('权重上报结果：', data);
            // 核心修改：上报成功后立即刷新所有广告，让权重生效
            if (data.code === 200) {
                refreshAdsOnPageChange();
                console.log('权重上报成功，已刷新广告列表');
            }
            return data;
        })
        .catch(err => {
            console.error('权重上报失败：', err);
            return {code: 500, msg: '接口异常'};
        });
}

// 3. 上报广告点击量（按广告系统格式）
function reportAdClick(adId, tag) {
    const deviceId = getDeviceId();
    const requestData = {
        anonymousUserId: deviceId,
        tag: tag,
        score: 1, // 点击行为默认score=1
        platform: "news",
        adClicks: [{adId: adId, clicks: 1}]
    };

    return fetch(API_CONFIG.getAds, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(requestData)
    })
        .then(res => res.json().catch(() => ({code: res.status, msg: '非JSON响应'})))
        .then(data => {
            console.log('点击量上报结果：', data);
            // 广告点击后也刷新广告
            if (data.code === 200) {
                refreshAdsOnPageChange();
                console.log('广告点击上报成功，已刷新广告列表');
            }
            return data;
        })
        .catch(err => {
            console.error('点击量上报失败：', err);
            return {code: 500, msg: '接口异常'};
        });
}

// 4. 请求广告（核心：前端控制广告数量+动态路径+无闪烁）
function getRecommendAds(containerId, currentTag = 'others') {
    const deviceId = getDeviceId();
    const adContainer = document.getElementById(containerId);
    if (!adContainer) return;

    // 动态获取上下文 + 拼接图片路径
    const contextPath = getContextPath();
    const defaultAdImg = `${contextPath}/images/ad-default.jpg`;

    // 初始隐藏容器，避免兜底图闪烁
    adContainer.style.display = 'none';

    // 兜底广告
    adContainer.innerHTML = `
        <div class="news-card ad-card" data-tag="${currentTag}">
            <span class="ad-tag">广告</span>
            <img src="${defaultAdImg}" alt="广告" class="news-img">
            <h3 class="news-title">【广告】加载中...</h3>
        </div>
    `;

    // 请求广告（广告系统基于anonymousUserId返回聚合权重后的广告）
    fetch(API_CONFIG.getAds, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            anonymousUserId: deviceId,
            tag: currentTag,
            platform: "news",
            limit: 3 // 请求3个广告
        })
    })
        .then(res => res.json())
        .then(adData => {
            // 打印广告数据，方便调试
            console.log('广告系统返回的完整数据：', adData);
            console.log('广告数组内容：', adData?.ads);

            // 适配广告系统响应格式
            if (adData && adData.code === 200 && adData.ads && adData.ads.length > 0) {
                // 核心修改：前端强制截取前3个广告，解决返回5个的问题
                const filteredAds = adData.ads.slice(0, 3);
                // 随机选一个广告（从筛选后的3个里选）
                const randomAd = filteredAds[Math.floor(Math.random() * filteredAds.length)];

                // 修改后（动态拼接广告标题）
                adContainer.innerHTML = `
                   <div class="news-card ad-card" data-adid="${randomAd.adId}" data-tag="${currentTag}">
                     <span class="ad-tag">广告</span>
                     <img src="${randomAd.url || defaultAdImg}" alt="广告" class="news-img">
                     <h3 class="news-title">
                            【广告】猜你喜欢${randomAd.title ? '：' + randomAd.title : ''}
                     </h3>
                   </div>
                `;
                // 点击事件：上报+跳转
                adContainer.querySelector('.ad-card').addEventListener('click', function() {
                    const adId = this.dataset.adid;
                    const tag = this.dataset.tag;
                    if (adId) {
                        reportAdClick(adId, tag).then(() => {
                            window.open(`${contextPath}/ad/detail?adId=${adId}`, '_blank');
                        });
                    }
                });
            }
            // 显示容器（无论是否有广告，最后都显示）
            adContainer.style.display = 'block';
        })
        .catch(err => {
            console.error('广告请求失败：', err);
            // 报错时显示兜底广告并展示容器
            adContainer.innerHTML = `
                <div class="news-card ad-card" data-tag="${currentTag}">
                    <span class="ad-tag">广告</span>
                    <img src="${defaultAdImg}" alt="广告" class="news-img">
                    <h3 class="news-title">广告加载失败</h3>
                </div>
            `;
            adContainer.style.display = 'block';
        });
}

// 5. 页面切换时刷新广告（通用函数）
function refreshAdsOnPageChange() {
    // 刷新首页广告
    if (document.getElementById('index-ad-container')) {
        getRecommendAds('index-ad-container', 'digital');
    }
    // 刷新分类页广告
    document.querySelectorAll('[id^="ad-container-"]').forEach(container => {
        const tag = container.dataset.tag || 'others';
        getRecommendAds(container.id, tag);
    });
    // 刷新搜索页广告
    if (document.getElementById('search-ad-container')) {
        const keyword = document.querySelector('.search-container h2')?.innerText || '';
        let tag = 'others';
        if (keyword.includes('食品') || keyword.includes('美食')) tag = 'food';
        else if (keyword.includes('数码')) tag = 'digital';
        else if (keyword.includes('服装')) tag = 'clothes';
        else if (keyword.includes('美妆')) tag = 'makeup';
        else if (keyword.includes('运动')) tag = 'sport';
        else if (keyword.includes('书籍')) tag = 'book';
        getRecommendAds('search-ad-container', tag);
    }
}

// 6. 监听导航点击，刷新广告
document.addEventListener('DOMContentLoaded', function() {
    // 页面加载完成后先初始化广告
    refreshAdsOnPageChange();

    // 导航点击刷新广告
    document.querySelectorAll('.nav a').forEach(link => {
        link.addEventListener('click', () => setTimeout(refreshAdsOnPageChange, 100));
    });
});

// 测试函数
function testTrackBehavior(tag = 'digital', score = 3) {
    reportUserBehavior(tag, score).then(data => {
        alert(data.code === 200 ? '上报成功' : `上报失败：${data.msg}`);
    });
}