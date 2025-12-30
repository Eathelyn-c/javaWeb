

function generateUUID() {
    return 'user_' + ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
        (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
    );
}

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


    sendInterestData();
    return true;
}

function sendInterestData() {
    const anonymousUserId = localStorage.getItem('anonymousUserId');
    const scores = JSON.parse(localStorage.getItem('userInterestScores'));
    const platform = 'shopping'; // 固定平台标识

    const requestData = Object.entries(scores).map(([Tag, score]) => ({
        anonymousUserId,
        Tag,
        score,
        platform
    }));

    if (requestData.length === 0) {
        console.log('没有兴趣数据需要发送');
        return;
    }

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
    event.preventDefault();

    const searchInput = document.getElementById('searchKeyword');
    const keyword = searchInput.value.trim();

    if (keyword) {
        const form = event.target;
        form.submit();
    } else {
        const form = event.target;
        form.submit();
    }

    return false;
}

function handleCategoryClick(event, tag) {
    event.preventDefault(); // 阻止默认链接跳转
    const link = event.target.closest('a');
    const url = link.getAttribute('href');

    addInterestScore(tag, 'clickCategory');

    setTimeout(() => {
        window.location.href = url;
    }, 100);
}


function handleViewDetailClick(event, tag, productId) {
    event.preventDefault();
    const link = event.target.closest('a');
    const url = link.getAttribute('href');

    addInterestScore(tag, 'viewDetail');

    setTimeout(() => {
        window.location.href = url;
    }, 100);
}

function processSearchResults() {

    const searchResults = document.querySelectorAll('.search-category');
    if (searchResults.length === 0) return;

    console.log('处理搜索结果，共找到', searchResults.length, '个商品');

    const categoryCounts = {};
    searchResults.forEach(element => {
        const category = element.textContent.trim();

        if (category.toLowerCase() === 'all') {
            console.log('跳过all标签，不记录分数');
            return;
        }
        categoryCounts[category] = (categoryCounts[category] || 0) + 1;
    });

    for (const [category, count] of Object.entries(categoryCounts)) {
        const scoreToAdd = Math.min(count, 10);

        updateCategoryScore(category, scoreToAdd, 'search');
    }

    console.log('搜索结果品类统计：', categoryCounts);

    setTimeout(() => {
        sendInterestData();
    }, 500);
}

function updateCategoryScore(tag, score, behavior) {
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

    scores[tag] = (scores[tag] || 0) + (addScore * score);
    localStorage.setItem('userInterestScores', JSON.stringify(scores));
    console.log(`品类[${tag}]批量更新：增加${(addScore * score)}分，总分=${scores[tag]}`);
}

function handleBuyClick(event, tag) {
    event.preventDefault();

    const success = addInterestScore(tag, 'buy');

    if (success) {
        alert('购买成功！感谢您的购买，订单已生成。');


    }
    return false;
}

function handleAddToCartClick(event, tag) {
    event.preventDefault();

    const success = addInterestScore(tag, 'addToCart');

    if (success) {
        alert('商品已成功加入购物车！');
    }
    return false;
}

document.addEventListener('DOMContentLoaded', function() {
    initLocalStorage();
});