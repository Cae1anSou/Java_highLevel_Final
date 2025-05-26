package cxz.Final_Project.model;

public class Course {
    private String code;
    private String name;
    private double credits;
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

    public double getCredits() {
        return credits;
    }

    public void setCredits(double c) {
        this.credits = c;
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
        return String.format("Course{courseCode=%s, name=%s, credits=&d}", code, name, credits);
    }
}
