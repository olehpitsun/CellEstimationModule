package sample.model;

import org.opencv.core.Mat;

public class Image {

    private static Mat image;

    static {
        image = new Mat();
    }

    public static Mat getImageMat(){
        return image;
    }

    public static Mat setImageMat(Mat newImage){
        image = newImage;
        return image;
    }


}