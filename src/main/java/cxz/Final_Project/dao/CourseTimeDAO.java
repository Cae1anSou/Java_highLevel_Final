package cxz.Final_Project.dao;

import cxz.Final_Project.model.CourseTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CourseTimeDAO extends BaseDAO {
    public void insert (CourseTime cs) {
        String sql = "insert into course_times (teaching_class_code, time_string, time_type) values(?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cs.getClassCode());
            ps.setString(2, cs.getTimeString());
            ps.setString(3, cs.getTimeType());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("插入课程时间出错");
            e.printStackTrace();
        }
    }
}
