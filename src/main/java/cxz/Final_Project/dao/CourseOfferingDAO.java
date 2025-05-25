package cxz.Final_Project.dao;

import cxz.Final_Project.model.CourseOffering;
import cxz.Final_Project.model.SchedulableCourse;
import cxz.Final_Project.model.TimeSlot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CourseOfferingDAO extends BaseDAO {
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

    public List<SchedulableCourse> getSchedulableCoursesBySemester(String semester) {
        List<SchedulableCourse> courses = new ArrayList<>();
        String sql = "SELECT c.course_code, c.name AS course_name, c.credits, m.module_name, " +
                "GROUP_CONCAT(ct.time_string SEPARATOR ';') AS time_strings " +
                "FROM course_offerings AS co " +
                "JOIN courses AS c ON co.course_code = c.course_code " +
                "JOIN modules AS m ON c.module_id = m.module_id " +
                "JOIN course_times AS ct ON co.teaching_class_code = ct.teaching_class_code " +
                "WHERE co.semester = ? " +
                "GROUP BY co.teaching_class_code, c.course_code, c.name, c.credits, m.module_name";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    String moduleName = rs.getString("module_name");
                    double credits = rs.getDouble("credits");
                    String timeStrings = rs.getString("time_strings");

                    List<TimeSlot> timeSlots = new ArrayList<>();
                    if (timeStrings != null && !timeStrings.isEmpty()) {
                        timeSlots = Arrays.stream(timeStrings.split(";"))
                                .map(s -> new TimeSlot(s)) // 使用我们创建的TimeSlot构造函数
                                .collect(Collectors.toList());
                    }

                    courses.add(new SchedulableCourse(courseCode, courseName, moduleName, credits, timeSlots));
                }
            }
        } catch (SQLException e) {
            System.err.println("错误：查询可排课程列表时发生数据库错误。");
            e.printStackTrace();
        }
        return courses;
    }
}
