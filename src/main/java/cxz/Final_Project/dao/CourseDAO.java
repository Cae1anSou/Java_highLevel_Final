package cxz.Final_Project.dao;

import cxz.Final_Project.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseDAO extends BaseDAO {
    public void insertIfNotExist(Course course) {
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
            return false;  // TODO: 这里的逻辑需要再想想，可以加入更多的处理
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

        private void update(Course course) {
        String sql = "UPDATE courses SET name = ?, credits = ?, module_id = ?, property_id = ? WHERE course_code = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, course.getName());
            ps.setDouble(2, course.getCredits());

            if (course.getModuleId() != 0 && course.getModuleId() != -1) {
                ps.setInt(3, course.getModuleId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            if (course.getPropertyId() != 0 && course.getPropertyId() != -1) {
                ps.setInt(4, course.getPropertyId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setString(5, course.getCode());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("更新课程失败，未找到课程代码: " + course.getCode());
            }

        } catch (SQLException e) {
            System.err.println("更新课程信息时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void insertOrUpdate(Course course) {
        if (exist(course.getCode())) {
            // 如果课程已存在，检查是否需要更新其 module_id 或 property_id
            // 这是一个简化的逻辑：如果传入的 module_id 或 property_id 有效，就更新。
            // 更复杂的逻辑可能需要先查询数据库中已有的 module_id 和 property_id，
            // 仅当新值更“完整”时才更新。但对于目前的需求，直接更新是可行的。
            update(course);
        } else {
            insert(course);
        }
    }
}
