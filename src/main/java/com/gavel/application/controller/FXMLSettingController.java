package com.gavel.application.controller;

import com.gavel.application.MainApp;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Category;
import com.gavel.entity.CategoryMapping;
import com.gavel.entity.Itemparameter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class FXMLSettingController {

    @FXML
    private AnchorPane root;

    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }


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

    private List<Itemparameter> itemparameters;

    @FXML
    private void initialize() {

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

    /**
     * 类目属性详情
     * @param newValue
     */
    private void showCategoryDetails(Category newValue) {

        if ( newValue==null ) {
            return;
        }

        params.getChildren().clear();

        itemparameters = null;
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

                    params.add(label, 0, i1);
                    params.add( new Label(itemparameter.getParUnit()), 2, i1);
                    params.add( new Label(itemparameter.getParaTemplateCode()), 3, i1);
                    params.add( new Label(itemparameter.getParaTemplateDesc()), 4, i1);
                    params.add( new Label(itemparameter.getDataType()), 5, i1);


                    switch (itemparameter.getParType()) {
                        case "1":
                        case "2":

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

                           if ( itemparameter.getParam()!=null && itemparameter.getParam().trim().length() > 0 ) {

                              String defaultValue = itemparameter.getParam();
                               Itemparameter.ParOption value = itemparameter.getParOption().get(0);
                               for (Itemparameter.ParOption option : itemparameter.getParOption()) {
                                   if ( defaultValue.equalsIgnoreCase(option.getParOptionCode()) ) {
                                       value = option;
                                       break;
                                   }
                               }
                               parOptionComboBox.getSelectionModel().select(value);

                           }


                            parOptionComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Itemparameter.ParOption>() {
                                @Override
                                public void changed(ObservableValue<? extends Itemparameter.ParOption> observable, Itemparameter.ParOption oldValue, Itemparameter.ParOption newValue) {
                                    itemparameter.setParam(newValue.getParOptionCode());
                                }
                            });

                            break;
                        case "3":
                            final TextField field = new TextField();
                            field.setText(itemparameter.getParam());
                            field.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    itemparameter.setParam(field.getText());
                                }
                            });
                            params.add(field, 1, i1);
                            break;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 自动匹配类目
     * @param actionEvent
     */
    public void handleAutoMappingAction(ActionEvent actionEvent) {


        List<Category> categories = null;
        try {
            categories = SQLExecutor.executeQueryBeanList("select * from CATEGORY", Category.class);
        } catch (Exception e) {
            e.printStackTrace();
            categories = Collections.EMPTY_LIST;
        }


        for (CategoryMapping categoryMapping : cateMapping.getItems()) {
            String cateName = categoryMapping.getName();

            for (Category category : categories) {
                if ( category.getCategoryName().contains(cateName) || cateName.contains(category.getCategoryName()) || category.getDescPath().contains(cateName)  ) {
                    categoryMapping.setCategoryCode(category.getCategoryCode());
                    categoryMapping.setCategoryName(category.getCategoryName());
                    categoryMapping.setDescPath(category.getDescPath());
                    try {
                        SQLExecutor.update(categoryMapping);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }

        }

        initialize();
    }

    /**
     * 类目匹配修改
     * @param actionEvent
     */
    public void handleCateMappingAction(ActionEvent actionEvent) {

        CategoryMapping categoryMapping = cateMapping.getSelectionModel().getSelectedItem();
        if ( categoryMapping==null ) {
            return;
        }

        Category mappingCate = new Category();
        boolean okClicked = showCategoryMappingEditDialog(mappingCate);
        if (okClicked) {
            //mainApp.getPersonData().add(tempPerson);
            try {
                categoryMapping.setCategoryCode(mappingCate.getCategoryCode());
                categoryMapping.setCategoryName(mappingCate.getCategoryName());
                categoryMapping.setDescPath(mappingCate.getDescPath());
                SQLExecutor.update(categoryMapping);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean showCategoryMappingEditDialog(Category mappingCate) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/SuningCategorySelectDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("上架类目映射");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLSuningCateSelectedController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bind(mappingCate);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 类目属性更新
     * @param actionEvent
     */
    public void handleCateParamsUpdateAction(ActionEvent actionEvent) {

        if ( itemparameters==null || itemparameters.size()==0 ) {
            return;
        }

        for (Itemparameter itemparameter : itemparameters) {
            try {
                SQLExecutor.update(itemparameter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
