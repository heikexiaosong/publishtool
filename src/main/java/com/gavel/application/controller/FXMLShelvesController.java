package com.gavel.application.controller;

import com.gavel.application.MainApp;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.ShelvesTask;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class FXMLShelvesController {

    @FXML
    private AnchorPane root;

    @FXML
    private TableView<ShelvesTask> taskTable;

    @FXML
    private TableColumn<ShelvesTask, String> title;
    @FXML
    private TableColumn<ShelvesTask, Integer> skunum;
    @FXML
    private TableColumn<ShelvesTask, Integer> success;

    @FXML
    private void initialize() {
        title.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        skunum.setCellValueFactory(new PropertyValueFactory<>("skunum"));
        success.setCellValueFactory(new PropertyValueFactory<>("success"));


        taskTable.getItems().addAll(loadData());

    }

    private List<ShelvesTask> loadData(){

        try {
            List<ShelvesTask> tasks = SQLExecutor.executeQueryBeanList("select * from SHELVESTASK", ShelvesTask.class);
            return tasks;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.EMPTY_LIST;
    }


    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }

    public void handleNewPerson(ActionEvent actionEvent) {

        ShelvesTask tempShelvesTask = new ShelvesTask();
        boolean okClicked = showShelvesTaskEditDialog(tempShelvesTask);
        if (okClicked) {
            //mainApp.getPersonData().add(tempPerson);
            try {
                SQLExecutor.insert(tempShelvesTask);
                taskTable.getItems().add(tempShelvesTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void handleDeletePerson(ActionEvent actionEvent) {

    }

    public boolean showShelvesTaskEditDialog(ShelvesTask task) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/ShelvesTaskEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("上架任务编辑");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            ShelvesTaskEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(task);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
