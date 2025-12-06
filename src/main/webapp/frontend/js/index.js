// Check authentication
if (!requireAuth()) {
    // Will redirect to login
} else {
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
}
