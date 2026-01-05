function generateUUID() {
    return 'user_' + ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
        (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
    );
}

// 2. 初始化本地存储（匿名ID+兴趣分数）
function initLocalStorage() {
    if (!localStorage.getItem('anonymousUserId')) {
        const anonymousUserId = generateUUID();
        localStorage.setItem('anonymousUserId', anonymousUserId);
        console.log('初始化匿名用户ID：', anonymousUserId);
    }
    if (!localStorage.getItem('userInterestScores')) {
        localStorage.setItem('userInterestScores', JSON.stringify({}));
    }
    // 初始化广告点击记录
    if (!localStorage.getItem('adClicksData')) {
        localStorage.setItem('adClicksData', JSON.stringify({}));
    }
}

// 3. 分数计算逻辑
function addInterestScore(tag, behavior) {
    const scores = JSON.parse(localStorage.getItem('userInterestScores'));

    const weightMap = {
        buy: 5,
        addToCart: 4,
        viewDetail: 3,
        clickCategory: 3,
        search: 2,
        browse: 1
    };

    const addScore = weightMap[behavior] || 0;

    scores[tag] = (scores[tag] || 0) + addScore;
    localStorage.setItem('userInterestScores', JSON.stringify(scores));
    console.log(`品类[${tag}] ${behavior}行为 +${addScore}分，总分=${scores[tag]}`);

    // 立即发送数据（包含 adClicks 数组）
    sendInterestDataWithAdClicks(tag, addScore);

    return true;
}

// 4. 发送兴趣数据（包含广告点击数组）
function sendInterestDataWithAdClicks(tag, score) {
    console.log("=== 发送兴趣数据（包含广告点击） ===");

    const anonymousUserId = localStorage.getItem('anonymousUserId');
    const platform = 'shopping';

    // 获取所有广告点击数据
    const adClicksData = JSON.parse(localStorage.getItem('adClicksData') || '{}');
    const adClicks = [];

    for (const [adId, clicks] of Object.entries(adClicksData)) {
        adClicks.push({
            adId: adId,
            clicks: clicks
        });
    }

    // 构建数据对象
    const requestData = {
        tag: tag,
        platform: platform,
        anonymousUserId: anonymousUserId,
        score: score,
        adClicks: adClicks  // 广告点击数组
    };

    console.log("发送数据:", JSON.stringify(requestData, null, 2));

    const proxyUrl = `${window.location.origin}${window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/'))}/ad-data-forward`;

    fetch(proxyUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8'
        },
        body: JSON.stringify(requestData),
        credentials: 'include'
    })
        .then(response => {
            console.log("响应状态:", response.status);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(res => {
            console.log('✅ 数据发送成功：', res);
        })
        .catch(err => {
            console.error('❌ 数据发送失败：', err);
        });
}

// 5. 处理搜索表单提交事件
function handleSearchSubmit(event) {
    event.preventDefault();
    const searchInput = document.getElementById('searchKeyword');
    const keyword = searchInput.value.trim();
    const form = event.target;
    form.submit();
    return false;
}

// 6. 处理品类点击事件
function handleCategoryClick(event, tag) {
    event.preventDefault();
    const link = event.target.closest('a');
    const url = link.getAttribute('href');
    addInterestScore(tag, 'clickCategory');
    setTimeout(() => {
        window.location.href = url;
    }, 100);
}

// 7. 处理查看详情点击事件
function handleViewDetailClick(event, tag, productId) {
    event.preventDefault();
    const link = event.target.closest('a');
    const url = link.getAttribute('href');
    addInterestScore(tag, 'viewDetail');
    setTimeout(() => {
        window.location.href = url;
    }, 100);
}

// 8. 处理搜索结果加分
function processSearchResults() {

    const searchResults = document.querySelectorAll('.search-category');
    if (searchResults.length === 0) {
        console.log('⚠️ 未找到搜索结果元素');
        return;
    }

    console.log('处理搜索结果，共找到', searchResults.length, '个商品');

    // 统计每个品类出现的次数
    const categoryCounts = {};
    searchResults.forEach(element => {
        const category = element.textContent.trim();
        if (category.toLowerCase() === 'all') {
            return;
        }
        categoryCounts[category] = (categoryCounts[category] || 0) + 1;
    });

    console.log('品类统计:', categoryCounts);

    // 获取当前分数
    const scores = JSON.parse(localStorage.getItem('userInterestScores') || '{}');

    // 为每个品类加分
    for (const [category, count] of Object.entries(categoryCounts)) {
        // 计算加分（每个商品2分，最多10分）
        const scoreToAdd = Math.min(count * 2, 10);

        // 更新本地分数
        scores[category] = (scores[category] || 0) + scoreToAdd;

        console.log(`品类[${category}] 搜索结果加分：+${scoreToAdd}分（共${count}个商品），总分=${scores[category]}`);
    }

    // 保存更新后的分数
    localStorage.setItem('userInterestScores', JSON.stringify(scores));
    console.log('已更新本地分数:', scores);

    // 为每个品类发送数据（包含广告点击）
    for (const [category, count] of Object.entries(categoryCounts)) {
        const scoreToAdd = Math.min(count * 2, 10);
        sendInterestDataWithAdClicks(category, scoreToAdd);
    }
}

// 9. 处理购买按钮点击事件
function handleBuyClick(event, tag) {
    event.preventDefault();
    const success = addInterestScore(tag, 'buy');
    if (success) {
        alert('购买成功！感谢您的购买，订单已生成。');
    }
    return false;
}

// 10. 处理加入购物车按钮点击事件
function handleAddToCartClick(event, tag) {
    event.preventDefault();
    const success = addInterestScore(tag, 'addToCart');
    if (success) {
        alert('商品已成功加入购物车！');
    }
    return false;
}

// 11. 获取并显示广告
function fetchAndDisplayAds() {
    console.log("=== 开始获取广告 ===");

    // 首先检查广告容器是否存在
    const adContainer = document.getElementById('adContainer');
    const adBanner = document.getElementById('adBanner');

    if (!adContainer || !adBanner) {
        console.log('广告容器不存在，跳过广告获取');
        return;
    }

    const pathArray = window.location.pathname.split('/');
    const contextPath = pathArray.length > 1 ? '/' + pathArray[1] : '';
    const apiUrl = `${contextPath}/ad-fetch`;

    console.log('请求广告API URL:', apiUrl);

    fetch(apiUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8'
        },
        credentials: 'include'
    })
        .then(response => {
            console.log('广告API响应状态:', response.status);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.text();
        })
        .then(responseText => {
            console.log('广告API响应文本:', responseText);

            try {
                const data = JSON.parse(responseText);
                console.log('解析后的广告数据:', data);

                let ads = [];

                if (data.code === 200 && data.ads) {
                    ads = data.ads;
                } else if (data.success === true && data.data && data.data.ads) {
                    ads = data.data.ads;
                } else if (data.success === true && Array.isArray(data.ads)) {
                    ads = data.ads;
                }

                if (ads && ads.length > 0) {
                    console.log(`✅ 成功获取 ${ads.length} 条广告`);
                    createAdCarousel(ads);
                } else {
                    console.warn('⚠️ 没有广告数据:', data.message || data.msg || '未知原因');
                    hideAdBanner();
                }
            } catch (parseError) {
                console.error('❌ 解析JSON响应失败:', parseError);
                console.error('原始响应文本:', responseText);
                hideAdBanner();
            }
        })
        .catch(err => {
            console.error('❌ 获取广告失败:', err);
            hideAdBanner();
        });
}

// 12. 创建广告轮播图（支持点击追踪）
function createAdCarousel(ads) {
    console.log('创建广告轮播，广告数量:', ads.length);

    let adContainer = document.getElementById('adContainer');
    if (!adContainer) {
        console.error('广告容器不存在');
        return;
    }

    adContainer.innerHTML = '';

    ads.forEach((ad, index) => {
        const adItem = document.createElement('div');
        adItem.className = 'ad-item';
        if (index === 0) adItem.classList.add('active');

        const img = document.createElement('img');
        img.src = ad.url;
        img.alt = `广告${index + 1}`;
        img.style.width = '100%';
        img.style.height = '100%';
        img.style.objectFit = 'cover';
        img.style.cursor = 'pointer';

        // 保存 adId 到 data 属性
        img.setAttribute('data-ad-id', ad.adId);

        // 添加点击事件
        img.addEventListener('click', function() {
            const adId = this.getAttribute('data-ad-id');
            console.log('用户点击了广告 ID:', adId);
            recordAdClick(adId);
        });

        adItem.appendChild(img);
        adContainer.appendChild(adItem);
    });

    const adDots = document.getElementById('adDots');
    if (adDots) {
        adDots.innerHTML = '';
        ads.forEach((_, index) => {
            const dot = document.createElement('span');
            dot.className = 'ad-dot';
            if (index === 0) dot.classList.add('active');
            dot.setAttribute('data-index', index);
            dot.onclick = function() {
                const index = parseInt(this.getAttribute('data-index'));
                showAdSlide(index);
            };
            adDots.appendChild(dot);
        });
    }

    initAdCarousel(ads.length);
}

// 13. 初始化广告轮播
function initAdCarousel(totalSlides) {
    let currentSlide = 0;
    let slideInterval;

    function showAdSlide(index) {
        const adItems = document.querySelectorAll('.ad-item');
        const dots = document.querySelectorAll('.ad-dot');

        currentSlide = index;
        if (currentSlide >= totalSlides) currentSlide = 0;
        if (currentSlide < 0) currentSlide = totalSlides - 1;

        adItems.forEach(item => item.classList.remove('active'));
        dots.forEach(dot => dot.classList.remove('active'));

        if (adItems[currentSlide]) {
            adItems[currentSlide].classList.add('active');
        }
        if (dots[currentSlide]) {
            dots[currentSlide].classList.add('active');
        }
    }

    function nextAdSlide() {
        showAdSlide(currentSlide + 1);
    }

    window.nextAdSlide = nextAdSlide;
    window.prevAdSlide = () => showAdSlide(currentSlide - 1);
    window.showAdSlide = showAdSlide;

    function startAutoPlay() {
        if (slideInterval) clearInterval(slideInterval);
        slideInterval = setInterval(nextAdSlide, 5000);
    }

    function stopAutoPlay() {
        if (slideInterval) clearInterval(slideInterval);
    }

    const adBanner = document.getElementById('adBanner');
    if (adBanner) {
        adBanner.addEventListener('mouseenter', stopAutoPlay);
        adBanner.addEventListener('mouseleave', startAutoPlay);
    }

    startAutoPlay();
}

// 14. 隐藏广告横幅
function hideAdBanner() {
    const adBanner = document.getElementById('adBanner');
    if (adBanner) {
        adBanner.style.display = 'none';
    }
}

// 15. 记录广告点击（只更新本地存储）
function recordAdClick(adId) {
    console.log('=== 记录广告点击 ===');
    console.log('广告ID:', adId);

    // 检查adId是否有效
    if (!adId || adId === 'undefined' || adId === 'null') {
        console.error('❌ 无效的广告ID:', adId);
        return;
    }

    // 获取广告点击数据
    const adClicksData = JSON.parse(localStorage.getItem('adClicksData') || '{}');

    // 增加点击次数
    adClicksData[adId] = (adClicksData[adId] || 0) + 1;

    localStorage.setItem('adClicksData', JSON.stringify(adClicksData));

    console.log(`广告 ${adId} 当前点击次数: ${adClicksData[adId]}`);
    console.log('所有广告点击数据:', adClicksData);
}

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', function() {
    initLocalStorage();

    // 只在有广告容器的页面获取广告
    const adContainer = document.getElementById('adContainer');
    if (adContainer) {
        setTimeout(fetchAndDisplayAds, 500);
    } else {
        console.log('当前页面没有广告容器，跳过广告获取');
    }
});
