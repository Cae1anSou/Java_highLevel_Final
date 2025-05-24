package cxz.Final_Project.model;

public class Module {
    private int id;
    private String name;

    public Module() {}

    public Module(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Module{moduleID=%s,moduleName=%s}", id, name);
    }
}
