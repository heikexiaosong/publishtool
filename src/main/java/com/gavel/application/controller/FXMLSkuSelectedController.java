package com.gavel.application.controller;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.Item;
import com.gavel.entity.ShelvesItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.List;

public class FXMLSkuSelectedController {

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
            ShelvesItem shelvesItem = new ShelvesItem();
            shelvesItem.setItemCode(item.getCode());
            shelvesItem.setBrandCode(item.getBrand());
            //shelvesItem.setGraingerbrandname(item.getBrandname());
            shelvesItem.setCategoryCode(item.getCategory());
            //shelvesItem.setGraingercategoryname(item.getCategoryname());
            items.add(shelvesItem);

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

        String _skucode = skucode.getText().trim();

        skuList.getItems().clear();

        List<Item> items = null;
        try {
            items = SQLExecutor.executeQueryBeanList("select * from ITEM where code = ? ", Item.class, _skucode);
        } catch (Exception e) {
            e.printStackTrace();
            items = Collections.EMPTY_LIST;
        }

        skuList.getItems().addAll(items);

    }
}
