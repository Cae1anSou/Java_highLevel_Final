package cxz.Final_Project.dao;

import cxz.Final_Project.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseDAO extends BaseDAO {
    public void insetIfNotExist(Course course) {
        if (!exist(course.getCode())) {
            insert(course);
        }
    }

    private boolean exist(String code) {
        String sql = "select * from courses where course_code = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("检查课程是否存在时出错");
            e.printStackTrace();
            return true;  // TODO: 这里的逻辑需要再想想，可能不能保守地认为课程已存在
        }
    }

    private void insert(Course course) {
        String sql = "insert into courses (course_code, name, credits, module_id) value(?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, course.getCode());
            ps.setString(2, course.getName());
            ps.setDouble(3, course.getCredits());
            ps.setInt(4, course.getModuleId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("插入课程信息时出错");
            e.printStackTrace();
        }
    }
}
