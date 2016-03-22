package sample.model;

import javafx.beans.property.*;

/**
 * Created by oleh on 22.03.2016.
 */
public class Nuclei {

    private final StringProperty contourNum;
    private final StringProperty contourArea;
    private final DoubleProperty contourPerimetr;
    private final DoubleProperty contourHeight;
    private final DoubleProperty contourWidth;
    private final DoubleProperty contourCircularity;




    public Nuclei(String contourNum, String contourArea, int contourPerimetr, int contourHeight,
                  int contourWidth, int contourCircularity){

        this.contourNum = new SimpleStringProperty(contourNum);
        this.contourArea = new SimpleStringProperty(contourArea);
        this.contourPerimetr = new SimpleDoubleProperty(contourPerimetr);
        this.contourHeight = new SimpleDoubleProperty(contourHeight);
        this.contourWidth = new SimpleDoubleProperty(contourWidth);
        this.contourCircularity = new SimpleDoubleProperty(contourCircularity);
    }


    public void setContourNum(String contourNum){
        this.contourNum.set(contourNum);
    }

    public String getContourNum(){
        return contourNum.get();
    }

    public StringProperty contourNumProperty() {
        return contourNum;
    }

    public void setContourArea(String contourArea){
        this.contourArea.set(contourArea);
    }

    public String getContourArea(){
        return contourArea.get();
    }

    public StringProperty contourAreaProperty() {
        return contourArea;
    }

    public void setContourPerimetr(Double contourPerimetr){
        this.contourPerimetr.set(contourPerimetr);
    }

    public Double getContourPerimetr(){
        return contourPerimetr.get();
    }

    public DoubleProperty contourPerimetrProperty() {
        return contourPerimetr;
    }

    public void setContourHeight(Double contourHeight){
        this.contourHeight.set(contourHeight);
    }

    public Double getContourHeight(){
        return contourHeight.get();
    }

    public DoubleProperty contourHeightProperty() {
        return contourHeight;
    }

    public void setContourWidth(Double contourWidth){
        this.contourWidth.set(contourWidth);
    }

    public Double getContourWidth(){
        return contourWidth.get();
    }

    public DoubleProperty contourWidthtProperty() {
        return contourWidth;
    }

    public void setContourCircularity(Double contourCircularity){
        this.contourCircularity.set(contourCircularity);
    }
    public Double getContourCircularity(){
        return contourCircularity.get();
    }
    public DoubleProperty contourCircularityProperty() {
        return contourCircularity;
    }


}
