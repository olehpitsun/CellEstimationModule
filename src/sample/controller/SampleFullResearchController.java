package sample.controller;

import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import sample.Main;
import sample.core.DB;
import sample.model.*;
import sample.tools.ImageOperations;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * вибір класу (досліду)
     * @throws SQLException
     */
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
        }
        comboBoxClasses.setItems(comboBoxClassesData);
    }

    /**
     * вибір зображення певного класу (досліду)
     * @throws SQLException
     * @throws NullPointerException
     */
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

    /**
     * завантаження маски (сегментованого зображення)
     * @param imgName
     */
    public void loadSegmentedImage(String imgName){
        String research_name = ResearchParam.getResName();// назва досліду
        this.image = Highgui.imread( "C:\\biomedical images\\detected microobjects\\" + research_name + "\\segmented\\" + imgName, Highgui.CV_LOAD_IMAGE_COLOR);
        sample.model.Image.setImageMat(this.image);
        Mat newImage = sample.model.Image.getImageMat();
        this.setSegmentedImage(newImage);

        /*** обробка вибору окремого обєкта*/
        nucleiTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    showOnlyOneObject(newImage, nucleiTable.getSelectionModel().getSelectedItem().contourNumProperty().get());
                }
            }
        });
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

    /**
     * відображення вибраного обєкта
     * @param img Mat
     * @param objNum номер обєкта
     */
    @FXML
    public void showOnlyOneObject(Mat img, Integer objNum){
        Mat src = img;
        Mat src_gray = new Mat();
        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(src_gray, src_gray, new Size(3, 3));

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Mat mMaskMat = new Mat();

        Scalar lowerThreshold = new Scalar ( 0, 0, 0 ); // Blue color – lower hsv values
        Scalar upperThreshold = new Scalar ( 10, 10, 10 ); // Blue color – higher hsv values
        Core.inRange(src, lowerThreshold, upperThreshold, mMaskMat);

        Imgproc.findContours(mMaskMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Moments> mu = new ArrayList<Moments>(contours.size());
        List<Point> mc = new ArrayList<Point>(contours.size());
        Mat drawing = Mat.zeros( mMaskMat.size(), CvType.CV_8UC3 );
        Rect rect ;

        for( int i = 0; i< contours.size(); i++ )
        {
            rect = Imgproc.boundingRect(contours.get(i));
            mu.add(i, Imgproc.moments(contours.get(i), false));
            mc.add(i, new Point(mu.get(i).get_m10() / mu.get(i).get_m00(), mu.get(i).get_m01() / mu.get(i).get_m00()));
            /** малювання обєктів**/
            if(objNum == i){
                Imgproc.drawContours(drawing, contours, i, new Scalar(255, 0, 0), 4, 1, hierarchy, 0, new Point());
                Core.circle(drawing, mc.get(i), 4, new Scalar(0, 0, 255), -1, 2, 0);
                //////////////////////////////////////////////////////////////////////////////////////////////////////
                Core.putText(drawing, Integer.toString(i) , new Point(rect.x,rect.y),
                        Core.FONT_HERSHEY_COMPLEX, 10.0 ,new  Scalar(0,255,0));
            }else{
                Imgproc.drawContours(drawing, contours, i, new Scalar(255, 255, 255), 5, 1, hierarchy, 0, new Point());
            }

            MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
            contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);
        }
        this.setSegmentedImage(drawing);

    }

    /**
     * вибірка параметрів ядер окремого зображення
     * @param imageId ід зображеня
     * @throws SQLException
     */
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



