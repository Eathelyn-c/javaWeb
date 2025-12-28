// API Configuration
const API_BASE_URL = '/ad-management/api';

// API Endpoints
const API = {
    auth: {
        login: `${API_BASE_URL}/auth/login`,
        register: `${API_BASE_URL}/auth/register`
    },
    ads: {
        list: `${API_BASE_URL}/ads`,
        create: `${API_BASE_URL}/ads`,
        update: (id) => `${API_BASE_URL}/ads/${id}`,
        delete: (id) => `${API_BASE_URL}/ads/${id}`,
        toggle: (id) => `${API_BASE_URL}/ads/${id}/toggle`,
        view: (id) => `${API_BASE_URL}/ads/view/${id}`,
        click: `${API_BASE_URL}/ads/click`,
        stats: (id) => `${API_BASE_URL}/ads/stats/${id}`,
        active: `${API_BASE_URL}/ads/active`,
        byCategory: (id) => `${API_BASE_URL}/ads/category/${id}`
    },
    categories: `${API_BASE_URL}/categories`,
    publish: {
        news: `${API_BASE_URL}/publish/news`,
        video: `${API_BASE_URL}/publish/video`,
        shopping: `${API_BASE_URL}/publish/shopping`,
        multiple: `${API_BASE_URL}/publish/multiple`
    }
};

// Utility Functions
function getAuthToken() {
    return localStorage.getItem('token');
}

function setAuthToken(token) {
    localStorage.setItem('token', token);
}

function removeAuthToken() {
    localStorage.removeItem('token');
}

function getUserInfo() {
    const userJson = localStorage.getItem('user');
    return userJson ? JSON.parse(userJson) : null;
}

function setUserInfo(user) {
    localStorage.setItem('user', JSON.stringify(user));
}

function removeUserInfo() {
    localStorage.removeItem('user');
}

function isAuthenticated() {
    return !!getAuthToken();
}

function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

function logout() {
    removeAuthToken();
    removeUserInfo();
    window.location.href = 'login.html';
}

// API Request Helper
async function apiRequest(url, options = {}) {
    const token = getAuthToken();
    
    const headers = {
        ...options.headers
    };
    
    // Add Authorization header if token exists
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Add Content-Type for JSON requests
    if (options.body && !(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }
    
    const response = await fetch(url, {
        ...options,
        headers
    });
    
    // Check if unauthorized
    if (response.status === 401) {
        logout();
        return null;
    }
    
    const data = await response.json();
    return data;
}

// Show/Hide Message
function showMessage(elementId, message, type = 'success') {
    const messageEl = document.getElementById(elementId);
    if (messageEl) {
        messageEl.textContent = message;
        messageEl.className = `message ${type}`;
        messageEl.style.display = 'block';
        
        // Auto hide after 5 seconds
        setTimeout(() => {
            messageEl.style.display = 'none';
        }, 5000);
    }
}

// Category mapping (Chinese names)
const categoryNames = {
    'food': '食品',
    'makeup': '美妆',
    'digital': '数码',
    'sport': '运动',
    'clothes': '服饰',
    'book': '图书',
    'others': '其他'
};

// Ad type mapping (Chinese names)
const adTypeNames = {
    'text': '文字',
    'image': '图片',
    'video': '视频',
    'text_image': '文字+图片'
};

// Status mapping (Chinese names)
const statusNames = {
    'active': '活跃',
    'paused': '暂停',
    'deleted': '已删除'
};
