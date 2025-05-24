package cxz.Final_Project.model;

public class CourseTime {
    private int timeId;
    private String classCode;
    private String timeString;
    private String timeType;

    public int getTimeId() {
        return timeId;
    }

    public void setTimeId(int id) {
        this.timeId = id;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String c) {
        this.classCode = c;
    }

    public String getTimeString() {
        return this.timeString;
    }

    public void setTimeString(String c) {
        this.timeString = c;
    }

    public String getTimeType() {
        return timeType;
    }

    public void setTimeType(String c) {
        this.timeType = c;
    }
}
