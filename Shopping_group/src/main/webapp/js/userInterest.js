// userInterest.js - 修改后的完整版本
// 1. 生成匿名用户ID（UUID格式：user_xxx）
function generateUUID() {
    return 'user_' + ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
        (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
    );
}

// 2. 初始化本地存储（匿名ID+兴趣分数）
function initLocalStorage() {
    // 初始化匿名用户ID（永久存储，除非清除浏览器缓存）
    if (!localStorage.getItem('anonymousUserId')) {
        const anonymousUserId = generateUUID();
        localStorage.setItem('anonymousUserId', anonymousUserId);
        console.log('初始化匿名用户ID：', anonymousUserId);
    }
    // 初始化兴趣分数（键：品类Tag，值：累计分数）
    if (!localStorage.getItem('userInterestScores')) {
        localStorage.setItem('userInterestScores', JSON.stringify({}));
    }
}

// 3. 分数计算逻辑（加权规则：主动行为权重>被动行为）
/**
 * @param tag 商品品类（如digital、food、makeup等）
 * @param behavior 行为类型：clickCategory(+3)、search(+2)、browse(+1)
 */
function addInterestScore(tag, behavior) {
    const scores = JSON.parse(localStorage.getItem('userInterestScores'));
    // 行为-权重映射表（可根据需求调整）
    const weightMap = {
        clickCategory: 3,  // 点击品类导航（主动选择，兴趣最强）
        search: 2,         // 搜索品类相关商品（主动查询，兴趣次强）
        browse: 1          // 浏览详情页≥30秒（被动浏览，兴趣较弱）
    };
    const addScore = weightMap[behavior] || 0;
    // 累加分数（同一品类多次行为累计）
    scores[tag] = (scores[tag] || 0) + addScore;
    localStorage.setItem('userInterestScores', JSON.stringify(scores));
    console.log('品类[' + tag + ']分数更新：' + scores[tag] + '（+' + addScore + '）');
    // 分数更新后立即发送到广告网站
    sendInterestData();
    return true; // 允许表单提交继续
}

// 4. 发送兴趣数据到广告网站（通过后端代理避免跨域）
function sendInterestData() {
    const anonymousUserId = localStorage.getItem('anonymousUserId');
    const scores = JSON.parse(localStorage.getItem('userInterestScores'));
    const platform = 'shopping';  // 固定平台标识

    // 构建请求数据（每个品类一条记录，数组格式）
    const requestData = Object.entries(scores).map(([Tag, score]) => ({
        anonymousUserId,
        Tag,
        score,
        platform
    }));

    // 如果分数为0，不发送
    if (requestData.length === 0) {
        console.log('没有兴趣数据需要发送');
        return;
    }

    // 后端代理接口（对应AdDataForwardServlet的映射地址）
    const proxyUrl = `${window.location.origin}${window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/'))}/ad-data-forward`;

    // 发送POST请求（JSON格式）
    fetch(proxyUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8'
        },
        body: JSON.stringify(requestData),
        credentials: 'include'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(res => console.log('兴趣数据发送成功：', res))
        .catch(err => console.error('兴趣数据发送失败：', err));
}

// 5. 处理搜索表单提交事件
function handleSearchSubmit(event) {
    // 阻止表单默认提交行为
    event.preventDefault();

    const searchInput = document.getElementById('searchKeyword');
    const keyword = searchInput.value.trim();

    if (keyword) {
        // 直接提交表单，搜索结果的加分在搜索结果页处理
        const form = event.target;
        form.submit();
    } else {
        // 如果没有关键词，直接提交表单（会重定向到首页）
        const form = event.target;
        form.submit();
    }

    return false;
}

// 6. 处理品类点击事件
function handleCategoryClick(event, tag) {
    event.preventDefault(); // 阻止默认链接跳转
    const link = event.target.closest('a');
    const url = link.getAttribute('href');

    // 增加品类点击分数
    addInterestScore(tag, 'clickCategory');

    // 延迟跳转以确保数据发送
    setTimeout(() => {
        window.location.href = url;
    }, 100);
}

// 7. 处理搜索结果加分（在搜索结果页调用）
function processSearchResults() {
    // 获取所有搜索结果的品类
    const searchResults = document.querySelectorAll('.search-category');
    if (searchResults.length === 0) return;

    console.log('处理搜索结果，共找到', searchResults.length, '个商品');

    // 统计每个品类出现的次数
    const categoryCounts = {};
    searchResults.forEach(element => {
        const category = element.textContent.trim();
        categoryCounts[category] = (categoryCounts[category] || 0) + 1;
    });

    // 为每个品类添加搜索分数（根据出现次数加权）
    for (const [category, count] of Object.entries(categoryCounts)) {
        // 每个商品加1分，最多不超过10分（防止一次搜索加太多分）
        const scoreToAdd = Math.min(count, 10);

        // 更新分数（这里使用原有的addInterestScore函数，但需要修改行为类型）
        // 我们创建一个临时的函数来直接更新分数，不发送数据（等会统一发送）
        updateCategoryScore(category, scoreToAdd, 'search');
    }

    console.log('搜索结果品类统计：', categoryCounts);

    // 统一发送更新后的兴趣数据
    setTimeout(() => {
        sendInterestData();
    }, 500);
}

// 8. 直接更新品类分数（不发送数据）
function updateCategoryScore(tag, score, behavior) {
    const scores = JSON.parse(localStorage.getItem('userInterestScores'));
    // 行为-权重映射表
    const weightMap = {
        clickCategory: 3,
        search: 2,
        browse: 1
    };
    const addScore = weightMap[behavior] || 0;

    // 累加分数（这里score是商品的个数，我们乘以权重）
    scores[tag] = (scores[tag] || 0) + (addScore * score);
    localStorage.setItem('userInterestScores', JSON.stringify(scores));
    console.log('品类[' + tag + ']批量更新：增加' + (addScore * score) + '分，总分=' + scores[tag]);
}

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', function() {
    initLocalStorage();
});