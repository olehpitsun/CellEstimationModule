package sample.controller;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import sample.Main;
import sample.model.Filters.FilterColection;
import sample.model.Nuclei;
import sample.model.ResearchParam;
import sample.tools.ImageOperations;
import sample.core.DB;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sample.util.PreProcessingParam;

import static java.lang.Math.sqrt;

public class StartController {

    @FXML
    private Button researchNameButton, loadImageButton;
    @FXML
    private ComboBox<FilterColection> comboBox;
    @FXML
    private TextField researchNameField;
    @FXML
    private Label researchName;
    @FXML
    private ImageView originalImage;
    private ObservableList<FilterColection> comboBoxData = FXCollections.observableArrayList();
    @FXML
    private TableView<Nuclei> nucleiTable;
    @FXML
    private TableColumn<Nuclei, Integer> contourNumColumn;
    @FXML
    private TableColumn<Nuclei, Double> contourAreaColumn, contourPerimetrColumn, contourHeightColumn, contourWidthColumn,
            contourCircularityColumn, contourXcColumn, contourYcColumn, contourMajor_axisColumn, contourMinor_axisColumn,
            contourThetaColumn, contourquiDiameterColumn;
    private boolean okClicked = false;
    private Stage stage;
    private FileChooser fileChooser;
    protected Mat image;
    private Stage dialogStage;
    private List<Mat> planes;
    private Mat changedimage;
    private Main mainApp;
    private String researchname;
    private String originalImagePath;
    private ObservableList<Nuclei> nucleiData = FXCollections.observableArrayList();
    private ObservableList<Nuclei> getNucleiData() {
        return nucleiData;
    }
    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public StartController(){
        comboBoxData.add(new FilterColection("0", "Новий клас"));
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

        this.fileChooser = new FileChooser();
        this.changedimage = new Mat();
        this.image = new Mat();
        this.planes = new ArrayList<>();

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

        comboBox.setItems(comboBoxData);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void chooseFile(ActionEvent actionEvent) throws IOException {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files","*.bmp", "*.png", "*.jpg", "*.gif"));
        File file = chooser.showOpenDialog(new Stage());
        if(file != null) {

            this.image = Highgui.imread(file.getAbsolutePath(), Highgui.CV_LOAD_IMAGE_COLOR);
            sample.model.Image.setImageMat(this.image);
            originalImagePath = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("\\")+1);
            try {
                this.imageName(originalImagePath);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Mat newImage = sample.model.Image.getImageMat();
            this.setOriginalImage(newImage);            // show the image
            // call to object detection function
            try {
                this.SimpleDetect();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Please Select a File");
            alert.showAndWait();
        }
    }

    /**
     * функція, де відбувається підрахунок параметрів обєктів на зображення
     * та відбувається занесення їх в БД
     * Спочатку визначаються усі контури,
     * потім оброхунок по кожному контуру
     * input:  (Mat) this.image
     * @throws SQLException
     */
    @FXML
    public void SimpleDetect() throws SQLException {

        double xc,yc,major_axis,minor_axis,theta;

        if(nucleiData.size()>0){// очищаємо таблицю, якщо вона заповнена
            //clearTable();
        }
        Mat src = this.image;
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
            MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(i).toArray() );
            /** малювання обєктів**/
            Imgproc.drawContours(drawing, contours, i, new Scalar(255, 0, 0), 4, 1, hierarchy, 0, new Point());
            Core.circle(drawing, mc.get(i), 4, new Scalar(0, 0, 255), -1, 2, 0);
            //////////////////////////////////////////////////////////////////////////////////////////////////////

            /**
             * Занесення даних до бази даних
             */
            Connection c = DB.getConn();

            String query = "INSERT INTO nuclei_params (image_id, contour_num, contour_area, contour_perimetr," +
                    " contour_height,contour_width, contour_circularity, xc, yc, major_axis, minor_axis, theta," +
                    " equiDiameter  ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,? )";
            PreparedStatement preparedStmt = null;
            preparedStmt = (PreparedStatement) c.prepareStatement(query);

            double contourArea = Imgproc.contourArea(contours.get(i));
            double perimetr = Imgproc.arcLength(contour2f, true);
            double i_height = rect.height;
            double i_width = rect.y;
            double circular = 4*Math.PI * Imgproc.contourArea(contours.get(i)) / Imgproc.arcLength(contour2f, true)
                    * Imgproc.arcLength(contour2f, true);

            /**
             * блок підрахунку xc, yc, major_axis, minor_axis, theta
             * якщо площа більше 2. то все йде норм, інакше 0 , щоб не викидало помилок
             */
            MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
            contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);

            if(contourArea > 2) {
                RotatedRect e = Imgproc.fitEllipse(mMOP2f1);
                 xc = e.center.x;
                 yc = e.center.y;
                 major_axis = e.size.height;    // width >= height
                 minor_axis = e.size.width;
                 theta = e.angle;
            }else{
                 xc = 0;
                 yc = 0;
                 major_axis = 0;    // width >= height
                 minor_axis = 0;
                 theta = 0;
            }

            System.out.println(perimetr);
            double equiDiameter = sqrt(4*contourArea/Math.PI);

            preparedStmt.setInt  (1, ResearchParam.getImg_id());
            preparedStmt.setInt  (2, i);
            preparedStmt.setDouble(3,contourArea);
            preparedStmt.setDouble(4, perimetr);
            preparedStmt.setDouble(5, i_height);
            preparedStmt.setDouble(6, i_width);
            preparedStmt.setDouble(7, circular);
            preparedStmt.setDouble(8, xc);
            preparedStmt.setDouble(9, yc);
            preparedStmt.setDouble(10, major_axis);
            preparedStmt.setDouble(11, minor_axis);
            preparedStmt.setDouble(12, theta);
            preparedStmt.setDouble(13, equiDiameter);
            preparedStmt.executeUpdate();

            nucleiData.add(new Nuclei(i,contourArea,perimetr, i_height,i_width,circular, xc,yc,major_axis,minor_axis,
                    theta,equiDiameter));

        }
        nucleiTable.setItems(getNucleiData());
        this.setOriginalImage(drawing);
    }

    /**
     * функція, для очистки таблиці перед кожним дослідом
     */
    private void clearTable(){
        for(int i=0;i<nucleiData.size();i++){
            Nuclei currentNuclei = (Nuclei) nucleiTable.getItems().get(i);
            nucleiData.remove(currentNuclei);
        }
    }

    @FXML
    private void nextImSetting(){
        Connection c_test = DB.getConn();
        if(c_test != null ) {
            try {
                // c_test.close();// nothing to do. Only for connection test
                this.showNucleiClasses();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }else{
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Помилка");
            alert.setHeaderText("Виникла помилка");
            alert.setContentText("Підключіться до БД");

            alert.showAndWait();
        }
    }

    @FXML
    private void handleComboBoxAction() {

        FilterColection selectedFilter = comboBox.getSelectionModel().getSelectedItem();
        if(selectedFilter.getId() == "0"){
            researchNameField.setVisible(true);
            researchNameButton.setVisible(true);
        }else{
            ResearchParam.setResearch_id(Integer.parseInt(selectedFilter.getId()));
            researchNameField.setVisible(false);
            researchNameButton.setVisible(false);
            loadImageButton.setVisible(true);
        }
    }

    public void showNucleiClasses()throws java.sql.SQLException, ClassNotFoundException{

        ResultSet rs = null;
        Connection c = DB.getConn();
        Statement stmt = (Statement) c.createStatement();
        String query = "select id, name from research_name";
        try {
            rs = (ResultSet) stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (rs.next()) {
            int id = rs.getInt(1);
            String name = rs.getString(2);
            comboBoxData.add(new FilterColection(Integer.toString(id), name));
        }
        comboBox.setVisible(true);
    }

    @FXML
    public void setResearchName() throws IOException, SQLException, ClassNotFoundException {

        if(researchNameField.getText().isEmpty()){
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Попередження");
            alert.setHeaderText("Поле не може бути пустим");
            alert.showAndWait();
        }else{
            this.researchname = researchNameField.getText();
            this.insertResearchNameToDb(this.researchname);
            loadImageButton.setVisible(true);
        }
    }

    @FXML
    public void imageName(String imgN) throws SQLException {

        System.out.print(imgN);
        System.out.println("Research id " + ResearchParam.getResearch_id());
        ResearchParam.setImg_name(imgN);
        Connection c = DB.getConn();
        String query = "INSERT INTO images (research_id, image_name) VALUES (?,?)";
        PreparedStatement preparedStmt = null;

        preparedStmt = (PreparedStatement) c.prepareStatement(query);
        preparedStmt.setInt  (1, ResearchParam.getResearch_id());
        preparedStmt.setString  (2, imgN);
        preparedStmt.executeUpdate();
        //////////////////////////////////////////////////////////////////////////
        ResultSet rs_1 = null;
        Statement stmt_1 = (Statement) c.createStatement();
        String query_1 = "select MAX(id) from images";
        try {
            rs_1 = (ResultSet) stmt_1.executeQuery(query_1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (rs_1.next()) {
            int img_id = rs_1.getInt(1);
            ResearchParam.setImg_id(img_id);

            System.out.printf("img_id: "+img_id);
        }
    }

    @FXML
    public void insertResearchNameToDb(String res_name) throws java.sql.SQLException, ClassNotFoundException {

        Connection c = DB.getConn();

        String query = "INSERT INTO research_name (name) VALUES (?)";
        PreparedStatement preparedStmt = (PreparedStatement) c.prepareStatement(query);
        preparedStmt.setString  (1, res_name);
        preparedStmt.executeUpdate();
        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////

        ResultSet rs_1 = null;
        Statement stmt_1 = (Statement) c.createStatement();
        String query_1 = "select MAX(id) from research_name";
        try {
            rs_1 = (ResultSet) stmt_1.executeQuery(query_1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        while (rs_1.next()) {
            int size = rs_1.getInt(1);
            ResearchParam.setResearch_id(size);

            System.out.printf("size: "+size);
        }
    }

    private void setOriginalImage(Mat dst ){
        this.originalImage.setImage(ImageOperations.mat2Image(dst));
        this.originalImage.setFitWidth(650.0);
        this.originalImage.setFitHeight(650.0);
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    public void handleOk() throws ClassNotFoundException {

        PreProcessingParam prparam = new PreProcessingParam();
        String errorMessage = "";

        if (researchNameField.getText() == null || researchNameField.getText().length() == 0) {
            errorMessage += "Заповніть коректно поле Назва досліду!\n";
        }else{
            prparam.setResearchName(researchNameField.getText());
        }

        if (errorMessage.length() != 0) {
            // Show the error message.
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Заповніть коректно поля");
            alert.setContentText(errorMessage);
            alert.showAndWait();
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleDBConnect() {

        boolean okClicked = mainApp.showDbConnectDialog();
        if (okClicked) {
            mainApp.startProcessing();
        }
    }
}