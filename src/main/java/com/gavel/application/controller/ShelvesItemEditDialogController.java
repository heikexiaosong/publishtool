package com.gavel.application.controller;

import com.gavel.application.MainApp;
import com.gavel.entity.Brand;
import com.gavel.entity.Category;
import com.gavel.utils.StringUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ShelvesItemEditDialogController {

    @FXML
    private TextField prefix;
    @FXML
    private TextField suffix;
    @FXML
    private TextField src;
    @FXML
    private TextField dest;

    @FXML
    private TextField codePrefix;
    @FXML
    private TextField codeSuffix;

    @FXML
    private TextField categoryName;

    @FXML
    private TextField brandName;

    private Stage dialogStage;
    private boolean okClicked = false;

    private FXMLShelvesController.EditTask editTask;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {

        // TODO
        if ( editTask!=null  ) {
            editTask.setPrefix(prefix.getText().trim());
            editTask.setSuffix(suffix.getText().trim());
            editTask.setSrc(src.getText().trim());
            editTask.setDest(dest.getText().trim());

            editTask.setCodePrefix(codePrefix.getText().trim());
            editTask.setCodeSuffix(codeSuffix.getText().trim());
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

    public void bindEditTask(FXMLShelvesController.EditTask _editTask) {
        this.editTask = _editTask;
    }

    /**
     * 类目选择
     * @param actionEvent
     */
    public void handleCategorySelect(ActionEvent actionEvent) {
        Category mappingCate = new Category();
        boolean okClicked = showCategoryMappingEditDialog(mappingCate);
        if (okClicked) {
            try {
                editTask.setCategory(mappingCate);
                categoryName.setText(mappingCate.getCategoryName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean showCategoryMappingEditDialog(Category mappingCate) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/SuningCategorySelectDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage _dialogStage = new Stage();
            _dialogStage.setTitle("上架类目选择");
            _dialogStage.initModality(Modality.WINDOW_MODAL);
            _dialogStage.initOwner(dialogStage);
            Scene scene = new Scene(page);
            _dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLSuningCateSelectedController controller = loader.getController();
            controller.setDialogStage(_dialogStage);
            controller.bind(mappingCate);

            // Show the dialog and wait until the user closes it
            _dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 品牌选择
     * @param actionEvent
     */
    public void handleBrandSelect(ActionEvent actionEvent) {

        if ( editTask.getCategory()==null || StringUtils.isBlank(editTask.getCategory().getCategoryCode())) {
            Alert _alert = new Alert(Alert.AlertType.INFORMATION);
            _alert.setTitle("信息");
            _alert.setHeaderText("请先选择上架类目");
            _alert.initOwner(dialogStage);
            _alert.show();

            return;
        }

        Brand mappingBrand = new Brand();
        mappingBrand.setCategoryCode(editTask.getCategory().getCategoryCode());
        boolean okClicked = showBrandMappingEditDialog(mappingBrand);
        if (okClicked) {
            //mainApp.getPersonData().add(tempPerson);
            try {
                editTask.setBrand(mappingBrand);
                brandName.setText(mappingBrand.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean showBrandMappingEditDialog(Brand mappingBrand) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/SuningBrandSelectDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage _dialogStage = new Stage();
            _dialogStage.setTitle("品牌选择");
            _dialogStage.initModality(Modality.WINDOW_MODAL);
            _dialogStage.initOwner(dialogStage);
            Scene scene = new Scene(page);
            _dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLSuningBrandSelectedController controller = loader.getController();
            controller.setDialogStage(_dialogStage);
            controller.bind(mappingBrand);

            // Show the dialog and wait until the user closes it
            _dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
