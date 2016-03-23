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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ThreadFactory;

import sample.model.Filters.FiltersOperations;
import sample.model.HistogramEQ;
import sample.model.PreProcessing.PreProcessingOperation;
import sample.model.PreProcessing.StartImageParams;
import sample.model.Segmentation.SegmentationColection;
import sample.model.Segmentation.SegmentationOperations;
import sample.tools.ImageOperations;
import sample.tools.ValidateOperations;
import sample.util.Estimate;
import sample.util.PreProcessingParam;

import static sample.controller.DbConnectDialogController.*;

public class StartController {

    @FXML
    private Button researchNameButton;
    @FXML
    private Button researchPathButton;
    @FXML
    private Button loadImageButton;
    @FXML
    private Button nextImSettingButton;

    @FXML
    private Button setPreProcSettingsButton;
    @FXML
    private Button saveChangeButton;
    @FXML
    private Button correctionButton;

    @FXML
    private Button researchNameLabel;

    @FXML
    private ComboBox<FilterColection> comboBox;
    @FXML
    private TextField researchNameField;

    @FXML
    private TextField researchPathField;


    @FXML
    private Label researchName;
    //@FXML
    //private Label researchPathLabel;

    @FXML
    protected ImageView preProcImage;
    @FXML
    protected ImageView segmentationImage;
    @FXML
    protected ImageView originalImage;

    private ObservableList<FilterColection> comboBoxData = FXCollections.observableArrayList();


    @FXML
    private TableView<Nuclei> nucleiTable;
    @FXML
    private TableColumn<Nuclei, String > contourNumColumn;
    @FXML
    private TableColumn<Nuclei, String> contourAreaColumn;

    private boolean okClicked = false;

    private String filterType;
    private String segType;

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
    private String researchPath;

    private String originalImagePath;
    private String generatedImagePath;

    private float meanSquaredError;
    private double psnr;

    @FXML
    private Label mseResLabel;
    @FXML
    private Label psnrResLabel;

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



        contourNumColumn.setCellValueFactory(cellData -> cellData.getValue().contourNumProperty());
        contourAreaColumn.setCellValueFactory(cellData -> cellData.getValue().contourAreaProperty());

        comboBox.setItems(comboBoxData);

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
        System.out.println(selectedFilter.getId());
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
            System.out.printf("value: %d, roiGroupMeasurementNameId_KEY: %s %n", id, name);

            comboBoxData.add(new FilterColection(Integer.toString(id), name));
        }

        //researchNameLabel.setVisible(true);
        comboBox.setVisible(true);

    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void setResearchName() throws IOException, SQLException, ClassNotFoundException {

        this.researchname = researchNameField.getText();

        this.insertResearchNameToDb(this.researchname);


        // ResearchParam.setResearch_id(Integer.parseInt(selectedFilter.getId()));



        //researchPathLabel.setVisible(true);
        //researchPathField.setVisible(true);
        //researchPathButton.setVisible(true);
        //this.setResearchPath(this.researchname);

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
    public void setResearchPath(String rsname){

        File file = new File(rsname);
        String path = file.getAbsolutePath();

        researchPathField.setText(path);
        this.researchPath = path;

    }

    @FXML
    public void setFullPathName(){

        File dir = new File(researchPathField.getText());

        if (!dir.exists()) {
            try {
                dir.mkdirs();

            } catch (SecurityException secEx) {

                // Show the error message.
                Alert alert = new Alert(AlertType.ERROR);
                alert.initOwner(dialogStage);
                alert.setTitle("Помилка");
                alert.setHeaderText("Виникла помилка");
                alert.setContentText("Немає прав доступу");

                alert.showAndWait();
                return;

            }
        }
        loadImageButton.setVisible(true);
    }


    public void showHisImage(String orImage){
        Mat image = Highgui.imread(orImage);

        Mat src = new Mat(image.height(), image.width(), CvType.CV_8UC2);


        Imgproc.cvtColor(image, src, Imgproc.COLOR_RGB2GRAY);



        Vector<Mat> bgr_planes = new Vector<>();
        Core.split(src, bgr_planes);

        MatOfInt histSize = new MatOfInt(256);


        final MatOfFloat histRange = new MatOfFloat(0f, 256f);

        boolean accumulate = false;

        Mat b_hist = new  Mat();

        Imgproc.calcHist(bgr_planes, new MatOfInt(0),new Mat(), b_hist, histSize, histRange, accumulate);

        int hist_w = 512;
        int hist_h = 600;
        long bin_w;
        bin_w = Math.round((double) (hist_w / 256));

        Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC1);

        Core.normalize(b_hist, b_hist, 3, histImage.rows(), Core.NORM_MINMAX);



        for (int i = 1; i < 256; i++) {


            Core.line(histImage, new Point(bin_w * (i - 1),hist_h- Math.round(b_hist.get( i-1,0)[0])),
                    new Point(bin_w * (i), hist_h-Math.round(Math.round(b_hist.get(i, 0)[0]))),
                    new  Scalar(255, 0, 0), 2, 8, 0);

        }

        //this.setSegmentationImage(histImage);

        //ImageViwer.viewImage(histImage);
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

    @FXML
    public void saveDataToDb() throws java.sql.SQLException, ClassNotFoundException {

        //Connection con = getConn();
        //Statement stmt;
        ResultSet rs = null;
        //Connection c = DB.connect("127.0.0.1","3306","ams","root","oleh123");

        Connection c = DB.getConn();
        Statement stmt = (Statement) c.createStatement();

        String query = "select value, roiGroupMeasurementNameId_KEY from roigrouphbm_roimeasurementhbms";
        try {
            rs = (ResultSet) stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (rs.next()) {
            double value = rs.getDouble(1);
            int roiGroupMeasurementNameId_KEY = rs.getInt(2);
            //String surname = rs.getString(3);
            System.out.printf("value: %f, roiGroupMeasurementNameId_KEY: %d %n", value, roiGroupMeasurementNameId_KEY);
        }
        c.close();
        //this.image= this.changedimage ;
        //sample.model.Image.setImageMat(this.image);

    }


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
    }


    @FXML
    public void SimpleDetect() throws SQLException {

        Mat src = this.image;
        Mat src_gray = new Mat();
        int thresh = 100;
        int max_thresh = 255;

        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(src_gray, src_gray, new Size(3, 3));

        Mat canny_output = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Mat mHsvMat = new Mat();
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

            preparedStmt.setInt  (1, ResearchParam.getImg_id());
            preparedStmt.setInt  (2, i);
            preparedStmt.setDouble(3,Imgproc.contourArea(contours.get(i)));
            preparedStmt.setDouble(4, Imgproc.arcLength(contour2f, true));
            preparedStmt.setDouble(5, rect.height);
            preparedStmt.setDouble(6, rect.y);
            preparedStmt.setDouble(7, circularity);



            preparedStmt.executeUpdate();

            //mainApp.showNucleiParamOverview("2", "Oleh");
            nucleiData.add(new Nuclei("2","olko",12,13,14,15));

        }
        nucleiTable.setItems(getNucleiData());

        //this.saveObjParamValueXMLfile.setDisable(false);
        //this.saveXMLfile.setDisable(true);
        this.setOriginalImage(drawing);

        //this.changedimage = drawing;
        // this.setCurrentImage(drawing);

    }

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

    /**
     * next 3 functions used to show image: current, after preprocessing, after segmentation
     */
    @FXML
    private void showCurrentImg(){
        //this.setPreProcImage(sample.model.Image.getImageMat());
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

    protected Mat optimizeImageDim(Mat image) {
        // init
        Mat padded = new Mat();
        // get the optimal rows size for dft
        int addPixelRows = Core.getOptimalDFTSize(image.rows());
        // get the optimal cols size for dft
        int addPixelCols = Core.getOptimalDFTSize(image.cols());
        // apply the optimal cols and rows size to the image
        Imgproc.copyMakeBorder(image, padded, 0, addPixelRows - image.rows(), 0, addPixelCols - image.cols(),
                Imgproc.BORDER_CONSTANT, Scalar.all(0));

        return padded;
    }

    @FXML
    public void autoSetting(){
        this.autoPreProcFiltersSegmentationSetting();
    }

    @FXML
    public void autoPreProcFiltersSegmentationSetting(){

        Mat dst = new Mat();
        Mat testDst = new Mat();

        this.image.copyTo(dst);
        this.image.copyTo(testDst);

        /** use testing parametrs for getting HistAverValue **/
        PreProcessingOperation properation = new PreProcessingOperation(testDst,"1","15", "1", "1");

        SegmentationOperations segoperation = new SegmentationOperations(properation.getOutputImage(), "3",
                "0", "0");
        testDst.release();//clear memory
        properation.getOutputImage().release();


        float tempBrightValue = Estimate.getBrightVal();

        /** for very blue **/

        if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 83 && Estimate.getRedAverage() > 140){
            System.out.println ("39");
            this.setImageParam(dst, "1","20","1","2");
        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 110 && Estimate.getRedAverage() > 140){
            System.out.println ("38");
            this.setImageParam(dst, "1","25","1","2");
        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 110 && Estimate.getRedAverage() > 135){
            System.out.println ("37");
            this.setImageParam(dst, "1","25","1","2");
        }





        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 110 && Estimate.getRedAverage() > 115){
            System.out.println ("41");
            this.setImageParam(dst, "1","20","1","2");
        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 120 && Estimate.getRedAverage() > 130){
            System.out.println ("43");
            this.setImageParam(dst, "1","25","2","2");
        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 120 && Estimate.getRedAverage() > 115){
            System.out.println ("42");
            this.setImageParam(dst, "1","20","2","4");
        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 130 && Estimate.getRedAverage() > 90){
            System.out.println ("35");
            this.setImageParam(dst, "1","23","2","2");
        }

        else if(tempBrightValue < 0.9 && Estimate.getBlueAverage() > 130 && Estimate.getRedAverage() < 100
                ){
            System.out.println ("36");

            this.setImageParam(dst, "1","10","2","2");
        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() > 130 && Estimate.getBlueAverage() < 185 && Estimate.getRedAverage() < 90){
            System.out.println ("29");

            this.setImageParam(dst, "1","20","1","6");
        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() > 130 && Estimate.getBlueAverage() < 185 && Estimate.getRedAverage() < 100){
            System.out.println ("1");

            this.setImageParam(dst, "1","17","2","2");
        }
        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() > 130 && Estimate.getBlueAverage() < 200 && Estimate.getRedAverage() < 100){
            System.out.println ("6");

            this.setImageParam(dst, "1","15","10","10");
        }

        else if(tempBrightValue > 1.5 && tempBrightValue < 2 && Estimate.getBlueAverage() > 130 && Estimate.getRedAverage() > 220){
            System.out.println ("13");

            this.setImageParam(dst, "1","16","1","2");

        }

        else if(tempBrightValue > 1.5 && tempBrightValue < 2 && Estimate.getBlueAverage() > 130 && Estimate.getRedAverage() > 100){
            System.out.println ("23");

            this.setImageParam(dst, "1","13","1","5");

        }

        else if(tempBrightValue > 1.1 && tempBrightValue < 2 && Estimate.getBlueAverage() > 130 && Estimate.getRedAverage() > 160){
            System.out.println ("16");

            this.setImageParam(dst, "1","25","20","1");

        }

        else if(tempBrightValue > 1.1 && tempBrightValue < 2 && Estimate.getBlueAverage() > 130 && Estimate.getRedAverage() > 140){
            System.out.println ("21");

            this.setImageParam(dst, "1","19","15","1");

        }

        else if(tempBrightValue > 1.1 && tempBrightValue < 2 && Estimate.getBlueAverage() > 130 && Estimate.getRedAverage() > 100){
            System.out.println ("11");

            this.setImageParam(dst, "1","19","1","1");

        }

        else if(tempBrightValue > 1 && tempBrightValue < 2 && Estimate.getBlueAverage() > 130 && Estimate.getRedAverage() > 100){
            System.out.println ("31");

            this.setImageParam(dst, "1","22","1","1");

        }

        else if(tempBrightValue > 1 && tempBrightValue < 2 && Estimate.getBlueAverage() > 130 && Estimate.getRedAverage() > 130){
            System.out.println ("30");

            this.setImageParam(dst, "1","10","1","3");

        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() > 120 && Estimate.getRedAverage() > 100
                && Estimate.getSecondHistAverageValue() > 140){
            System.out.println ("32");

            this.setImageParam(dst, "1","22","1","3");

        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() > 130 && Estimate.getRedAverage() > 100){
            System.out.println ("2");

            this.setImageParam(dst, "1","15","1","3");

        }


        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 130
                && Estimate.getFirstHistAverageValue() >100){
            System.out.println ("20");

            this.setImageParam(dst, "1","20","1","1");

        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 90 && Estimate.getRedAverage() > 130
                && Estimate.getSecondHistAverageValue() >110){
            System.out.println ("17");

            this.setImageParam(dst, "1","11","1","9");

        }


        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 100 && Estimate.getRedAverage() > 130
                && Estimate.getSecondHistAverageValue() >45){
            System.out.println ("27");

            this.setImageParam(dst, "1","18","9","1");
        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 130 && Estimate.getRedAverage() > 130
                && Estimate.getSecondHistAverageValue() >165){
            System.out.println ("22");

            this.setImageParam(dst, "1","32","17","1");

        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 130 && Estimate.getRedAverage() > 130
                && Estimate.getSecondHistAverageValue() >110){
            System.out.println ("15");

            this.setImageParam(dst, "1","33","8","1");

        }


        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 130
                && Estimate.getFirstHistAverageValue() >55){
            System.out.println ("29");

            this.setImageParam(dst, "1","17","6","1");

        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 130
                && Estimate.getFirstHistAverageValue() >55){
            System.out.println ("28");

            this.setImageParam(dst, "1","18","6","1");

        }

        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 130
                && Estimate.getFirstHistAverageValue() >20){
            System.out.println ("18");

            this.setImageParam(dst, "1","18","1","1");

        }


        else if(tempBrightValue > 0.9 && tempBrightValue < 2 && Estimate.getBlueAverage() < 130
                && Estimate.getSecondHistAverageValue() >20){
            System.out.println ("3");

            this.setImageParam(dst, "1","21","3","1");

        }


        else if(tempBrightValue <= 0.9 && Estimate.getFirstHistAverageValue() < 100 && Estimate.getRedAverage() < 80) {
            System.out.println ("8");
            this.setImageParam(dst, "1","9","25","11");

        }

        else if(tempBrightValue <= 0.1 && Estimate.getFirstHistAverageValue() < 40 && Estimate.getRedAverage() >= 200 && Estimate.getBlueAverage() >= 100) {
            System.out.println ("45");
            this.setImageParam(dst, "1","5","1","1");//6-br

        }

        else if(tempBrightValue <= 0.1 && Estimate.getFirstHistAverageValue() < 100 && Estimate.getBlueAverage() >= 210 && Estimate.getRedAverage() >= 210) {
            System.out.println ("46");

            //thresholdSegmentation(dst);
            this.setImageParam(dst, "1","15","1","1");//6-br

        }

        else if(tempBrightValue <= 0.9 && Estimate.getFirstHistAverageValue() < 100 && Estimate.getRedAverage() >= 110) {
            System.out.println ("25");
            this.setImageParam(dst, "1","5","1","3");//6-br

        }

        else if(tempBrightValue <= 0.9 && Estimate.getFirstHistAverageValue() < 100 && Estimate.getRedAverage() >= 80) {
            System.out.println ("9");
            this.setImageParam(dst, "1","8","23","1");

        }

        else if(tempBrightValue <= 0.9 && Estimate.getFirstHistAverageValue()>100 && Estimate.getRedAverage() < 80) {
            System.out.println ("4");
            this.setImageParam(dst, "1","9","25","11");

        }

        else if(tempBrightValue <= 0.9 && tempBrightValue >= 0.5 && Estimate.getFirstHistAverageValue()>100 && Estimate.getRedAverage() > 100) {
            System.out.println ("19");
            this.setImageParam(dst, "1","15","14","11");

        }


        else if(tempBrightValue <= 0.5 && Estimate.getRedAverage() > 170 && Estimate.getRedAverage()<190 && Estimate.getBlueAverage()>205
                && Estimate.getBlueAverage()<225) {
            System.out.println ("40");
            //thresholdSegmentation(dst);
            this.setImageParam(dst, "1","20","1","1");

        }

        else if(tempBrightValue <= 0.5 && Estimate.getRedAverage() > 140 && Estimate.getBlueAverage()>200
                && Estimate.getFirstHistAverageValue() > 120) {
            System.out.println ("41");
            thresholdSegmentation(dst);
            //this.setImageParam(dst, "1","20","1","1");

        }

        else if(tempBrightValue <= 0.5 && Estimate.getFirstHistAverageValue()>130 && Estimate.getRedAverage() > 170 && Estimate.getBlueAverage()>170) {
            System.out.println ("24");
            thresholdSegmentation(dst);
            //this.setImageParam(dst, "1","1","1","1");

        }


        else if(tempBrightValue <= 0.5 && Estimate.getRedAverage() > 190 && Estimate.getBlueAverage()>100) {
            System.out.println ("43");
            //thresholdSegmentation(dst);
            this.setImageParam(dst, "1","15","1","1");

        }

        else if(tempBrightValue > 0.8 && tempBrightValue < 2 && Estimate.getBlueAverage() > 100 && Estimate.getRedAverage() > 100){
            System.out.println ("33");

            this.setImageParam(dst, "1","20","5","1");

        }



        else {
            this.setImageParam(dst, "1","15","1","1");
            System.out.println ("else");
        }

    }


    /**
     * compare 2 images:
     * original and after filtering
     */
    private void compareImages(){
        //System.out.println(originalImagePath);
        //System.out.println(generatedImagePath);
        // this.meanSquaredError = Psnr.getmeanSquaredError(originalImagePath, generatedImagePath);
        // this.psnr = Psnr.getPsnr(this.meanSquaredError);

        mseResLabel.setText(String.valueOf(this.meanSquaredError));
        psnrResLabel.setText(String.valueOf(this.psnr));
        //System.out.println(this.meanSquaredError );
        //System.out.println(this.psnr );

    }

    /**
     *
     * @param dst
     * @param contrast
     * @param bright
     * @param dilate
     * @param erode
     */
    public void setImageParam(Mat dst, String contrast, String bright, String dilate, String erode){

        FiltersOperations filtroperation = new FiltersOperations(dst, "4", "9", "", "", "");



        // save image on disk
        String path ="";
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        Highgui.imwrite( path + timeStamp + ".png", filtroperation.getOutputImage());
        generatedImagePath = path + timeStamp + ".png";
        this.compareImages();// call to compare function
        // delete temp filtered image
        ImageOperations.deleteFile(path + timeStamp + ".png");




        PreProcessingOperation properation = new PreProcessingOperation(filtroperation.getOutputImage(),contrast,bright,
                dilate, erode);

        filtroperation.getOutputImage().release();

        //this.setPreProcImage(properation.getOutputImage());


        SegmentationOperations segoperation = new SegmentationOperations(properation.getOutputImage(), "3",
                "0", "0");

        properation.getOutputImage().release();
        //this.setSegmentationImage(segoperation.getOutputImage());

        SegmentationOperations segoperation_1 = new SegmentationOperations(segoperation.getOutputImage(), "1",
                "200", "255");


        //this.setSegmentationImage(segoperation_1.getOutputImage());

        segoperation_1.getOutputImage().release();

        Estimate.setFirstHistAverageValue(null);
        Estimate.setSecondHistAverageValue(null);
        System.out.println("------------------------------------------------------------------------------------------");
    }



    private void thresholdSegmentation(Mat dst){

/*
        FiltersOperations filtroperation = new FiltersOperations(dst, "4", "9", "", "", "");
        PreProcessingOperation properation = new PreProcessingOperation(filtroperation.getOutputImage(),"1.1","10",
                "1", "1");
        filtroperation.getOutputImage().release();*/

        SegmentationOperations segoperation_1 = new SegmentationOperations(dst, "1",
                "0", "10");

        // properation.getOutputImage().release();

        // this.setSegmentationImage(segoperation_1.getOutputImage());

        segoperation_1.getOutputImage().release();
    }



































    public boolean isOkClicked() {
        return okClicked;
    }





    @FXML
    private void handleDBConnect() {

        boolean okClicked = mainApp.showDbConnectDialog();
        if (okClicked) {
            mainApp.startProcessing();
            //showPersonDetails(selectedPerson);
        }
    }

    @FXML
    private void saveChangeImage(){
        sample.model.Image.setImageMat(this.changedimage);
        this.image = sample.model.Image.getImageMat();
    }

    @FXML
    private void correctionSegmentation(){
        Mat frame = this.image;
        //Imgproc.dilate(frame, frame, new Mat(), new Point(-1, -1), 1);

        Mat hsvImg = new Mat();
        List<Mat> hsvPlanes = new ArrayList<>();
        Mat thresholdImg = new Mat();

        int thresh_type = Imgproc.THRESH_BINARY_INV;
        //if (this.inverse.isSelected())
        // thresh_type = Imgproc.THRESH_BINARY;

        // threshold the image with the average hue value
        hsvImg.create(frame.size(), CvType.CV_8U);
        Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2HSV);
        Core.split(hsvImg, hsvPlanes);

        // get the average hue value of the image
        double threshValue = PreProcessingOperation.getHistAverage(hsvImg, hsvPlanes.get(0));
        System.out.print(threshValue);
        //Imgproc.threshold(hsvPlanes.get(0), thresholdImg, 0.1, 255 , thresh_type);
        Imgproc.threshold(thresholdImg, thresholdImg, 1, 179, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 1);

        //Imgproc.blur(thresholdImg, thresholdImg, new Size(9, 9));



        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 3);
        //Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 3);

        Size s = new Size(31, 31);
        Imgproc.GaussianBlur(thresholdImg, thresholdImg, s, 2.0);

        //Imgproc.threshold(thresholdImg, thresholdImg, 0.1, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(thresholdImg, thresholdImg, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        // create the new image
        Mat foreground = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        frame.copyTo(foreground, thresholdImg);
        Imgproc.medianBlur(foreground, foreground, 9);

        //this.setSegmentationImage(foreground);// show image after segmentation
        //this.changedimage = foreground;
    }
/*
    @FXML
    public void saveChangeFile()throws
            ClassNotFoundException,SQLException {
        Connection con;
        Statement stmt;
        ResultSet rs;
        Connection c = DB.connect("127.0.0.1","3306","ki","root","oleh123");
        stmt = c.createStatement();
        String query = "select id, name, surname from users";
        rs = stmt.executeQuery(query);
        while (rs.next()) {
            int id = rs.getInt(1);
            String name = rs.getString(2);
            String surname = rs.getString(3);
            System.out.printf("id: %d, name: %s, surname: %s %n", id, name, surname);
        }
        c.close();
        /*this.image= this.changedimage ;
        sample.model.Image.setImageMat(this.image);
*/
    /*}*/

    /*
    private void rangeValues(String field, String fieldType){
        String[] rangeValue = field.split("-");
        if(rangeValue.length == 3){
            this.rangeFlag = fieldType;
            this.firstValue = Integer.parseInt(rangeValue[0]);
            this.lastValue = Integer.parseInt(rangeValue[1]);
            this.step = Integer.parseInt(rangeValue[2]);
        }
        else {
            if(fieldType.compareTo("Contrast") == 0) {
                this.contrast = rangeValue[0];
            }
        }
    }*/

}