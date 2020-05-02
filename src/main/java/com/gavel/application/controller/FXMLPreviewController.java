package com.gavel.application.controller;

import com.gavel.HttpUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class FXMLPreviewController {

    @FXML
    private AnchorPane root;


    @FXML
    private WebView webView;

    @FXML
    private TextField address;

    private WebEngine webEngine;


    private FXMLPreviewController.Callback callback;

    public FXMLPreviewController() {

    }

    public FXMLPreviewController.Callback getCallback() {
        return callback;
    }

    public void setCallback(FXMLPreviewController.Callback _callback) {
        this.callback = _callback;
        System.out.println("Set _callback: " + _callback);
    }


    @FXML
    private void initialize() {

        webEngine = webView.getEngine();

        webEngine.setUserAgent(HttpUtils.USERAGENT);

        webEngine.locationProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                address.setText(newValue);
            }
        });

        webEngine.load("https://www.grainger.cn");

    }


    private Stage stage() {
        return  (Stage) root.getScene().getWindow();
    }

    public void handleOKAction(ActionEvent actionEvent) {
        System.out.println("callback: " + callback);
        if ( callback!=null ){
            callback.invoke(address.getText());
        }
        stage().close();
    }

    public interface Callback {
        void invoke(String url);
    }
}
