package sample.model;

import javafx.beans.property.*;

/**
 * Created by oleh on 22.03.2016.
 */
public class Nuclei {

    private final IntegerProperty contourNum;
    private final DoubleProperty contourArea;
    private final DoubleProperty contourPerimetr;
    private final DoubleProperty contourHeight;
    private final DoubleProperty contourWidth;
    private final DoubleProperty contourCircularity;




    public Nuclei(int contourNum, double contourArea, double contourPerimetr, double contourHeight,
                  double contourWidth, double contourCircularity){

        this.contourNum = new SimpleIntegerProperty(contourNum);
        this.contourArea = new SimpleDoubleProperty(contourArea);
        this.contourPerimetr = new SimpleDoubleProperty(contourPerimetr);
        this.contourHeight = new SimpleDoubleProperty(contourHeight);
        this.contourWidth = new SimpleDoubleProperty(contourWidth);
        this.contourCircularity = new SimpleDoubleProperty(contourCircularity);
    }


    public void setContourNum(Integer contourNum){
        this.contourNum.set(contourNum);
    }

    public Integer getContourNum(){
        return contourNum.get();
    }

    public IntegerProperty contourNumProperty() {
        return contourNum;
    }

    public void setContourArea(Double contourArea){
        this.contourArea.set(contourArea);
    }

    public Double getContourArea(){
        return contourArea.get();
    }

    public DoubleProperty contourAreaProperty() {
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