package com.gavel.application.controller;

import com.gavel.application.MainApp;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Item;
import com.gavel.entity.ShelvesItem;
import com.gavel.shelves.ShelvesItemParser;
import com.gavel.utils.StringUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

import java.io.IOException;
import java.util.*;

public class FXMLSkuImportController {

    @FXML
    private ComboBox<String> shop;


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
    private TableColumn<Item, String> priceCol;
    @FXML
    private TableColumn<Item, String> ownCol;
    @FXML
    private TableColumn<Item, String> shopCol;
    @FXML
    private TableColumn<Item, String> stockCol;

    @FXML
    private TextField min;

    @FXML
    private TextField max;

    @FXML
    private CheckBox own;

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
        priceCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPrice())));
        ownCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOwn()));
        shopCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getShop()));
        stockCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStock()));

        shop.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> onShopChange());

        initShopcombox();

        skuList.setItems(FXCollections.observableList(datas));

        min.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("当前字符数为：" + min.getText());
                onShopChange();
            }
        });

        max.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("当前字符数为：" + max.getText());
                onShopChange();
            }
        });

    }

    private void onShopChange() {

        String _shop = shop.getSelectionModel().getSelectedItem();

        float min_price = 0;
        float max_price = Float.MAX_VALUE;

        boolean _own = own.isSelected();

        String _min =  min.getText();
        String _max =  max.getText();
        if ( _min!=null && _min.trim().length() > 0 ) {
            try {
                min_price = Float.parseFloat(_min);
            } catch (Exception e) {

            }
        }

        if ( _max!=null && _max.trim().length() > 0 ) {
            try {
                max_price = Float.parseFloat(_max);
            } catch (Exception e) {

            }
        }

        final List<Item> filterItems = new ArrayList<>();

        if ( _shop==null || _shop.trim().length()==0 ) {
            filterItems.addAll(datas);
        } else {
            for (Item data : datas) {
                if ( _shop.equalsIgnoreCase(data.getShop())  ) {
                    filterItems.add(data);
                }
            }
        }


        Iterator<Item> it = filterItems.iterator();
        while ( it.hasNext() ) {
            Item item = it.next();
            if ( item.getPrice() < min_price || item.getPrice() > max_price ) {
                it.remove();
            }

            if ( _own && "N".equalsIgnoreCase(item.getOwn()) ) {
                it.remove();
            }
        }


        skuList.setItems(FXCollections.observableList(filterItems));
    }

    // 初始化任务列表
    private void initShopcombox() {

        Set<String>  shops = new HashSet<>();

        if ( datas==null || datas.size()==0 ) {
            shop.setItems(FXCollections.observableArrayList(shops));
        }

        shops.add("");
        for (Item item : datas) {
            shops.add(item.getShop());
        }

        shop.setItems(FXCollections.observableArrayList(shops));

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



        Service<String> service = new Service<String>() {

            @Override
            protected javafx.concurrent.Task<String> createTask() {
                return new javafx.concurrent.Task<String>() {

                    @Override
                    protected String call() throws Exception {
                        final  int total = skuList.getItems().size();
                        for (int i = 0; i < skuList.getItems().size(); i++) {
                            Item item = skuList.getItems().get(i);

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
                                    //shelvesItem.setSellingPoints(item.getSubname());

                                    shelvesItem.setPrice(item.getPrice());
                                    shelvesItem.setShop(item.getShop());
                                    shelvesItem.setStock(item.getStock());
                                    shelvesItem.setOwn(item.getOwn());

                                    shelvesItem.setSellingPoints(item.getSubname());
                                    if ( shelvesItem.getSellingPoints()==null || shelvesItem.getSellingPoints().trim().length()==0 ) {
                                        shelvesItem.setSellingPoints(item.getName());
                                    }

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

                        updateProgress(total, total);

                        return "解析完成, 请点击确定或者取消";
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

        skuList.setItems(FXCollections.observableList(datas));

        initShopcombox();
    }

    public void handleOwnCheck(ActionEvent actionEvent) {

        onShopChange();

    }
}
