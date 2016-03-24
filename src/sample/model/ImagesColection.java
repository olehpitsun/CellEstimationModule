package sample.model;


public class ImagesColection {
    private String id;
    private String classId;
    private String imageName;

    public ImagesColection(String id, String classId,String imageName) {
        this.id = id;
        this.classId = classId;
        this.imageName = imageName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassId() {
        return classId;
    }

    public void setclassId(String classId) {
        this.classId = classId;
    }


    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public String toString() {
        return id + " " + classId + " " + imageName;
    }
}