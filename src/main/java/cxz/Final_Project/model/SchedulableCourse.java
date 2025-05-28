package cxz.Final_Project.model;

import java.util.List;

public class SchedulableCourse {
    private String courseCode;
    private String courseName;
    private String moduleName;
    private double credit;
    private List<TimeSlot> timeSlots; // 一门课可能的所有上课时间
    private String teacherName;

    public SchedulableCourse(String courseCode, String courseName, String moduleName, double credit, List<TimeSlot> timeSlots, String teacherName) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.moduleName = moduleName;
        this.credit = credit;
        this.timeSlots = timeSlots;
        this.teacherName = teacherName;
    }

    public boolean conflictsWith(SchedulableCourse other) {
        for (TimeSlot thisSlot : this.timeSlots) {
            for (TimeSlot otherSlot : other.timeSlots) {
                if (thisSlot.conflictsWith(otherSlot)) {
                    return true; // 只要有一对时间冲突，这两门课就冲突
                }
            }
        }
        return false;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public double getCredit() {
        return this.credit;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public String getTeacherName() {
        return teacherName;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s, %.1f学分", courseName, courseCode, moduleName, credit);
    }
}