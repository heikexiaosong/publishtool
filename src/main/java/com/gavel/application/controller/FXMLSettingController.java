package com.gavel.application.controller;

import com.gavel.application.MainApp;
import com.gavel.config.APPConfig;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.*;
import com.gavel.suning.ItemparamterLoad;
import com.gavel.utils.StringUtils;
import com.google.gson.Gson;
import com.suning.api.SelectSuningResponse;
import com.suning.api.SuningResponse;
import com.suning.api.entity.item.*;
import com.suning.api.exception.SuningApiException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
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
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


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
    private TableColumn<CategoryMapping, Boolean> select;
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

    @FXML
    private CheckBox categoryAll;

    @FXML
    private Label selectedNum;

    @FXML
    private TextField cateKeyword;

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
    private TextField itemparamKeyword;

    private List<Itemparameter> itemparameters;

    // 品牌
    @FXML
    private TableView<BrandMapping> brandMapping;
    @FXML
    private TableColumn<BrandMapping, String> graingercode;
    @FXML
    private TableColumn<BrandMapping, String> name1;
    @FXML
    private TableColumn<BrandMapping, String> name2;
    @FXML
    private TableColumn<BrandMapping, String> replacename_zh;
    @FXML
    private TableColumn<BrandMapping, String> replacename_en;
    @FXML
    private TableColumn<BrandMapping, String> brand;
    @FXML
    private TableColumn<BrandMapping, String> brandname;

    @FXML
    private TextField brandKeyword;

    @FXML
    private TextArea log;

    private Map<String, SimpleStringProperty> paramers = new ConcurrentHashMap<>();
    private String paramsCategoryCode = "";


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



        // 类目属性 ----------

        // Initialize the person table with the two columns.
        select.setCellFactory(column -> new CheckBoxTableCell<>());
        select.setCellValueFactory(cellData -> {
            CategoryMapping cellValue = cellData.getValue();
            BooleanProperty property = cellValue.selectedProperty();

            // Add listener to handler change
            property.addListener((observable, oldValue, newValue) -> updateSelectStatus(cellValue, newValue));

            return property;
        });

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
            categories = SQLExecutor.executeQueryBeanList("select * from category where SUPPLIERCODE = ? ", Category.class, APPConfig.getInstance().getShopinfo().getCode());
        } catch (Exception e) {
            e.printStackTrace();
        }

        cateParams.setItems(FXCollections.observableArrayList(categories));



        // 品牌
        graingercode.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getGraingercode()));
        name1.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getName1()));
        name2.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getName2()));
        replacename_zh.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getReplacename_zh()));
        replacename_en.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getReplacename_en()));
        brand.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getBrand()));
        brandname.setCellValueFactory(cellData -> new SimpleStringProperty( cellData.getValue().getBrandname()));


        List<BrandMapping> brandMappings = null;
        try {
            brandMappings = SQLExecutor.executeQueryBeanList("select * from BRANDMAPPING where TASKID = ? ", BrandMapping.class, "0");
        } catch (Exception e) {
            e.printStackTrace();
            brandMappings = Collections.EMPTY_LIST;
        }

        brandMapping.setItems(FXCollections.observableArrayList(brandMappings));

    }

    private void updateSelectStatus(CategoryMapping cellValue, Boolean newValue) {
        cellValue.selectedProperty().setValue(newValue );

        int count = 0;
        for (CategoryMapping categoryMapping : cateMapping.getItems()) {
            if ( categoryMapping.isSelected() ) {
                count++;
            }
        }

        selectedNum.setText(String.valueOf(count));
    }

    /**
     * 类目属性详情
     * @param newValue
     */
    private void showCategoryDetails(Category newValue) {

        paramsCategoryCode = null;
        if ( newValue==null ) {
            return;
        }

        paramsCategoryCode = newValue.getCategoryCode();
        paramers.clear();
        params.getChildren().clear();

        itemparameters = null;
        try {
            itemparameters = SQLExecutor.executeQueryBeanList("select * from ITEMPARAMETER where categoryCode = ? and SUPPLIERCODE = ? order by isMust desc ", Itemparameter.class, paramsCategoryCode, APPConfig.getInstance().getShopinfo().getCode());
        } catch (Exception e) {
            e.printStackTrace();
            itemparameters = Collections.EMPTY_LIST;
        }

        if ( itemparameters.size()==0 ) {
            ItemparamterLoad.loadItemparameters(paramsCategoryCode, APPConfig.getInstance().getShopinfo().getCode());
        }

        try {
            itemparameters = SQLExecutor.executeQueryBeanList("select * from ITEMPARAMETER where categoryCode = ? and SUPPLIERCODE = ? order by isMust desc ", Itemparameter.class, paramsCategoryCode, APPConfig.getInstance().getShopinfo().getCode());
        } catch (Exception e) {
            e.printStackTrace();
            itemparameters = Collections.EMPTY_LIST;
        }


        int i1 = 0;
        for (Itemparameter itemparameter : itemparameters) {
            if ( "cmModel".equalsIgnoreCase(itemparameter.getParCode()) ) {
                continue;
            }

            if ( "001360".equalsIgnoreCase(itemparameter.getParCode()) ) {
                continue;
            }

            final SimpleStringProperty property = new SimpleStringProperty(itemparameter.getParam());
            paramers.put(itemparameter.getParCode(), property);


            Label label = new Label(itemparameter.getParName() + ": ");
            if ( "X".equalsIgnoreCase(itemparameter.getIsMust()) ) {
                label.setStyle("-fx-text-fill: red;");
            }

            params.add(label, 0, i1);
            params.add( new Label(itemparameter.getParUnit()), 2, i1);
            params.add( new Label(itemparameter.getParCode()), 3, i1);
            params.add( new Label(itemparameter.getParaTemplateDesc()), 4, i1);
            params.add( new Label(itemparameter.getDataType()), 5, i1);


            switch (itemparameter.getParType()) {
                case "1":
                case "2":

                    ComboBox<Itemparameter.ParOption> parOptionComboBox = new ComboBox<Itemparameter.ParOption>();

                    parOptionComboBox.getItems().addAll( itemparameter.getParOption());

                    parOptionComboBox.setOnAction((ActionEvent ev) -> {
                        Itemparameter.ParOption option = parOptionComboBox.getSelectionModel().getSelectedItem();
                        System.out.println(option.getParOptionCode() + ": " + option.getParOptionDesc());

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
                    try {
                        SQLExecutor.update(categoryMapping);
                        categoryMapping.setCategoryCode(category.getCategoryCode());
                        categoryMapping.setCategoryName(category.getCategoryName());
                        categoryMapping.setDescPath(category.getDescPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }

        }


        cateMapping.refresh();
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
        boolean okClicked = showCategoryMappingEditDialog(mappingCate, categoryMapping);
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
        cateMapping.refresh();
    }

    private boolean showCategoryMappingEditDialog(Category mappingCate, CategoryMapping selected) {
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

            controller.initparams((selected==null ? "" : selected.getName()), APPConfig.getInstance().getShopinfo().getCode());

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

        if ( paramsCategoryCode==null ) {
            return;
        }

        try {
            List<Itemparameter> cateParams = SQLExecutor.executeQueryBeanList("select * from ITEMPARAMETER where CATEGORYCODE = ? ", Itemparameter.class,  paramsCategoryCode);

            for (Itemparameter cateParam : cateParams) {
                if (paramers.containsKey(cateParam.getParCode()) ) {
                    SimpleStringProperty property = paramers.remove(cateParam.getParCode());
                    cateParam.setParam(property.getValue());
                    SQLExecutor.update(cateParam);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 品牌自动匹配
     * @param actionEvent
     */
    public void handleBrandAutoMappingAction(ActionEvent actionEvent) {


        List<Brand> brands = null;
        try {
            brands = SQLExecutor.executeQueryBeanList("select * from BRAND", Brand.class);
        } catch (Exception e) {
            e.printStackTrace();
            brands = Collections.EMPTY_LIST;
        }


        for (BrandMapping brandMapping : brandMapping.getItems()) {
            String name1 = brandMapping.getName1();
            String name2 = brandMapping.getName2();

            String brandName = name1.trim() + "(" + name2 +")";
            if ( name2.equalsIgnoreCase(name1) ) {
                brandName = name1.trim();
            }


            for (Brand brand : brands) {
                if ( brandName.equalsIgnoreCase(brand.getName())  ) {
                    try {
                        SQLExecutor.update(brandMapping);
                        brandMapping.setBrand(brand.getCode());
                        brandMapping.setBrandname(brand.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }


        }

        brandMapping.refresh();
    }

    /**
     * 品牌映射
     * @param actionEvent
     */
    public void handleBrandCateMappingAction(ActionEvent actionEvent) {

        BrandMapping _brandMapping = brandMapping.getSelectionModel().getSelectedItem();
        if ( _brandMapping==null ) {
            return;
        }

        Brand mappingBrand = new Brand();
        boolean okClicked = showBrandMappingEditDialog(mappingBrand);
        if (okClicked) {
            //mainApp.getPersonData().add(tempPerson);
            try {
                _brandMapping.setBrand(mappingBrand.getCode());
                _brandMapping.setBrandname(mappingBrand.getName());
                SQLExecutor.update(_brandMapping);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                brandMapping.refresh();
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
            Stage dialogStage = new Stage();
            dialogStage.setTitle("上架类目映射");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLSuningBrandSelectedController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bind(mappingBrand);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 品牌替换
     * @param actionEvent
     */
    public void handleBrandReplaceMappingAction(ActionEvent actionEvent) {
        BrandMapping _brandMapping = brandMapping.getSelectionModel().getSelectedItem();
        if ( _brandMapping==null ) {
            return;
        }

        boolean okClicked = showBrandReplaceEditDialog(_brandMapping);
        if (okClicked) {
            try {
                SQLExecutor.update(_brandMapping);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                brandMapping.refresh();
            }
        }
    }

    private boolean showBrandReplaceEditDialog(BrandMapping _brandMapping) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/SuningBrandReplaceDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("上架类目映射");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLSuningBrandReplaceController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bind(_brandMapping);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 类目 选择所有页
     * @param actionEvent
     */
    public void handleCateALlPageAction(ActionEvent actionEvent) {

        for (CategoryMapping categoryMapping : cateMapping.getItems()) {
            categoryMapping.selectedProperty().setValue(categoryAll.isSelected());
        }

        if ( categoryAll.isSelected() ) {
            selectedNum.setText(String.valueOf(cateMapping.getItems().size()));
        } else {
            selectedNum.setText("0");
        }

        cateMapping.refresh();

    }

    public void handleKeyReleasedAction(KeyEvent keyEvent) {

        if ( keyEvent.getCode().equals(KeyCode.ENTER) ) {
            String keyword = cateKeyword.getText().trim();

            List<CategoryMapping> categoryMappings = null;
            if ( keyword!=null && keyword.length()>0  ) {
                try {
                    String param = "%" + keyword  +"%";
                    categoryMappings = SQLExecutor.executeQueryBeanList("select * from categorymapping where CODE like ? or NAME like ?", CategoryMapping.class, param, param);
                } catch (Exception e) {
                    e.printStackTrace();
                    categoryMappings = Collections.EMPTY_LIST;
                }
            } else {
                try {
                    categoryMappings = SQLExecutor.executeQueryBeanList("select * from categorymapping", CategoryMapping.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    categoryMappings = Collections.EMPTY_LIST;
                }
            }

            cateMapping.setItems(FXCollections.observableArrayList(categoryMappings));
        }


    }

    /**
     * 类目 批量设置
     * @param actionEvent
     */
    public void handleCateBatchMappingAction(ActionEvent actionEvent) {

        Category mappingCate = new Category();

        CategoryMapping selected = null;
        for (CategoryMapping categoryMapping : cateMapping.getItems()) {
            if ( categoryMapping.isSelected() ) {
                selected = categoryMapping;
            }
        }

        boolean okClicked = showCategoryMappingEditDialog(mappingCate,  selected);
        if (okClicked) {
            for (CategoryMapping categoryMapping : cateMapping.getItems()) {
                if ( categoryMapping.isSelected() ) {
                    categoryMapping.setCategoryCode(mappingCate.getCategoryCode());
                    categoryMapping.setCategoryName(mappingCate.getCategoryName());
                    categoryMapping.setDescPath(mappingCate.getDescPath());
                    try {
                        SQLExecutor.update(categoryMapping);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        cateMapping.refresh();

    }

    public void itemparamKeywordAction(KeyEvent keyEvent) {

        if ( keyEvent.getCode().equals(KeyCode.ENTER) ) {
            String keyword = itemparamKeyword.getText().trim();

            List<Category> categories = null;
            if ( keyword!=null && keyword.length()>0  ) {
                try {
                    String param = "%" + keyword  +"%";
                    categories = SQLExecutor.executeQueryBeanList("select * from category where CATEGORYCODE like ? or DESCPATH like ?", Category.class, param, param);
                } catch (Exception e) {
                    e.printStackTrace();
                    categories = Collections.EMPTY_LIST;
                }
            } else {
                try {
                    categories = SQLExecutor.executeQueryBeanList("select * from category", Category.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    categories = Collections.EMPTY_LIST;
                }

            }
            cateParams.setItems(FXCollections.observableArrayList(categories));
        }
    }

    public void handleBrandKeyReleasedAction(KeyEvent keyEvent) {


        if ( keyEvent.getCode().equals(KeyCode.ENTER) ) {
            String keyword = brandKeyword.getText().trim();

            List<BrandMapping> brandMappings = null;
            if ( keyword!=null && keyword.length()>0  ) {
                try {
                    String param = "%" + keyword  +"%";
                    brandMappings = SQLExecutor.executeQueryBeanList("select * from BRANDMAPPING where TASKID = ? and ( graingercode like ? or name1 like ? or name2 like ? ) ", BrandMapping.class, "0", param, param, param);
                } catch (Exception e) {
                    e.printStackTrace();
                    brandMappings = Collections.EMPTY_LIST;
                }
            } else {
                try {
                    brandMappings = SQLExecutor.executeQueryBeanList("select * from BRANDMAPPING where TASKID = ? ", BrandMapping.class, "0");
                } catch (Exception e) {
                    e.printStackTrace();
                    brandMappings = Collections.EMPTY_LIST;
                }
            }

            brandMapping.setItems(FXCollections.observableArrayList(brandMappings));
        }
    }

    /**
     * 类目同步
     * @param actionEvent
     */
    public void handleCateSyncAction(ActionEvent actionEvent) {


        Shopinfo shopinfo = APPConfig.getInstance().getShopinfo();

        System.out.println(shopinfo.getName());

        log.appendText("开始同步[" + shopinfo.getName() + "]的类目数据\n");


        new Thread(new Runnable() {
            @Override
            public void run() {

                int pageNo = 1;

                CategoryQueryRequest request = new CategoryQueryRequest();
                request.setPageSize(50);
                request.setPageNo(pageNo);
                //        request.setCategoryName("家装建材及五金");
                //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
                request.setCheckParam(true);

                try {
                    CategoryQueryResponse response = APPConfig.getInstance().client().excute(request);
                    System.out.println("CategoryQueryRequest :" + response.getBody());

                    while ( response.getSnbody()!=null && response.getSnbody().getCategoryQueries()!=null && response.getSnbody().getCategoryQueries().size() == 50 ) {

                        final SelectSuningResponse.SnHead head = response.getSnhead();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                log.appendText(new Gson().toJson(head) + "\n");
                            }
                        });


                        for (CategoryQueryResponse.CategoryQuery categoryQuery : response.getSnbody().getCategoryQueries()) {
                            System.out.println(new Gson().toJson(categoryQuery));

                            Category category = new Category();

                            category.setSupplierCode(shopinfo.getCode());
                            category.setCategoryCode(categoryQuery.getCategoryCode());
                            category.setCategoryName(categoryQuery.getCategoryName());
                            category.setIsBottom(categoryQuery.getIsBottom());
                            category.setDescPath(categoryQuery.getDescPath());
                            category.setGrade(categoryQuery.getGrade());

                            try {
                                SQLExecutor.insert(category);
                            } catch (Exception e) {
                                System.out.println(categoryQuery.getCategoryCode() + "： " + e.getMessage());
                            }

                        }


                        pageNo += 1;
                        request.setPageNo(pageNo);
                        response = APPConfig.getInstance().client().excute(request);
                    }

                    if ( response.getSnbody()!=null && response.getSnbody().getCategoryQueries()!=null && response.getSnbody().getCategoryQueries().size() > 0 ) {

                        final SelectSuningResponse.SnHead head = response.getSnhead();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                log.appendText(new Gson().toJson(head) + "\n");
                            }
                        });


                        for (CategoryQueryResponse.CategoryQuery categoryQuery : response.getSnbody().getCategoryQueries()) {
                            System.out.println(new Gson().toJson(categoryQuery));

                            //新增

                            Category category = new Category();

                            category.setSupplierCode(shopinfo.getCode());
                            category.setCategoryCode(categoryQuery.getCategoryCode());
                            category.setCategoryName(categoryQuery.getCategoryName());
                            category.setIsBottom(categoryQuery.getIsBottom());
                            category.setDescPath(categoryQuery.getDescPath());
                            category.setGrade(categoryQuery.getGrade());

                            try {
                                SQLExecutor.insert(category);
                            } catch (Exception e) {
                                System.out.println(categoryQuery.getCategoryCode() + "： " + e.getMessage());
                            }
                        }
                    }
                } catch (SuningApiException e) {
                    e.printStackTrace();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            log.appendText(e.getMessage());
                        }
                    });
                } finally {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            log.appendText("同步完成\n");
                        }
                    });
                }
            }
        }).start();

    }

    /**
     * 品牌同步
     * @param actionEvent
     */
    public void handleBrandSyncAction(ActionEvent actionEvent) {

        Shopinfo shopinfo = APPConfig.getInstance().getShopinfo();

        System.out.println(shopinfo.getName());

        log.appendText("开始同步[" + shopinfo.getName() + "]的品牌数据\n");


        List<Category> _cates = null;
        try {
            _cates = SQLExecutor.executeQueryBeanList("select * from  CATEGORY where SUPPLIERCODE = ? ", Category.class, shopinfo.getCode());
        } catch (Exception e) {
            e.printStackTrace();
            _cates = Collections.EMPTY_LIST;
        }
        log.appendText("店铺[" + shopinfo.getName() + "]类目: " + _cates.size());


        final  List<Category> cates = _cates;
        if ( cates.size() > 0 ) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    for (int i = 0; i < cates.size(); i++) {
                        Category cate = cates.get(i);
                        final String msg = "[" +(i+1) + "/" + cates.size()  + "] " + cate.getCategoryCode() + " - " + cate.getCategoryName();
                        Platform.runLater( () -> log.appendText(msg + "\n"));
                        try {
                            queryBrand(cate.getCategoryCode(), shopinfo.getCode());
                        } catch (Exception e) {
                            Platform.runLater( () -> log.appendText(e.getMessage() + "\n"));
                        }
                    }

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            log.appendText("同步完成\n");
                        }
                    });
                }
            }).start();
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    log.appendText("同步完成\n");
                }
            });
        }
    }

    private void queryBrand(String categoryCode, String shopid)  {

        int pageNo = 1;

        BrandQueryRequest request = new BrandQueryRequest();
        request.setPageSize(50);
        request.setPageNo(pageNo);
        request.setCategoryCode(categoryCode);
//        request.setCategoryName("家装建材及五金");
        //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
        request.setCheckParam(true);

        try {
            BrandQueryResponse response = APPConfig.getInstance().client().excute(request);

            while ( response.getSnbody()!=null && response.getSnbody().getBrandQueries()!=null && response.getSnbody().getBrandQueries().size() == 50 ) {
                final SelectSuningResponse.SnHead head = response.getSnhead();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        log.appendText(new Gson().toJson(head) + "\n");
                    }
                });

                for (BrandQueryResponse.BrandQuery brandQuery : response.getSnbody().getBrandQueries()) {
                    //新增
                    Brand brand = new Brand();
                    brand.setSupplierCode(shopid);
                    brand.setCategoryCode(categoryCode);
                    brand.setCode(brandQuery.getBrandCode());
                    brand.setName(brandQuery.getBrandName());
                    try {
                        SQLExecutor.insert(brand);
                    } catch (Exception e) {
                        System.out.println(categoryCode + "-" + brandQuery.getBrandCode() + "： " + e.getMessage());
                    }

                }

                pageNo += 1;
                request.setPageNo(pageNo);
                response = APPConfig.getInstance().client().excute(request);
            }

            if (response.getSnbody()!=null && response.getSnbody().getBrandQueries()!=null && response.getSnbody().getBrandQueries().size() > 0 ) {
                final SelectSuningResponse.SnHead head = response.getSnhead();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        log.appendText(new Gson().toJson(head) + "\n");
                    }
                });
                for (BrandQueryResponse.BrandQuery brandQuery : response.getSnbody().getBrandQueries()) {
                    Brand brand = new Brand();
                    brand.setSupplierCode(shopid);
                    brand.setCategoryCode(categoryCode);
                    brand.setCode(brandQuery.getBrandCode());
                    brand.setName(brandQuery.getBrandName());
                    try {
                        SQLExecutor.insert(brand);
                    } catch (Exception e) {
                        System.out.println(categoryCode + "-" + brandQuery.getBrandCode() + "： " + e.getMessage());
                    }
                }

            }

        } catch (SuningApiException e) {
            e.printStackTrace();
        }

    }

    /**
     * 类目属性同步
     * @param actionEvent
     */
    public void handleParamterSyncAction(ActionEvent actionEvent) {


        Shopinfo shopinfo = APPConfig.getInstance().getShopinfo();

        System.out.println(shopinfo.getName());

        log.appendText("开始同步[" + shopinfo.getName() + "]的类目属性数据\n");


        List<Category> _cates = null;
        try {
            _cates = SQLExecutor.executeQueryBeanList("select * from  CATEGORY where SUPPLIERCODE = ? ", Category.class, shopinfo.getCode());
        } catch (Exception e) {
            e.printStackTrace();
            _cates = Collections.EMPTY_LIST;
        }
        log.appendText("店铺[" + shopinfo.getName() + "]类目: " + _cates.size());


        final  List<Category> cates = _cates;
        if ( cates.size() > 0 ) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    for (int i = 0; i < cates.size(); i++) {
                        Category cate = cates.get(i);
                        final String msg = "[" +(i+1) + "/" + cates.size()  + "] " + cate.getCategoryCode() + " - " + cate.getCategoryName();
                        Platform.runLater( () -> log.appendText(msg + "\n"));
                        try {
                            loadItemparameters(cate.getCategoryCode(), shopinfo.getCode());
                        } catch (Exception e) {
                            Platform.runLater( () -> log.appendText(e.getMessage() + "\n"));
                        }
                    }

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            log.appendText("同步完成\n");
                        }
                    });
                }
            }).start();
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    log.appendText("同步完成\n");
                }
            });
        }

    }

    private List<Itemparameter> loadItemparameters(String categoryCode, String shopid) {

        List<Itemparameter> itemparameters = new ArrayList<>();
        if ( categoryCode==null || categoryCode.trim().length()<=0 ) {
            return itemparameters;
        }

        ItemparametersQueryRequest request = new ItemparametersQueryRequest();
        request.setCategoryCode(categoryCode);


        int next = 1;
        int total = 1;

        while ( next <= total ) {
            request.setPageNo(next);
            request.setPageSize(20);
            //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
            request.setCheckParam(true);

            try {
                ItemparametersQueryResponse response = APPConfig.getInstance().client().excute(request);
                System.out.println("NationQueryRequest :" + response.getBody());

                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {

                    next = response.getSnhead().getPageNo() + 1;
                    total = response.getSnhead().getPageTotal();

                    final SelectSuningResponse.SnHead head = response.getSnhead();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            log.appendText(new Gson().toJson(head) + "\n");
                        }
                    });

                    for (ItemparametersQueryResponse.ItemparametersQuery item : response.getSnbody().getItemparametersQueries()) {

                        Itemparameter itemparameter = new Itemparameter();

                        itemparameter.setCategoryCode(categoryCode.trim());
                        itemparameter.setSupplierCode(shopid);
                        itemparameter.setParaTemplateCode(item.getParaTemplateCode());
                        itemparameter.setParaTemplateDesc(item.getParaTemplateDesc());
                        itemparameter.setParCode(item.getParCode());
                        itemparameter.setParName(item.getParName());
                        itemparameter.setParType(item.getParType());
                        itemparameter.setParUnit(item.getParUnit());
                        itemparameter.setDataType(item.getDataType());
                        itemparameter.setIsMust(item.getIsMust());
                        itemparameter.setOptions(new Gson().toJson(item.getParOption()));

                        System.out.println(new Gson().toJson(item));

                        if ( item.getParOption()!=null &&  item.getParOption().size() == 1 ) {
                            ItemparametersQueryResponse.ParOption option = item.getParOption().get(0);
                            itemparameter.setParam(option.getParOptionCode());
                            if (StringUtils.isBlank(option.getParOptionCode())) {
                                itemparameter.setParam(option.getParOptionDesc());
                            }
                        }


                        try {
                            SQLExecutor.insert(itemparameter);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }



            } catch (SuningApiException e) {
                e.printStackTrace();
            }

        }



        return itemparameters;
    }
}
