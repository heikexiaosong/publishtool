package com.gavel.application.controller;

import com.gavel.HttpUtils;
import com.gavel.application.FXMLNavigation;
import com.gavel.application.model.Task;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class FXMLWebpageController {




    private static final Pattern CODE_PATTERN = Pattern.compile("/([a-zA-Z])-([^\\.]*).html", Pattern.CASE_INSENSITIVE);

    @FXML
    private AnchorPane root;

    @FXML
    private TextArea textArea;

    @FXML
    private WebView webView;

    @FXML
    private TextField address;

    private WebEngine webEngine;


    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }

    private ObservableList<Task> taskList = FXCollections.observableArrayList();

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

        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

            @Override
            public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
                textArea.appendText("Loading state: " + newValue.toString() + "\n");
                if (newValue == Worker.State.SUCCEEDED) {
                    textArea.appendText( webEngine.getLocation() +  " Finish!\n" );
                }
            }
        });

    }

    public void handleGoAction(ActionEvent actionEvent) {


        FXMLPreviewController controller = FXMLNavigation.toModality("/fxml/preview.fxml", 1280, 720);

        controller.setCallback(new FXMLPreviewController.Callback() {
            @Override
            public void invoke(String url) {
                address.setText(url);
                webView.getEngine().load(url);
            }
        });

    }
}
