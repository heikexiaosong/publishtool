package com.gavel.application.controller;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.Category;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.util.List;


public class FXMLSettingController {

    @FXML
    private AnchorPane root;

    @FXML
    private TableView<Category> cateParams;
    @FXML
    private TableColumn<Category, String> categoryCode;
    @FXML
    private TableColumn<Category, String> categoryName;
    @FXML
    private TableColumn<Category, String> descPath;

    @FXML
    private void initialize() {

        // Initialize the person table with the two columns.
        categoryCode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryCode()));
        categoryName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryName()));
        descPath.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescPath()));

        // Clear person details.
       showCategoryDetails(null);

        // Listen for selection changes and show the person details when changed.
        cateParams.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCategoryDetails(newValue));


        List<Category> categories = null;
        try {
            categories = SQLExecutor.executeQueryBeanList("select * from category", Category.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        cateParams.setItems(FXCollections.observableArrayList(categories));

    }

    private void showCategoryDetails(Category newValue) {

    }
}
