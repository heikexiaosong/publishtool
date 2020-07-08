package com.gavel.application.controller;

import com.gavel.application.DataPagination;
import com.gavel.application.MainApp;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Item;
import com.gavel.entity.ShelvesItem;
import com.gavel.entity.Task;
import com.gavel.shelves.ShelvesItemParser;
import com.gavel.utils.StringUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FXMLSkuImportController {

    @FXML
    private ComboBox<Task> taskid;


    @FXML
    private TableView<Item> skuList;
    @FXML
    private TableColumn<Item, String> picCol;
    @FXML
    private TableColumn<Item, String> codeCol;
    @FXML
    private TableColumn<Item, String> nameCol;
    @FXML
    private TableColumn<Item, String> brandnameCol;
    @FXML
    private TableColumn<Item, String> categorynameCol;

    @FXML
    private Pagination pagination;

    private Stage dialogStage;
    private boolean okClicked = false;

    private List<ShelvesItem> items = new ArrayList<>();

    private List<Item> datas = new ArrayList<>();

    private String  taskId;



    @FXML
    private void initialize() {

        picCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUrl()));
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCode()));
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        brandnameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBrandname()));
        categorynameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryname()));


        initTaskcombox();

        DataPagination dataPagination = new DataPagination(items, 30);
        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());
        pagination.setPageFactory(pageIndex -> {
            skuList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return skuList;
        });

    }

    // 初始化任务列表
    private void initTaskcombox() {
        List<Task> taskList = null;
        try {
            taskList = SQLExecutor.executeQueryBeanList("select * from TASK", Task.class);
        } catch (Exception e) {
            e.printStackTrace();
            taskList = Collections.EMPTY_LIST;
        }
        taskid.setItems(FXCollections.observableArrayList(taskList));


        taskid.setConverter(new StringConverter<Task>(){
            @Override
            public String toString(Task object) {
                return object == null ? null : object.getTitle();
            }
            @Override
            public Task fromString(String string) {
                return taskid.getItems().stream().filter(i -> i.getTitle().equals(string)).findAny().orElse(null);
            }

        });

        taskid.setCellFactory(lv -> new ListCell<Task>() {

            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);

                // use full text in list cell (list popup)
                setText(item == null ? null : item.getTitle());
            }

        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }


    public void bindItems(List<ShelvesItem> _shelvesItems, String  _taskId) {
        items = _shelvesItems;
        this.taskId = _taskId;
        if (StringUtils.isNotBlank(taskId)) {
            load();
        }
    }

    @FXML
    private void handleOk() {

        final  int total = datas.size();

        Service<String> service = new Service<String>() {

            @Override
            protected javafx.concurrent.Task<String> createTask() {
                return new javafx.concurrent.Task<String>() {

                    @Override
                    protected String call() throws Exception {
                        for (int i = 0; i < datas.size(); i++) {
                            Item item = datas.get(i);

                            ShelvesItem shelvesItem = null;


                            try {
                                if ( "JD".equalsIgnoreCase(item.getType())) {
                                    shelvesItem = new ShelvesItem();

                                    shelvesItem.setSkuCode(item.getCode());

                                    shelvesItem.setCategoryCode(item.getCategory());
                                    shelvesItem.setCategoryname(item.getCategoryname());

                                    shelvesItem.setBrandCode(item.getBrand());
                                    shelvesItem.setBrandname(item.getBrandname());

                                    shelvesItem.setItemCode(item.getCode());
                                    shelvesItem.setProductName(item.getName());

                                    shelvesItem.setModel(item.getCode());
                                    //shelvesItem.setDelivery(attrs.get(4).text());
                                    shelvesItem.setCmTitle(item.getName());

                                    shelvesItem.setType(item.getType());
                                    shelvesItem.setSellingPoints(item.getSubname());

                                } else {
                                    shelvesItem = ShelvesItemParser.parse(item);
                                }
                                items.add(shelvesItem);
                                System.out.print("\r[" + i + "/" +  total + "][Item: " + item.getCode() +"]解析成功: ");
                            } catch (Exception e) {
                                System.out.println("\r[" + i + "/" + total + "][Item: " + item.getCode() +"]解析失败: " + e.getMessage());
                            } finally {
                                updateProgress(i, total);
                                updateValue(""+ i +"/" + total);
                            }
                        }
                        return null;
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
            _dialogStage.setTitle("进度");
            _dialogStage.initModality(Modality.WINDOW_MODAL);
            _dialogStage.initOwner(dialogStage);
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

        okClicked = true;
        dialogStage.close();
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public void handleImportAction(ActionEvent actionEvent) {

        datas.clear();
        skuList.setItems(FXCollections.observableArrayList());

        Task task = taskid.getSelectionModel().getSelectedItem();
        if ( task==null ) {
            return;
        }

        taskId = task.getId();

        load();
    }

    private void load(){
        try {
            List<Item> items = SQLExecutor.executeQueryBeanList(" select item.* from item left join searchitem on searchitem.CODE = item.CODE where searchitem.TYPE = 'u' and TASKID = ? ", Item.class,  taskId);
            System.out.println("u");
            if ( items!=null ) {
                datas.addAll(items);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            List<Item> items = SQLExecutor.executeQueryBeanList(" select item.* from item left join searchitem on searchitem.CODE = item.PRODUCTCODE where searchitem.TYPE = 'g' and TASKID = ? ", Item.class,  taskId);
            System.out.println("g");
            if ( items!=null ) {
                datas.addAll(items);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        DataPagination dataPagination = new DataPagination(datas, 30);

        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());

        pagination.setPageFactory(pageIndex -> {
            skuList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return skuList;
        });
    }
}
