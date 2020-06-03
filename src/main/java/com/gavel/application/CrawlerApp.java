package com.gavel.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.IOException;

public class CrawlerApp extends Application {

    private Stage primaryStage;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    /**
     * Constructor
     */
    public CrawlerApp() {
        // Add some sample data
    }


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("京苏商品上架工具");

        initRootLayout();
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(CrawlerApp.class.getResource("/fxml/crawler.fxml"));

              // Show the scene containing the root layout.
            Scene scene = new Scene( loader.load());
            primaryStage.setScene(scene);
            primaryStage.show();


            //ALTER TABLE brand_info ADD FLAG varchar(8) NULL;d
            primaryStage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}