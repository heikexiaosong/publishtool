package com.gavel.application.controller;

import com.gavel.application.model.Task;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

public class FXMLMainController {

    @FXML
    private BorderPane root;

    @FXML
    private TextArea textArea;

    @FXML
    private TableView<Task> taskTable;

    @FXML
    private TableColumn<Task, String> idColumn;

    @FXML
    private TableColumn<Task, String> urlColumn;

    @FXML
    private TableColumn<Task, String> timeColumn;

    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }

    private ObservableList<Task> taskList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        taskList.add(new Task("Hans", "Muster"));
        taskList.add(new Task("Ruth", "Mueller"));

        idColumn.setCellValueFactory(cellData -> cellData.getValue().taskidProperty());
        urlColumn.setCellValueFactory(cellData -> cellData.getValue().urlProperty());

        taskTable.setItems(getTaskData());

        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPersonDetails(newValue));

    }

    private void showPersonDetails(Task task) {
        System.out.println(task.getTaskid() + ": " + task.getUrl());
//        if (person != null) {
//            // Fill the labels with info from the person object.
//            firstNameLabel.setText(person.getFirstName());
//            lastNameLabel.setText(person.getLastName());
//            streetLabel.setText(person.getStreet());
//            postalCodeLabel.setText(Integer.toString(person.getPostalCode()));
//            cityLabel.setText(person.getCity());
//
//            // TODO: We need a way to convert the birthday into a String!
//            // birthdayLabel.setText(...);
//        } else {
//            // Person is null, remove all the text.
//            firstNameLabel.setText("");
//            lastNameLabel.setText("");
//            streetLabel.setText("");
//            postalCodeLabel.setText("");
//            cityLabel.setText("");
//            birthdayLabel.setText("");
//        }
    }

    public ObservableList<Task> getTaskData() {
        return taskList;
    }

    public void handleBtn1Action(ActionEvent actionEvent) {

       // textArea.appendText(webView.getEngine().getLocation());
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
