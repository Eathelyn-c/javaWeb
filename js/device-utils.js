// 保留原有常量定义
const API_CONFIG = {
    getAds: 'http://10.100.164.6:8080/ad-management/api/external/api/getAds'
};

// ========== 核心：最近10次行为 + 纯分数累加逻辑 ==========
// 初始化用户最近10次行为队列（localStorage）
function initRecentBehaviors() {
    let behaviors = localStorage.getItem('userRecentBehaviors');
    if (!behaviors) {
        behaviors = JSON.stringify([]); // 队列初始为空
    }
    return JSON.parse(behaviors);
}

// 新增行为到队列（最多保留10条，超过则删除最早的）
function addRecentBehavior(tag, score) {
    let behaviors = initRecentBehaviors();
    // 新增行为（仅记录tag和score，无其他冗余字段）
    behaviors.push({ tag: tag || 'others', score: Number(score) || 0 });
    // 严格限制最多10条，超过则删除最早期的记录
    if (behaviors.length > 10) {
        behaviors.shift(); // 移除数组第一个元素（最早的行为）
    }
    localStorage.setItem('userRecentBehaviors', JSON.stringify(behaviors));
    console.log('最近10次行为队列：', behaviors);
    return behaviors;
}

// 核心：纯分数累加 → 取总分最高的tag（无“次数”相关逻辑）
function getTopRecentTag() {
    const behaviors = initRecentBehaviors();
    if (behaviors.length === 0) {
        return 'digital'; // 无行为时默认tag
    }

    // 步骤1：对最近10次行为的tag做纯分数累加
    const tagTotalScores = {};
    behaviors.forEach(behavior => {
        const { tag, score } = behavior;
        // 初始化tag的总分（不存在则为0）
        if (!tagTotalScores[tag]) {
            tagTotalScores[tag] = 0;
        }
        // 仅做分数累加（核心：只加分数，不统计次数）
        tagTotalScores[tag] += score;
    });

    // 步骤2：找出累加总分最高的tag
    let topTag = 'others';
    let maxTotalScore = 0;
    // 遍历所有tag的累加总分，找最大值
    for (const [tag, totalScore] of Object.entries(tagTotalScores)) {
        if (totalScore > maxTotalScore) {
            maxTotalScore = totalScore;
            topTag = tag;
        }
    }

    console.log('各tag累加总分：', tagTotalScores, '｜最高总分tag：', topTag);
    return topTag;
}

// ========== 原有函数（仅调用上述核心函数，逻辑不变） ==========
// 获取设备ID（未改动）
function getDeviceId() {
    let deviceId = localStorage.getItem('anonymousUserId');
    if (!deviceId) {
        deviceId = 'user-' + Math.random().toString(36).substr(2, 16);
        localStorage.setItem('anonymousUserId', deviceId);
    }
    return deviceId;
}

// 获取上下文路径（未改动）
function getContextPath() {
    let pathName = window.location.pathname;
    let index = pathName.substr(1).indexOf('/');
    let result = pathName.substr(0, index + 1);
    return result;
}

// 广告请求：使用纯分数累加的topTag（核心修改仅此处调用）
function getRecommendAds(containerId) {
    const deviceId = getDeviceId();
    const adContainer = document.getElementById(containerId);
    if (!adContainer) {
        console.warn('广告容器不存在:', containerId);
        return;
    }

    const contextPath = getContextPath();
    const defaultAdImg = `${contextPath}/images/ad-default.jpg`;

    adContainer.innerHTML = `
        <div class="news-card ad-card">
            <span class="ad-tag">广告</span>
            <img src="${defaultAdImg}" alt="广告加载中" class="news-img">
            <h3 class="news-title">【广告】加载中...</h3>
        </div>
    `;
    adContainer.style.display = 'block';

    // 核心：获取最近10次行为累加总分最高的tag
    const topTag = getTopRecentTag();
    const requestParams = {
        anonymousUserId: deviceId,
        platform: "news",
        limit: 3,
        tag: topTag // 仅传累加总分最高的tag
    };

    // 后续请求逻辑完全未改动
    fetch(API_CONFIG.getAds, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(requestParams)
    })
        .then(res => res.json())
        .then(adData => {
            if (adData && adData.code === 200 && adData.ads && adData.ads.length > 0) {
                const filteredAds = adData.ads.slice(0, 3);
                const randomAd = filteredAds[Math.floor(Math.random() * filteredAds.length)];

                adContainer.innerHTML = `
               <div class="news-card ad-card" data-adid="${randomAd.adId}" data-tag="${randomAd.categoryName || topTag}">
                 <span class="ad-tag">广告</span>
                 <img src="${randomAd.url || defaultAdImg}" alt="广告" class="news-img">
                 <h3 class="news-title">
                        【广告】猜你喜欢${randomAd.title ? '：' + randomAd.title : ''}
                 </h3>
               </div>
            `;
                console.log('广告加载成功:', containerId, randomAd.adId);
            } else {
                adContainer.innerHTML = `
                <div class="news-card ad-card">
                    <span class="ad-tag">广告</span>
                    <img src="${defaultAdImg}" alt="广告" class="news-img">
                    <h3 class="news-title">【广告】猜你喜欢</h3>
                </div>
            `;
            }
        })
        .catch(err => {
            console.error('广告请求失败：', err);
            adContainer.innerHTML = `
            <div class="news-card ad-card">
                <span class="ad-tag">广告</span>
                <img src="${defaultAdImg}" alt="广告" class="news-img">
                <h3 class="news-title">广告加载失败</h3>
            </div>
        `;
        })
        .finally(() => {
            adContainer.style.display = 'block';
        });
}

// 刷新广告（未改动）
function refreshAdsOnPageChange() {
    console.log('刷新页面广告...');

    if (document.getElementById('index-ad-container')) {
        console.log('刷新首页广告');
        getRecommendAds('index-ad-container');
    }
    document.querySelectorAll('[id^="ad-container-"]').forEach(container => {
        console.log(`刷新分类广告: ${container.id}`);
        getRecommendAds(container.id);
    });
    if (document.getElementById('search-ad-container')) {
        console.log(`刷新搜索广告`);
        getRecommendAds('search-ad-container');
    }
}

// 用户行为上报：添加行为到队列（仅分数累加）
function reportUserBehavior(tag, score) {
    // 核心：仅添加行为到最近10次队列，做分数累加
    addRecentBehavior(tag, score);

    const deviceId = getDeviceId();
    const requestData = {
        anonymousUserId: deviceId,
        tag: tag,
        score: score,
        platform: "news"
    };

    return fetch(API_CONFIG.getAds, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(requestData)
    })
        .then(res => res.json().catch(() => ({code: res.status, msg: '非JSON响应'})))
        .then(data => {
            console.log('行为上报结果：', data);
            if (data.code === 200) {
                refreshAdsOnPageChange();
                console.log('行为上报成功，已刷新广告列表');
            }
            return data;
        })
        .catch(err => {
            console.error('行为上报失败：', err);
            return {code: 500, msg: '接口异常'};
        });
}

// 广告点击上报：添加行为到队列（仅分数累加）
function reportAdClick(adId) {
    const deviceId = getDeviceId();
    const adCard = document.querySelector(`.ad-card[data-adid="${adId}"]`);
    let clickTag = 'others';
    if (adCard) {
        clickTag = adCard.getAttribute('data-tag') || 'others';
    }

    // 核心：仅添加点击行为到队列（score固定为1，纯分数累加）
    addRecentBehavior(clickTag, 1);

    const requestData = {
        anonymousUserId: deviceId,
        score: 1,
        platform: "news",
        adClicks: [{adId: adId, clicks: 1}]
    };

    return fetch(API_CONFIG.getAds, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(requestData)
    })
        .then(res => res.json().catch(() => ({code: res.status, msg: '非JSON响应'})))
        .then(data => {
            console.log('点击量上报结果：', data);
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

// 页面加载初始化（未改动）
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(refreshAdsOnPageChange, 100);
});

// 导航点击刷新广告（未改动）
document.addEventListener('click', function(e) {
    if (e.target.tagName === 'A' && e.target.href) {
        setTimeout(refreshAdsOnPageChange, 50);
    }
});

// ========== 调试函数（仅用于测试，可选删除） ==========
// 清空最近10次行为队列
function clearRecentBehaviors() {
    localStorage.removeItem('userRecentBehaviors');
    console.log('最近10次行为队列已清空');
}

// 查看各tag的累加总分
function getTagTotalScores() {
    const behaviors = initRecentBehaviors();
    const tagTotalScores = {};
    behaviors.forEach(behavior => {
        const { tag, score } = behavior;
        if (!tagTotalScores[tag]) tagTotalScores[tag] = 0;
        tagTotalScores[tag] += score;
    });
    console.log('各tag累加总分：', tagTotalScores);
    return tagTotalScores;
}