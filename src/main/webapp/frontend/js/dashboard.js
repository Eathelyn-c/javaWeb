// Check authentication
if (!requireAuth()) {
    // Will redirect to login
}

// Display user info
const user = getUserInfo();
if (user) {
    document.getElementById('username').textContent = user.username;
}

// Logout functionality
document.getElementById('logoutBtn').addEventListener('click', (e) => {
    e.preventDefault();
    logout();
});

let allAds = [];
let categories = [];

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
        const data = await apiRequest(API.ads.list);
        if (data && data.success) {
            allAds = data.data;
            displayAds(allAds);
        }
    } catch (error) {
        console.error('Error loading ads:', error);
    }
}

// Display ads
function displayAds(ads) {
    const adsList = document.getElementById('adsList');
    const noAds = document.getElementById('noAds');
    
    if (!ads || ads.length === 0) {
        adsList.innerHTML = '';
        noAds.style.display = 'block';
        return;
    }
    
    noAds.style.display = 'none';
    adsList.innerHTML = '';
    
    ads.forEach(ad => {
        const adCard = createAdCard(ad);
        adsList.appendChild(adCard);
    });
}

// Create ad card
function createAdCard(ad) {
    const template = document.getElementById('adCardTemplate');
    const card = template.content.cloneNode(true);
    
    // Set ad data
    card.querySelector('.ad-title').textContent = ad.title;
    card.querySelector('.ad-status').textContent = statusNames[ad.status] || ad.status;
    card.querySelector('.ad-status').classList.add(ad.status);
    card.querySelector('.ad-category').textContent = categoryNames[ad.categoryName] || ad.categoryName;
    card.querySelector('.ad-type').textContent = adTypeNames[ad.adType] || ad.adType;
    card.querySelector('.ad-description').textContent = ad.description || '';
    
    // Load statistics
    loadAdStatistics(ad.adId, card);
    
    // Set button texts based on status
    const toggleBtn = card.querySelector('.toggle-btn');
    toggleBtn.textContent = ad.status === 'active' ? '暂停' : '激活';
    
    // Add event listeners
    toggleBtn.addEventListener('click', () => toggleAdStatus(ad.adId));
    card.querySelector('.publish-btn').addEventListener('click', () => openPublishModal(ad.adId));
    card.querySelector('.delete-btn').addEventListener('click', () => deleteAd(ad.adId));
    
    return card;
}

// Load ad statistics
async function loadAdStatistics(adId, card) {
    try {
        const data = await apiRequest(API.ads.stats(adId));
        if (data && data.success) {
            const stats = data.data;
            card.querySelector('.views-count').textContent = stats.viewCount || 0;
            card.querySelector('.clicks-count').textContent = stats.clickCount || 0;
        }
    } catch (error) {
        console.error('Error loading statistics:', error);
    }
}

// Toggle ad status
async function toggleAdStatus(adId) {
    try {
        const data = await apiRequest(API.ads.toggle(adId), {
            method: 'PUT'
        });
        
        if (data && data.success) {
            await loadAds(); // Reload ads
        } else {
            alert(data?.message || '操作失败');
        }
    } catch (error) {
        console.error('Error toggling status:', error);
        alert('操作失败');
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

// Publish modal
let currentPublishAdId = null;

function openPublishModal(adId) {
    currentPublishAdId = adId;
    const modal = document.getElementById('publishModal');
    modal.classList.add('show');
}

// Close modal
document.querySelector('.close').addEventListener('click', () => {
    const modal = document.getElementById('publishModal');
    modal.classList.remove('show');
    document.getElementById('publishResult').style.display = 'none';
});

// Publish form
document.getElementById('publishForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const checkboxes = document.querySelectorAll('input[name="platform"]:checked');
    const platforms = Array.from(checkboxes).map(cb => cb.value);
    
    if (platforms.length === 0) {
        alert('请至少选择一个平台');
        return;
    }
    
    try {
        const data = await apiRequest(API.publish.multiple, {
            method: 'POST',
            body: JSON.stringify({
                adId: currentPublishAdId,
                platforms: platforms
            })
        });
        
        if (data) {
            const resultEl = document.getElementById('publishResult');
            resultEl.innerHTML = '<h3>发布结果：</h3>';
            
            const results = data.data.results;
            for (const [platform, result] of Object.entries(results)) {
                const status = result.success ? '成功 ✓' : '失败 ✗';
                resultEl.innerHTML += `<p><strong>${platform}:</strong> ${status}</p>`;
            }
            
            resultEl.style.display = 'block';
            resultEl.className = data.data.allSuccessful ? 'message success' : 'message error';
        }
    } catch (error) {
        console.error('Error publishing:', error);
        alert('发布失败');
    }
});

// Filter functionality
document.getElementById('categoryFilter').addEventListener('change', filterAds);
document.getElementById('statusFilter').addEventListener('change', filterAds);

function filterAds() {
    const categoryFilter = document.getElementById('categoryFilter').value;
    const statusFilter = document.getElementById('statusFilter').value;
    
    let filtered = allAds;
    
    if (categoryFilter) {
        filtered = filtered.filter(ad => ad.categoryName === categoryFilter);
    }
    
    if (statusFilter) {
        filtered = filtered.filter(ad => ad.status === statusFilter);
    }
    
    displayAds(filtered);
}

// Initialize
init();
