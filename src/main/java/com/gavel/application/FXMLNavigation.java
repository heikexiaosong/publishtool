package com.gavel.application;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FXMLNavigation {

   public static <T> T toView( String viewPath, Stage stage) {

       if ( viewPath!=null && viewPath.trim().length() > 0 ) {
           if ( stage!=null ) {
               FXMLLoader fxmlLoader = new FXMLLoader();
               try {
                   fxmlLoader.setLocation(FXMLNavigation.class.getResource(viewPath));
                   Parent parent = fxmlLoader.load();
                   stage.setScene(new Scene(parent));
                   stage.show();
                   stage.setMaximized(true);

                   return fxmlLoader.getController();
               } catch (Exception e){
                   e.printStackTrace();
               } finally {
                   fxmlLoader = null;
               }
           }
       }

       return null;
   }

    public static <T> T toModality( String viewPath, int width, int height) {

        if ( viewPath!=null && viewPath.trim().length() > 0 ) {

            Stage stage = new Stage();
            //stage.initStyle(StageStyle.UNDECORATED);//设定窗口无边框
//            stage.initStyle(StageStyle.TRANSPARENT);
//            stage.setOpacity(0.9);

            AnchorPane root = new AnchorPane();
            root.setStyle("-fx-background-color: #DFDFDFB4;");

            Scene scene = new Scene(root, 1920, 1080, Color.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);


            FXMLLoader fxmlLoader = new FXMLLoader();
            try {
                fxmlLoader.setLocation(FXMLNavigation.class.getResource(viewPath));
                Parent parent = fxmlLoader.load();

                root.getChildren().add(parent);

                AnchorPane.setTopAnchor(parent, 540-height/2 + 80.0);
                AnchorPane.setLeftAnchor(parent, 960.0-width/2);

                stage.setScene(scene);
                stage.centerOnScreen();
                stage.toFront();
                stage.setY(0);
                stage.setX(0);
//                stage.setX(960-width/2);
//                stage.setY(540-height/2 + 80);
                Platform.runLater( () -> stage.showAndWait() );
                return fxmlLoader.getController();
            } catch (Exception e){
                e.printStackTrace();
            }

        }

        return null;
    }

}
