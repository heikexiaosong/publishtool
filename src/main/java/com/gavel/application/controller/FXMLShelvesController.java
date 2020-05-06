package com.gavel.application.controller;

import com.gavel.ProductShelves;
import com.gavel.application.MainApp;
import com.gavel.crawler.HtmlPageLoader;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.ShelvesItem;
import com.gavel.entity.ShelvesTask;
import com.gavel.utils.MD5Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FXMLShelvesController {

    @FXML
    private AnchorPane root;

    // 上架任务列表
    @FXML
    private TableView<ShelvesTask> taskTable;
    @FXML
    private TableColumn<ShelvesTask, String> title;
    @FXML
    private TableColumn<ShelvesTask, Integer> skunum;
    @FXML
    private TableColumn<ShelvesTask, Integer> success;

    // 产品SKU列表
    @FXML
    private TableView<ShelvesItem> skuList;
    @FXML
    private TableColumn<ShelvesItem, Integer> xhCol;
    @FXML
    private TableColumn<ShelvesItem, String> picCol;
    @FXML
    private TableColumn<ShelvesItem, String> codeCol;
    @FXML
    private TableColumn<ShelvesItem, String> graingerbrandnameCol;
    @FXML
    private TableColumn<ShelvesItem, String> graingercategorynameCol;
    @FXML
    private TableColumn<ShelvesItem, String> brandnameCol;
    @FXML
    private TableColumn<ShelvesItem, String> categorynameCol;
    @FXML
    private TableColumn<ShelvesItem, String> statusCol;
    @FXML
    private TableColumn<ShelvesItem, String> msgCol;
    @FXML
    private TableColumn<ShelvesItem, String> updatetimeCol;

    @FXML
    private Pagination pagination;

    @FXML
    private void initialize() {



        xhCol.setCellValueFactory(new PropertyValueFactory<>("xh"));
        picCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemCode()));
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemCode()));
        graingerbrandnameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBrandCode()));
        graingercategorynameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryCode()));
        brandnameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBrandCode()));
        categorynameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryCode()));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        msgCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMsg()));
        updatetimeCol.setCellValueFactory(new PropertyValueFactory<>("updatetime"));



        title.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        skunum.setCellValueFactory(new PropertyValueFactory<>("skunum"));
        success.setCellValueFactory(new PropertyValueFactory<>("success"));



        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showShelvesDetails(newValue));


        taskTable.getItems().addAll(loadData());
    }

    private void showShelvesDetails(ShelvesTask newValue) {

        skuList.getItems().clear();
        if ( newValue==null ) {
            return;
        }

        List<ShelvesItem> items = new ArrayList<>();
        try {
            List<ShelvesItem> temp = SQLExecutor.executeQueryBeanList("select * from SHELVESITEM where TASKID = ? ", ShelvesItem.class, newValue.getId());
            if ( temp!=null ) {
                items.addAll(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        skuList.getItems().addAll(items);

        pagination.setPageCount(1);

        skuList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, _newValue) -> System.out.println(_newValue.getItemCode()));

    }

    private List<ShelvesTask> loadData(){

        try {
            List<ShelvesTask> tasks = SQLExecutor.executeQueryBeanList("select * from SHELVESTASK", ShelvesTask.class);
            return tasks;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.EMPTY_LIST;
    }


    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }

    public void handleNewPerson(ActionEvent actionEvent) {

        ShelvesTask tempShelvesTask = new ShelvesTask();
        boolean okClicked = showShelvesTaskEditDialog(tempShelvesTask);
        if (okClicked) {
            //mainApp.getPersonData().add(tempPerson);
            try {
                SQLExecutor.insert(tempShelvesTask);
                taskTable.getItems().add(tempShelvesTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void handleDeletePerson(ActionEvent actionEvent) {

    }

    public boolean showShelvesTaskEditDialog(ShelvesTask task) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/ShelvesTaskEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("上架任务编辑");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            ShelvesTaskEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(task);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void handleAddSkuPerson(ActionEvent actionEvent) {
        List<ShelvesItem> items = new ArrayList<>();
        boolean okClicked = showShelvesItemEditDialog(items);
        if (okClicked) {

            ShelvesTask taskSelected = taskTable.getSelectionModel().getSelectedItem();
            for (ShelvesItem item : items) {
                //mainApp.getPersonData().add(tempPerson);
                item.setId(MD5Utils.md5Hex(item.getTaskid() + item.getItemCode()));
                item.setTaskid(taskSelected.getId());
                try {
                    SQLExecutor.insert(item);
                    skuList.getItems().add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    // 选择上架商品
    private boolean showShelvesItemEditDialog(List<ShelvesItem> shelvesItems) {



        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/SkuSelectedDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("选择上架商品");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLSkuSelectedController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bindItems(shelvesItems);
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void handleShelvesAction(ActionEvent actionEvent) {
        // TODO 上架

        ObservableList<ShelvesItem> items = skuList.getItems();
        if ( items==null || items.size()==0 ) {
            return;
        }

        for (ShelvesItem shelvesItem : skuList.getItems()) {

            String code = shelvesItem.getItemCode();
            String suningBrand = "04XT";
            String suningCate = "R9002778";

            HtmlCache htmlCache = null;
            try {
                htmlCache = HtmlPageLoader.getInstance().loadHtmlPage("https://www.grainger.cn/u-" + code + ".html", true);
                if ( htmlCache==null || htmlCache.getHtml().trim().length() <=0 ) {
                    System.out.println("Html 获取失败。。");
                    continue;
                }

                ProductShelves.run(htmlCache, suningCate, suningBrand);
            } catch (Exception e) {
                e.printStackTrace();
            }




        }

    }

    /**
     * 操作
     *
     * @param actionEvent
     */
    public void handleEditSkuPerson(ActionEvent actionEvent) {
        List<ShelvesItem> items = new ArrayList<>();
        boolean okClicked = shelvesItemEditDialog();
        if (okClicked) {

            ShelvesTask taskSelected = taskTable.getSelectionModel().getSelectedItem();
            for (ShelvesItem item : items) {
                //mainApp.getPersonData().add(tempPerson);
                item.setId(MD5Utils.md5Hex(item.getTaskid() + item.getItemCode()));
                item.setTaskid(taskSelected.getId());
                try {
                    SQLExecutor.insert(item);
                    skuList.getItems().add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private boolean shelvesItemEditDialog() {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/ShelvesItemEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("选择上架商品");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            ShelvesItemEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            //controller.bindItems(shelvesItems);
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
