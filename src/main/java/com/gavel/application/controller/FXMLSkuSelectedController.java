package com.gavel.application.controller;

import com.gavel.application.DataPagination;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.GraingerBrand;
import com.gavel.entity.GraingerCategory;
import com.gavel.entity.Item;
import com.gavel.entity.ShelvesItem;
import com.gavel.shelves.ShelvesItemParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FXMLSkuSelectedController {

    @FXML
    private ComboBox<GraingerCategory> category;

    @FXML
    private ComboBox<GraingerBrand>  brand;

    @FXML
    private TextField skucode;

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

    private List<ShelvesItem> items;


    @FXML
    private void initialize() {

        picCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUrl()));
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCode()));
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        brandnameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBrandname()));
        categorynameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryname()));

        // 类目列表
        initCatecombox();

        // 品牌列表
        initBrandbox();


        pagination.setPageCount(0);
    }

    // 初始化品牌列表
    private void initBrandbox() {

        List<GraingerBrand> datas = null;
        try {
            datas = SQLExecutor.executeQueryBeanList("select distinct  BRAND code, BRANDNAME name1 from ITEM", GraingerBrand.class);
        } catch (Exception e) {
            e.printStackTrace();
            datas = Collections.EMPTY_LIST;
        }
        brand.setItems(FXCollections.observableArrayList(datas));


        brand.setConverter(new StringConverter<GraingerBrand>(){
            @Override
            public String toString(GraingerBrand object) {
                return object == null ? null : object.getName1();
            }
            @Override
            public GraingerBrand fromString(String string) {
                return brand.getItems().stream().filter(i -> i.getName1().equals(string)).findAny().orElse(null);
            }

        });

        brand.setCellFactory(lv -> new ListCell<GraingerBrand>() {

            @Override
            protected void updateItem(GraingerBrand item, boolean empty) {
                super.updateItem(item, empty);

                // use full text in list cell (list popup)
                setText(item == null ? null : item.getName1());
            }

        });
    }

    // 初始化类目列表
    private void initCatecombox() {

        List<GraingerCategory> datas = null;
        try {
            datas = SQLExecutor.executeQueryBeanList("select distinct  CATEGORY code, CATEGORYNAME name from ITEM", GraingerCategory.class);
        } catch (Exception e) {
            e.printStackTrace();
            datas = Collections.EMPTY_LIST;
        }
        category.setItems(FXCollections.observableArrayList(datas));


        category.setConverter(new StringConverter<GraingerCategory>(){
            @Override
            public String toString(GraingerCategory object) {
                return object == null ? null : object.getName();
            }
            @Override
            public GraingerCategory fromString(String string) {
                return category.getItems().stream().filter(i -> i.getName().equals(string)).findAny().orElse(null);
            }

        });

        category.setCellFactory(lv -> new ListCell<GraingerCategory>() {

            @Override
            protected void updateItem(GraingerCategory item, boolean empty) {
                super.updateItem(item, empty);

                // use full text in list cell (list popup)
                setText(item == null ? null : item.getName());
            }

        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        for (Item item : skuList.getItems()) {
            try {
                ShelvesItem shelvesItem = ShelvesItemParser.parse(item);
                items.add(shelvesItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
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


    public void bindItems(List<ShelvesItem> _shelvesItems) {
        items = _shelvesItems;
    }

    public void handleSearchAction(ActionEvent actionEvent) {


        GraingerCategory _cate = category.getSelectionModel().getSelectedItem();

        GraingerBrand _brand = brand.getSelectionModel().getSelectedItem();


        String _skucode = skucode.getText().trim();



        skuList.getItems().clear();

        List<Item> items = null;
        try {

            List<Object> params = new ArrayList<>();
            StringBuilder sql = new StringBuilder("select * from ITEM where 1=1  ");

            if ( _cate!=null && _cate.getCode()!=null && _cate.getCode().trim().length() > 0  ) {
                sql.append(" and CATEGORY = ? ");
                params.add( _cate.getCode().trim());
            }

            if ( _brand!=null && _brand.getCode()!=null && _brand.getCode().trim().length() > 0  ) {
                sql.append(" and BRAND = ? ");
                params.add( _brand.getCode().trim());
            }

            if ( _skucode!=null && _skucode.trim().length() > 0  ) {
                sql.append(" and CODE like  ? ");
                params.add("%" +  _skucode.trim() + "%");
            }

            GraingerCategory cate = category.getSelectionModel().getSelectedItem();
            items = SQLExecutor.executeQueryBeanList(sql.toString(), Item.class, params.toArray(new Object[params.size()]));
        } catch (Exception e) {
            e.printStackTrace();
            items = Collections.EMPTY_LIST;
        }

        DataPagination dataPagination = new DataPagination(items, 30);

        pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());

        pagination.setPageFactory(pageIndex -> {
            skuList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
            return skuList;
        });



    }
}
