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

let categories = [];

// Load categories
async function loadCategories() {
    try {
        const data = await apiRequest(API.categories);
        if (data && data.success) {
            categories = data.data;
            populateCategorySelect();
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

// Populate category select
function populateCategorySelect() {
    const categorySelect = document.getElementById('category');
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.categoryId;
        option.textContent = categoryNames[category.categoryName] || category.categoryName;
        categorySelect.appendChild(option);
    });
}

// Ad type change handler
document.getElementById('adType').addEventListener('change', (e) => {
    const adType = e.target.value;
    
    // Hide all content groups
    document.getElementById('textContentGroup').style.display = 'none';
    document.getElementById('imageGroup').style.display = 'none';
    document.getElementById('videoGroup').style.display = 'none';
    
    // Clear required attributes
    document.getElementById('textContent').removeAttribute('required');
    document.getElementById('image').removeAttribute('required');
    document.getElementById('video').removeAttribute('required');
    
    // Show relevant groups based on ad type
    switch (adType) {
        case 'text':
            document.getElementById('textContentGroup').style.display = 'block';
            document.getElementById('textContent').setAttribute('required', 'required');
            break;
        case 'image':
            document.getElementById('imageGroup').style.display = 'block';
            document.getElementById('image').setAttribute('required', 'required');
            break;
        case 'video':
            document.getElementById('videoGroup').style.display = 'block';
            document.getElementById('video').setAttribute('required', 'required');
            break;
        case 'text_image':
            document.getElementById('textContentGroup').style.display = 'block';
            document.getElementById('imageGroup').style.display = 'block';
            document.getElementById('textContent').setAttribute('required', 'required');
            document.getElementById('image').setAttribute('required', 'required');
            break;
    }
});

// Image preview
document.getElementById('image').addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            const preview = document.getElementById('imagePreview');
            preview.innerHTML = `<img src="${e.target.result}" alt="Preview">`;
        };
        reader.readAsDataURL(file);
    }
});

// Video preview
document.getElementById('video').addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            const preview = document.getElementById('videoPreview');
            preview.innerHTML = `<video controls src="${e.target.result}"></video>`;
        };
        reader.readAsDataURL(file);
    }
});

// Create ad form submission
document.getElementById('createAdForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    
    // Convert datetime-local to SQL timestamp format
    const startDate = formData.get('startDate');
    const endDate = formData.get('endDate');
    
    if (startDate) {
        formData.set('startDate', startDate.replace('T', ' ') + ':00');
    }
    if (endDate) {
        formData.set('endDate', endDate.replace('T', ' ') + ':00');
    }
    
    try {
        const data = await apiRequest(API.ads.create, {
            method: 'POST',
            body: formData
        });
        
        if (data && data.success) {
            showMessage('message', '广告创建成功！正在跳转...', 'success');
            
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 1500);
        } else {
            showMessage('message', data?.message || '创建失败', 'error');
        }
    } catch (error) {
        console.error('Error creating ad:', error);
        showMessage('message', '创建失败：' + error.message, 'error');
    }
});

// Initialize
loadCategories();
