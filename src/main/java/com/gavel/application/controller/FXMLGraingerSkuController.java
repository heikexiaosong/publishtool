package com.gavel.application.controller;

import com.gavel.application.IDCell;
import com.gavel.application.MainApp;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.BrandInfo;
import com.gavel.entity.ShelvesItem;
import com.gavel.entity.Task;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class FXMLGraingerSkuController {

    @FXML
    private AnchorPane root;

    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }

    @FXML
    private TableView<BrandInfo> brandInfoTable;
    @FXML
    private TableColumn<BrandInfo, Boolean> select;
    @FXML
    private TableColumn<ShelvesItem, String> xh;
    @FXML
    private TableColumn<BrandInfo, String> code;
    @FXML
    private TableColumn<BrandInfo, String> name1;
    @FXML
    private TableColumn<BrandInfo, String> name2;
    @FXML
    private TableColumn<BrandInfo, String> skunum;
    @FXML
    private TableColumn<BrandInfo, String> productnum;
    @FXML
    private TableColumn<BrandInfo, String> pagenum;
    @FXML
    private TableColumn<BrandInfo, String> url;


    @FXML
    private TextField keyword;


    @FXML
    private void initialize() {


        select.setCellFactory(column -> new CheckBoxTableCell<>());
        select.setCellValueFactory(cellData -> {
            BrandInfo cellValue = cellData.getValue();
            BooleanProperty property = cellValue.selectedProperty();

            // Add listener to handler change
            property.addListener((observable, oldValue, newValue) ->  cellValue.selectedProperty().setValue(newValue));

            //property.addListener((observable, oldValue, newValue) -> cellValue.selectedProperty().setValue(newValue));

            return property;
        });

        xh.setCellFactory(new IDCell<>());
        code.setCellValueFactory(cellData -> cellData.getValue().codeProperty());
        name1.setCellValueFactory(cellData -> cellData.getValue().name1Property());
        name2.setCellValueFactory(cellData -> cellData.getValue().name2Property());
        skunum.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSkunum())));
        productnum.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getProductnum())));
        pagenum.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPagenum())));
        url.setCellValueFactory(cellData -> cellData.getValue().urlProperty());


        // Clear person details.
        showTaskDetails(null);

        // Listen for selection changes and show the person details when changed.
        brandInfoTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTaskDetails(newValue));


        List<BrandInfo> tasks = null;
        try {
            tasks = SQLExecutor.executeQueryBeanList("select * from BRAND_INFO order by SKUNUM desc", BrandInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        brandInfoTable.setItems(FXCollections.observableArrayList(tasks));

    }

    private void showTaskDetails(BrandInfo task) {
        if ( task!=null ) {
            task.setSelected(!task.isSelected());
        }
    }

    /**
     * 新增任务
     * @param actionEvent
     */
    public void handleNewTaskAction(ActionEvent actionEvent) {

    }

    private boolean showAddTaskDialog(Task task) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/TaskAddDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("选择采集页面");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLTaskAddDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bindTask(task);
            dialogStage.setMaximized(true);
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 删除任务
     * @param actionEvent
     */
    public void handleDelTaskAction(ActionEvent actionEvent) {


    }

    public void handleSearchAction(ActionEvent actionEvent) {



    }

    public void handleAddBrandTaskAction(ActionEvent actionEvent) {

    }

    public void handleCateALlPageAction(ActionEvent actionEvent) {
    }

    public void handleKeyReleasedAction(KeyEvent keyEvent) {
    }

    public void handleAutoMappingAction(ActionEvent actionEvent) {
    }

    public void handleCateBatchMappingAction(ActionEvent actionEvent) {
    }
}
