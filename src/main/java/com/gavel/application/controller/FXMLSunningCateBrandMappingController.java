package com.gavel.application.controller;

import com.gavel.application.DataPagination;
import com.gavel.application.MainApp;
import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.CateBrandMapping;
import com.gavel.entity.Category;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FXMLSunningCateBrandMappingController {

    @FXML
    private TextField keyword;


    // 品牌
    @FXML
    private TableView<CateBrandMapping> brandTable;

    @FXML
    private TableColumn<CateBrandMapping, String> brandCodeCol;
    @FXML
    private TableColumn<CateBrandMapping, String> brandNameCol;



    @FXML
    private TableView<CateBrandMapping> cateBrandTable;
    @FXML
    private Pagination pagination;

    @FXML
    private TableColumn<CateBrandMapping, String> brandnameCol;
    @FXML
    private TableColumn<CateBrandMapping, String> catenameCol;
    @FXML
    private TableColumn<CateBrandMapping, String> categoryCodeCol;
    @FXML
    private TableColumn<CateBrandMapping, String> categoryNameCol;


    private Stage dialogStage;
    private boolean okClicked = false;

    private String suppliercode;

    private DataPagination dataPagination = new DataPagination(30);

    private final SimpleStringProperty _keyword = new SimpleStringProperty();

    private List<CateBrandMapping> cateBrandMappings;

    @FXML
    private void initialize() {


        keyword.textProperty().bindBidirectional(_keyword);

        brandnameCol.setCellValueFactory(cellData -> cellData.getValue().brandnameProperty());
        catenameCol.setCellValueFactory(cellData -> cellData.getValue().catenameProperty());
        categoryCodeCol.setCellValueFactory(cellData -> cellData.getValue().categorycodeProperty());
        categoryNameCol.setCellValueFactory(cellData -> cellData.getValue().categorynameProperty());


        brandCodeCol.setCellValueFactory(cellData -> cellData.getValue().brandcodeProperty());
        brandNameCol.setCellValueFactory(cellData -> cellData.getValue().brandnameProperty());

        brandTable.getSelectionModel().selectedItemProperty().addListener( (observable, oldValue, newValue) -> showBrandDetails(newValue) );
        brandTable.setItems(FXCollections.observableArrayList());

        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());
        pagination.setPageFactory(pageIndex -> {
            cateBrandTable.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return cateBrandTable;
        });
    }

    private void showBrandDetails(CateBrandMapping newValue) {

        List<CateBrandMapping> items = new ArrayList<>();
        if ( newValue!=null ) {
            for (CateBrandMapping cateBrandMapping : cateBrandMappings) {
                if ( newValue.getBrandcode()==null || newValue.getBrandcode().trim().length()==0 || newValue.getBrandcode().equalsIgnoreCase(cateBrandMapping.getBrandcode()) ) {
                    items.add(cateBrandMapping);
                }
            }
        }

        cateBrandTable.setItems(FXCollections.observableArrayList(items));
    }


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
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


    public void handleKeyPressedAction(KeyEvent keyEvent) {


    }


    public void bind(String _suppliercode, List<CateBrandMapping> _cateBrandMappings) {
        this.suppliercode = _suppliercode;
        this.cateBrandMappings = _cateBrandMappings;

        dataPagination.setDatas(_cateBrandMappings);

        if ( _cateBrandMappings==null ) {
            brandTable.setItems(FXCollections.emptyObservableList());
        } else {
            Map<String, CateBrandMapping> brandMap = _cateBrandMappings.stream().collect(Collectors.toMap(CateBrandMapping::getBrandcode, e->e, (v1, v2) -> v1));
            brandTable.setItems(FXCollections.observableArrayList(brandMap.values()));
        }
    }

    public void handleAutoMappingAction(ActionEvent actionEvent) {
    }



    public void handleCateBatchMappingAction(ActionEvent actionEvent) {


        CateBrandMapping selected = cateBrandTable.getSelectionModel().getSelectedItem();
        if ( selected==null ) {
            Alert _alert = new Alert(Alert.AlertType.INFORMATION);
            _alert.setTitle("信息");
            _alert.setHeaderText("请先选择类目");
            _alert.initOwner(dialogStage);
            _alert.show();

            return;
        }

        Category category = new Category();
        boolean okClicked = showCategoryMappingEditDialog(category,  selected);
        if (okClicked) {
            selected.setCategorycode(category.getCategoryCode());
            selected.setCategoryname(category.getCategoryName());
            selected.setDescpath(category.getDescPath());
            try {
                SQLExecutor.update(selected);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean showCategoryMappingEditDialog(Category mappingCate, CateBrandMapping selected) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/SuningCategorySelectDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage _dialogStage = new Stage();
            _dialogStage.setTitle("上架类目映射");
            _dialogStage.initModality(Modality.WINDOW_MODAL);
            _dialogStage.initOwner(dialogStage);
            Scene scene = new Scene(page);
            _dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLSuningCateSelectedController controller = loader.getController();
            controller.setDialogStage(_dialogStage);
            controller.bind(mappingCate);

            controller.initparams((selected==null ? "" : selected.getCatename()),  APPConfig.getInstance().getShopinfo().getCode());

            // Show the dialog and wait until the user closes it
            _dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
