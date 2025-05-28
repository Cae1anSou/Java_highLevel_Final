package cxz.Final_Project.model;

public class Course {
    private String code;
    private String name;
    private double credit;
    private int moduleId;
    private int propertyId;

    // TODO: 构造

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCredit() {
        return this.credit;
    }

    public void setCredit(double c) {
        this.credit = c;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int id) {
        this.moduleId = id;
    }

    public int getPropertyId() {
        return this.propertyId;
    }

    public void setPropertyId(int id) {
        this.propertyId = id;
    }

    @Override
    public String toString() {
        return String.format("Course{courseCode=%s, name=%s, credit=&d}", code, name, credit);
    }
}
