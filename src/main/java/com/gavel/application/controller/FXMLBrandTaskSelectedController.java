package com.gavel.application.controller;

import com.gavel.application.IDCell;
import com.gavel.crawler.DriverHtmlLoader;
import com.gavel.entity.Task;
import com.gavel.utils.StringUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class FXMLBrandTaskSelectedController {

    @FXML
    private TextField keyword;

    @FXML
    private TableView<Task> skuList;

    @FXML
    private TableColumn<Task, Boolean> select;
    @FXML
    private TableColumn<Task, String> noCol;
    @FXML
    private TableColumn<Task, String> titleCol;
    @FXML
    private TableColumn<Task, String> urlCol;
    @FXML
    private TableColumn<Task, String> pagenumCol;
    @FXML
    private TableColumn<Task, String> productnumCol;
    @FXML
    private TableColumn<Task, String> skunumCol;
    @FXML
    private TableColumn<Task, String> statusCol;


    private Stage dialogStage;
    private boolean okClicked = false;

    private List<Task> items = new ArrayList<>();


    private  List<Task> bindTasks = null;

    private boolean singleSelected = false;


    @FXML
    private void initialize() {

        select.setCellFactory(column -> new CheckBoxTableCell<>());
        select.setCellValueFactory(cellData -> {
            Task cellValue = cellData.getValue();
            BooleanProperty property = cellValue.selectedProperty();

            // Add listener to handler change
            property.addListener((observable, oldValue, newValue) -> cellValue.setSelected(newValue));

            return property;
        });

        noCol.setCellFactory(new IDCell<>());
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        urlCol.setCellValueFactory(cellData -> cellData.getValue().urlProperty());
        pagenumCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPagenum())));
        productnumCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getProductnum())));
        skunumCol.setCellValueFactory(cellData ->  new SimpleStringProperty(String.valueOf(cellData.getValue().getSkunum())));
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());


        skuList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateStatus(newValue));

        skuList.setItems(FXCollections.observableList(items));
    }

    private void updateStatus(Task newValue) {

        newValue.setSelected(!newValue.isSelected());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        for (Task item : items) {
            if ( item.isSelected() ) {
                bindTasks.add(item);
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


    public void bindItems(List<Task> _task) {
        this.bindTasks = _task;
    }

    public void setSingleSelected(boolean singleSelected) {
        this.singleSelected = singleSelected;
    }

    public void handleSearchAction(ActionEvent actionEvent) {

         String _keyword = keyword.getText();
         if (StringUtils.isBlank(_keyword)) {
             skuList.setItems(FXCollections.observableList(items));
             return;
         }


         List<Task> filterItems = new ArrayList<>();
        for (Task item : items) {
            if ( item.getTitle().contains(_keyword.trim()) ) {
                filterItems.add(item);
            }
        }

        skuList.setItems(FXCollections.observableList(filterItems));
    }

    public void setPage(String url, StringWriter writer) {
        String html = null;

        if (writer!=null ) {
            html = writer.toString();
        }

        if ( html==null || html.trim().length() <= 0 ) {
            html =  DriverHtmlLoader.getInstance().loadHtml(url, 10000);
        }

        Document doc = Jsoup.parse(html);

        String cate = "";
        Elements crumbs = doc.select("div.crumbs-nav-item span.curr");
        if ( crumbs!=null && crumbs.size() > 0 ) {
            Element crumb = crumbs.get(crumbs.size() - 1);
            cate = crumb.text();
        }



        long cur = System.currentTimeMillis();
        Elements brands = doc.select("ul#brandsArea li a");
        if ( brands==null ||  brands.size() == 0 ) {
            brands = doc.select("div.s-brand ul.J_valueList li a");
        }
        if ( brands!=null && brands.size()>0 ) {
            items = new ArrayList<>();
            for (Element brand : brands) {
                Task _task = new Task();
                _task.setId(String.valueOf(cur++));
                _task.setTitle(brand.attr("title"));
                String href = brand.attr("href");
                if ( href.startsWith("search") ) {
                    _task.setUrl(url.substring(0, url.indexOf("jd.com") + 7) + brand.attr("href"));
                } else {
                    String query = href.split("\\?")[1];
                    String[] params = query.split("&");
                    if ( params!=null && params.length > 0 ) {
                        for (String param : params) {
                            if ( param.startsWith("ev=") ) {
                                _task.setUrl(url + "&" + param);
                                break;
                            }
                        }
                    }
                }


//                String href = brand.attr("href");


                _task.setBrand(brand.attr("title"));
                _task.setCategory(cate);
                _task.setType("JD");
                _task.setStatus("init");
                items.add(_task);
            }
        }

        skuList.setItems(FXCollections.observableList(items));
    }
}
