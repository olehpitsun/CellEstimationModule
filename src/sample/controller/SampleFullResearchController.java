package sample.controller;

import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import sample.Main;
import sample.core.DB;
import sample.model.*;
import sample.model.Filters.FilterColection;
import sample.tools.ImageOperations;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SampleFullResearchController {

    private Stage stage;
    private Mat image;
    private Main mainApp;
    private Stage dialogStage;
    @FXML
    private TableView<Nuclei> nucleiTable;
    @FXML
    private TableColumn<Nuclei, Integer> contourNumColumn;
    @FXML
    private TableColumn<Nuclei, Double> contourAreaColumn, contourPerimetrColumn, contourHeightColumn, contourWidthColumn,
            contourCircularityColumn, contourXcColumn, contourYcColumn, contourMajor_axisColumn, contourMinor_axisColumn,
            contourThetaColumn, contourquiDiameterColumn;
    @FXML
    private ImageView originalImage, segmentedImage;
    @FXML
    private ComboBox<ClassesColection> comboBoxClasses;
    @FXML
    private ComboBox<ImagesColection> comboBoxImages;
    @FXML
    private Button generateFileButton;
    @FXML
    private Label researchNameLabel, imageNameLabel;
    private ObservableList<ClassesColection> comboBoxClassesData = FXCollections.observableArrayList();
    private ObservableList<ImagesColection> comboBoxImagesData = FXCollections.observableArrayList();
    private ArrayList selectedNucleiParam = new ArrayList();
    private ArrayList selectedImagesId = new ArrayList();
    private ObservableList<Nuclei> nucleiData = FXCollections.observableArrayList();
    private ObservableList<Nuclei> getNucleiData() {
        return nucleiData;
    }
    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public SampleFullResearchController() {

        //comboBoxClassesData.add(new ClassesColection("0", "Новий клас"));
    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        contourNumColumn.setCellValueFactory(cellData -> cellData.getValue().contourNumProperty().asObject());
        contourAreaColumn.setCellValueFactory(cellData -> cellData.getValue().contourAreaProperty().asObject());
        contourPerimetrColumn.setCellValueFactory(cellData -> cellData.getValue().contourPerimetrProperty().asObject());
        contourHeightColumn.setCellValueFactory(cellData -> cellData.getValue().contourHeightProperty().asObject());
        contourWidthColumn.setCellValueFactory(cellData -> cellData.getValue().contourWidthtProperty().asObject());
        contourCircularityColumn.setCellValueFactory(cellData -> cellData.getValue().contourCircularityProperty().asObject());
        contourXcColumn.setCellValueFactory(cellData -> cellData.getValue().contourXcProperty().asObject());
        contourYcColumn.setCellValueFactory(cellData -> cellData.getValue().contourYcProperty().asObject());
        contourMajor_axisColumn.setCellValueFactory(cellData -> cellData.getValue().contourMajor_axisProperty().asObject());
        contourMinor_axisColumn.setCellValueFactory(cellData -> cellData.getValue().contourMinor_axisProperty().asObject());
        contourThetaColumn.setCellValueFactory(cellData -> cellData.getValue().contourThetaProperty().asObject());
        contourquiDiameterColumn.setCellValueFactory(cellData -> cellData.getValue().contourEquiDiameterProperty().asObject());

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void startResearch(){
        System.out.println(comboBoxClassesData.size());
        if(comboBoxClassesData.size() > 0){
            mainApp.showFullReport();
        }else{
            try {
                handleClassesAction();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void handleClassesAction() throws SQLException {


        comboBoxClasses.setVisible(true);
        ResultSet rs = null;
        Connection c = DB.getConn();
        Statement stmt = (Statement) c.createStatement();
        String table = "research_name";
        String query = "select id, name from " + table ;
        try {
            rs = (ResultSet) stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (rs.next()) {
            int id = rs.getInt(1);
            String name = rs.getString(2);
            comboBoxClassesData.add(new ClassesColection(Integer.toString(id), name));

            System.out.println(id + name);
        }
        comboBoxClasses.setItems(comboBoxClassesData);
    }

    public void handleImagesAction() throws SQLException, NullPointerException {

        comboBoxImages.setVisible(true);
        ClassesColection selectedClass = comboBoxClasses.getSelectionModel().getSelectedItem();
        ResearchParam.setResearch_name(selectedClass.getId());
        ResearchParam.setResName(selectedClass.getclassName());//назва досліду
        researchNameLabel.setText("Дослід: " + selectedClass.getclassName());
        ResultSet rs = null;
        Connection c = DB.getConn();
        Statement stmt = (Statement) c.createStatement();
        String table = "images";
        String query = "select id, research_id, image_name from " + table + "  where research_id = " + selectedClass.getId();
        try {
            rs = (ResultSet) stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (rs.next()) {
            int id = rs.getInt(1);
            int classId = rs.getInt(2);
            String name = rs.getString(3);
            comboBoxImagesData.add(new ImagesColection(Integer.toString(id), Integer.toString(classId), name));
        }
        comboBoxImages.setItems(comboBoxImagesData);
    }

    public void handleAddImagesAction(){

        ImagesColection selectedImage = comboBoxImages.getSelectionModel().getSelectedItem();
        selectedImagesId.add(selectedImage.getId());
        loadOriginalImage(selectedImage.getImageName());// завантаження оригінального зображення
        loadSegmentedImage(selectedImage.getImageName());// завантаження сегментованого зображення

        imageNameLabel.setText("Зображення: " + selectedImage.getImageName());
        System.out.println(selectedImage.getId());
        try {
            getNucleiParamByImage(selectedImage.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * функція завантажує зображення
     * @param imgName
     */
    public void loadOriginalImage(String imgName){

        String research_name = ResearchParam.getResName();// назва досліду

        imgName = imgName.substring(imgName.indexOf('(') + 1, imgName.indexOf(')'));// розПАРСЕРНЯ стрічки. Витягуєио лише назву в 1 дужках
        try{
            Mat orgIm = Highgui.imread( "C:\\biomedical images\\detected microobjects\\" + research_name + "\\origin\\" + imgName, Highgui.CV_LOAD_IMAGE_COLOR);
            this.setOriginalImage(orgIm);            // show the image
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Помилка");
            alert.setHeaderText("Виникла помилка із шляхом до оригінального зображення");
            alert.showAndWait();
        }
    }

    public void loadSegmentedImage(String imgName){
        String research_name = ResearchParam.getResName();// назва досліду
        this.image = Highgui.imread( "C:\\biomedical images\\detected microobjects\\" + research_name + "\\segmented\\" + imgName, Highgui.CV_LOAD_IMAGE_COLOR);
        sample.model.Image.setImageMat(this.image);
        Mat newImage = sample.model.Image.getImageMat();
        this.setSegmentedImage(newImage);
    }



    private void setOriginalImage(Mat dst ){

        this.originalImage.setImage(ImageOperations.mat2Image(dst));
        this.originalImage.setFitWidth(450.0);
        this.originalImage.setFitHeight(450.0);

    }
    private void setSegmentedImage(Mat dst ){
        this.segmentedImage.setImage(ImageOperations.mat2Image(dst));
        this.segmentedImage.setFitWidth(450.0);
        this.segmentedImage.setFitHeight(450.0);
    }

    public void getNucleiParamByImage(String imageId) throws SQLException {

        ResultSet rs = null;
        Connection c = DB.getConn();
        Statement stmt = (Statement) c.createStatement();
        String query = "select contour_num, contour_area, contour_perimetr, contour_height,contour_width," +
                " contour_circularity, xc, yc, major_axis, minor_axis, theta, equiDiameter from nuclei_params " +
                "where image_id = " + imageId;
        try {
            rs = (ResultSet) stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (rs.next()) {
            int id = rs.getInt(1);
            Double contour_area = rs.getDouble(2);
            Double contour_perimetr = rs.getDouble(3);
            Double contour_height = rs.getDouble(4);
            Double contour_width = rs.getDouble(5);
            Double contour_circularity = rs.getDouble(6);
            Double xc = rs.getDouble(7);
            Double yc = rs.getDouble(8);
            Double major_axis = rs.getDouble(9);
            Double minor_axis = rs.getDouble(10);
            Double theta = rs.getDouble(11);
            Double equiDiameter = rs.getDouble(12);


            nucleiData.add(new Nuclei(id,contour_area,contour_perimetr, contour_height,contour_width,
                    contour_circularity, xc, yc, major_axis, minor_axis, theta,equiDiameter));
        }
        nucleiTable.setItems(getNucleiData());

        comboBoxClasses.setVisible(false);
        comboBoxImages.setVisible(false);
    }
}



