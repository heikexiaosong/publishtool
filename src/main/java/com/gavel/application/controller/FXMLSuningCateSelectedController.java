package com.gavel.application.controller;

import com.gavel.application.DataPagination;
import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Brand;
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

import java.util.ArrayList;
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


    // 品牌
    @FXML
    private TableView<Brand> brandTable;

    @FXML
    private TableColumn<Brand, String> brandCodeCol;
    @FXML
    private TableColumn<Brand, String> brandNameCol;


    private Stage dialogStage;
    private boolean okClicked = false;

    private Category mappingCate;

    private List<Category> categories;

    private String suppliercode;
    private final SimpleStringProperty _keyword = new SimpleStringProperty();

    @FXML
    private void initialize() {


        keyword.textProperty().bindBidirectional(_keyword);

        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryCode()));
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryName()));
        descPathCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescPath()));



        brandCodeCol.setCellValueFactory(cellData -> cellData.getValue().codeProperty());
        brandNameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());



        itemList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showBrandDetails(newValue));
//
        DataPagination dataPagination = new DataPagination(Collections.EMPTY_LIST, 30);
        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());
        pagination.setPageFactory(pageIndex -> {
            itemList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return itemList;
        });
    }

    private void showBrandDetails(Category newValue) {

        brandTable.setItems(FXCollections.emptyObservableList());
        if ( newValue!=null ) {
            List<Brand> brands = new ArrayList<>();
            try {
                brands = SQLExecutor.executeQueryBeanList("select  distinct CODE, NAME from BRAND where SUPPLIERCODE = ?  and CATEGORYCODE = ? ", Brand.class, APPConfig.getInstance().getShopinfo().getCode(), newValue.getCategoryCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
            brandTable.setItems(FXCollections.observableArrayList(brands));
        }
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

        try {
            String key = _keyword.getValue();
            String params = "%" + ( key==null ? "" : key.trim() ) +"%";
            categories = SQLExecutor.executeQueryBeanList("select * from CATEGORY where SUPPLIERCODE = ? and (CATEGORYCODE like ? or CATEGORYNAME like ? or DESCPATH like ?)", Category.class, suppliercode, params, params, params);
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

    public void initparams(String keyword, String code) {
        System.out.println(keyword + " -- " + code);
        this._keyword.setValue(keyword);
        this.suppliercode = code;

        try {
            String key = _keyword.getValue();
            String params = "%" + ( key==null ? "" : key.trim() ) +"%";
            categories = SQLExecutor.executeQueryBeanList("select * from CATEGORY where SUPPLIERCODE = ? and (CATEGORYCODE like ? or CATEGORYNAME like ? or DESCPATH like ?)", Category.class, suppliercode, params, params, params);
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
