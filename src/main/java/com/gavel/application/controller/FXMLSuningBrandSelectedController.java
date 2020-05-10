package com.gavel.application.controller;

import com.gavel.application.DataPagination;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Brand;
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

public class FXMLSuningBrandSelectedController {

    @FXML
    private TextField keyword;

    @FXML
    private TableView<Brand> itemList;
    @FXML
    private Pagination pagination;

    @FXML
    private TableColumn<Brand, String> codeCol;
    @FXML
    private TableColumn<Brand, String> nameCol;


    private Stage dialogStage;
    private boolean okClicked = false;

    private Brand mappingBrand;

    private List<Brand> brands;

    @FXML
    private void initialize() {

        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCode()));
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        try {
            brands = SQLExecutor.executeQueryBeanList("select distinct CODE, NAME from brand", Brand.class);
        } catch (Exception e) {
            e.printStackTrace();
            brands = Collections.EMPTY_LIST;
        }


        DataPagination dataPagination = new DataPagination(brands, 50);
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
        Brand selected = itemList.getSelectionModel().getSelectedItem();
        if ( selected!=null ){
            mappingBrand.setCode(selected.getCode());
            mappingBrand.setName(selected.getName());
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


    public void bind(Brand _mappingBrand) {
        this.mappingBrand = _mappingBrand;
    }

    public void handleKeyPressedAction(KeyEvent keyEvent) {

        String _keyword = keyword.getText().trim();
        try {
            String params = "%" + _keyword.trim() +"%";
            brands = SQLExecutor.executeQueryBeanList("select distinct CODE, NAME from BRAND where CODE like ? or NAME like ? ", Brand.class, params, params);
        } catch (Exception e) {
            e.printStackTrace();
            brands = Collections.EMPTY_LIST;
        }


        DataPagination dataPagination = new DataPagination(brands, 50);
        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());
        pagination.setPageFactory(pageIndex -> {
            itemList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return itemList;
        });

    }
}
