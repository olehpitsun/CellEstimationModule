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

public class StartController {

    @FXML
    private Button researchNameButton;
    @FXML
    private Button loadImageButton;

    @FXML
    private ComboBox<FilterColection> comboBox;
    @FXML
    private TextField researchNameField;

    @FXML
    private Label researchName;
    @FXML
    protected ImageView originalImage;

    private ObservableList<FilterColection> comboBoxData = FXCollections.observableArrayList();

    @FXML
    private TableView<Nuclei> nucleiTable;
    @FXML
    private TableColumn<Nuclei, Integer > contourNumColumn;
    @FXML
    private TableColumn<Nuclei, Double> contourAreaColumn;
    @FXML
    private TableColumn<Nuclei, Double> contourPerimetrColumn;
    @FXML
    private TableColumn<Nuclei, Double> contourHeightColumn;
    @FXML
    private TableColumn<Nuclei, Double> contourWidthColumn;
    @FXML
    private TableColumn<Nuclei, Double> contourCircularityColumn;
    private boolean okClicked = false;
    private Stage stage;
    // the JavaFX file chooser
    private FileChooser fileChooser;
    // support variables
    protected Mat image;
    private Stage dialogStage;

    protected List<Mat> planes;
    // Reference to the main application.
    protected Mat changedimage;
    protected Main mainApp;
    private String researchname;
    private String originalImagePath;

    private ObservableList<Nuclei> nucleiData = FXCollections.observableArrayList();
    public ObservableList<Nuclei> getNucleiData() {
        return nucleiData;
    }
    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public StartController(){

        comboBoxData.add(new FilterColection("0", "Новий клас"));
        //personData.add(new Person("Hans", "Muster"));
    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {

        this.mainApp = mainApp;
        // nucleiTable.setItems(mainApp.getNucleiData());

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
            // show the image
            this.setOriginalImage(newImage);


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

    @FXML
    public void SimpleDetect() throws SQLException {

        Mat src = this.image;
        Mat src_gray = new Mat();
        //int thresh = 100;
        //int max_thresh = 255;

        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(src_gray, src_gray, new Size(3, 3));

        //Mat canny_output = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        //Mat mHsvMat = new Mat();
        Mat mMaskMat = new Mat();

        Scalar lowerThreshold = new Scalar ( 0, 0, 0 ); // Blue color – lower hsv values
        Scalar upperThreshold = new Scalar ( 10, 10, 10 ); // Blue color – higher hsv values
        Core.inRange(src, lowerThreshold, upperThreshold, mMaskMat);

        //Imgproc.Canny(src_gray, canny_output, thresh, thresh * 2, 3, false);
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

            Moments p = mu.get(i);
            int x = (int) (p.get_m10() / p.get_m00());
            int y = (int) (p.get_m01() / p.get_m00());

            MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(i).toArray() );

            double circularity = 4*Math.PI * Imgproc.contourArea(contours.get(i)) / Imgproc.arcLength(contour2f, true)
                    * Imgproc.arcLength(contour2f, true);

            System.out.println(" Контур: " + i + " Площа: " + Imgproc.contourArea(contours.get(i)) + " Периметр: "
                    + Imgproc.arcLength(contour2f, true) + " X: " + rect.x + " Y: " + rect.y
                    + " height: " + rect.height + " width: " + rect.width + " Окружність: " + circularity);

            //objectsDs.add(new ObjectsD(i,Imgproc.contourArea(contours.get(i)), Imgproc.arcLength(contour2f, true) ));
            //Scalar color = Scalar( rng.uniform(0, 255), rng.uniform(0,255), rng.uniform(0,255) );
            Imgproc.drawContours(drawing, contours, i, new Scalar(255, 0, 0), 4, 1, hierarchy, 0, new Point());
            Core.circle(drawing, mc.get(i), 4, new Scalar(0, 0, 255), -1, 2, 0);

            ResultSet rs = null;
            Connection c = DB.getConn();
            Statement stmt = (Statement) c.createStatement();

            String query = "INSERT INTO nuclei_params (image_id, contour_num, contour_area, contour_perimetr," +
                    " contour_height,contour_width, contour_circularity  ) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement preparedStmt = null;

            preparedStmt = (PreparedStatement) c.prepareStatement(query);

            double contourArea = Imgproc.contourArea(contours.get(i));
            double perimetr = Imgproc.arcLength(contour2f, true);
            double i_height = rect.height;
            double i_width = rect.y;

            preparedStmt.setInt  (1, ResearchParam.getImg_id());
            preparedStmt.setInt  (2, i);
            preparedStmt.setDouble(3,contourArea);
            preparedStmt.setDouble(4, perimetr);
            preparedStmt.setDouble(5, i_height);
            preparedStmt.setDouble(6, i_width);
            preparedStmt.setDouble(7, circularity);
            preparedStmt.executeUpdate();

            nucleiData.add(new Nuclei(i,contourArea,perimetr, i_height,i_width,circularity));

        }
        nucleiTable.setItems(getNucleiData());
        this.setOriginalImage(drawing);
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
        //System.out.println(selectedFilter.getId());
    }

    public void showNucleiClasses()throws java.sql.SQLException, ClassNotFoundException{

        ResultSet rs = null;
        //Connection c = DB.connect("127.0.0.1","3306","ams","root","oleh123");

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
            //String surname = rs.getString(3);
            //System.out.printf("value: %d, roiGroupMeasurementNameId_KEY: %s %n", id, name);

            comboBoxData.add(new FilterColection(Integer.toString(id), name));
        }

        //researchNameLabel.setVisible(true);
        comboBox.setVisible(true);

    }

    @FXML
    public void setResearchName() throws IOException, SQLException, ClassNotFoundException {

        this.researchname = researchNameField.getText();
        this.insertResearchNameToDb(this.researchname);
        loadImageButton.setVisible(true);
    }

    @FXML
    public void imageName(String imgN) throws SQLException {

        System.out.print(imgN);
        System.out.println(ResearchParam.getResearch_id());
        ResearchParam.setImg_name(imgN);

        ResultSet rs = null;
        Connection c = DB.getConn();

        Statement stmt = (Statement) c.createStatement();

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

        ResultSet rs = null;
        Connection c = DB.getConn();
        Statement stmt = (Statement) c.createStatement();

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
        //this.originalImage.setPreserveRatio(true);
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


        // called main function for image processing

        if (errorMessage.length() != 0) {
            // Show the error message.
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Заповніть коректно поля");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            //return false;
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


/*
    @FXML
    public void detectByColor(){
        Mat src = this.optimizeImageDim(this.image);
        Mat mHsvMat = new Mat();
        Mat mMaskMat = new Mat();
        Mat mDilatedMat = new Mat();
        Mat mRgbMat = new Mat();
        Imgproc.cvtColor(src, mHsvMat, Imgproc.COLOR_RGB2HSV, 8);
        Scalar lowerThreshold = new Scalar ( 0, 0, 0 ); // Blue color – lower hsv values
        Scalar upperThreshold = new Scalar ( 10, 10, 10 ); // Blue color – higher hsv values
        Core.inRange(mHsvMat, lowerThreshold, upperThreshold, mMaskMat);
        Imgproc.dilate(mMaskMat, mDilatedMat, new Mat());
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mDilatedMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Rect rect ;
        for ( int contourIdx=0; contourIdx < contours.size(); contourIdx++ )
        {
            //if(contours.get(contourIdx).size()>100)  // Minimum size allowed for consideration
            // {
            rect = Imgproc.boundingRect(contours.get(contourIdx));
            //System.out.println(rect.x);
            String areValueTemp = Double.toString (rect.height * rect.width);
            //rdbcData.add(new RegionDetectByColor(areValueTemp));
            Imgproc.drawContours ( src, contours, contourIdx, new Scalar(0,0,255), 2);
            MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(contourIdx).toArray() );
            System.out.println(" Контур: " + contourIdx + " Площа: " + Imgproc.contourArea(contours.get(contourIdx))
                    + " Периметр: " + Imgproc.arcLength(contour2f, true) +  " X: " + rect.x + " Y: " + rect.y
                    + " height: " + rect.height + " width: " + rect.width);
            //this.saveXMLfile.setDisable(false);
            //this.saveObjParamValueXMLfile.setDisable(true);
            //Core.rectangle(src, new Point(rect.x, rect.height), new Point(rect.y, rect.width), new Scalar(0, 0, 255));
        }
        this.changedimage = src;
        this.setOriginalImage(src);
    }*/



/*
    public static void averageColor(File file)throws IOException {
        BufferedImage bi = ImageIO.read(file);
        for (int i = 0; i < 256; i++) {}
        int x0 =0;
        int y0 = 0;
        int w = bi.getWidth();
        int h = bi.getHeight();
        int x1 = x0 + w;
        int y1 = y0 + h;
        long sumr = 0, sumg = 0, sumb = 0;
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                Color pixel = new Color(bi.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
            }
        }
        int num = w * h;
        System.out.println(sumr/ num);
        System.out.println(sumg/ num);
        System.out.println(sumb/ num);
        double y = (299 * sumr + 587 * sumg + 114 * sumb) / 1000;
        System.out.println("Bright = " + y);
        //return new Color(sumr / num, sumg / num, sumb / num);
    }
*/