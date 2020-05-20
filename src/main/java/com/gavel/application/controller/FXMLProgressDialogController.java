package com.gavel.application.controller;

import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class FXMLProgressDialogController {

    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label text;

    private Service<String> service;

    @FXML
    private void initialize() {

    }

    public void bind(Service<String> _service){
        this.service = _service;
        if ( service!=null ) {
            text.textProperty().bind(service.valueProperty());
            progressBar.progressProperty().bind(service.progressProperty());
            service.start();
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

}
