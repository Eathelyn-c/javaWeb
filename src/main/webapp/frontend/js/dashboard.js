let allAds = [];
let categories = [];

// Check authentication first (before DOM loads)
if (!requireAuth()) {
    // Will redirect to login, so stop execution
    throw new Error('Authentication required');
}

// Load categories and ads
async function init() {
    await loadCategories();
    await loadAds();
}

// Load categories
async function loadCategories() {
    try {
        const data = await apiRequest(API.categories);
        if (data && data.success) {
            categories = data.data;
            populateCategoryFilter();
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

// Populate category filter
function populateCategoryFilter() {
    const categoryFilter = document.getElementById('categoryFilter');
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.categoryName;
        option.textContent = categoryNames[category.categoryName] || category.categoryName;
        categoryFilter.appendChild(option);
    });
}

// Load user's ads
async function loadAds() {
    try {
        console.log('Loading ads from:', API.ads.list);
        const data = await apiRequest(API.ads.list);
        console.log('Ads data received:', data);
        if (data && data.success) {
            allAds = data.data;
            console.log('Total ads:', allAds.length);
            displayAds(allAds);
        } else {
            console.error('Failed to load ads:', data);
            document.getElementById('adsList').innerHTML = '<p style="color: red;">加载失败: ' + (data?.message || '未知错误') + '</p>';
        }
    } catch (error) {
        console.error('Error loading ads:', error);
        document.getElementById('adsList').innerHTML = '<p style="color: red;">加载失败: ' + error.message + '</p>';
    }
}

// Display ads
function displayAds(ads) {
    console.log('Displaying ads:', ads);
    const adsList = document.getElementById('adsList');
    const noAds = document.getElementById('noAds');
    
    if (!ads || ads.length === 0) {
        console.log('No ads to display');
        adsList.innerHTML = '';
        noAds.style.display = 'block';
        return;
    }
    
    console.log('Displaying', ads.length, 'ads');
    noAds.style.display = 'none';
    adsList.innerHTML = '';
    
    ads.forEach((ad, index) => {
        try {
            console.log('Creating card for ad:', ad.title);
            const adCard = createAdCard(ad);
            adsList.appendChild(adCard);
            
            // Load statistics after card is in DOM
            loadAdStatistics(ad.adId);
        } catch (error) {
            console.error('Error creating card for ad', index, ':', error, ad);
        }
    });
    console.log('All ads displayed');
}

// Create ad card
function createAdCard(ad) {
    console.log('Creating card for ad:', ad.adId, ad.title);
    const template = document.getElementById('adCardTemplate');
    if (!template) {
        console.error('Template not found!');
        throw new Error('adCardTemplate not found');
    }
    
    const card = template.content.cloneNode(true);
    
    // Set ad data
    card.querySelector('.ad-title').textContent = ad.title;
    card.querySelector('.ad-category').textContent = categoryNames[ad.categoryName] || ad.categoryName;
    card.querySelector('.ad-type').textContent = adTypeNames[ad.adType] || ad.adType;
    card.querySelector('.ad-description').textContent = ad.description || '';
    
    console.log('Ad type:', ad.adType, '-> Display:', adTypeNames[ad.adType]);
    
    // Set initial statistics
    card.querySelector('.views-count').textContent = '0';
    card.querySelector('.clicks-count').textContent = '0';
    
    // Make card clickable
    const cardDiv = card.querySelector('.ad-card');
    cardDiv.dataset.adId = ad.adId;
    cardDiv.addEventListener('click', () => viewAdDetails(ad.adId));
    
    // Add event listeners
    const viewBtn = card.querySelector('.view-btn');
    if (viewBtn) {
        viewBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            viewAdDetails(ad.adId);
        });
    }
    
    const deleteBtn = card.querySelector('.delete-btn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            deleteAd(ad.adId);
        });
    }
    
    return card;
}

// Load ad statistics (from internal stats table)
async function loadAdStatistics(adId) {
    try {
        const data = await apiRequest(API.ads.stats(adId));
        if (data && data.success) {
            const stats = data.data;
            // Find the card in DOM using data attribute
            const cardDiv = document.querySelector(`.ad-card[data-ad-id="${adId}"]`);
            if (cardDiv) {
                const viewsCount = cardDiv.querySelector('.views-count');
                const clicksCount = cardDiv.querySelector('.clicks-count');
                if (viewsCount) viewsCount.textContent = stats.viewCount || 0;
                if (clicksCount) clicksCount.textContent = stats.clickCount || 0;
                console.log('Loaded stats for ad', adId, ':', stats);
            }
        }
    } catch (error) {
        console.error('Error loading statistics for ad', adId, ':', error);
    }
}

// View ad details
async function viewAdDetails(adId) {
    try {
        console.log('Viewing ad details for:', adId);
        const data = await apiRequest(API.ads.view(adId));
        console.log('View ad response:', data);
        if (data && data.success) {
            // 数据结构是扁平的，所有信息在 data.data 中
            const adData = data.data;
            const stats = adData.statistics || {
                viewCount: adData.viewCount || 0,
                clickCount: adData.clickCount || 0,
                ctr: adData.ctr || '0.00%'
            };
            console.log('Ad data:', adData);
            console.log('Statistics:', stats);
            
            // Create modal to show ad details
            const modal = document.createElement('div');
            modal.className = 'modal show';
            modal.innerHTML = `
                <div class="modal-content" style="max-width: 800px;">
                    <span class="close" onclick="this.parentElement.parentElement.remove()">&times;</span>
                    <h2>广告详情</h2>
                    
                    <div style="margin: 20px 0;">
                        <h3>${adData.title}</h3>
                        <p><strong>类别：</strong>${categoryNames[adData.categoryName] || adData.categoryName}</p>
                        <p><strong>广告类型：</strong>${adTypeNames[adData.adType?.toUpperCase()] || adData.adType}</p>
                        <p><strong>描述：</strong>${adData.description || '无'}</p>
                        
                        ${adData.textContent ? `<p><strong>文本内容：</strong>${adData.textContent}</p>` : ''}
                        ${adData.imageUrl ? `<p><strong>图片：</strong><br><img src="${adData.imageUrl}" style="max-width: 100%; margin-top: 10px;"></p>` : ''}
                        ${adData.videoUrl ? `<p><strong>视频：</strong><br><video src="${adData.videoUrl}" controls style="max-width: 100%; margin-top: 10px;"></video></p>` : ''}
                        ${adData.targetUrl ? `<p><strong>目标链接：</strong><a href="${adData.targetUrl}" target="_blank">${adData.targetUrl}</a></p>` : ''}
                        
                        <h3 style="margin-top: 20px;">外部平台访问统计</h3>
                        <p><strong>外部平台浏览量：</strong>${stats.viewCount || 0}</p>
                        <p><strong>外部用户点击量：</strong>${stats.clickCount || 0}</p>
                        <p style="color: #666; font-size: 0.9em; margin-top: 10px;">注：统计数据来自外部平台通过API的访问记录</p>
                    </div>
                    
                    <div style="margin-top: 20px; display: flex; gap: 10px;">
                        <button class="btn btn-primary" onclick="window.location.href='/ad-management/frontend/create-ad.html?id=${adId}'">编辑</button>
                        <button class="btn btn-secondary" onclick="this.parentElement.parentElement.parentElement.remove()">关闭</button>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
        }
    } catch (error) {
        console.error('Error viewing ad:', error);
        alert('加载失败：' + error.message);
    }
}

// Delete ad
async function deleteAd(adId) {
    if (!confirm('确定要删除这个广告吗？此操作不可恢复。')) {
        return;
    }
    
    try {
        const data = await apiRequest(API.ads.delete(adId), {
            method: 'DELETE'
        });
        
        if (data && data.success) {
            await loadAds(); // Reload ads
        } else {
            alert(data?.message || '删除失败');
        }
    } catch (error) {
        console.error('Error deleting ad:', error);
        alert('删除失败');
    }
}

// Filter functionality
function filterAds() {
    const categoryFilter = document.getElementById('categoryFilter').value;
    
    let filtered = allAds;
    
    if (categoryFilter) {
        filtered = filtered.filter(ad => ad.categoryName === categoryFilter);
    }
    
    displayAds(filtered);
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing...');
    
    // Display user info
    const user = getUserInfo();
    if (user) {
        const usernameEl = document.getElementById('username');
        if (usernameEl) {
            usernameEl.textContent = user.username;
        }
    }
    
    // Logout functionality
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            logout();
        });
    }
    
    // Add filter event listener after DOM is ready
    const categoryFilter = document.getElementById('categoryFilter');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', filterAds);
    }
    
    // Initialize app
    init();
});
