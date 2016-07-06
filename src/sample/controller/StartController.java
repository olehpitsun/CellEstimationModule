package sample.controller;

import com.mysql.jdbc.PreparedStatement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import sample.Main;
import sample.model.DataBase;
import sample.model.Filters.FilterColection;
import sample.model.Nuclei;
import sample.model.ResearchParam;
import sample.tools.ImageOperations;
import sample.core.DB;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import sample.util.PreProcessingParam;
import javax.sql.DataSource;
import java.sql.Statement;
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

    @FXML
    public void folder(){

        String f = "C:\\IMAGES\\mask_nuclei_(16042015_gisto_neproliferatyvna_mastopotia)";

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(f))) {
            for (Path file: stream) {
                if(!file.toFile().isDirectory() ) {
                    System.out.println(file.getFileName());
                    calculate(f, file.getFileName().toString());
                }
            }
        } catch (IOException | DirectoryIteratorException x) {
            System.err.println(x);
        }


    }

    public void calculate(String path, String filename){

        String fullpath = path + "//" + filename;
        this.image = Highgui.imread( fullpath,  Highgui.CV_LOAD_IMAGE_COLOR);
        sample.model.Image.setImageMat(this.image);
        originalImagePath = filename;
        System.out.println("img " + originalImagePath);
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



    public void chooseFile(ActionEvent actionEvent) throws IOException {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files","*.bmp", "*.png", "*.jpg", "*.gif"));
        File file = chooser.showOpenDialog(new Stage());
        if(file != null) {

            System.out.println(file.getAbsolutePath());
            this.image = Highgui.imread(file.getAbsolutePath(), Highgui.CV_LOAD_IMAGE_COLOR);
            sample.model.Image.setImageMat(this.image);
            originalImagePath = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("\\")+1);
            System.out.println("1 " + originalImagePath);
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
        Mat src = this.image;
        Mat src_gray = new Mat();
        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(src_gray, src_gray, new Size(3, 3));

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Mat mMaskMat = new Mat();

        Scalar lowerThreshold = new Scalar ( 0, 0, 0 );
        Scalar upperThreshold = new Scalar ( 10, 10, 10 );
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
            Core.putText(drawing, Integer.toString(i) , new Point(rect.x-20,rect.y),
                    Core.FONT_HERSHEY_TRIPLEX, 1.7 ,new  Scalar(255,255,255));
            /*** Занесення даних до бази даних*/
            double contourArea, perimetr, i_height, i_width, circular, equiDiameter;
            MatOfPoint2f mMOP2f1;

            contourArea = Imgproc.contourArea(contours.get(i));
            perimetr = Imgproc.arcLength(contour2f, true);
            i_height = rect.height;
            i_width = rect.y;
            circular = 4 * Math.PI * Imgproc.contourArea(contours.get(i)) / Imgproc.arcLength(contour2f, true)
                    * Imgproc.arcLength(contour2f, true);



            circular = Math.round(circular * 100.0) / 100.0;
            /**
             * блок підрахунку xc, yc, major_axis, minor_axis, theta
             * якщо площа більше 2. то все йде норм, інакше 0 , щоб не викидало помилок
             */
            mMOP2f1 = new MatOfPoint2f();
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
            equiDiameter = sqrt(4 * contourArea / Math.PI);

            Connection con ;
            com.mysql.jdbc.PreparedStatement stmt = null;
            com.mysql.jdbc.ResultSet rs = null;
            try {
                con = DB.getConn();
                String query = "INSERT INTO nuclei_params (image_id, contour_num, contour_area, contour_perimetr," +
                        " contour_height,contour_width, contour_circularity, xc, yc, major_axis, minor_axis, theta," +
                        " equiDiameter  ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,? )";
                stmt = (PreparedStatement) con.prepareStatement(query);
                stmt.setInt(1, ResearchParam.getImg_id());
                stmt.setInt(2, i);
                stmt.setDouble(3, contourArea);
                stmt.setDouble(4, perimetr);
                stmt.setDouble(5, i_height);
                stmt.setDouble(6, i_width);
                stmt.setDouble(7, circular);
                stmt.setDouble(8, xc);
                stmt.setDouble(9, yc);
                stmt.setDouble(10, major_axis);
                stmt.setDouble(11, minor_axis);
                stmt.setDouble(12, theta);
                stmt.setDouble(13, equiDiameter);
                stmt.executeUpdate();
            }catch (NullPointerException e) {
                e.printStackTrace();
            }finally{
                try {
                    if(rs != null) rs.close();
                    if(stmt != null) stmt.close();
                    //if(con != null) con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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

    /**
     * обробка кнопки "Далі",
     * якщо немає зєднання з БД, то вивід помилки
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @FXML
    private void nextImSetting() throws SQLException, ClassNotFoundException {
        Connection c_test = DB.getConn();
        if(c_test != null ) {
            this.showNucleiClasses();
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

    /**
     * відображає усі класи(досліди)
     * @throws java.sql.SQLException
     * @throws ClassNotFoundException
     */
    public void showNucleiClasses()throws java.sql.SQLException, ClassNotFoundException{

        nucleiTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    showOnlyOneObject(nucleiTable.getSelectionModel().getSelectedItem().contourNumProperty().get());
                }
            }
        });

        DataBase.getConnection();
        Connection con;
        Statement stmt = null;
        com.mysql.jdbc.ResultSet rs = null;

        try {
            con = DB.getConn();
            stmt = (Statement) con.createStatement();
            rs = (com.mysql.jdbc.ResultSet) stmt.executeQuery("select id, name from research_name");
            while(rs.next()){
                int id = rs.getInt("id");
                String name = rs.getString("name");
                comboBoxData.add(new FilterColection(Integer.toString(id), name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
     catch (NullPointerException e) {
        e.printStackTrace();
    }finally{
            try {
                if(rs != null) rs.close();
                if(stmt != null) stmt.close();
                //if(con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        comboBox.setVisible(true);
    }

    /**
     * відображає лише одне ядро на зображенні
     * по номеру
     * @param objNum
     */
    @FXML
    public void showOnlyOneObject(Integer objNum){
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

            if(objNum == i){
                Imgproc.drawContours(drawing, contours, i, new Scalar(255, 0, 0), 4, 1, hierarchy, 0, new Point());
                Core.circle(drawing, mc.get(i), 4, new Scalar(0, 0, 255), -1, 2, 0);
                //////////////////////////////////////////////////////////////////////////////////////////////////////
                Core.putText(drawing, Integer.toString(i) , new Point(rect.x,rect.y),
                    Core.FONT_HERSHEY_COMPLEX, 10.0 ,new  Scalar(0,255,0));
            }else{
                Imgproc.drawContours(drawing, contours, i, new Scalar(255, 255, 255), 4, 1, hierarchy, 0, new Point());
            }
                MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
                contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);
        }
        this.setOriginalImage(drawing);

    }

    /**
     * Поле вводу назви нового класу (досліду)
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
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

    /**
     * занесення нового класу (досліду) до БД
     * @param res_name
     */
    @FXML
    public void insertResearchNameToDb(String res_name)  {

        DataBase.getConnection();
        Connection con ;
        com.mysql.jdbc.PreparedStatement stmt = null;
        com.mysql.jdbc.ResultSet rs = null;
        try {
            con = DB.getConn();
            String query = "INSERT INTO research_name (name) VALUES (?)";
            stmt = (PreparedStatement) con.prepareStatement(query);
            stmt.setString  (1, res_name);
            stmt.executeUpdate();

            com.mysql.jdbc.ResultSet rs_1 = null;
            com.mysql.jdbc.Statement stmt_1 = (com.mysql.jdbc.Statement) con.createStatement();
            String query_1 = "select MAX(id) from research_name";
            try {
                rs_1 = (com.mysql.jdbc.ResultSet) stmt_1.executeQuery(query_1);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            while (rs_1.next()) {
                int size = rs_1.getInt(1);
                ResearchParam.setResearch_id(size);
            }
            } catch (SQLException e1) {
            e1.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }finally{
            try {
                if(rs != null) rs.close();
                if(stmt != null) stmt.close();
                //if(con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void imageName(String imgN) throws SQLException {

        imgN = imgN.replace( ' ', '#' );
        System.out.println(imgN);

        ResearchParam.setImg_name(imgN);
        Connection c = DB.getConn();
        String query = "INSERT INTO images (research_id, image_name) VALUES (?,?)";
        PreparedStatement preparedStmt = null;

        preparedStmt = (PreparedStatement) c.prepareStatement(query);
        preparedStmt.setInt  (1, ResearchParam.getResearch_id());
        preparedStmt.setString  (2, imgN);
        preparedStmt.executeUpdate();
        //////////////////////////////////////////////////////////////////////////
        com.mysql.jdbc.ResultSet rs_1 = null;
        com.mysql.jdbc.Statement stmt_1 = (com.mysql.jdbc.Statement) c.createStatement();
        String query_1 = "select MAX(id) from images";
        try {
            rs_1 = (com.mysql.jdbc.ResultSet) stmt_1.executeQuery(query_1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (rs_1.next()) {
            int img_id = rs_1.getInt(1);
            ResearchParam.setImg_id(img_id);
        }
    }
    /**відображення завантаженого зображення
     */
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