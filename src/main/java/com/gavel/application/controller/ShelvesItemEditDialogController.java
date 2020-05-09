package com.gavel.application.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ShelvesItemEditDialogController {

    @FXML
    private TextField prefix;
    @FXML
    private TextField suffix;
    @FXML
    private TextField src;
    @FXML
    private TextField dest;

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
}
