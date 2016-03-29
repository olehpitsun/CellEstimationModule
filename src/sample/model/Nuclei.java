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
    private final DoubleProperty contourXc;
    private final DoubleProperty contourYc;
    private final DoubleProperty contourMajor_axis;
    private final DoubleProperty contourMinor_axis;
    private final DoubleProperty contourTheta;
    private final DoubleProperty contourEquiDiameter;

    public Nuclei(int contourNum, double contourArea, double contourPerimetr, double contourHeight,
                  double contourWidth, double contourCircularity, double contourXc, double contourYc,
                  double contourMajor_axis, double contourMinor_axis, double contourTheta, double contourEquiDiameter){

        this.contourNum = new SimpleIntegerProperty(contourNum);
        this.contourArea = new SimpleDoubleProperty(contourArea);
        this.contourPerimetr = new SimpleDoubleProperty(contourPerimetr);
        this.contourHeight = new SimpleDoubleProperty(contourHeight);
        this.contourWidth = new SimpleDoubleProperty(contourWidth);
        this.contourCircularity = new SimpleDoubleProperty(contourCircularity);
        this.contourXc = new SimpleDoubleProperty(contourXc);
        this.contourYc = new SimpleDoubleProperty(contourYc);
        this.contourMajor_axis = new SimpleDoubleProperty(contourMajor_axis);
        this.contourMinor_axis = new SimpleDoubleProperty(contourMinor_axis);
        this.contourTheta = new SimpleDoubleProperty(contourTheta);
        this.contourEquiDiameter = new SimpleDoubleProperty(contourEquiDiameter);


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

    public void setContourXc(Double xc){
        this.contourXc.set(xc);
    }
    public Double getContourXc(){
        return contourXc.get();
    }
    public DoubleProperty contourXcProperty() {
        return contourXc;
    }

    public void setContourYc(Double yc){
        this.contourYc.set(yc);
    }
    public Double getContourYc(){
        return contourYc.get();
    }
    public DoubleProperty contourYcProperty() {
        return contourYc;
    }

    public void setContourMajor_axis(Double major_axis){
        this.contourMajor_axis.set(major_axis);
    }
    public Double getContourMajor_axis(){
        return contourMajor_axis.get();
    }
    public DoubleProperty contourMajor_axisProperty() {
        return contourMajor_axis;
    }

    public void setContourMinor_axis(Double minor_axis){
        this.contourMinor_axis.set(minor_axis);
    }
    public Double getContourMinor_axis(){
        return contourMinor_axis.get();
    }
    public DoubleProperty contourMinor_axisProperty() {
        return contourMinor_axis;
    }

    public void setContourTheta(Double theta){
        this.contourTheta.set(theta);
    }
    public Double getContourTheta(){
        return contourTheta.get();
    }
    public DoubleProperty contourThetaProperty() {
        return contourTheta;
    }

    public void setContourEquiDiameter(Double equiDiameter){
        this.contourEquiDiameter.set(equiDiameter);
    }
    public Double getContourEquiDiameter(){
        return contourEquiDiameter.get();
    }
    public DoubleProperty contourEquiDiameterProperty() {
        return contourEquiDiameter;
    }

}