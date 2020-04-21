package com.gavel.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class StartApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/mainui.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);

        stage.setTitle("京苏电子自动上架工具  V1.0");

        //stage.initStyle(StageStyle.UNDECORATED);//设定窗口无边框
        stage.show();
        stage.setMaximized(true);

    }
}
