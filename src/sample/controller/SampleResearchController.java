package sample.controller;

import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.opencv.core.*;
import sample.Main;
import sample.core.DB;
import sample.model.ClassesColection;
import sample.model.FileParam;
import sample.model.ImagesColection;
import sample.model.ResearchParam;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.io.*;
import java.util.Date;


public class SampleResearchController {

    private Stage stage;
    protected Mat image;
    protected Main mainApp;
    private Stage dialogStage;

    @FXML
    private ComboBox<ClassesColection> comboBoxClasses;
    @FXML
    private ComboBox<ImagesColection> comboBoxImages;
    @FXML
    private CheckBox contour_area, contour_perimetr, contour_height, contour_width, contour_circularity,
            xc, yc, major_axis, minor_axis, theta, equiDiameter ;
    @FXML
    private Button generateFileButton;
    private ObservableList<ClassesColection> comboBoxClassesData = FXCollections.observableArrayList();
    private ObservableList<ImagesColection> comboBoxImagesData = FXCollections.observableArrayList();
    public ArrayList selectedNucleiParam = new ArrayList();

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public SampleResearchController() {

        comboBoxClassesData.add(new ClassesColection("0", "Новий клас"));
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
        contour_area.setSelected(true);
        contour_perimetr.setSelected(true);
        contour_height.setSelected(true);
        contour_width.setSelected(true);
        contour_circularity.setSelected(true);
        xc.setSelected(true);
        yc.setSelected(true);
        major_axis.setSelected(true);
        minor_axis.setSelected(true);
        theta.setSelected(true);
        equiDiameter.setSelected(true);

        handleCheckBoxAction();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleClassesAction() throws SQLException {

        comboBoxClasses.setVisible(true);
        ResultSet rs = null;
        Connection c = DB.getConn();

        Statement stmt = (Statement) c.createStatement();

        String table = "research_name";
        String par ="1";
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

    public void handleImagesAction() throws SQLException {

        generateFileButton.setVisible(true);
        comboBoxImages.setVisible(true);
        comboBoxImagesData.clear();// очистка comboBoxImagesData , щоб не впливало на наступні досліди

        ClassesColection selectedClass = comboBoxClasses.getSelectionModel().getSelectedItem();

        ResearchParam.setResearch_name(selectedClass.getId());

        ResultSet rs = null;
        Connection c = DB.getConn();

        System.out.println("                                              d "+selectedClass.getId() );
        Statement stmt = (Statement) c.createStatement();

        String table = "images";
        String par ="1";
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

            System.out.println(id +  classId + name);
        }
        comboBoxImages.setItems(comboBoxImagesData);
    }


    /**
     * занесення вибраних параметрів для ядер
     */
    @FXML
    private void handleCheckBoxAction() {

        selectedNucleiParam.clear();

        if(contour_area.isSelected()){
            selectedNucleiParam.add(contour_area.getId());
        }
        if(contour_perimetr.isSelected()){
            selectedNucleiParam.add(contour_perimetr.getId());
        }
        if(contour_height.isSelected()){
            selectedNucleiParam.add(contour_height.getId());
        }
        if(contour_width.isSelected()){
            selectedNucleiParam.add(contour_width.getId());
        }
        if(contour_circularity.isSelected()){
            selectedNucleiParam.add(contour_circularity.getId());
        }
        if(xc.isSelected()){
            selectedNucleiParam.add(xc.getId());
        }
        if(yc.isSelected()){
            selectedNucleiParam.add(yc.getId());
        }
        if(major_axis.isSelected()){
            selectedNucleiParam.add(major_axis.getId());
        }
        if(minor_axis.isSelected()){
            selectedNucleiParam.add(minor_axis.getId());
        }
        if(theta.isSelected()){
            selectedNucleiParam.add(theta.getId());
        }
        if(equiDiameter.isSelected()){
            selectedNucleiParam.add(equiDiameter.getId());
        }
        System.out.println("////////////////////////////////////////////");
        for(int i =0;i<selectedNucleiParam.size();i++){
            System.out.println(selectedNucleiParam.get(i));
        }
    }

    public void generateFile() throws IOException, SQLException {

        /**
         * якщо немає підключення до БД
         * та не вибрано класу
         * генерація файлу не відбуватиметься
         */

        if(selectedNucleiParam.size() == 0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Помилка");
            alert.setHeaderText("Виникла помилка");
            alert.setContentText("Виберіть параметри оцінки ядра");

            alert.showAndWait();
        }

        ResultSet rs = null;
        Connection c = DB.getConn();
        if(c==null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Помилка");
            alert.setHeaderText("Виникла помилка");
            alert.setContentText("Підключіться до БД");

            alert.showAndWait();
        }
        Statement stmt = (Statement) c.createStatement();
        String table = "research_name";
        String id = ResearchParam.getResearch_name();
        if(id == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Помилка");
            alert.setHeaderText("Виникла помилка");
            alert.setContentText("Виберіть клас досліду");
            alert.showAndWait();
        }
        String query = "select name from " + table + "  where id = " + id;
        try {
            rs = (ResultSet) stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (rs.next()) {
            String filename = rs.getString(1);
            FileParam.setFilename(filename);
            System.out.println("Oleh " + filename);
        }

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        File fout = new File(FileParam.getFilename() + "_" + timeStamp + ".txt");// назва файлу
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        /** встановлення назви відношення */
        bw.write("@relation shuttle " + FileParam.getFilename());
        bw.newLine();

        for (int i = 0; i < selectedNucleiParam.size(); i++) {
            bw.write("@attribute " + selectedNucleiParam.get(i) + " numeric");
            bw.newLine();
        }

        bw.write("@data");
        bw.newLine();

        for(int i=0; i<comboBoxImagesData.size(); i++ ){
            /**
             * вибірка параметрів ядер для кожного зображення із comboBoxImagesData
             */
        System.out.println(comboBoxImagesData.get(i).getId());
            showNucleiParam(bw, comboBoxImagesData.get(i).getId() );
        }
        bw.close();
    }

    /**
     * вибірка параметрів для кожного ядра
     * кожного зображення
     * запис в ФАЙЛ
     * @param bw
     * @throws SQLException
     * @throws IOException
     */
    public void showNucleiParam(BufferedWriter bw, String image_id) throws SQLException, IOException {

        String ncp = nucleiParamToString();/** виклик функції для формування полів для запиту**/

        ResultSet rs = null;
        Connection c = DB.getConn();
        Statement stmt = (Statement) c.createStatement();
        String table = "nuclei_params";
        String query = "select image_id, contour_num, " + ncp + " from " + table + "  where image_id = " + image_id;
        try {
            rs = (ResultSet) stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (rs.next()) {
            Integer img_id = rs.getInt(1);
            Integer contour_num = rs.getInt(2);
            /*
            Double contour_area = rs.getDouble(3);
            Double contour_perimetr = rs.getDouble(4);
            Double contour_height = rs.getDouble(5);
            Double contour_width = rs.getDouble(6);
            Double contour_circularity = rs.getDouble(7);
            Double xc = rs.getDouble(8);
            Double yc = rs.getDouble(9);
            Double major_axis = rs.getDouble(10);
            Double minor_axis = rs.getDouble(11);
            Double theta = rs.getDouble(12);
            Double equiDiameter = rs.getDouble(13);*/

            String image_name = getImageName(img_id);
            bw.write(image_name + " " + contour_num + " " );

            for(int i =3; i <= selectedNucleiParam.size()+2; i++){
                Double tempvalue = rs.getDouble(i);
                bw.write(tempvalue + " ");
            }
            bw.newLine();

           /* bw.write(image_name + " " + contour_num + " " + contour_area + " " + contour_perimetr + " " + contour_height + " " + contour_width + " " + contour_circularity +
                    xc + " " + yc + " " + major_axis + " " + minor_axis + " " + theta + " " + equiDiameter);*/
            bw.newLine();
        }
    }

    /**
     * функція генерує SQl стрічку запиту полів
     * @return
     */
    public String nucleiParamToString(){
        String str = "";
        for(int i = 0; i< selectedNucleiParam.size(); i++){
            if(i < selectedNucleiParam.size() - 1){
                str+=selectedNucleiParam.get(i) + ", ";
            }else{
                str+=selectedNucleiParam.get(i);
            }
        }
        System.out.println(str);
        return str;
    }

    /**
     * функція повертає назву зображення по його id
     * @param img_id
     * @return
     * @throws SQLException
     */
    public String getImageName(Integer img_id) throws SQLException {

        String img_name ="";
        ResultSet rs = null;
        Connection c = DB.getConn();
        Statement stmt = (Statement) c.createStatement();
        String query = "select image_name from images where id = " + img_id;
        try {
            rs = (ResultSet) stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (rs.next()) {
            img_name = rs.getString(1);
        }
        return img_name;
    }

}



