package sample.controller;

import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.opencv.core.*;
import sample.Main;
import sample.core.DB;
import sample.model.ClassesColection;
import sample.model.ImagesColection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;


public class SampleResearchController {

    private Stage stage;
    protected Mat image;
    protected Main mainApp;
    @FXML
    private ComboBox<ClassesColection> comboBoxClasses;
    @FXML
    private ComboBox<ImagesColection> comboBoxImages;
    @FXML
    private CheckBox contour_area;
    @FXML
    private CheckBox contour_perimetr;
    @FXML
    private CheckBox contour_height;
    @FXML
    private CheckBox contour_width;
    @FXML
    private CheckBox contour_circularity;

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

        comboBoxImages.setVisible(true);

        ClassesColection selectedClass = comboBoxClasses.getSelectionModel().getSelectedItem();

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

        System.out.println("////////////////////////////////////////////");
        for(int i =0;i<selectedNucleiParam.size();i++){
            System.out.println(selectedNucleiParam.get(i));
        }
    }
}



