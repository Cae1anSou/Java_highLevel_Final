package cxz.Final_Project.utill;

import java.sql.*;

public class DBUtill {
    private static final String url = "jdbc:mysql://172.30.236.185:3306/course?useSSL=false&serverTimezone=GMT&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
    private static final String user = "caelan";
    private static final String psw = "Silvis0852";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch ( ClassNotFoundException e) {
            System.err.println(String.format("加载数据库失败：%s", e.getMessage()));
        }
    }

    private DBUtill() {}

    public static Connection getConnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(url, user, psw);
        } catch (SQLException e) {
            System.err.println("数据库驱动加载失败");
            e.printStackTrace();
        }
        return con;
    }

    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
