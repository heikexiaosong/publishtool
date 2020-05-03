package com.gavel.application.controller;

import com.gavel.application.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class FXMLMainAppController {

    @FXML
    private BorderPane root;


    @FXML
    private void initialize() {

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
    }

    /**
     * 上架
     * @param actionEvent
     */
    public void handlShelvesAction(ActionEvent actionEvent) {
    }
}
