package com.gavel.application.controller;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.List;

public class FXMLMainController {

    @FXML
    private BorderPane root;

    @FXML
    private WebView webView;

    @FXML
    private TextArea textArea;

    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }

    @FXML
    private void initialize() {
        webView.getEngine().load("https://www.grainger.cn");

    }

    public void handleBtn1Action(ActionEvent actionEvent) {

        textArea.appendText(webView.getEngine().getLocation());
        textArea.appendText("\n");


        List<Product> products = null;
        try {
            products = SQLExecutor.executeQueryBeanList("select * from graingerproduct where type = 'g' order by code desc", Product.class);
            textArea.appendText("Products: " + products.size());
            textArea.appendText("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
