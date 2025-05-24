package cxz.Final_Project.model;

public class Teacher {
    private int id;
    private String employeeId = null;
    private String name;
    private String college;
    private String department;

    // TODO: 构造方法

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String id) {
        this.employeeId = id;
    }

    public String getName() {
        return name;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String c) {
        this.college = c;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return String.format("Teacher{teacherId=%d, name=%s, college=%s}", id, name, college);
    }
}
