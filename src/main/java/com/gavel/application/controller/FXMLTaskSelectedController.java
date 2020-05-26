package com.gavel.application.controller;

import com.gavel.application.IDCell;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Task;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FXMLTaskSelectedController {

    @FXML
    private TextField keyword;

    @FXML
    private TableView<Task> skuList;

    @FXML
    private TableColumn<Task, Boolean> select;
    @FXML
    private TableColumn<Task, String> noCol;
    @FXML
    private TableColumn<Task, String> titleCol;
    @FXML
    private TableColumn<Task, String> urlCol;
    @FXML
    private TableColumn<Task, String> pagenumCol;
    @FXML
    private TableColumn<Task, String> productnumCol;
    @FXML
    private TableColumn<Task, String> skunumCol;
    @FXML
    private TableColumn<Task, String> statusCol;


    private Stage dialogStage;
    private boolean okClicked = false;

    private List<Task> items = new ArrayList<>();


    private  List<Task> bindTasks = null;


    @FXML
    private void initialize() {

        select.setCellFactory(column -> new CheckBoxTableCell<>());
        select.setCellValueFactory(cellData -> {
            Task cellValue = cellData.getValue();
            BooleanProperty property = cellValue.selectedProperty();

            // Add listener to handler change
            property.addListener((observable, oldValue, newValue) -> cellValue.setSelected(newValue));

            return property;
        });

        noCol.setCellFactory(new IDCell<>());
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        urlCol.setCellValueFactory(cellData -> cellData.getValue().urlProperty());
        pagenumCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPagenum())));
        productnumCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getProductnum())));
        skunumCol.setCellValueFactory(cellData ->  new SimpleStringProperty(String.valueOf(cellData.getValue().getSkunum())));
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());


        skuList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateStatus(newValue));

        try {
            items = SQLExecutor.executeQueryBeanList("select * from TASK where STATUS = 'success' ", Task.class);
        } catch (Exception e) {
            e.printStackTrace();
            items = Collections.EMPTY_LIST;
        }




        skuList.setItems(FXCollections.observableList(items));
    }

    private void updateStatus(Task newValue) {

        newValue.setSelected(!newValue.isSelected());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        for (Task item : items) {
            if ( item.isSelected() ) {
                bindTasks.add(item);
            }
        }
        okClicked = true;
        dialogStage.close();
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }


    public void bindItems(List<Task> _task) {
        this.bindTasks = _task;
    }

    public void handleSearchAction(ActionEvent actionEvent) {

    }
}
