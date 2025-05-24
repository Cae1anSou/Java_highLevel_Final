package cxz.Final_Project.dao;

import cxz.Final_Project.model.CourseOffering;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseOfferingDAO extends BaseDAO{
    public void insertIfNotExist(CourseOffering offering) {
        if (!exists(offering.getClassCode())) {
            insert(offering);
        }
    }

    private boolean exists(String code) {
        String sql = "select * from course_offerings where teaching_class_code = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("查找课程开课情况出错");
            e.printStackTrace();
            return true; // TODO: 同样，考虑是否合理，可能不能简单粗暴地return true；
        }
    }

    private void insert(CourseOffering offering) {
        String sql = "insert into course_offerings (teaching_class_code, course_code, teacher_id, semester) values (?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, offering.getClassCode());
            ps.setString(2, offering.getCourseCode());
            ps.setInt(3, offering.getTeacherId());
            ps.setString(4, offering.getSemester());

            ps.execute();
        } catch (SQLException e) {
            System.err.println("插入开课情况出错");
            e.printStackTrace();
        }
    }
}
