package com.gavel.application.controller;

import com.gavel.application.MainApp;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Brand;
import com.gavel.entity.Category;
import com.gavel.entity.Itemparameter;
import com.gavel.entity.ShelvesItem;
import com.gavel.utils.StringUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShelvesItemDetailEditDialogController {

    @FXML
    private TextField skuCode;

    @FXML
    private TextField itemCode;

    @FXML
    private TextField model;

    @FXML
    private TextField productName;

    @FXML
    private TextArea cmTitle;

    @FXML
    private TextField sellingPoints;

    @FXML
    private TextField categoryCode;

//    @FXML
//    private TextField categoryname;

    @FXML
    private TextField brandCode;

//    @FXML
//    private TextField brandname;

    @FXML
    private TextField mappingcategorycode;

    @FXML
    private TextField mappingbrandcode;

//    @FXML
//    private TextField mappingcategoryname;
//
//    @FXML
//    private TextField mappingbrandname;


    @FXML
    private GridPane params;


    private Stage dialogStage;
    private boolean okClicked = false;

    private ShelvesItem selectedItem;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    private Map<String, SimpleStringProperty> paramers = new ConcurrentHashMap<>();

    @FXML
    private void handleOk() {

        if ( selectedItem !=null  ) {
            selectedItem.setItemCode(itemCode.getText().trim());
            selectedItem.setProductName(productName.getText().trim());
            selectedItem.setCmTitle(cmTitle.getText().trim());
            selectedItem.setSellingPoints(sellingPoints.getText().trim());
        }

        // TODO
        if ( selectedItem!=null  && paramers.size() > 0 ) {
            String cate = selectedItem.getMappingcategorycode();

            try {
                List<Itemparameter> cateParams = SQLExecutor.executeQueryBeanList("select * from ITEMPARAMETER where CATEGORYCODE = ? ", Itemparameter.class, cate);

                for (Itemparameter cateParam : cateParams) {
                    if (paramers.containsKey(cateParam.getParCode()) ) {
                        SimpleStringProperty property = paramers.remove(cateParam.getParCode());
                        cateParam.setParam(property.getValue());

                        System.out.println(cateParam.getParCode() + " -> " + property.getValue() );

                        SQLExecutor.update(cateParam);
                    }
                }
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


    public void bind(ShelvesItem _selectedItem) {
        this.selectedItem = _selectedItem;

        if ( selectedItem!=null ) {
            skuCode.setText(selectedItem.getSkuCode());
            itemCode.setText(selectedItem.getItemCode());
            model.setText(selectedItem.getModel());
            productName.setText(selectedItem.getProductName());
            cmTitle.setText(selectedItem.getCmTitle());
            sellingPoints.setText(selectedItem.getSellingPoints());
            categoryCode.setText(selectedItem.getCategoryname());
//            categoryname.setText(selectedItem.getCategoryname());
            brandCode.setText(selectedItem.getBrandname());
//            brandname.setText(selectedItem.getBrandname());
            mappingcategorycode.setText(selectedItem.getMappingcategoryname());
//            mappingcategoryname.setText(selectedItem.getMappingcategoryname());
            mappingbrandcode.setText(selectedItem.getMappingbrandname());
//            mappingbrandname.setText(selectedItem.getMappingbrandname());

            loadIetmparamers(selectedItem.getMappingcategorycode());
        }
    }

    private void loadIetmparamers(String categorycode){
        paramers.clear();
        params.getChildren().clear();
        if ( categorycode==null || categorycode.trim().length()==0 ) {
            return;
        }
        try {
            List<Itemparameter> itemparameters = SQLExecutor.executeQueryBeanList("select * from ITEMPARAMETER where categoryCode = ? order by isMust desc", Itemparameter.class, categorycode);
            if ( itemparameters!=null ) {

                int i1 = 0;
                for (final Itemparameter itemparameter : itemparameters) {

                    if ( "cmModel".equalsIgnoreCase(itemparameter.getParCode()) ) {
                        continue;
                    }

                    if ( "001360".equalsIgnoreCase(itemparameter.getParCode()) ) {
                        continue;
                    }

                    final SimpleStringProperty property = new SimpleStringProperty(itemparameter.getParam());
                    paramers.put(itemparameter.getParCode(), property);

                    System.out.println(itemparameter.getParCode() + ": " + itemparameter.getParName());

                    Label label = new Label(itemparameter.getParName() + ": ");
                    if ( "X".equalsIgnoreCase(itemparameter.getIsMust()) ) {
                        label.setStyle("-fx-text-fill: red;");
                    }

                    params.add(label, 0, i1);
                    params.add( new Label(itemparameter.getParUnit()), 2, i1);
                    params.add( new Label(itemparameter.getParaTemplateDesc()), 3, i1);
                    params.add( new Label(itemparameter.getParCode() ), 4, i1);
                    params.add( new Label(itemparameter.getDataType()), 5, i1);

                    switch (itemparameter.getParType()) {
                        case "1":
                        case "2":

                            ComboBox<Itemparameter.ParOption> parOptionComboBox = new ComboBox<Itemparameter.ParOption>();

                            parOptionComboBox.getItems().addAll( itemparameter.getParOption());

                            parOptionComboBox.setOnAction((ActionEvent ev) -> {
                                Itemparameter.ParOption option = parOptionComboBox.getSelectionModel().getSelectedItem();
                                System.out.println( itemparameter.getParCode() + " ==> " + option.getParOptionCode() + ": " + option.getParOptionDesc());

                                if (!StringUtils.isBlank(option.getParOptionCode())) {
                                    property.setValue(option.getParOptionCode());
                                } else {
                                    property.setValue(option.getParOptionDesc());
                                }
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

                                    String _value = option.getParOptionCode();
                                    if ( StringUtils.isBlank(_value)) {
                                        _value = option.getParOptionDesc();
                                    }

                                    if (  defaultValue.equalsIgnoreCase(_value) ) {
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
                            field.textProperty().bindBidirectional(property);
                            field.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    itemparameter.setParam(field.getText());
                                }
                            });
                            params.add(field, 1, i1);
                            break;
                    }

                    i1++;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleCategorySelect(ActionEvent actionEvent) {
        Category mappingCate = new Category();
        boolean okClicked = showCategoryMappingEditDialog(mappingCate);
        if (okClicked) {
            //mainApp.getPersonData().add(tempPerson);
            try {
                selectedItem.setMappingcategorycode(mappingCate.getCategoryCode());
                selectedItem.setMappingcategoryname(mappingCate.getCategoryName());
                SQLExecutor.update(selectedItem);
                mappingcategorycode.setText(mappingCate.getCategoryName());

                loadIetmparamers(selectedItem.getMappingcategorycode());
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
            Stage _dialogStage = new Stage();
            _dialogStage.setTitle("上架类目选择");
            _dialogStage.initModality(Modality.WINDOW_MODAL);
            _dialogStage.initOwner(dialogStage);
            Scene scene = new Scene(page);
            _dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLSuningCateSelectedController controller = loader.getController();
            controller.setDialogStage(_dialogStage);
            controller.bind(mappingCate);

            // Show the dialog and wait until the user closes it
            _dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void handleBrandSelect(ActionEvent actionEvent) {

;

        if ( selectedItem.getMappingcategorycode()==null || selectedItem.getMappingcategorycode().trim().length()==0 ) {
            Alert _alert = new Alert(Alert.AlertType.INFORMATION);
            _alert.setTitle("信息");
            _alert.setHeaderText("请先选择上架类目");
            _alert.initOwner(dialogStage);
            _alert.show();

            return;
        }



        Brand mappingBrand = new Brand();
        mappingBrand.setCategoryCode(selectedItem.getMappingcategorycode());
        boolean okClicked = showBrandMappingEditDialog(mappingBrand);
        if (okClicked) {
            //mainApp.getPersonData().add(tempPerson);
            try {
                selectedItem.setMappingbrandcode(mappingBrand.getCode());
                selectedItem.setMappingbrandname(mappingBrand.getName());
                SQLExecutor.update(selectedItem);
                mappingbrandcode.setText(mappingBrand.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean showBrandMappingEditDialog(Brand mappingBrand) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/SuningBrandSelectDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage _dialogStage = new Stage();
            _dialogStage.setTitle("上架类目映射");
            _dialogStage.initModality(Modality.WINDOW_MODAL);
            _dialogStage.initOwner(dialogStage);
            Scene scene = new Scene(page);
            _dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLSuningBrandSelectedController controller = loader.getController();
            controller.setDialogStage(_dialogStage);
            controller.bind(mappingBrand);

            // Show the dialog and wait until the user closes it
            _dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
