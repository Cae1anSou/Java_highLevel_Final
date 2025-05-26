package cxz.Final_Project.dao;

import java.sql.*;

public class ModuleDAO extends BaseDAO {
    // TODO: 构造

    public int findOrInsert(String name) {
        String sql = "select module_id from modules where module_name = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("module_id");
            } else {
                return insertModule(name, con);
            }
        } catch(SQLException e) {
            System.err.println("findOrInsertModule中发生错误");
            e.printStackTrace();
        return -1;
    }
}

private int insertModule(String name, Connection con) throws SQLException {
    String sql = "insert into modules (module_name) values (?)";

    try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, name);
        int lines = ps.executeUpdate();

        if (lines == 0) {
            throw new SQLException("插入模块失效，插入失败");
        }

        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("插入模块失效，无法获取生成的ID");
            }
        }
    }
}
}
