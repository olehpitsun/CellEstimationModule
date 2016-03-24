package sample.model;


public class ClassesColection {
    private String id;
    private String className;

    public ClassesColection(String id, String className) {
        this.id = id;
        this.className = className;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getclassName() {
        return className;
    }

    public void setclassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return id + " " + className;
    }
}