package com.gavel.application.controller;

import com.gavel.ProductPicsDownload;
import com.gavel.application.MainApp;
import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.SearchItem;
import com.gavel.entity.Task;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        boolean okClicked = showShopSelectedDialog();
        if (okClicked) {
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
    }

    private  boolean showShopSelectedDialog() {
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

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void handlExportImagesAction(ActionEvent actionEvent) {

        List<Task> tasks = new ArrayList<>();
        boolean okClicked = showSelectTaskDialog(tasks);
        if (okClicked) {

            final  int total = tasks.size();

            Service<String> service = new Service<String>() {

                @Override
                protected javafx.concurrent.Task<String> createTask() {
                    return new javafx.concurrent.Task<String>() {

                        @Override
                        protected String call() throws Exception {

                            String dir = "d:\\" + System.currentTimeMillis();


                            for (int i = 0; i < tasks.size(); i++) {
                                Task task = tasks.get(i);

                                try {

                                    System.out.println(task.getTitle());

                                    List<SearchItem> items = Collections.EMPTY_LIST;
                                    try {
                                        items = SQLExecutor.executeQueryBeanList("select * from SEARCHITEM where TASKID = ? ", SearchItem.class, task.getId());
                                    } catch (Exception e) {

                                    }

                                    for (SearchItem item : items) {
                                        try {
                                            try {
                                                ProductPicsDownload.download(item, dir + File.separator + task.getId());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } catch (Exception e) {

                                        } finally {
                                            updateValue("["+ i +"/" + total  + "]正在导出" + task.getTitle() + "]... ");
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("\r[" + i + "/" + total + "][Item: " + task.getTitle() +"]图片导出异常: " + e.getMessage());
                                } finally {
                                    updateProgress(i, total);
                                    updateValue( task.getTitle() +  "[" + i + "/" + total + "]导出完成.");

                                }
                            }

                            return "导出图片目录: "  + dir;
                        };
                    };
                }

            };


            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(MainApp.class.getResource("/fxml/ProgressDialog.fxml"));
                AnchorPane page = (AnchorPane) loader.load();

                // Create the dialog Stage.
                Stage _dialogStage = new Stage();
                _dialogStage.setTitle("导出进度");
                _dialogStage.initModality(Modality.WINDOW_MODAL);
                _dialogStage.initOwner(stage());
                _dialogStage.setScene(new Scene(page));

                // Set the person into the controller.
                FXMLProgressDialogController controller = loader.getController();
                // Show the dialog and wait until the user closes it
                controller.setDialogStage(_dialogStage);
                controller.bind(service);
                _dialogStage.showAndWait();

                if ( service.isRunning() ) {
                    service.cancel();
                    service.reset();
                    service = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private boolean showSelectTaskDialog(List<Task> tasks) {

        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/TaskSelectedDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("采集任务选择");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLTaskSelectedController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bindItems(tasks);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
