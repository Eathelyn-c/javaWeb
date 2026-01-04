package com.admanagement.servlet;

import com.admanagement.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@WebServlet("/api/test-connection")
public class TestConnectionServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 读取配置
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            props.load(input);
            
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");
            String driver = props.getProperty("db.driver");
            
            result.put("config_url", url);
            result.put("config_username", username);
            result.put("config_driver", driver);
            
            // 加载驱动
            Class.forName(driver);
            result.put("driver_loaded", true);
            
            // 尝试连接
            long startTime = System.currentTimeMillis();
            Connection conn = DriverManager.getConnection(url, username, password);
            long endTime = System.currentTimeMillis();
            
            result.put("connection_success", true);
            result.put("connection_time_ms", endTime - startTime);
            result.put("connection_valid", conn.isValid(5));
            
            conn.close();
            
            ResponseUtil.sendSuccess(response, result, "Connection test successful");
            
        } catch (Exception e) {
            result.put("connection_success", false);
            result.put("error_type", e.getClass().getName());
            result.put("error_message", e.getMessage());
            
            // 获取完整堆栈
            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace.append(element.toString()).append("\n");
            }
            result.put("stack_trace", stackTrace.toString());
            
            if (e.getCause() != null) {
                result.put("cause_type", e.getCause().getClass().getName());
                result.put("cause_message", e.getCause().getMessage());
            }
            
            ResponseUtil.sendError(response, 500, "Connection test failed: " + e.getMessage());
        }
    }
}
