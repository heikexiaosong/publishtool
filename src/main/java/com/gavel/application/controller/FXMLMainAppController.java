package com.gavel.application.controller;

import com.gavel.application.MainApp;
import com.gavel.config.APPConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class FXMLMainAppController {

    @FXML
    private BorderPane root;

    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }

    @FXML
    private Hyperlink shopinfo;


    @FXML
    private void initialize() {
        shopinfo.textProperty().bindBidirectional(APPConfig.getInstance().getShopinfo().nameProperty());
    }

    /**
     * 设置
     * @param actionEvent
     */
    public void handlSettingAction(ActionEvent actionEvent) {

        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/setting.fxml"));

            // Set person overview into the center of root layout.
            root.setCenter(loader.load());


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 采集
     * @param actionEvent
     */
    public void handlCollectionAction(ActionEvent actionEvent) {

        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/collection.fxml"));

            // Set person overview into the center of root layout.
            root.setCenter(loader.load());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上架
     * @param actionEvent
     */
    public void handlShelvesAction(ActionEvent actionEvent) {

        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/shelves.fxml"));

            // Set person overview into the center of root layout.
            root.setCenter(loader.load());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleShopChangeAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/ShopSelectedDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("店铺选择");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLShopSelectedDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bindItems(APPConfig.getInstance().getShopinfo());

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
