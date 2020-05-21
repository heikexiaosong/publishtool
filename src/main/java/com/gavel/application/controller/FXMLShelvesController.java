package com.gavel.application.controller;

import com.gavel.application.DataPagination;
import com.gavel.application.IDCell;
import com.gavel.application.MainApp;
import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.*;
import com.gavel.shelves.CatetoryBrand;
import com.gavel.shelves.ShelvesItemParser;
import com.gavel.shelves.ShelvesService;
import com.gavel.shelves.suning.SuningCatetoryBrandSelector;
import com.gavel.shelves.suning.SuningShelvesService;
import com.gavel.utils.MD5Utils;
import com.gavel.utils.StringUtils;
import com.google.common.io.Files;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private TableColumn<ShelvesTask, Integer> moq;

    @FXML
    private Label idField;
    @FXML
    private Label titleField;
    @FXML
    private Label remarkField;

    @FXML
    private Label pic;

    @FXML
    private Label msg;

    @FXML
    private Label logo;

    @FXML
    private Label logo_msg;

    @FXML
    private Label brand_zh;

    @FXML
    private Label brand_en;

    // 产品SKU列表
    @FXML
    private TableView<ShelvesItem> itemList;
    @FXML
    private TableColumn<ShelvesItem, String> noCol;
    @FXML
    private TableColumn<ShelvesItem, Boolean> select;
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
    private CheckBox curPage;

    @FXML
    private CheckBox allPage;

    @FXML
    private Label selectedNum;

    @FXML
    private Pagination pagination;

    @FXML
    private ComboBox<String> status;

    private List<ShelvesItem> items = new ArrayList<>();

    @FXML
    private void initialize() {

        // 状态选择
        status.setItems(FXCollections.observableArrayList("上架状态", "上架失败", "上架成功"));
        status.getSelectionModel().select(0);
        status.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showShelvesStatusDetails(newValue));

        select.setCellFactory(column -> new CheckBoxTableCell<>());
        select.setCellValueFactory(cellData -> {
            ShelvesItem cellValue = cellData.getValue();
            BooleanProperty property = cellValue.selectedProperty();

            // Add listener to handler change
            //property.addListener((observable, oldValue, newValue) ->  cellValue.selectedProperty().setValue(newValue));

            property.addListener((observable, oldValue, newValue) -> updateSelectStatus(cellValue, newValue));

            return property;
        });

        noCol.setCellFactory(new IDCell<>());

        cmTitleCol.setCellValueFactory(cellData -> cellData.getValue().cmTitleProperty());
        codeCol.setCellValueFactory(cellData -> cellData.getValue().itemCodeProperty());
        graingerbrandnameCol.setCellValueFactory(cellData -> cellData.getValue().brandnameProperty());
        graingercategorynameCol.setCellValueFactory(cellData -> cellData.getValue().categorynameProperty());
        brandnameCol.setCellValueFactory(cellData -> cellData.getValue().mappingbrandnameProperty());
        categorynameCol.setCellValueFactory(cellData -> cellData.getValue().mappingcategorynameProperty());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        msgCol.setCellValueFactory(cellData -> cellData.getValue().messageProperty());

        title.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        skunum.setCellValueFactory(new PropertyValueFactory<>("skunum"));
        moq.setCellValueFactory(new PropertyValueFactory<>("moq"));

        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showShelvesDetails(newValue));

        taskTable.getItems().addAll(loadData());

        showShelvesDetails(null);
    }

    private void updateSelectStatus(ShelvesItem cellValue, Boolean newValue) {
        cellValue.selectedProperty().setValue(newValue);

        int count = 0;

        for (ShelvesItem item : items) {
            if ( item.isSelected() ) {
                count++;
            }
        }
        selectedNum.setText(String.valueOf(count));
    }

    private void showShelvesStatusDetails(String newValue) {

        itemList.setItems(FXCollections.observableArrayList());

        final List<ShelvesItem> filterItems = new ArrayList<>();

        //"上架状态", "上架失败", "上架成功"

        boolean filter = true;
        switch ( newValue.trim() ) {
            case "上架状态":
                filter = false;
                break;
            case "上架失败":
            case "上架成功":
                break;
        }


        for (ShelvesItem item : items) {
            if ( !filter || (newValue!=null && newValue.equalsIgnoreCase(item.getStatus())) ) {
                filterItems.add(item);
            }
        }

        DataPagination dataPagination = new DataPagination(filterItems, 10000);
        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());
        pagination.setPageFactory(pageIndex -> {
            itemList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return itemList;
        });

    }

    private void showShelvesDetails(ShelvesTask newValue) {

        items.clear();
        itemList.setItems(FXCollections.observableList(items));
        if ( newValue!=null ) {

            status.getSelectionModel().select(0);
            allPage.setSelected(false);
            curPage.setSelected(false);
            selectedNum.setText("0");

            idField.setText(newValue.getId());
            titleField.setText(newValue.getTitle());
            remarkField.setText(newValue.getRemark());
            pic.setText(newValue.getPic());


            msg.setText("");
            if (StringUtils.isNotBlank(newValue.getPic())) {
                File picFile = new File(newValue.getPic());
                if (!picFile.exists()) {
                    msg.setText("文件不存在,请检查");
                }
            }


            logo.setText(newValue.getLogo());
            logo_msg.setText("");
            if (StringUtils.isNotBlank(newValue.getLogo())) {
                File picFile = new File(newValue.getLogo());
                if (!picFile.exists()) {
                    logo_msg.setText("文件不存在,请检查");
                }
            }

            brand_zh.setText(newValue.getBrand_zh());
            brand_en.setText(newValue.getBrand_en());

            try {
                List<ShelvesItem> temp = SQLExecutor.executeQueryBeanList("select * from SHELVESITEM where TASKID = ? ", ShelvesItem.class, newValue.getId());
                if ( temp!=null ) {
                    items.addAll(temp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DataPagination dataPagination = new DataPagination(items, 10000);
        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());
        pagination.setPageFactory(pageIndex -> {
            itemList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return itemList;
        });

    }

    private List<ShelvesTask> loadData(){


        Shopinfo shopinfo = APPConfig.getInstance().getShopinfo();

        try {
            List<ShelvesTask> tasks = SQLExecutor.executeQueryBeanList("select * from SHELVESTASK where SHOPID = ? ", ShelvesTask.class, (shopinfo==null ? "" : shopinfo.getCode()));
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
            tempShelvesTask.setShopid(APPConfig.getInstance().getShopinfo().getCode());
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

    /**
     * 添加 SKU 商品
     *
     * @param actionEvent
     */
    public void handleAddSkuPerson(ActionEvent actionEvent) {
        List<ShelvesItem> items = new ArrayList<>();
        boolean okClicked = showShelvesItemEditDialog(items);
        if (okClicked) {

            SuningCatetoryBrandSelector catetoryBrandSelector = new SuningCatetoryBrandSelector();

            ShelvesTask taskSelected = taskTable.getSelectionModel().getSelectedItem();
            for (ShelvesItem item : items) {
                item.setTaskid(taskSelected.getId());
                item.setId(MD5Utils.md5Hex(item.getTaskid() + item.getSkuCode()));


                CatetoryBrand catetoryBrand = catetoryBrandSelector.selectCatetoryBrand(item.getCategoryCode(), item.getBrandCode());
                if ( catetoryBrand!=null ) {
                    item.setMappingbrandcode(catetoryBrand.getBrandCode());
                    item.setMappingbrandname(catetoryBrand.getBrandZh());
                    item.setMappingcategorycode(catetoryBrand.getCategoryCode());
                    item.setMappingcategoryname(catetoryBrand.getCategory());
                }

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

    /**
     * 上架
     * @param actionEvent
     */
    public void handleShelvesAction(ActionEvent actionEvent) {
        // TODO 上架

        ObservableList<ShelvesItem> items = itemList.getItems();
        if ( items==null || items.size()==0 ) {
            return;
        }

        ShelvesTask shelvesTask = taskTable.getSelectionModel().getSelectedItem();
        try {
            shelvesTask =  SQLExecutor.executeQueryBean("select * from SHELVESTASK where ID = ? ", ShelvesTask.class, items.get(0).getTaskid());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (StringUtils.isNotBlank(shelvesTask.getLogo())) {
            File logoFile = new File(shelvesTask.getLogo());
            if (!logoFile.exists()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", new ButtonType("取消", ButtonBar.ButtonData.NO), new ButtonType("确定", ButtonBar.ButtonData.YES));
                alert.initOwner(stage());
                alert.setTitle("信息确认");
                alert.setHeaderText("设置了水印图片, 但是图片文件不存在, 是否继续上架?");

                Optional<ButtonType> _buttonType = alert.showAndWait();

                if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.NO)){
                   return;
                }
            }
        }

        ShelvesService shelvesService = new SuningShelvesService(shelvesTask.getMoq(), shelvesTask.getPic(), shelvesTask.getLogo());
        new Thread(new Runnable() {
            @Override
            public void run() {

                for (final ShelvesItem shelvesItem : itemList.getItems()) {

                    if ( !shelvesItem.isSelected() || "上架成功".equalsIgnoreCase(shelvesItem.getStatus())) {
                        continue;
                    }

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            itemList.getSelectionModel().select(shelvesItem);
                        }
                    });

                    shelvesItem.setStatus("正在上架...");
                    shelvesItem.setMessage("");
                    try {
                        shelvesService.shelves(shelvesItem);
                        shelvesItem.setStatus("上架成功");
                    } catch (Exception e){
                        shelvesItem.setStatus("上架失败");
                        shelvesItem.setMessage(e.getMessage());
                    }

                    try {
                        SQLExecutor.update(shelvesItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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
                    if ( !item.isSelected() ) {
                        continue;
                    }
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

                    if ( editTask.getCodePrefix()!=null && editTask.getCodePrefix().trim().length()>0 ) {
                        item.setItemCode( editTask.getCodePrefix().trim() + item.getItemCode().trim() );
                    }

                    if ( editTask.getCodeSuffix()!=null && editTask.getCodeSuffix().trim().length()>0 ) {
                        item.setItemCode(item.getItemCode().trim() + editTask.getCodeSuffix().trim() );
                    }

                    if ( editTask.getCategory()!=null ) {
                        item.setMappingcategorycode(editTask.getCategory().getCategoryCode());
                        item.setMappingcategoryname(editTask.getCategory().getCategoryName());
                    }

                    if ( editTask.getBrand()!=null ) {
                        item.setMappingbrandcode(editTask.getBrand().getCode());
                        item.setMappingbrandname(editTask.getBrand().getName());
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

            SuningCatetoryBrandSelector catetoryBrandSelector = new SuningCatetoryBrandSelector();

            ShelvesTask taskSelected = taskTable.getSelectionModel().getSelectedItem();
            System.out.println("SKU: " + items.size());


            int total = items.size();
            for (int i = 0; i < items.size(); i++) {
                ShelvesItem item = items.get(i);

                item.setTaskid(taskSelected.getId());
                item.setId(MD5Utils.md5Hex(item.getTaskid() + item.getItemCode()));

                CatetoryBrand catetoryBrand = catetoryBrandSelector.selectCatetoryBrand(item.getCategoryCode(), item.getBrandCode());
                if ( catetoryBrand!=null ) {
                    item.setMappingbrandcode(catetoryBrand.getBrandCode());
                    item.setMappingbrandname(catetoryBrand.getBrandZh());
                    item.setMappingcategorycode(catetoryBrand.getCategoryCode());
                    item.setMappingcategoryname(catetoryBrand.getCategory());
                }

                try {
                    SQLExecutor.insert(item);
                    itemList.getItems().add(item);
                    System.out.print("\r[" + i + "/" +  total + "][Item: " + item.getSkuCode() +"]导入成功: ");
                } catch (Exception e) {
                    System.out.println("\r[" + i + "/" +  total + "][Item: " + item.getSkuCode() +"]导入失败: " + e.getMessage() );
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

        for (ShelvesItem shelvesItem : items) {
            shelvesItem.selectedProperty().setValue(allPage.isSelected());
        }

        for (ShelvesItem shelvesItem : itemList.getItems()) {
            shelvesItem.selectedProperty().setValue(curPage.isSelected());
        }

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

        for (ShelvesItem shelvesItem : items) {
            shelvesItem.selectedProperty().setValue(allPage.isSelected());
        }

        if ( allPage.isSelected() ) {
            selectedNum.setText(String.valueOf(items.size()));
        } else {
            selectedNum.setText("0");
        }

        itemList.refresh();
    }

    /**
     * 编 辑
     *
     * @param actionEvent
     */
    public void handleDetailEditAction(ActionEvent actionEvent) {

        ShelvesItem selectedItem = itemList.getSelectionModel().getSelectedItem();

        if ( selectedItem==null ) {
            Alert _alert = new Alert(Alert.AlertType.INFORMATION);
            _alert.setTitle("信息");
            _alert.setHeaderText("请先选择需要编辑的商品");
            _alert.initOwner(stage());
            _alert.show();
            return;
        }


        boolean okClicked = shelvesItemDetailEditDialog(selectedItem);
        if (okClicked) {
            try {
                SQLExecutor.update(selectedItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
            itemList.refresh();
        }
    }

    private boolean shelvesItemDetailEditDialog(ShelvesItem selectedItem) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/ShelvesItemDetailEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("上架商品信息");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            ShelvesItemDetailEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bind(selectedItem);
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
     * 上架任务编辑
     * @param actionEvent
     */
    public void handleEditTask(ActionEvent actionEvent) {
        ShelvesTask tempShelvesTask = taskTable.getSelectionModel().getSelectedItem();
        boolean okClicked = showShelvesTaskEditDialog(tempShelvesTask);
        if (okClicked) {
            //mainApp.getPersonData().add(tempPerson);
            try {
                SQLExecutor.update(tempShelvesTask);
                taskTable.refresh();
                showShelvesDetails(taskTable.getSelectionModel().getSelectedItem());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handleImagesExportAction(ActionEvent actionEvent) {


       final  String taskId =  idField.getText();

        final  int total = items.size();

        Service<String> service = new Service<String>() {

            @Override
            protected javafx.concurrent.Task<String> createTask() {
                return new javafx.concurrent.Task<String>() {

                    @Override
                    protected String call() throws Exception {

                        for (int i = 0; i < items.size(); i++) {
                            ShelvesItem item = items.get(i);
                            try {
                                List<String> images = ShelvesItemParser.getProductImages(item.getSkuCode());
                                if ( images!=null && images.size() > 0 ) {
                                    for (String s : images) {
                                        System.out.println( " ==> " + s);

                                        try {
                                            File dest = new File(s.replace("D:\\images", "D:\\images" + File.separator + taskId + File.separator + item.getSkuCode() ));
                                            if ( !dest.getParentFile().exists() ) {
                                                dest.getParentFile().mkdirs();
                                            }
                                            Files.copy(new File(s), dest);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                updateProgress(i, total);
                                updateValue(""+ i +"/" + total);
                            }
                        }

                        updateValue("图片导出完成");
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
            _dialogStage.setTitle("图片导出进度");
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

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage());
        alert.setTitle("图片导出完成");
        alert.setHeaderText("图片导出目录: " + "D:\\images\\" + taskId);
        alert.showAndWait();
    }


    /**
     * 导入其他店铺的上架任务
     * @param actionEvent
     */
    public void handleImportShelvesTask(ActionEvent actionEvent) {
        List<ShelvesItem> items = new ArrayList<>();
        boolean okClicked = showShelvesTaskImportDialog(items);
        System.out.println("okClicked: " + okClicked);
        if (okClicked) {
            //mainApp.getPersonData().add(tempPerson);
            //tempShelvesTask.setShopid( APPConfig.getInstance().getShopinfo().getCode());

            ShelvesTask taskSelected = taskTable.getSelectionModel().getSelectedItem();

            for (ShelvesItem item : items) {

                item.setSelected(false);
                item.setTaskid(taskSelected.getId());
                item.setId(MD5Utils.md5Hex(item.getTaskid() + item.getSkuCode()));


                item.setMappingcategorycode(null);
                item.setMappingcategoryname(null);
                item.setMappingbrandname(null);
                item.setMappingbrandcode(null);
                item.setStatus("");
                try {
                    SQLExecutor.insert(item);
                    //taskTable.getItems().add(tempShelvesTask);
                    itemList.getItems().add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private boolean showShelvesTaskImportDialog(List<ShelvesItem> shelvesTasks) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/ShelvesTaskImportDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("上架任务导入");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLShelvesTaskImportDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bind(shelvesTasks);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class EditTask {

        private String prefix;

        private String suffix;

        private String src;

        private String dest;

        private String codePrefix;

        private String codeSuffix;

        private Category category;

        private Brand brand;

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

        public String getCodePrefix() {
            return codePrefix;
        }

        public void setCodePrefix(String codePrefix) {
            this.codePrefix = codePrefix;
        }

        public String getCodeSuffix() {
            return codeSuffix;
        }

        public void setCodeSuffix(String codeSuffix) {
            this.codeSuffix = codeSuffix;
        }

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }

        public Brand getBrand() {
            return brand;
        }

        public void setBrand(Brand brand) {
            this.brand = brand;
        }
    }
}
