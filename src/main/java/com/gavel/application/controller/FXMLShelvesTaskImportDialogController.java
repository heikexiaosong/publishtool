package com.gavel.application.controller;

import com.gavel.application.IDCell;
import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.ShelvesItem;
import com.gavel.entity.ShelvesTask;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.List;

public class FXMLShelvesTaskImportDialogController {

    @FXML
    private TextField keyword;

    @FXML
    private TableView<ShelvesTask> shelvesTaskTable;
    @FXML
    private TableColumn<ShelvesTask, String> shopidCol;
    @FXML
    private TableColumn<ShelvesTask, String> titleCol;
    @FXML
    private TableColumn<ShelvesTask, Integer> moqCol;


    // 产品SKU列表
    @FXML
    private TableView<ShelvesItem> itemList;
    @FXML
    private TableColumn<ShelvesItem, String> noCol;
    @FXML
    private TableColumn<ShelvesItem, Boolean> select;
    @FXML
    private TableColumn<ShelvesItem, String> codeCol;
    @FXML
    private TableColumn<ShelvesItem, String> cmTitleCol;
    @FXML
    private TableColumn<ShelvesItem, String> graingerbrandnameCol;
    @FXML
    private TableColumn<ShelvesItem, String> graingercategorynameCol;


    @FXML
    private CheckBox allPage;


    @FXML
    private Label selectedNum;

    private Stage dialogStage;
    private boolean okClicked = false;

    private List<ShelvesItem> shelvesTasks;

    @FXML
    private void initialize() {

        select.setCellFactory(column -> new CheckBoxTableCell<>());
        select.setCellValueFactory(cellData -> {
            ShelvesItem cellValue = cellData.getValue();
            BooleanProperty property = cellValue.selectedProperty();

            // Add listener to handler change
            //property.addListener((observable, oldValue, newValue) ->  cellValue.selectedProperty().setValue(newValue));

            property.addListener((observable, oldValue, newValue) -> updateSelectStatus(cellValue, newValue));

            return property;
        });

        noCol.setCellFactory(new IDCell<>());

        cmTitleCol.setCellValueFactory(cellData -> cellData.getValue().cmTitleProperty());
        codeCol.setCellValueFactory(cellData -> cellData.getValue().itemCodeProperty());
        graingerbrandnameCol.setCellValueFactory(cellData -> cellData.getValue().brandnameProperty());
        graingercategorynameCol.setCellValueFactory(cellData -> cellData.getValue().categorynameProperty());


        shopidCol.setCellValueFactory(cellData -> cellData.getValue().shopidProperty());
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        moqCol.setCellValueFactory(new PropertyValueFactory<>("moq"));

        shelvesTaskTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showShelvesDetails(newValue));


        List<ShelvesTask> shelvesTasks = null;
        try {
            shelvesTasks = SQLExecutor.executeQueryBeanList("select * from SHELVESTASK where SHOPID <> ? ", ShelvesTask.class, APPConfig.getInstance().getShopinfo().getCode());
        } catch (Exception e) {
            e.printStackTrace();
            shelvesTasks = Collections.EMPTY_LIST;
        }

        shelvesTaskTable.setItems(FXCollections.observableArrayList(shelvesTasks));
    }

    private void showShelvesDetails(ShelvesTask newValue) {

        itemList.setItems(FXCollections.observableArrayList());
        if ( newValue!=null ) {
            List<ShelvesItem> datas = null;
            try {
                datas = SQLExecutor.executeQueryBeanList("select * from SHELVESITEM where TASKID = ? ", ShelvesItem.class, newValue.getId());
            } catch (Exception e) {
                e.printStackTrace();
                datas = Collections.EMPTY_LIST;
            }
            itemList.setItems(FXCollections.observableList(datas));
        }
    }

    private void updateSelectStatus(ShelvesItem cellValue, Boolean newValue) {
        cellValue.selectedProperty().setValue(newValue);


        int count = 0;

        for (ShelvesItem shelvesItem : itemList.getItems()) {
            if ( shelvesItem.isSelected() ) {
                count++;
            }
        }
        selectedNum.setText(String.valueOf(count));
    }


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {

        for (ShelvesItem shelvesItem : itemList.getItems()) {
            if ( shelvesItem.isSelected() ) {
                shelvesTasks.add(shelvesItem);
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


    public void handleSearchAction(ActionEvent actionEvent) {


    }

    public void bind(List<ShelvesItem> _shelvesTasks) {
        this.shelvesTasks = _shelvesTasks;
    }

    public void handleALlPageAction(ActionEvent actionEvent) {
        int count = 0;
        for (ShelvesItem shelvesItem : itemList.getItems()) {
            shelvesItem.setSelected(allPage.isSelected());
            if ( shelvesItem.isSelected() ) {
                count++;
            }
        }
        selectedNum.setText(String.valueOf(count));
    }
}
