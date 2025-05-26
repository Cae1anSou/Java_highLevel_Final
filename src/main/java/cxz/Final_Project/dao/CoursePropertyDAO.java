package cxz.Final_Project.dao;

import java.sql.*;

public class CoursePropertyDAO extends BaseDAO {
    public int findOrInsert(String name) {
        if (name == null || name.trim().isEmpty()) {
            return -1; // 或者返回一个代表“未指定”的特定ID
        }
        String sql = "SELECT property_id FROM course_properties WHERE property_name = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("property_id");
                } else {
                    return insertProperty(name, con);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int insertProperty(String name, Connection con) throws SQLException {
        String sql = "INSERT INTO course_properties (property_name) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("插入课程性质失败，无法获取ID。");
                }
            }
        }
    }
}