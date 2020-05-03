package com.gavel.application.controller;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.Category;
import com.gavel.entity.CategoryMapping;
import com.gavel.entity.Itemparameter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.List;


public class FXMLSettingController {

    @FXML
    private AnchorPane root;


    // 类目
    @FXML
    private TableView<CategoryMapping> cateMapping;
    @FXML
    private TableColumn<CategoryMapping, String> cmCode;
    @FXML
    private TableColumn<CategoryMapping, String> cmName;
    @FXML
    private TableColumn<CategoryMapping, String> cmCategoryCode;
    @FXML
    private TableColumn<CategoryMapping, String> cmCategoryName;
    @FXML
    private TableColumn<CategoryMapping, String> cmDescPath;

    // 类目属性
    @FXML
    private TableView<Category> cateParams;
    @FXML
    private TableColumn<Category, String> categoryCode;
    @FXML
    private TableColumn<Category, String> categoryName;
    @FXML
    private TableColumn<Category, String> descPath;

    @FXML
    private GridPane params;

    @FXML
    private void initialize() {

        // 类目 --------
//        @FXML
//        private TableView<CategoryMapping> cateMapping;
//        @FXML
//        private TableColumn<CategoryMapping, String> cmCode;
//        @FXML
//        private TableColumn<CategoryMapping, String> cmName;
//        @FXML
//        private TableColumn<CategoryMapping, String> cmCategoryCode;
//        @FXML
//        private TableColumn<CategoryMapping, String> cmCategoryName;
//        @FXML
//        private TableColumn<CategoryMapping, String> cmDescPath;

        cmCode.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getCode()));
        cmName.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getName()));
        cmCategoryCode.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getCategoryCode()));
        cmCategoryName.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getCategoryName()));
        cmDescPath.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getDescPath()));


        List<CategoryMapping> categoryMappings = null;
        try {
            categoryMappings = SQLExecutor.executeQueryBeanList("select * from categorymapping", CategoryMapping.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        cateMapping.setItems(FXCollections.observableArrayList(categoryMappings));
        // 类目 ----------

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

        if ( newValue==null ) {
            return;
        }

        params.getChildren().clear();

        List<Itemparameter> itemparameters = null;
        try {
            itemparameters = SQLExecutor.executeQueryBeanList("select * from ITEMPARAMETER where categoryCode = ? ", Itemparameter.class, newValue.getCategoryCode());
            if ( itemparameters!=null ) {

                for (int i1 = 0; i1 < itemparameters.size(); i1++) {
                    Itemparameter itemparameter = itemparameters.get(i1);

                    System.out.println(itemparameter.getParCode() + ": " + itemparameter.getParName());

                    Label label = new Label("(" + itemparameter.getParCode() + ")" + itemparameter.getParName() + ": ");
                    if ( "X".equalsIgnoreCase(itemparameter.getIsMust()) ) {
                        label.setStyle("-fx-text-fill: red;");
                    }

                    TextField field = new TextField();

                    params.add(label, 0, i1);
                    params.add( new Label(itemparameter.getParUnit()), 2, i1);
                    params.add( new Label(itemparameter.getParaTemplateCode()), 3, i1);
                    params.add( new Label(itemparameter.getParaTemplateDesc()), 4, i1);
                    params.add( new Label(itemparameter.getIsMust()), 5, i1);
                    params.add( new Label(itemparameter.getParType()), 6, i1);
                    params.add( new Label(itemparameter.getOptions()), 7, i1);


                    switch (itemparameter.getParType()) {
                        case "1":


                            ComboBox<Itemparameter.ParOption> parOptionComboBox = new ComboBox<Itemparameter.ParOption>();

                            parOptionComboBox.getItems().addAll( itemparameter.getParOption());
                            parOptionComboBox.getSelectionModel().select(1);


                            parOptionComboBox.setOnAction((ActionEvent ev) -> {
                                Itemparameter.ParOption option =
                                        parOptionComboBox.getSelectionModel().getSelectedItem();
                                System.out.println(option.getParOptionCode() + ": " + option.getParOptionDesc());
                            });

                            parOptionComboBox.setConverter(new StringConverter<Itemparameter.ParOption>() {
                                @Override
                                public String toString(Itemparameter.ParOption object) {
                                    return object.getParOptionDesc();
                                }

                                @Override
                                public Itemparameter.ParOption fromString(String string) {
                                    return null;
                                }
                            });

                           params.add(parOptionComboBox, 1, i1);

                            break;
                        case "2":
                            params.add(field, 1, i1);
                            break;
                        case "3":
                            params.add(field, 1, i1);
                            break;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
