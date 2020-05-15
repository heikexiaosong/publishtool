package com.gavel.application.controller;

import com.gavel.entity.ShelvesTask;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Calendar;

/**
 * Dialog to edit details of a person.
 * 
 * @author Marco Jakob
 */
public class ShelvesTaskEditDialogController {

    @FXML
    private TextField id;
    @FXML
    private TextField title;
    @FXML
    private TextField moq;

    @FXML
    private TextField pic;

    @FXML
    private TextField reamark;

    private Stage dialogStage;
    private ShelvesTask task;
    private boolean okClicked = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    }

    /**
     * Sets the stage of this dialog.
     * 
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the person to be edited in the dialog.
     * 
     * @param _task
     */
    public void setPerson(ShelvesTask _task) {
        this.task = _task;

        if ( task!=null ) {
            id.setText(task.getId());
            title.setText(task.getTitle());
            moq.setText(String.valueOf(task.getMoq()));
            pic.setText(task.getPic());
            reamark.setText(task.getRemark());
        }


    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     * 
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            task.setTitle(title.getText());
            task.setRemark(reamark.getText());
            task.setUpdatetime(Calendar.getInstance().getTime());
            task.setPic(pic.getText());
            try {
                task.setMoq(Integer.parseInt(moq.getText()));
            } catch (Exception e) {
                task.setMoq(0);
            }

//            task.setFirstName(firstNameField.getText());
//            task.setLastName(lastNameField.getText());
//            task.setStreet(streetField.getText());
//            task.setPostalCode(Integer.parseInt(postalCodeField.getText()));
//            task.setCity(cityField.getText());
//            task.setBirthday(DateUtil.parse(birthdayField.getText()));

            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text fields.
     * 
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

//        if (firstNameField.getText() == null || firstNameField.getText().length() == 0) {
//            errorMessage += "No valid first name!\n";
//        }
//        if (lastNameField.getText() == null || lastNameField.getText().length() == 0) {
//            errorMessage += "No valid last name!\n";
//        }
//        if (streetField.getText() == null || streetField.getText().length() == 0) {
//            errorMessage += "No valid street!\n";
//        }
//
//        if (postalCodeField.getText() == null || postalCodeField.getText().length() == 0) {
//            errorMessage += "No valid postal code!\n";
//        } else {
//            // try to parse the postal code into an int.
//            try {
//                Integer.parseInt(postalCodeField.getText());
//            } catch (NumberFormatException e) {
//                errorMessage += "No valid postal code (must be an integer)!\n";
//            }
//        }
//
//        if (cityField.getText() == null || cityField.getText().length() == 0) {
//            errorMessage += "No valid city!\n";
//        }
//
//        if (birthdayField.getText() == null || birthdayField.getText().length() == 0) {
//            errorMessage += "No valid birthday!\n";
//        } else {
//            if (!DateUtil.validDate(birthdayField.getText())) {
//                errorMessage += "No valid birthday. Use the format dd.mm.yyyy!\n";
//            }
//        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            
            alert.showAndWait();
            
            return false;
        }
    }

    public void handleFileSelectAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();//构建一个文件选择器实例
        fileChooser.setTitle("选择默认图片");
        File selectedFile = fileChooser.showOpenDialog(dialogStage);

        String path = selectedFile.getPath();

        task.setPic(path);
        pic.setText(path);
    }
}