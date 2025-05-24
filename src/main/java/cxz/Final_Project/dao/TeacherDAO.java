package cxz.Final_Project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TeacherDAO extends BaseDAO {
    // TODO: 构造
    public int findOrInsert(String name, String college) {
        String sql = "select teacher_id from teachers where name = ? and college = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, college);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("teacher_id");
                } else {
                    return insertTeacher(name, college, con);
                }
            }
        } catch (SQLException e) {
            System.err.println("在findOrInsertTeacher中发生错误");
            e.printStackTrace();
            return -1;
        }
    }

    private int insertTeacher(String name, String college, Connection con) throws SQLException {
        String sql = "insert into teachers (name, college) values (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, college);
            int lines = ps.executeUpdate();

            if (lines == 0) {
                throw new SQLException("插入教师数据失败");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("插入教师失败， 无法获取生成的ID");
                }
            }
        }
    }
}
