package com.admanagement.service;

import com.admanagement.dao.UserDAO;
import com.admanagement.model.User;
import com.admanagement.util.JWTUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Service
 */
public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Register a new user
     */
    public Map<String, Object> register(String username, String email, String password, 
                                       String fullName, String companyName, String phone) {
        Map<String, Object> result = new HashMap<>();
        
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Username is required");
            return result;
        }
        
        if (email == null || email.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Email is required");
            return result;
        }
        
        if (password == null || password.length() < 6) {
            result.put("success", false);
            result.put("message", "Password must be at least 6 characters");
            return result;
        }
        
        // Check if username already exists
        if (userDAO.usernameExists(username)) {
            result.put("success", false);
            result.put("message", "Username already exists");
            return result;
        }
        
        // Check if email already exists
        if (userDAO.emailExists(email)) {
            result.put("success", false);
            result.put("message", "Email already exists");
            return result;
        }
        
        // Hash password
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        
        // Create user
        User user = new User(username, email, passwordHash);
        user.setFullName(fullName);
        user.setCompanyName(companyName);
        user.setPhone(phone);
        
        if (userDAO.createUser(user)) {
            // Generate token
            String token = JWTUtil.generateToken(user.getUserId(), user.getUsername());
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("fullName", user.getFullName());
            userData.put("companyName", user.getCompanyName());
            userData.put("token", token);
            
            result.put("success", true);
            result.put("message", "Registration successful");
            result.put("data", userData);
        } else {
            result.put("success", false);
            result.put("message", "Registration failed");
        }
        
        return result;
    }

    /**
     * Login user
     */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Username is required");
            return result;
        }
        
        if (password == null || password.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Password is required");
            return result;
        }
        
        // Get user by username or email
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            user = userDAO.getUserByEmail(username);
        }
        
        if (user == null) {
            result.put("success", false);
            result.put("message", "Invalid username or password");
            return result;
        }
     
        // Verify password
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            result.put("success", false);
            result.put("message", "Invalid username or password");
            return result;
        }
        
        // Generate token
        String token = JWTUtil.generateToken(user.getUserId(), user.getUsername());
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUserId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("fullName", user.getFullName());
        userData.put("companyName", user.getCompanyName());
        userData.put("phone", user.getPhone());
        userData.put("token", token);
        
        result.put("success", true);
        result.put("message", "Login successful");
        result.put("data", userData);
        
        return result;
    }

    /**
     * Validate token and get user ID
     */
    public int validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return -1;
        }
        
        try {
            if (JWTUtil.validateToken(token)) {
                return JWTUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }
}
