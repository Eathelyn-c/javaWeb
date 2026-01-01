package org.example.shopping_group_r1.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/shopping_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("MySQL驱动加载失败！请检查是否引入mysql-connector-java依赖");
        }
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }


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