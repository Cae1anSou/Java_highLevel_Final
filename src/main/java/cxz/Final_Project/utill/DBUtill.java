package cxz.Final_Project.utill;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtill {
    private static final String JDBC_URL = "jdbc:mysql://172.30.236.185:3306/DF?useSSL=false&serverTimezone=GMT&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
    private static final String USER = "caelan";
    private static final String PASSWORD = "Silvis0852";

    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(JDBC_URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);

            // 性能相关
            config.addDataSourceProperty("cachePrepStmts", "true"); // 缓存PreparedStatement
            config.addDataSourceProperty("prepStmtCacheSize", "250"); // 缓存大小
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048"); // SQL最大长度
            config.addDataSourceProperty("useServerPrepStmts", "true"); // 使用服务器端预处理
            config.addDataSourceProperty("rewriteBatchedStatements", "true"); // 批量更新时重写语句以提高性能

            // 连接池大小
            config.setMinimumIdle(5);      // 最小空闲连接数
            config.setMaximumPoolSize(20); // 最大连接数，对于桌面应用20个已经非常充足

            // 连接生命周期
            config.setConnectionTimeout(30000); // 获取连接的超时时间: 30秒
            config.setIdleTimeout(600000);      // 空闲连接存活时间: 10分钟
            config.setMaxLifetime(1800000);     // 连接最大生命周期: 30分钟

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            System.err.println("错误: HikariCP数据库连接池初始化失败！");
            e.printStackTrace();
            // 抛出运行时异常，使应用启动失败，以便及早发现问题
            throw new RuntimeException("无法初始化数据库连接池", e);
        }
    }

    private DBUtill() {}


    /**
     * 从连接池获取一个数据库连接。
     * @return 一个可用的数据库连接
     * @throws SQLException 如果连接池无法提供连接
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("HikariCP数据源未初始化，请检查静态初始化块。");
        }
        return dataSource.getConnection();
    }

    // close方法保持不变，现在它会将连接归还给池
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        // 实现保持不变...
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * 在应用程序关闭时，调用此方法来优雅地关闭连接池。
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}