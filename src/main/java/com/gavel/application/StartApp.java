package com.gavel.application;

import com.gavel.ProductPicsDownload;
import com.gavel.application.controller.FXMLProgressDialogController;
import com.gavel.application.controller.FXMLTaskSelectedController;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.SearchItem;
import com.gavel.entity.Task;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StartApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {


        List<Task> tasks = new ArrayList<>();
        boolean okClicked = showSelectTaskDialog(stage, tasks);
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

                                    int j = 1;
                                    for (SearchItem item : items) {
                                        try {
                                            try {
                                                ProductPicsDownload.download(item, dir + File.separator + task.getTitle().replace(" ", "_"));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } catch (Exception e) {

                                        } finally {
                                            updateValue("["+ i +"/" + total  + "][" + item.getType() + "-" + item.getCode() + "][" + (j++) + "][Sku: " + item.getSkunum() + "]正在导出" + task.getTitle() + "]... ");
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
                _dialogStage.initOwner(stage);
                _dialogStage.setScene(new Scene(page));

                // Set the person into the controller.
                FXMLProgressDialogController controller = loader.getController();
                // Show the dialog and wait until the user closes it
                controller.setDialogStage(_dialogStage);
                controller.bind(service);
                _dialogStage.showAndWait();

                stage.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private boolean showSelectTaskDialog(Stage stage, List<Task> tasks) {

        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/TaskSelectedDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("采集任务选择");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
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
