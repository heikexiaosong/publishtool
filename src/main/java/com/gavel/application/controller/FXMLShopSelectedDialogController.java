package com.gavel.application.controller;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.Shopinfo;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FXMLShopSelectedDialogController {

    @FXML
    private TextField keyword;

    @FXML
    private TableView<Shopinfo> shopinfoTable;
    @FXML
    private TableColumn<Shopinfo, String> codeCol;
    @FXML
    private TableColumn<Shopinfo, String> nameCol;
    @FXML
    private TableColumn<Shopinfo, String> platformCol;
    @FXML
    private TableColumn<Shopinfo, String> typeCol;

    private Stage dialogStage;
    private boolean okClicked = false;

    private Shopinfo shopinfo;

    @FXML
    private void initialize() {
        codeCol.setCellValueFactory(cellData -> cellData.getValue().codeProperty());
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        platformCol.setCellValueFactory(cellData -> cellData.getValue().platformProperty());
        typeCol.setCellValueFactory(cellData ->  cellData.getValue().typeProperty());


        List<Shopinfo> shopinfos = null;
        try {
            shopinfos = SQLExecutor.executeQueryBeanList("select * from SHOPINFO", Shopinfo.class);
        } catch (Exception e) {
            e.printStackTrace();
            shopinfos = Collections.EMPTY_LIST;
        }

        shopinfoTable.setItems(FXCollections.observableArrayList(shopinfos));

    }


    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {

        Shopinfo selected = shopinfoTable.getSelectionModel().getSelectedItem();

        if ( selected!=null &&  shopinfo!=null ) {

            if ( !selected.getCode().equalsIgnoreCase(shopinfo.getCode()) ) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", new ButtonType("取消", ButtonBar.ButtonData.NO), new ButtonType("确定", ButtonBar.ButtonData.YES));
                alert.initOwner(dialogStage);
                alert.setTitle("信息确认");
                alert.setHeaderText("确认要切换到: " + selected.getName()  + "?");

                Optional<ButtonType> _buttonType = alert.showAndWait();

                if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES)){
                    shopinfo.setId(selected.getId());
                    shopinfo.setCode(selected.getCode());
                    shopinfo.setName(selected.getName());
                    shopinfo.setPlatform(selected.getPlatform());
                    shopinfo.setType(selected.getType());
                    shopinfo.setEndpoint(selected.getEndpoint());
                    shopinfo.setAppkey(selected.getAppkey());
                    shopinfo.setAppsecret(selected.getAppsecret());
                } else {
                    return;
                }
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


    public void bindItems(Shopinfo _shopinfo) {
        this.shopinfo = _shopinfo;
    }

    public void handleSearchAction(ActionEvent actionEvent) {


    }
}
