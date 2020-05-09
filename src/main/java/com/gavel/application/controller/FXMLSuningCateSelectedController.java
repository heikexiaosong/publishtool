package com.gavel.application.controller;

import com.gavel.application.DataPagination;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Category;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.List;

public class FXMLSuningCateSelectedController {

    @FXML
    private TextField keyword;

    @FXML
    private TableView<Category> itemList;
    @FXML
    private Pagination pagination;

    @FXML
    private TableColumn<Category, String> codeCol;
    @FXML
    private TableColumn<Category, String> nameCol;
    @FXML
    private TableColumn<Category, String> descPathCol;


    private Stage dialogStage;
    private boolean okClicked = false;

    private Category mappingCate;

    private List<Category> categories;

    @FXML
    private void initialize() {

        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryCode()));
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryName()));
        descPathCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescPath()));


        try {
            categories = SQLExecutor.executeQueryBeanList("select * from CATEGORY", Category.class);
        } catch (Exception e) {
            e.printStackTrace();
            categories = Collections.EMPTY_LIST;
        }


        DataPagination dataPagination = new DataPagination(categories, 30);
        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());
        pagination.setPageFactory(pageIndex -> {
            itemList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return itemList;
        });
    }


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        Category selected = itemList.getSelectionModel().getSelectedItem();
        if ( selected!=null ){
            mappingCate.setCategoryCode(selected.getCategoryCode());
            mappingCate.setCategoryName(selected.getCategoryName());
            mappingCate.setDescPath(selected.getDescPath());
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


    public void bind(Category _mappingCate) {
        this.mappingCate = _mappingCate;
    }

    public void handleKeyPressedAction(KeyEvent keyEvent) {

        String _keyword = keyword.getText().trim();
        try {
            String params = "%" + _keyword.trim() +"%";
            categories = SQLExecutor.executeQueryBeanList("select * from CATEGORY where CATEGORYCODE like ? or CATEGORYNAME like ? or DESCPATH like ?", Category.class, params, params, params);
        } catch (Exception e) {
            e.printStackTrace();
            categories = Collections.EMPTY_LIST;
        }


        DataPagination dataPagination = new DataPagination(categories, 30);
        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());
        pagination.setPageFactory(pageIndex -> {
            itemList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return itemList;
        });

    }
}
