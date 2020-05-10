package com.gavel.application.controller;

import com.gavel.entity.BrandMapping;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FXMLSuningBrandReplaceController {

    @FXML
    private TextField srcCode;

    @FXML
    private TextField srcName1;

    @FXML
    private TextField srcName2;

    @FXML
    private TextField destName1;

    @FXML
    private TextField destName2;


    private Stage dialogStage;
    private boolean okClicked = false;


    private BrandMapping mappingBrand;


    @FXML
    private void initialize() {

    }


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {

        mappingBrand.setReplacename_zh(destName1.getText().trim());
        mappingBrand.setReplacename_en(destName2.getText().trim());

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


    public void bind(BrandMapping _mappingBrand) {
        this.mappingBrand = _mappingBrand;
        if ( mappingBrand!=null ) {
            srcCode.setText(mappingBrand.getGraingercode());
            srcName1.setText(mappingBrand.getName1());
            srcName2.setText(mappingBrand.getName2());

            destName1.setText(mappingBrand.getReplacename_zh());
            destName2.setText(mappingBrand.getReplacename_en());

        }
    }

}
