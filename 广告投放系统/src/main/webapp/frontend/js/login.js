// Login functionality
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    try {
        const data = await apiRequest(API.auth.login, {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        
        if (data && data.success) {
            // Save token and user info
            setAuthToken(data.data.token);
            setUserInfo(data.data);
            
            showMessage('message', '登录成功！正在跳转...', 'success');
            
            // Redirect to dashboard
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1000);
        } else {
            showMessage('message', data?.message || '登录失败', 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showMessage('message', '登录失败：' + error.message, 'error');
    }
});
