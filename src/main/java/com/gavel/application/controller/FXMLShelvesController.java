package com.gavel.application.controller;

import com.gavel.ProductShelves;
import com.gavel.application.DataPagination;
import com.gavel.application.MainApp;
import com.gavel.crawler.HtmlPageLoader;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.ShelvesItem;
import com.gavel.entity.ShelvesTask;
import com.gavel.utils.MD5Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

    @FXML
    private Label idField;
    @FXML
    private Label titleField;
    @FXML
    private Label remarkField;

    // 产品SKU列表
    @FXML
    private TableView<ShelvesItem> itemList;
    @FXML
    private TableColumn<ShelvesItem, Integer> xhCol;
    @FXML
    private TableColumn<ShelvesItem, String> codeCol;
    @FXML
    private TableColumn<ShelvesItem, String> cmTitleCol;
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
    private CheckBox curPage;

    @FXML
    private CheckBox allPage;

    @FXML
    private Label selectedNum;

    @FXML
    private Pagination pagination;

    @FXML
    private void initialize() {



        xhCol.setCellValueFactory(new PropertyValueFactory<>("xh"));
        cmTitleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCmTitle()));
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemCode()));
        graingerbrandnameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBrandname()));
        graingercategorynameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryname()));
        brandnameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMappingbrandname()));
        categorynameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMappingcategoryname()));
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

        itemList.setItems(FXCollections.observableArrayList());
        if ( newValue==null ) {
            return;
        }

        idField.setText(newValue.getId());
        titleField.setText(newValue.getTitle());
        remarkField.setText(newValue.getRemark());

        List<ShelvesItem> items = new ArrayList<>();
        try {
            List<ShelvesItem> temp = SQLExecutor.executeQueryBeanList("select * from SHELVESITEM where TASKID = ? ", ShelvesItem.class, newValue.getId());
            if ( temp!=null ) {
                items.addAll(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        DataPagination dataPagination = new DataPagination(items, 30);
        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());
        pagination.setPageFactory(pageIndex -> {
            itemList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return itemList;
        });

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
                    itemList.getItems().add(item);
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

        ObservableList<ShelvesItem> items = itemList.getItems();
        if ( items==null || items.size()==0 ) {
            return;
        }

        for (ShelvesItem shelvesItem : itemList.getItems()) {

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

        EditTask editTask = new EditTask();

        boolean okClicked = shelvesItemEditDialog(editTask);
        if (okClicked) {
            ObservableList<ShelvesItem> items = itemList.getItems();
            if ( items!=null && items.size() > 0 ) {
                for (ShelvesItem item : items) {
                    if ( editTask.getPrefix()!=null && editTask.getPrefix().trim().length()>0 ) {
                        item.setCmTitle( editTask.getPrefix().trim() + item.getCmTitle().trim() );
                    }

                    if ( editTask.getSuffix()!=null && editTask.getSuffix().trim().length()>0 ) {
                        item.setCmTitle(item.getCmTitle().trim() + editTask.getSuffix().trim() );
                    }

                    if ( editTask.getSrc()!=null && editTask.getSrc().trim().length()>0 ) {
                        String src = editTask.getSrc().trim();
                        String desc = editTask.getDest().trim();
                        item.setCmTitle(item.getCmTitle().replace(src, desc));
                    }

                    try {
                        SQLExecutor.update(item);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                itemList.setItems(items);
                itemList.refresh();
            }

        }
    }

    private boolean shelvesItemEditDialog(EditTask editTask) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/ShelvesItemEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("编辑上架商品");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            ShelvesItemEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bindEditTask(editTask);
            //controller.bindItems(shelvesItems);
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 导入采集任务中的SKU
     * @param actionEvent
     */
    public void handleImportSkuPerson(ActionEvent actionEvent) {

        List<ShelvesItem> items = new ArrayList<>();
        boolean okClicked = showShelvesItemImportDialog(items);
        if (okClicked) {

            ShelvesTask taskSelected = taskTable.getSelectionModel().getSelectedItem();
            for (ShelvesItem item : items) {
                //mainApp.getPersonData().add(tempPerson);
                item.setId(MD5Utils.md5Hex(item.getTaskid() + item.getItemCode()));
                item.setTaskid(taskSelected.getId());
                try {
                    SQLExecutor.insert(item);
                    itemList.getItems().add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private boolean showShelvesItemImportDialog(List<ShelvesItem> shelvesItems) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/SkuImportDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("选择上架商品");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLSkuImportController controller = loader.getController();
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

    /**
     * 当前页选择改变
     * @param actionEvent
     */
    public void handleCurPageAction(ActionEvent actionEvent) {
        allPage.setSelected(false);
        if ( curPage.isSelected() ) {
            selectedNum.setText(String.valueOf(itemList.getItems().size()));
        } else {
            selectedNum.setText("0");
        }
    }

    /**
     * 所有页选择改变
     * @param actionEvent
     */
    public void handleALlPageAction(ActionEvent actionEvent) {
        curPage.setSelected(false);
        if ( allPage.isSelected() ) {
            selectedNum.setText(String.valueOf(itemList.getItems().size()));
        } else {
            selectedNum.setText("0");
        }
    }

    public static class EditTask {

        private String prefix;

        private String suffix;

        private String src;

        private String dest;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getDest() {
            return dest;
        }

        public void setDest(String dest) {
            this.dest = dest;
        }
    }
}
