package cxz.Final_Project.model;

public class CourseOffering {
    private String classCode;
    private String courseCode;
    private int teacherId;
    private String semester;

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String c) {
        this.classCode = c;
    }

    public String getCourseCode() {
        return classCode;
    }

    public void setCourseCode(String c) {
        this.courseCode = c;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int id) {
        teacherId = id;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String s) {
        this.semester = s;
    }
}
