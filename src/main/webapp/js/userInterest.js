// userInterest.js - 完整修正版本

// 1. 生成匿名用户ID（UUID格式：user_xxx）
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

    // 立即发送单条数据
    sendSingleInterestData(tag, addScore, behavior);

    return true;
}

// 4. 发送单条兴趣数据到广告网站
function sendSingleInterestData(tag, score, behavior) {
    console.log("=== sendSingleInterestData 开始 ===");

    const anonymousUserId = localStorage.getItem('anonymousUserId');
    const platform = 'shopping';

    // 构建单条数据对象（字段名与广告服务器要求一致）
    const requestData = {
        anonymousUserId: anonymousUserId,
        tag: tag,
        score: score,
        platform: platform
    };

    console.log("发送单条数据:", JSON.stringify(requestData, null, 2));

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
            console.log('✅ 单条数据发送成功：', res);
        })
        .catch(err => {
            console.error('❌ 单条数据发送失败：', err);
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
    if (searchResults.length === 0) return;

    console.log('处理搜索结果，共找到', searchResults.length, '个商品');

    const categoryCounts = {};
    searchResults.forEach(element => {
        const category = element.textContent.trim();
        if (category.toLowerCase() === 'all') {
            return;
        }
        categoryCounts[category] = (categoryCounts[category] || 0) + 1;
    });

    for (const [category, count] of Object.entries(categoryCounts)) {
        const scoreToAdd = Math.min(count * 2, 10);

        const scores = JSON.parse(localStorage.getItem('userInterestScores'));
        scores[category] = (scores[category] || 0) + scoreToAdd;
        localStorage.setItem('userInterestScores', JSON.stringify(scores));
        console.log(`品类[${category}]搜索结果：增加${scoreToAdd}分，总分=${scores[category]}`);

        sendSingleInterestData(category, scoreToAdd, 'search');
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
// 11. 获取并显示广告
function fetchAndDisplayAds() {
    console.log("=== 开始获取广告 ===");

    // 动态获取当前应用的 context path
    const pathArray = window.location.pathname.split('/');
    const contextPath = pathArray. length > 1 ? '/' + pathArray[1] :  '';
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

                // ✅ 修改判断逻辑，兼容两种响应格式
                // 格式1: {code:  200, ads: [... ]}
                // 格式2: {success: true, data: {ads: [...]}}
                let ads = [];

                if (data.code === 200 && data.ads) {
                    // 格式1
                    ads = data.ads;
                } else if (data. success === true && data.data && data.data.ads) {
                    // 格式2
                    ads = data.data. ads;
                } else if (data.success === true && Array.isArray(data.ads)) {
                    // 格式3:  {success: true, ads: [...]}
                    ads = data.ads;
                }

                if (ads && ads.length > 0) {
                    console.log(`✅ 成功获取 ${ads.length} 条广告`);
                    createAdCarousel(ads);
                } else {
                    console.warn('⚠️ 没有广告数据:', data. message || data. msg || '未知原因');
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
// 12. 创建广告轮播图
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

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', function() {
    initLocalStorage();
    setTimeout(fetchAndDisplayAds, 500);
});
