package com.gavel.application;

import com.gavel.application.controller.FXMLJDCrawlerController;
import com.gavel.crawler.DriverHtmlLoader;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class JDCrawlerApp extends Application {

    private Stage primaryStage;

    /**
     * Constructor
     */
    public JDCrawlerApp() {
        // Add some sample data
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                DriverHtmlLoader.getInstance().quit();
            }
        });

        DriverHtmlLoader.getInstance().loadHtml("https://item.jd.com/68812355916.html");
    }


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("京东商品采集工具");

        initRootLayout();


        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {

                FXMLJDCrawlerController.executor.shutdown();
                if ( !FXMLJDCrawlerController.executor.isShutdown() ) {
                    FXMLJDCrawlerController.executor.shutdownNow();
                }

                System.exit(0);
            }
        });
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JDCrawlerApp.class.getResource("/fxml/jdcrawler.fxml"));

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