package org.example.shopping_group.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接工具类
 */
public class DBUtil {
    // 数据库连接配置（需根据本地数据库修改URL/用户名/密码）
    private static final String URL = "jdbc:mysql://localhost:3306/shopping_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String USERNAME = "root";  // 本地MySQL用户名
    private static final String PASSWORD = "123456";// 本地MySQL密码

    // 加载MySQL驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("MySQL驱动加载失败！请检查是否引入mysql-connector-java依赖");
        }
    }

    /**
     * 获取数据库连接
     * @return Connection 数据库连接对象
     * @throws SQLException 连接失败异常
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * 关闭资源（ResultSet/PreparedStatement/Connection）
     * @param resources 可变参数，传入需要关闭的资源
     */
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}