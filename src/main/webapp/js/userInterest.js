
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
        clickCategory: 3,
        search: 2,
        browse: 1
    };
    const addScore = weightMap[behavior] || 0;


    scores[tag] = (scores[tag] || 0) + addScore;
    localStorage.setItem('userInterestScores', JSON.stringify(scores));

    console.log('品类[' + tag + ']分数更新：' + scores[tag] + '（+' + addScore + '）');
    console.log('当前所有分数：', scores);

    sendInterestData();
    return true;
}


function sendInterestData() {
    const anonymousUserId = localStorage.getItem('anonymousUserId');
    const scores = JSON.parse(localStorage.getItem('userInterestScores'));
    const platform = 'shopping';

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
    event.preventDefault();
    const link = event.target.closest('a');
    const url = link.getAttribute('href');


    addInterestScore(tag, 'clickCategory');

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
        clickCategory: 3,
        search: 2,
        browse: 1
    };
    const addScore = weightMap[behavior] || 0;

    scores[tag] = (scores[tag] || 0) + (addScore * score);
    localStorage.setItem('userInterestScores', JSON.stringify(scores));
    console.log('品类[' + tag + ']批量更新：增加' + (addScore * score) + '分，总分=' + scores[tag]);
}

document.addEventListener('DOMContentLoaded', function() {
    initLocalStorage();
});