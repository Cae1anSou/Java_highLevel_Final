package cxz.Final_Project.dao;

import cxz.Final_Project.model.CourseOffering;
import cxz.Final_Project.model.SchedulableCourse;
import cxz.Final_Project.model.TimeSlot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static Pattern CTP = Pattern.compile("^(星期[一二三四五六日])第([\\d,-]+)节(\\{.+?\\})$");

    public List<SchedulableCourse> getSchedulableCourses(String semester) {
        List<SchedulableCourse> courses = new ArrayList<>();
        String sql = "SELECT c.course_code, c.name AS course_name, c.credits, m.module_name, t.name AS teacher_name, " +
                "GROUP_CONCAT(DISTINCT ct.time_string SEPARATOR '|||') AS all_time_segment " +
                "FROM course_offerings AS co " +
                "JOIN courses AS c ON co.course_code = c.course_code " +
                "JOIN modules AS m ON c.module_id = m.module_id " + // 使用LEFT JOIN确保即使没有module_id的课程也能被包含
                "JOIN course_times AS ct ON co.teaching_class_code = ct.teaching_class_code " +
                "JOIN teachers AS t ON co.teacher_id = t.teacher_id " +
                "WHERE co.semester = ? " +
                "GROUP BY co.teaching_class_code, c.course_code, c.name, c.credits, m.module_name";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    String moduleName = rs.getString("module_name"); // 如果module_id为NULL，这里会是NULL
                    String teacherName = rs.getString("teacher_name");
                    if (moduleName == null) {
                        moduleName = "未指定模块";
                    }
                    double credit = rs.getDouble("credits");
                    String allTimeSegment = rs.getString("all_time_segment");

                    List<TimeSlot> allTimes = new ArrayList<>();
                    if (allTimeSegment != null && !allTimeSegment.isEmpty()) {
                        String[] individualTime = allTimeSegment.split("\\|\\|\\|");
                        for (String singleTime : individualTime) {
                            singleTime = singleTime.trim();
                            /*
                             第二层处理：一个 time_string 内部可能包含逗号分隔的多个时间段
                             例如："星期五第1-1,6-7节{1-16周}"
                             我们需要将其拆分为 "星期五第1-1节{1-16周}" 和 "星期五第6-7节{1-16周}"
                             正则表达式来提取星期、节次部分和周次部分
                             例: "星期五第1-1,6-7节{1-16周}"
                             group(1): 星期五
                             group(2): 1-1,6-7 (节次部分)
                             group(3): {1-16周} (周次和类型部分)
                            */
                            Matcher complexMatcher = CTP.matcher(singleTime.trim());

                            if (complexMatcher.find()) {
                                String dayPart = complexMatcher.group(1);
                                String periodsPart = complexMatcher.group(2);
                                String weekPart = complexMatcher.group(3);

                                String[] periodSegments = periodsPart.split(",");
                                for (String periodSegment : periodSegments) {
                                    try {
                                        String parsableTimeString = dayPart + "第" + periodSegment.trim() + "节" + weekPart;
                                        allTimes.add(new TimeSlot(parsableTimeString));
                                    } catch (IllegalArgumentException e) {
                                        System.err.println("DAO内部创建TimeSlot失败，原始记录: '" + singleTime + "', 尝试解析: '" + (dayPart + "第" + periodSegment.trim() + "节" + weekPart) + "': " + e.getMessage());
                                    }
                                }
                            } else {
                                System.err.println("DAO内部无法解析时间记录，格式不符合基本规范: '" + singleTime + "'");
                            }
                        }
                    }
                    courses.add(new SchedulableCourse(courseCode, courseName, moduleName, credit, allTimes, teacherName));
                }
            }
        } catch (SQLException e) {
            System.err.println("错误：查询可排课程列表时发生数据库错误: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

        public List<String> findDistinctSemesters() {
        List<String> semesters = new ArrayList<>();
        String sql = "SELECT DISTINCT semester FROM course_offerings ORDER BY semester DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                semesters.add(rs.getString("semester"));
            }
        } catch (SQLException e) {
            System.err.println("查询所有学期失败: " + e.getMessage());
            // 在实际应用中，这里应该使用日志框架记录错误
        }
        return semesters;
    }
}
