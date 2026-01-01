// Register functionality
document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        fullName: document.getElementById('fullName').value,
        companyName: document.getElementById('companyName').value,
        phone: document.getElementById('phone').value
    };
    
    try {
        const data = await apiRequest(API.auth.register, {
            method: 'POST',
            body: JSON.stringify(formData)
        });
        
        if (data && data.success) {
            // Save token and user info
            setAuthToken(data.data.token);
            setUserInfo(data.data);
            
            showMessage('message', '注册成功！正在跳转...', 'success');
            
            // Redirect to dashboard
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1000);
        } else {
            showMessage('message', data?.message || '注册失败', 'error');
        }
    } catch (error) {
        console.error('Register error:', error);
        showMessage('message', '注册失败：' + error.message, 'error');
    }
});
