package com.gavel.application.controller;

import com.gavel.HttpUtils;
import com.gavel.application.DataPagination;
import com.gavel.application.IDCell;
import com.gavel.application.MainApp;
import com.gavel.config.APPConfig;
import com.gavel.crawler.HtmlPageLoader;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.*;
import com.gavel.jd.SkuPageLoader;
import com.gavel.shelves.CatetoryBrand;
import com.gavel.shelves.ShelvesItemParser;
import com.gavel.shelves.ShelvesService;
import com.gavel.shelves.suning.SuningCatetoryBrandSelector;
import com.gavel.shelves.suning.SuningShelvesService;
import com.gavel.utils.MD5Utils;
import com.gavel.utils.StringUtils;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private Label picdirField;

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

    @FXML
    private Label moqLabel;

    @FXML
    private TextField keyword;

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
    private TableColumn<ShelvesItem, String> cmTitleLenCol;
    @FXML
    private TableColumn<ShelvesItem, String> deliveryCol;
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
    private TableColumn<ShelvesItem, String> priceCol;
    @FXML
    private TableColumn<ShelvesItem, String> ownCol;
    @FXML
    private TableColumn<ShelvesItem, String> shopCol;
    @FXML
    private TableColumn<ShelvesItem, String> stockCol;

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

    @FXML
    private ComboBox<String> brand;

    private List<ShelvesItem> items = new ArrayList<>();

    @FXML
    private void initialize() {

        // 状态选择
        status.setItems(FXCollections.observableArrayList("上架状态", "上架失败", "上架成功"));
        status.getSelectionModel().select(0);
        status.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showShelvesStatusDetails());

        select.setCellFactory(column -> new CheckBoxTableCell<>());
        select.setCellValueFactory(cellData -> {
            ShelvesItem cellValue = cellData.getValue();
            BooleanProperty property = cellValue.selectedProperty();

            // Add listener to handler change
            //property.addListener((observable, oldValue, newValue) ->  cellValue.selectedProperty().setValue(newValue));

            property.addListener((observable, oldValue, newValue) -> updateSelectStatus(cellValue, newValue));

            return property;
        });

        brand.setItems(FXCollections.observableArrayList("所有品牌"));
        brand.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showShelvesStatusDetails());
        brand.getSelectionModel().select(0);

        noCol.setCellFactory(new IDCell<>());
        deliveryCol.setCellValueFactory(cellData -> cellData.getValue().deliveryProperty());
        cmTitleCol.setCellValueFactory(cellData -> cellData.getValue().cmTitleProperty());
        cmTitleLenCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCmTitle().length())));
        codeCol.setCellValueFactory(cellData -> cellData.getValue().itemCodeProperty());
        graingerbrandnameCol.setCellValueFactory(cellData -> cellData.getValue().brandnameProperty());
        graingercategorynameCol.setCellValueFactory(cellData -> cellData.getValue().categorynameProperty());
        brandnameCol.setCellValueFactory(cellData -> cellData.getValue().mappingbrandnameProperty());
        categorynameCol.setCellValueFactory(cellData -> cellData.getValue().mappingcategorynameProperty());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        msgCol.setCellValueFactory(cellData -> cellData.getValue().messageProperty());

        priceCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPrice())));
        ownCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOwn()));
        shopCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getShop()));
        stockCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStock()));


        title.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        skunum.setCellValueFactory(new PropertyValueFactory<>("skunum"));
        moq.setCellValueFactory(new PropertyValueFactory<>("moq"));

        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showShelvesDetails(newValue));

        taskTable.getItems().addAll(loadData(null));

        showShelvesDetails(null);

        if (taskTable.getItems().size() > 0 ) {
            taskTable.getSelectionModel().select(0);
        }
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

    private void showShelvesStatusDetails() {

        String _status = status.getSelectionModel().getSelectedItem();
        String _brand = brand.getSelectionModel().getSelectedItem();

        itemList.setItems(FXCollections.observableArrayList());

        final List<ShelvesItem> filterItems = new ArrayList<>();

        //"上架状态", "上架失败", "上架成功"


        boolean statusFilter = !( _status==null || "上架状态".equalsIgnoreCase(_status.trim()) );

        boolean brandFilter = !( _brand==null || "所有品牌".equalsIgnoreCase(_brand.trim()) );


        System.out.println(_status + statusFilter + " -- " + _brand + brandFilter);

        for (ShelvesItem item : items) {
            if (  ( !statusFilter ||  _status.equalsIgnoreCase(item.getStatus()))
                    &&  ( !brandFilter || _brand.equalsIgnoreCase(item.getBrandname()) ) ) {
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


        long start = System.currentTimeMillis();

        items.clear();
        itemList.setItems(FXCollections.observableArrayList());
        if ( newValue!=null ) {

            status.getSelectionModel().select(0);
            allPage.setSelected(false);
            curPage.setSelected(false);
            selectedNum.setText("0");

            idField.setText(newValue.getId());
            titleField.setText(newValue.getTitle());
            remarkField.setText(newValue.getRemark());
            pic.setText(newValue.getPic());

            picdirField.setText(newValue.getPicdir());
            moqLabel.setText(String.valueOf(newValue.getMoq()));


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

            System.out.println("Cost: " + ( System.currentTimeMillis()  - start) + " ms");
        }

        brand.setItems(FXCollections.observableArrayList());
        Set<String> brands = items.stream().map(e -> e.getBrandname() ).collect(Collectors.toSet());
        brand.getItems().add("所有品牌");
        brand.getItems().addAll(brands);
        brand.getSelectionModel().select(0);

        System.out.println("Cost: " + ( System.currentTimeMillis()  - start) + " ms");

        DataPagination dataPagination = new DataPagination(items, 10000);
        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());
        pagination.setPageFactory(pageIndex -> {
            itemList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return itemList;
        });

        System.out.println("Cost: " + ( System.currentTimeMillis()  - start) + " ms");

    }

    private List<ShelvesTask> loadData(String _keyword){


        Shopinfo shopinfo = APPConfig.getInstance().getShopinfo();

        if ( _keyword==null || _keyword.trim().length()==0 ) {
            try {
                List<ShelvesTask> tasks = SQLExecutor.executeQueryBeanList("select * from SHELVESTASK where SHOPID = ? order by ID desc ", ShelvesTask.class, (shopinfo==null ? "" : shopinfo.getCode()));
                return tasks;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                List<ShelvesTask> tasks = SQLExecutor.executeQueryBeanList("select * from SHELVESTASK where TITLE like ? and SHOPID = ? order by ID desc ", ShelvesTask.class,  "%" + _keyword.trim() + "%", (shopinfo==null ? "" : shopinfo.getCode()));
                return tasks;
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                String path = "D:\\pics\\" + tempShelvesTask.getId() + "_" + tempShelvesTask.getMoq();
                File dir = new File(path);
                if ( !dir.exists() ) {
                    dir.mkdirs();
                }
                tempShelvesTask.setPicdir(path);

                SQLExecutor.insert(tempShelvesTask);
                taskTable.getItems().add(0, tempShelvesTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void handleDeletePerson(ActionEvent actionEvent) {

        ShelvesTask selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if ( selectedTask==null ) {
            return;
        }


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", new ButtonType("取消", ButtonBar.ButtonData.NO), new ButtonType("确定", ButtonBar.ButtonData.YES));
        alert.initOwner(stage());
        alert.setTitle("删除确认");
        alert.setHeaderText("确定删除上架任务[" + selectedTask.getTitle() +"]?");

        Optional<ButtonType> _buttonType = alert.showAndWait();

        if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES)){

            try {
                SQLExecutor.delete(selectedTask);
                SQLExecutor.execute("delete from SHELVESITEM where TASKID = ?", selectedTask.getId());
                taskTable.getItems().remove(selectedTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

        ShelvesService shelvesService = new SuningShelvesService(shelvesTask.getMoq(), shelvesTask.getPic(), shelvesTask.getLogo(), shelvesTask.getPicdir(), shelvesTask.getBrand_zh(), shelvesTask.getBrand_en());
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
                        shelvesItem.setSelected(false);
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
                        if ( editTask.isCkALl() ) {
                            if ( item.getSellingPoints()!=null ) {
                                item.setSellingPoints(item.getSellingPoints().replace(src, desc));
                            }
                            item.setSrc(editTask.getSrc());
                            item.setDest(editTask.getDest());
                        }
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
        List<Task> importTasks = new ArrayList<>();
        boolean okClicked = showTaskSelectedDialog(importTasks);
        if ( okClicked && importTasks.size()>0 ) {
            List<ShelvesItem> items = new ArrayList<>();
            okClicked = showShelvesItemImportDialog(items, importTasks.get(0));
            if (okClicked) {

                final SuningCatetoryBrandSelector catetoryBrandSelector = new SuningCatetoryBrandSelector();

                ShelvesTask taskSelected = taskTable.getSelectionModel().getSelectedItem();
                System.out.println("SKU: " + items.size());

                final String picdir = taskSelected.getPicdir();
                File dir = new File(picdir);
                if ( !dir.exists() ) {
                    dir.mkdirs();
                }

                Service<String> service = new Service<String>() {

                    @Override
                    protected javafx.concurrent.Task<String> createTask() {
                        return new javafx.concurrent.Task<String>() {

                            @Override
                            protected String call() throws Exception {


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
                                        System.out.print("\r[" + i + "/" +  total + "][Item: " + item.getSkuCode() +"]导入成功: ");
                                    } catch (Exception e) {
                                        System.out.println("\r[" + i + "/" +  total + "][Item: " + item.getSkuCode() +"]导入失败: " + e.getMessage() );
                                    }  finally {
                                        updateProgress(i, total);
                                        updateValue(""+ i +"/" + total);
                                    }
                                }


                                itemList.setItems(FXCollections.observableArrayList(items));

                                updateProgress(total, total);

                                return "导入完成";
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
    }

    private boolean showTaskSelectedDialog(List<Task> importTasks) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/TaskSelectedDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("选择上架商品");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLTaskSelectedController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bindItems(importTasks);
            controller.setSingleSelected(true);
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean showShelvesItemImportDialog(List<ShelvesItem> shelvesItems, Task task) {
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
            controller.bindItems(shelvesItems, task.getId());
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

    /**
     * 品牌类目映射
     * @param actionEvent
     */
    public void handlBrandCateMappingAction(ActionEvent actionEvent) {

        String shopid = APPConfig.getInstance().getShopinfo().getCode();


        Map<String, CateBrandMapping> existMap = new HashMap<>();
        try {
            List<CateBrandMapping> exist = SQLExecutor.executeQueryBeanList("select * from BRAND_CATE_MAPPING where SHOPID = ? ", CateBrandMapping.class, shopid);
            existMap = exist.stream().collect(Collectors.toMap(CateBrandMapping::getId, e -> e));
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<CateBrandMapping> cateBrandMappings = new ArrayList<>();
        Map<String, CateBrandMapping> cateBrandMappingMap = new HashMap<>();
        for (ShelvesItem item : items) {
            CateBrandMapping cateBrandMapping = null;
            String key = shopid + "_" +item.getBrandCode() + "_" + item.getCategoryCode();
            if ( cateBrandMappingMap.containsKey(key) ) {
                cateBrandMapping = cateBrandMappingMap.get(key);
                cateBrandMapping.getCount().incrementAndGet();
            }

            if ( cateBrandMapping==null && existMap.containsKey(key) ) {
                cateBrandMapping = existMap.get(key);
            }

            if ( cateBrandMapping==null ) {
                cateBrandMapping = new CateBrandMapping();
                cateBrandMapping.setId(key);
                cateBrandMapping.setShopid(APPConfig.getInstance().getShopinfo().getCode());
                cateBrandMapping.setBrandcode(item.getBrandCode());
                cateBrandMapping.setBrandname(item.getBrandname());
                cateBrandMapping.setCatecode(item.getCategoryCode());
                cateBrandMapping.setCatename(item.getCategoryname());

                try {
                    SQLExecutor.insert(cateBrandMapping);
                } catch (Exception e) {

                }
            }

            if ( StringUtils.isBlank(cateBrandMapping.getSbrandcode()) &&  StringUtils.isNotBlank(item.getMappingbrandcode()) ) {
                cateBrandMapping.setSbrandcode(item.getMappingbrandcode());
                cateBrandMapping.setSbrandname(item.getMappingbrandname());
            }

            cateBrandMappingMap.put(key, cateBrandMapping);
        }

        cateBrandMappings.addAll(cateBrandMappingMap.values());
//
        boolean okClicked = shelvesItemMappingEditDialog(shopid, cateBrandMappings);
        if (okClicked) {
            if ( cateBrandMappings.size() > 0 ) {
                Map<String, CateBrandMapping> map = new HashMap<>();
                for (CateBrandMapping cateBrandMapping : cateBrandMappings) {
                    if ( StringUtils.isBlank(cateBrandMapping.getCategorycode()) && StringUtils.isBlank(cateBrandMapping.getSbrandcode()) ) {
                        continue;
                    }
                    try {
                        SQLExecutor.update(cateBrandMapping);
                        String key = cateBrandMapping.getBrandcode() + "_" + cateBrandMapping.getCatecode();
                        map.put(key,cateBrandMapping);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (ShelvesItem item : items) {
                    String key = item.getBrandCode() + "_" + item.getCategoryCode();
                    if ( map.containsKey(key) && map.get(key)!=null ) {
                        CateBrandMapping match = map.get(key);
                        item.setMappingcategorycode(match.getCategorycode());
                        item.setMappingcategoryname(match.getCategoryname());
                        if (StringUtils.isNotBlank(match.getSbrandcode()) ) {
                            item.setMappingbrandcode(match.getSbrandcode());
                            item.setMappingbrandname(match.getSbrandname());
                            try {
                                SQLExecutor.update(item);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

    }

    private boolean shelvesItemMappingEditDialog(String shopid, List<CateBrandMapping> cateBrandMappings) {


        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/SunningCateBrandMappingDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("品牌类目映射");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLSunningCateBrandMappingController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bind(shopid, cateBrandMappings);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 导出价格数据
     * @param actionEvent
     */
    public void handleExportAction(ActionEvent actionEvent) {

        final  int total = items.size();

        Service<String> service = new Service<String>() {

            @Override
            protected javafx.concurrent.Task<String> createTask() {
                return new javafx.concurrent.Task<String>() {

                    @Override
                    protected String call() throws Exception {

                        File file = new File("prices", titleField.getText().trim() + ".csv");

                        BufferedWriter writer = Files.newWriter(file, Charset.forName("GB2312"));

                        writer.write("一级类目ID,一级类目,二级类目ID,二级类目,三级类目ID,三级类目,四级类目ID,四级类目,五级类目ID,五级类目,SKU编码,产品标题,制造商型号,中文品牌,英文品牌,价格,促销价,URL ");
                        writer.newLine();



                        Map<String, JsonObject> pricesMap = new HashMap<>();

                        if ( items!=null && items.size() > 0 ) {

                            StringBuilder query = new StringBuilder("https://p.3.cn/prices/mgets?type=1&area=12_988_40034_0&pdbp=0&pdtk=&pdpin=hailinking1984&pin=hailinking1984&source=list_pc_front&skuIds=");

                            int cnt = 1;
                            StringBuilder skuids = new StringBuilder();


                            for (int i1 = 0; i1 < items.size(); i1++) {
                                ShelvesItem searchItem = items.get(i1);
                                skuids.append("J_").append(searchItem.getSkuCode()).append("%2C");
                                if ( cnt++ >= 30 || i1==items.size()-1 ) {
                                    cnt = 1;

                                    System.out.println(query.toString() + skuids.toString());

                                    String text = HttpUtils.get(query.toString() + skuids.toString(), "");
                                    JsonArray arrays = new JsonParser().parse(text).getAsJsonArray();
                                    if ( arrays!=null && arrays.size() > 0 ) {
                                        for (JsonElement array : arrays) {
                                            JsonObject object = (JsonObject)array;
                                            pricesMap.put(object.get("id").toString().replace("\"", ""), object);
                                        }
                                    }
                                }
                            }
                        }

                        for (int i = 1; i <= items.size(); i++) {
                            ShelvesItem item = items.get(i-1);


                            if ( "JD".equalsIgnoreCase(item.getType()) ) {


                                HtmlCache cache = HtmlPageLoader.getInstance().loadJDPage(item.getSkuCode(), true);
                                if ( cache == null ) {
                                    cache = HtmlPageLoader.getInstance().loadJDPage(item.getSkuCode(), false);
                                }
                                if ( cache == null ) {
                                    throw new Exception("[SKU: " + item.getSkuCode() + "]htmlCache未找到");
                                }
                                try {

                                    Document doc = Jsoup.parse(cache.getHtml());

                                    Element crumb = doc.selectFirst("div#crumb-wrap .crumb");
                                    if ( crumb==null ) {
                                        throw new Exception("[" + item.getSkuCode() + "]Html内容有异常");
                                    }


                                    String priceStr = Float.toString(item.getPrice());
                                    String hxPriceStr = Float.toString(item.getPrice());
                                    Element price = doc.selectFirst("span.p-price .price");
                                    if ( price!=null ) {
                                        priceStr = price.text();
                                        System.out.println(price.text());
                                    }

                                    Element page_hx_price = doc.selectFirst("del#page_hx_price");
                                    if ( page_hx_price!=null ) {
                                        hxPriceStr = page_hx_price.text().replace(",", "").replace("￥", "");
                                        System.out.println(hxPriceStr);
                                    }


                                    JsonObject priceObj = pricesMap.get("J_" + item.getSkuCode());
                                    if ( priceObj!=null && priceObj.has("op") ) {
                                        hxPriceStr = priceObj.get("op").toString();
                                    }
                                    if ( priceObj!=null && priceObj.has("p") ) {
                                        priceStr = priceObj.get("p").toString();
                                    }

                                    crumb.select("div.sep").remove();

                                    Elements elements = crumb.select("div.item");


                                    // 4级类目 + 产品组ID + ID
                                    Element c1 = elements.get(0);
                                    Element c2 = elements.get(1);
                                    Element c3 = elements.get(2);
                                    Element c4 = elements.get(3);
                                    Element c5 = elements.get(4);

                                    writer.write(escape(c1.text()) + ",");
                                    writer.write(escape(c1.text()) + ",");
                                    writer.write(escape(c2.text()) + ",");
                                    writer.write(escape(c2.text()) + ",");
                                    writer.write(escape(c3.text()) + ",");
                                    writer.write(escape(c3.text()) + ",");
                                    writer.write(escape(c4.text()) + ",");
                                    writer.write(escape(c4.text()) + ",");
                                    writer.write(escape(c5.text()) + ",");
                                    writer.write(escape(c5.text()) + ",");
                                    writer.write( item.getSkuCode() + ",");
                                    writer.write(item.getCmTitle() + ",");



                                    // 标题前 品牌
                                    String model = item.getSkuCode();
                                    Elements parameters = doc.select("ul.p-parameter-list li");
                                    for (Element parameter : parameters) {
                                        if ( parameter.text().contains("货号：") ) {
                                            model = parameter.attr("title");
                                            if ( model==null && model.trim().length()==0 ) {
                                                model = parameter.text().replace("货号：", "");
                                            }
                                        }
                                    }





                                    writer.write(escape(model) + ",");

                                    writer.write(escape(c4.text()) + "," + escape(c4.text()) + ",");

                                    writer.write(hxPriceStr + ", " + priceStr + ",");

                                    writer.write(cache.getUrl());
                                    writer.newLine();

                                    writer.flush();
                                    System.out.print("\r[" + i + "/" +  total + "][Item: " + item.getSkuCode() +"]解析成功: ");
                                } catch (Exception e) {
                                    System.out.println("\r[" + i + "/" + total + "][Item: " + item.getSkuCode() +"]解析失败: " + e.getMessage());
                                } finally {
                                    updateProgress(i, total);
                                    updateValue("["+ i +"/" + total  + "]导出文件: "  + file.getAbsolutePath());

                                }
                            } else {

                                HtmlCache cache = HtmlPageLoader.getInstance().loadGraingerPage(item.getSkuCode(), true);
                                if ( cache == null ) {
                                    throw new Exception("[SKU: " + item.getSkuCode() + "]htmlCache未找到");
                                }
                                try {

                                    Document doc = Jsoup.parse(cache.getHtml());

                                    Element err = doc.selectFirst("div.err-notice");
                                    if ( err!=null ) {
                                        throw new Exception("[" + cache.getUrl() + "]页面未找到");
                                    }

                                    // 品牌 + 标题
                                    Element proDetailCon = doc.selectFirst("div.proDetailCon");
                                    if ( proDetailCon==null ) {
                                        throw new Exception("[" + cache.getUrl() + "]Html内容有异常: " + doc.title());
                                    }


                                    // 4级类目 + 产品组ID + ID
                                    Elements elements = doc.select("div.crumbs  a");
                                    Element c1 = elements.get(1);
                                    Element c2 = elements.get(2);
                                    Element c3 = elements.get(3);
                                    Element c4 = elements.get(4);
                                    Element c5 = elements.get(5);
                                    Element c6 = elements.get(6);


                                    writer.write(com.gavel.grainger.StringUtils.getCode(c1.attr("href")) + ",");
                                    writer.write(escape(c1.text()) + ",");
                                    writer.write(com.gavel.grainger.StringUtils.getCode(c2.attr("href")) + ",");
                                    writer.write(escape(c2.text()) + ",");
                                    writer.write(com.gavel.grainger.StringUtils.getCode(c3.attr("href")) + ",");
                                    writer.write(escape(c3.text()) + ",");
                                    writer.write(com.gavel.grainger.StringUtils.getCode(c4.attr("href")) + ",");
                                    writer.write(escape(c4.text()) + ",");
                                    writer.write(com.gavel.grainger.StringUtils.getCode(c5.attr("href")) + ",");
                                    writer.write(escape(c5.text()) + ",");
                                    writer.write(com.gavel.grainger.StringUtils.getCode(c6.attr("href")) + ",");
                                    writer.write(escape(item.getCmTitle()) + ",");



                                    // 标题前 品牌
                                    String brand1 =  proDetailCon.selectFirst("h3 > span > a").html();

                                    Element price = doc.selectFirst("div.price");
                                    price.remove();


                                    Elements fonts = proDetailCon.select("div font");
                                    String brand = fonts.get(1).text();
                                    String model = fonts.get(2).text();



                                    writer.write(escape(model) + ",");

                                    /**
                                     * 订 货 号：5W8061
                                     * 品   牌：霍尼韦尔 Honeywell
                                     * 制造商型号： SHSL00202-42
                                     * 包装内件数：1双
                                     * 预计发货日： 停止销售
                                     *

                                     String code = fonts.get(0).text();
                                     String brand = fonts.get(1).text();
                                     String model = fonts.get(2).text();
                                     String number = fonts.get(3).text();
                                     String fahuori = fonts.get(4).text();
                                     */

                                    if ( brand1.trim().equalsIgnoreCase(brand.trim()) ) {
                                        writer.write(escape(brand1.trim()) + ",");
                                        writer.write(escape(brand.trim()) + ",");
                                    } else {
                                        writer.write(escape(brand1.trim()) + ",");
                                        writer.write(escape(brand.replace(brand1, "").trim()) + ",");
                                    }


                                    Elements prices = price.select("b");
                                    if ( prices.size()==1 ) {
                                        writer.write(prices.get(0).text().replace(",", "").replace("¥", "").trim() + ", ,");

                                    } else  if ( prices.size()==2 )  {
                                        writer.write(prices.get(0).text().replace(",", "").replace("¥", "").trim() + ", " + prices.get(1).text().replace(",", "").replace("¥", "").trim() + ",");
                                    }

                                    writer.write(cache.getUrl());
                                    writer.newLine();

                                    writer.flush();
                                    System.out.print("\r[" + i + "/" +  total + "][Item: " + item.getSkuCode() +"]解析成功: ");
                                } catch (Exception e) {
                                    System.out.println("\r[" + i + "/" + total + "][Item: " + item.getSkuCode() +"]解析失败: " + e.getMessage());
                                } finally {
                                    updateProgress(i, total);
                                    updateValue("["+ i +"/" + total  + "]导出文件: "  + file.getAbsolutePath());

                                }

                            }


                        }
                        writer.close();

                        return "导出文件: "  + file.getAbsolutePath();
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

    private static String escape(String text) {
        String res = text;
        if ( text!=null && text.contains(",") ) {
            res = "\"" + text + "\"";
        }

        return res;
    }

    public void onCliccked(MouseEvent mouseEvent) {

        String picdir = picdirField.getText();
        System.out.println("picdir: " + picdir);
        if ( picdir==null || picdir.trim().length()==0 ) {
            return;
        }

        File dir = new File(picdir);
        if ( !dir.exists() ) {
            return;
        }

        try {
            Desktop.getDesktop().browse(new File(picdir).toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handleTaskSeach(ActionEvent actionEvent) {

        taskTable.setItems(FXCollections.observableArrayList(loadData(keyword.getText())));

        showShelvesDetails(null);
        if (taskTable.getItems().size() > 0 ) {
            taskTable.getSelectionModel().select(0);
        }


    }

    /**
     * 上架商品预处理
     * @param actionEvent
     */
    public void handleShelvesPreAction(ActionEvent actionEvent) {

        Service<String> service = new Service<String>() {

            @Override
            protected javafx.concurrent.Task<String> createTask() {
                return new javafx.concurrent.Task<String>() {

                    @Override
                    protected String call() throws Exception {
                        List<ShelvesItem> pending =  items.stream().filter( ShelvesItem::isSelected  ).collect(Collectors.toList());

                        if ( pending==null || pending.size()==0 ) {
                            updateProgress(1, 1);
                            return "预处理完成";
                        }

                        final  int total = pending.size();
                        final String picDir = picdirField.getText();
                        File dir = new File(picDir);
                        if ( !dir.exists() ) {
                            dir.mkdirs();
                        }

                        final Pattern DETAIL_IMAGE = Pattern.compile("background-image:url\\(([^)]*)");
                        final Pattern PIC_IMAGE = Pattern.compile("360buyimg.com/n(\\d*)/");
                        final Pattern PIC_IMAGE_JFS = Pattern.compile("n12/(s.*_)jfs/");

                        updateProgress(0, total);
                        updateValue("开始预处理...");

                        // 水印图片
                        BufferedImage logoImage = null;
                        if ( StringUtils.isNotBlank(logo.getText()) ) {
                            File logoFile = new File(logo.getText());
                            if ( logoFile.exists() ) {
                                try {
                                    logoImage = ImageIO.read(logoFile);
                                } catch (Exception e) {
                                    System.out.println("logo 图片加载失败: " + e.getMessage());
                                    logoImage = null;
                                }
                            }
                        }

                        int i = 0;
                        for (ShelvesItem item : pending) {
                            //ShelvesItem item = SQLExecutor.executeQueryBean("select * from SHELVESITEM where ID = ?", ShelvesItem.class, _item.getId());

                            updateProgress(++i, total);
                            updateValue("["+ i +"/" + total + "][SKU: " + item.getSkuCode() + "]" + item.getCmTitle());

                            if ( "JD".equalsIgnoreCase(item.getType()) ) {
                                String skuCode =  item.getSkuCode();
                                try {
                                    String html =  SkuPageLoader.getInstance().loadPage(skuCode);

                                    Document doc = Jsoup.parse(html);
                                    Element crumb = doc.selectFirst("div#crumb-wrap .crumb");

                                    Element ellipsis = crumb.selectFirst("div.ellipsis");
                                    if ( ellipsis!=null ) {
                                        String text = ellipsis.attr("title");
                                        if ( StringUtils.isBlank(text) ) {
                                            text = ellipsis.text();
                                        }
                                        item.setSellingPoints(text);
                                    }


                                    Elements _items = crumb.select("div.item");
                                    if ( _items!=null && _items.size() > 5 ) {
                                        String cate =  _items.get(4).text();
                                        item.setCategoryCode(cate);
                                        item.setCategoryname(cate);
                                    }

                                    // 主图 图片
                                    Elements imgs = doc.select("div#spec-list li>img");
                                    for (int i1 = 0; i1 < imgs.size(); i1++) {
                                        Element img = imgs.get(i1);

                                        String text = img.attr("src");
                                        if ( text.startsWith("//") ) {
                                            text = "https:" + text;
                                        }
                                        Matcher mat = PIC_IMAGE.matcher(text);
                                        if (mat.find()){
                                            text = text.substring(0,  mat.start()) + "360buyimg.com/n12/" + text.substring(mat.end());
                                        }

                                        mat = PIC_IMAGE_JFS.matcher(text);
                                        if (mat.find()){
                                            text = text.substring(0, 32) + "jfs/" + text.substring(mat.end());
                                        }
                                        System.out.println("[SKU: " + item.getSkuCode() + "]主图: " +  text);



                                        String id = MD5Utils.md5Hex(item.getId() + "_M_" + i1);
                                        ImageInfo exist = SQLExecutor.executeQueryBean("select * from ITEM_IMAGE where ID = ? ", ImageInfo.class, id);
                                        if ( exist==null ) {
                                            exist = new ImageInfo();
                                            exist.setId(id);
                                            SQLExecutor.insert(exist);
                                        }

                                        boolean image_exist = false;
                                        if ( exist!=null && StringUtils.isNotBlank(exist.getFilepath()) ) {
                                            File image = new File(exist.getFilepath());
                                            if ( image.exists() && image.length() > 999 ) {
                                                // 图片存在
                                                image_exist  = true;
                                            }
                                        }

                                        if ( !image_exist ) {
                                            String imageFileName =  skuCode + "_" + (i1+1) + ".jpg";
                                            File imageFile = new File(dir, imageFileName);

                                            int cnt = 0;
                                            while ( cnt++ < 3 ) {
                                                try {
                                                    HttpUtils.download(text, imageFile.getAbsolutePath());


                                                    Mat src = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
                                                    if ( src.width()!=800 || src.height()!=800 ) {
                                                        Imgproc.resize(src, src, new Size(800, 800));
                                                    }

                                                    double[] pixes = src.get(src.width()-30, src.height()-10);
                                                    Scalar scalar =  new Scalar(238,  238,  238);
                                                    if ( pixes!=null && pixes.length==3 ) {
                                                        pixes[0] = pixes[0]-15;
                                                        pixes[1] = pixes[1]-15;
                                                        pixes[2] = pixes[2]-15;
                                                        scalar = new Scalar(pixes);
                                                    }

                                                    Imgproc.putText(src, Integer.toString(i1+1), new Point(src.width()-30, src.height()-10), 0, 1.0, scalar);
                                                    Imgcodecs.imwrite(imageFile.getAbsolutePath(), src);


                                                    if ( logoImage!=null ) {
                                                        try {
                                                            // ImageIO读取图片
                                                            BufferedImage pic = ImageIO.read(imageFile);
                                                            Thumbnails.of(pic)
                                                                    // 设置图片大小
                                                                    .size(pic.getWidth(), pic.getHeight())
                                                                    // 加水印 参数：1.水印位置 2.水印图片 3.不透明度0.0-1.0
                                                                    .watermark(Positions.TOP_LEFT, logoImage, 0.9f)
                                                                    // 输出到文件
                                                                    .toFile(imageFile);
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    exist.setRefid(item.getId());
                                                    exist.setCode(item.getSkuCode());
                                                    exist.setXh(i1+1);
                                                    exist.setType("M");
                                                    exist.setPicurl(text);
                                                    exist.setFilepath(imageFile.getAbsolutePath());

                                                    SQLExecutor.update(exist);
                                                    break;
                                                } catch (Exception e) {
                                                    System.out.println("图片下载失败: " + e.getMessage() + " ==> " + text);
                                                    Thread.sleep(1000);
                                                }
                                            }

                                        }
                                    }
                                    System.out.println("Images: " + imgs.size());

                                    // 详情图
                                    int i1 = 1;
                                    Element detail = doc.selectFirst("div#detail");
                                    if ( detail!=null ) {
                                        Element detailcontent = detail.selectFirst("div#detail div#J-detail-content style");
                                        if ( detailcontent!=null ) {
                                            Matcher mat = DETAIL_IMAGE.matcher(detailcontent.html());
                                            while(mat.find()){
                                                String text = mat.group(1);
                                                if ( text.startsWith("(") ) {
                                                    text = text.substring(1);
                                                }
                                                if ( text.endsWith(")") ) {
                                                    text = text.substring(0, text.length()-1);
                                                }
                                                if ( text.startsWith("\"") ) {
                                                    text = text.substring(1);
                                                }
                                                if ( text.endsWith("\"") ) {
                                                    text = text.substring(0, text.length()-1);
                                                }
                                                if ( text.startsWith("//") ) {
                                                    text = "https:" + text;
                                                }
                                                System.out.println("style image: " + text);

                                                if ( StringUtils.isBlank(text) ) {
                                                    continue;
                                                }

                                                String id = MD5Utils.md5Hex(item.getId() + "_D_" + i1);

                                                ImageInfo exist = SQLExecutor.executeQueryBean("select * from ITEM_IMAGE where ID = ? ", ImageInfo.class, id);
                                                if ( exist==null ) {
                                                    exist = new ImageInfo();
                                                    exist.setId(id);
                                                    SQLExecutor.insert(exist);
                                                }

                                                boolean image_exist = false;
                                                if ( exist!=null && StringUtils.isNotBlank(exist.getFilepath()) ) {
                                                    File image = new File(exist.getFilepath());
                                                    if ( image.exists() && image.length() > 999 ) {
                                                        // 图片存在
                                                        image_exist  = true;
                                                    }
                                                }


                                                if ( !image_exist ) {
                                                    String imageFileName =  skuCode + "_" + i1 + "_detail" + ".jpg";
                                                    File imageFile = new File(dir, imageFileName);

                                                    int cnt = 0;
                                                    while ( cnt++ < 3 ) {
                                                        try {
                                                            HttpUtils.download(text, imageFile.getAbsolutePath());

                                                            exist.setRefid(item.getId());
                                                            exist.setCode(item.getSkuCode());
                                                            exist.setXh(i1);
                                                            exist.setType("D");
                                                            exist.setPicurl(text);
                                                            exist.setFilepath(imageFile.getAbsolutePath());

                                                            SQLExecutor.update(exist);
                                                            break;
                                                        } catch (Exception e) {
                                                            System.out.println("图片下载失败: " + e.getMessage() + " ==> " + text);
                                                            Thread.sleep(1000);
                                                        }
                                                    }
                                                }

                                                i1++;

                                            }
                                        }

                                        Elements detailImgs = detail.select("div#J-detail-content img");
                                        if ( detailImgs!=null ) {
                                            for (Element detailImg : detailImgs) {

                                                String src = detailImg.attr("src");
                                                if ( src==null || src.trim().length()==0 ||  src.endsWith("blank.gif") ) {
                                                    src = detailImg.attr("data-lazyload");
                                                }
                                                if ( src.startsWith("//") ) {
                                                    src = "https:" + src;
                                                }
                                                System.out.println("J-detail-content img: " + src);


                                                String id = MD5Utils.md5Hex(item.getId() + "_D_" + i1);
                                                ImageInfo exist = SQLExecutor.executeQueryBean("select * from ITEM_IMAGE where ID = ? ", ImageInfo.class, id);
                                                if ( exist==null ) {
                                                    exist = new ImageInfo();
                                                    exist.setId(id);
                                                    SQLExecutor.insert(exist);
                                                }

                                                boolean image_exist = false;
                                                if ( exist!=null && StringUtils.isNotBlank(exist.getFilepath()) ) {
                                                    File image = new File(exist.getFilepath());
                                                    if ( image.exists() && image.length() > 999 ) {
                                                        // 图片存在
                                                        image_exist  = true;
                                                    }
                                                }


                                                if ( !image_exist ) {
                                                    String imageFileName =  skuCode + "_" + i1 + "_detail" + ".jpg";
                                                    File imageFile = new File(dir, imageFileName);

                                                    int cnt = 0;
                                                    while ( cnt++ < 3 ) {
                                                        try {
                                                            HttpUtils.download(src, imageFile.getAbsolutePath());

                                                            exist.setRefid(item.getId());
                                                            exist.setCode(item.getSkuCode());
                                                            exist.setXh(i1);
                                                            exist.setType("D");
                                                            exist.setPicurl(src);
                                                            exist.setFilepath(imageFile.getAbsolutePath());

                                                            SQLExecutor.update(exist);
                                                            break;
                                                        } catch (Exception e) {
                                                            System.out.println("图片下载失败: " + e.getMessage() + " ==> " + src);
                                                            Thread.sleep(1000);
                                                        }
                                                    }
                                                }

                                                i1++;
                                            }
                                        }
                                    }


                                    item.setZt(1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            SQLExecutor.update(item);
                        }

                        return "[Complete]预处理完成";
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
            _dialogStage.setTitle("预处理进度");
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

    public static class EditTask {

        private String prefix;

        private String suffix;

        private String src;

        private String dest;

        private String codePrefix;

        private String codeSuffix;

        private Category category;

        private Brand brand;

        private boolean ckALl;

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

        public boolean isCkALl() {
            return ckALl;
        }

        public void setCkALl(boolean ckALl) {
            this.ckALl = ckALl;
        }
    }

    public static void main(String[] args) {

        Pattern PIC_IMAGE = Pattern.compile("360buyimg.com/n(\\d*)/");

        String text = "https://img12.360buyimg.com/n5/s54x54_jfs/t1/24469/22/2790/120612/5c209a8eEd4945f31/0b0937df042c368d.jpg";

        Matcher mat = PIC_IMAGE.matcher(text);
        if (mat.find()){
            text = text.substring(0,  mat.start()) + "360buyimg.com/n12/" + text.substring(mat.end());
        }


        Pattern PIC_IMAGE_JFS = Pattern.compile("n12/(s.*_)jfs/");

        mat = PIC_IMAGE_JFS.matcher(text);
        if (mat.find()){
            text = text.substring(0, 32) + "jfs/" + text.substring(mat.end());
        }

        System.out.println(text);
    }
}
