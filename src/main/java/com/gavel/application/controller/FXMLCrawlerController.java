package com.gavel.application.controller;


import com.gavel.application.DataPagination;
import com.gavel.application.MainApp;
import com.gavel.crawler.DriverHtmlLoader;
import com.gavel.crawler.ItemSupplement;
import com.gavel.crawler.ProductPageLoader;
import com.gavel.crawler.SkuPageLoader;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.*;
import com.gavel.utils.StringUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class FXMLCrawlerController {

    @FXML
    private AnchorPane root;

    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }

    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn<Task, String> title;
    @FXML
    private TableColumn<Task, String> url;
    @FXML
    private TableColumn<Task, String> status;

    @FXML
    private Label taskId;
    @FXML
    private Label taskTitle;
    @FXML
    private Label taskUrl;
    @FXML
    private Label taskPagenum;
    @FXML
    private Label taskProductnum;
    @FXML
    private Label taskSkunum;

    @FXML
    private TableView<SearchItem> searchList;
    @FXML
    private TableColumn<SearchItem, Integer> pagenumCol;
    @FXML
    private TableColumn<SearchItem, Integer> xhCol;
    @FXML
    private TableColumn<SearchItem, String> typeCol;
    @FXML
    private TableColumn<SearchItem, String> codeCol;
    @FXML
    private TableColumn<SearchItem, String> titleCol;
    @FXML
    private TableColumn<SearchItem, String> picCol;
    @FXML
    private TableColumn<SearchItem, String> urlCol;
    @FXML
    private TableColumn<SearchItem, Integer> skunumCol;
    @FXML
    private TableColumn<SearchItem, Integer> actualCol;
    @FXML
    private TableColumn<SearchItem, String> statusCol;

    @FXML
    private Pagination pagination;

    @FXML
    private TextField keyword;


    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private BlockingQueue<Task> taskQueue = new LinkedBlockingDeque<>();

    @FXML
    private void initialize() {

        title.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        url.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUrl()));
        status.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));


        // Clear person details.
        showTaskDetails(null);

        // Listen for selection changes and show the person details when changed.
        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTaskDetails(newValue));


        List<Task> tasks = null;
        try {
            tasks = SQLExecutor.executeQueryBeanList("select * from TASK order by UPDATETIME desc", Task.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        taskTable.setItems(FXCollections.observableArrayList(tasks));


        // 搜索结果列表
        pagenumCol.setCellValueFactory(new PropertyValueFactory<>("pagenum"));
        xhCol.setCellValueFactory(new PropertyValueFactory<>("xh"));
        typeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCode()));
        titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        picCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPic()));
        urlCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUrl()));
        skunumCol.setCellValueFactory(new PropertyValueFactory<>("skunum"));
        actualCol.setCellValueFactory(new PropertyValueFactory<>("actual"));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));


        pagination.setPageCount(0);
        pagination.setMaxPageIndicatorCount(0);



        //
        if (  tasks!=null ) {
            for (Task task : tasks) {
                if ( StringUtils.isBlank(task.getStatus())  || !"success".equalsIgnoreCase(task.getStatus())  ) {
                    try {
                        taskQueue.put(task);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    loadSkus();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 3000, 5000, TimeUnit.MILLISECONDS);
    }



    private void loadSkus() throws Exception {

        System.out.println("Task: " + taskQueue.size());

        if ( taskQueue.isEmpty() ) {
            try {
                List<Task> tasks = SQLExecutor.executeQueryBeanList("select * from TASK  where STATUS is null or STATUS <> 'success' order by UPDATETIME desc", Task.class);
                System.out.println("未完成Task: " + taskQueue.size());
                if ( tasks!=null && tasks.size()>0 ) {
                    for (Task task : tasks) {
                        taskQueue.put(task);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if ( taskQueue.isEmpty() ) {

            try {
                List<BrandInfo> brandInfos = SQLExecutor.executeQueryBeanList("select * from BRAND_INFO where FLAG is null or FLAG <> 'X' order by SKUNUM desc", BrandInfo.class);
                System.out.println("brandInfos: " + brandInfos.size());
                if ( brandInfos!=null && brandInfos.size() > 0 ) {
                    for (BrandInfo brandInfo : brandInfos) {
                        System.out.println("品牌Task: " + brandInfo.getName1());
                        String url = brandInfo.getUrl();

                        try {
                            Task task = SQLExecutor.executeQueryBean("select * from TASK  where URL = ?", Task.class, url);
                            if ( task!=null ) {
                                System.out.println("Brand: " + brandInfo.getName1() + " exist!");
                                brandInfo.setFlag("X");
                                SQLExecutor.update(brandInfo);
                                continue;
                            }

                            System.out.println("Brand: " + brandInfo.getName1() + " run...");

                            task = new Task();

                            task.setUrl(url);
                            task.setTitle(brandInfo.getName1() + " " + ( brandInfo.getName2()==null ? "" : brandInfo.getName2() ));
                            task.setStatus("init");



                            try {
                                HtmlCache htmlCache = DriverHtmlLoader.getInstance().loadHtmlPage(url);
                                if ( htmlCache!=null && htmlCache.getHtml()!=null ) {

                                    Document document = Jsoup.parse(htmlCache.getHtml());
                                    task.setTitle(document.title());

                                    Elements wrappers = document.select("div.wrapper h3");
                                    if ( wrappers!=null ) {
                                        if ( "暂无商品".equalsIgnoreCase(wrappers.text()) ){
                                            try {
                                                task.setStatus("success");
                                                task.setPagenum(0);
                                                task.setProductnum(0);
                                                task.setSkunum(0);
                                                task.setUpdatetime(Calendar.getInstance().getTime());

                                                SQLExecutor.insert(task);
                                                taskTable.getItems().add(0, task);

                                                brandInfo.setFlag("X");
                                                SQLExecutor.update(brandInfo);
                                            } catch (Exception e) {
                                                System.out.println("[Task]" +brandInfo.getName1() + "任务生成失败");
                                            }

                                            return;
                                        }
                                    }



                                    Element cpz = document.selectFirst("font.cpz");
                                    if ( cpz==null ) {
                                        continue;
                                    }
                                    Element total = document.selectFirst("font.total");
                                    System.out.println("产品组: " + cpz.text() + "; 产品: " + total.text());

                                    task.setProductnum(Integer.parseInt(cpz.text()));
                                    task.setSkunum(Integer.parseInt(total.text()));

                                    int pageCur = 0;
                                    int pageTotal = 0;
                                    Elements labels = document.select("div.pagination > label");
                                    if ( labels.size()==2 ) {
                                        pageCur = Integer.parseInt(labels.get(0).text());
                                        pageTotal = Integer.parseInt(labels.get(1).text());
                                    }

                                    System.out.println("当前页: " + pageCur);
                                    System.out.println("总页数: " + pageTotal);

                                    task.setPagenum(pageTotal);


                                    task.setTitle(document.title());
                                    task.setUpdatetime(Calendar.getInstance().getTime());


                                    try {
                                        SQLExecutor.insert(task);
                                        taskTable.getItems().add(0, task);

                                        ItemSupplement.loadSearchItems(task);

                                        taskQueue.put(task);
                                        brandInfo.setFlag("X");
                                        SQLExecutor.update(brandInfo);
                                    } catch (Exception e) {
                                        System.out.println("[Task]" +brandInfo.getName1() + "任务生成失败");
                                    }

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }



                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        Task task = taskQueue.poll();
        if ( task==null ) {
            return;
        }

        task.setStatus("正在爬取...");

        System.out.println("爬取任务: " + task.getTitle());

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                taskTable.getSelectionModel().select(task);
            }
        });


        List<SearchItem> searchItemList =   SQLExecutor.executeQueryBeanList("select * from  SEARCHITEM where TASKID = ? and SKUNUM <> ACTUAL order by PAGENUM, XH ", SearchItem.class, task.getId());

        System.out.println("SearchItem: " + searchItemList.size());

        if ( searchItemList==null || searchItemList.size() ==0 ) {
            task.setStatus("success");
            try {
                SQLExecutor.update(task);
            } catch (Exception e) {

            }
            return;
        }

        boolean complete = true;

        int total = searchItemList.size();
        for (int i = 0; i < total; i++) {
            boolean success = true;
            try {
                SearchItem searchItem = searchItemList.get(i);
                System.out.println("\r" + (i+1) + "/" + total + ". " + searchItem.getUrl() + ": " + searchItem.getSkunum());

                searchItem.setActual(searchItem.getSkunum());
                if ( searchItem.getType().equalsIgnoreCase("g") ){

                    List<Item>  skus = ProductPageLoader.getInstance().loadPage(searchItem);
                    searchItem.setStatus(SearchItem.Status.SUCCESS);
                    if ( skus==null || skus.size() < searchItem.getSkunum() ) {
                        searchItem.setActual((skus==null ? 0: skus.size()));
                        searchItem.setStatus(SearchItem.Status.EXCEPTION);
                        searchItem.setRemarks("预期: " + searchItem.getSkunum() + "; 实际: " + (skus==null ? 0: skus.size()));
                        System.out.println("\t...... 预期: " + searchItem.getSkunum() + "; 实际: " + (skus==null ? 0: skus.size()) );
                        success = false;
                        complete = false;
                    }

                } else {
                    Item item =  SkuPageLoader.getInstance().loadPage(searchItem);
                    searchItem.setStatus(SearchItem.Status.SUCCESS);
                    if ( item==null ) {
                        searchItem.setActual(0);
                        searchItem.setStatus(SearchItem.Status.EXCEPTION);
                        searchItem.setRemarks("");
                        System.out.println("\t...... load failed!");
                        success = false;
                        complete = false;
                    }
                }

                SQLExecutor.update(searchItem);
                if ( !success ) {
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {

                    }
                }
            } catch (Exception e) {

            }
        }

        if ( complete ) {
            task.setStatus("success");
            SQLExecutor.update(task);
        }
        System.out.println("爬取任务[" + task.getTitle() + "]完成一轮.");
    }

    private void showTaskDetails(Task task) {
        if ( task==null ) {
            pagination.setPageCount(0);
        } else {

            taskId.setText(task.getId());
            taskTitle.setText(task.getTitle());
            taskUrl.setText(task.getUrl());
            taskPagenum.setText(String.valueOf(task.getPagenum()));
            taskProductnum.setText(String.valueOf(task.getProductnum()));
            taskSkunum.setText(String.valueOf(task.getSkunum()));

            List<SearchItem> searchItems = null;
            try {
                searchItems = SQLExecutor.executeQueryBeanList("select * from SEARCHITEM where taskid = ? ", SearchItem.class, task.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            DataPagination dataPagination = new DataPagination(searchItems, 30);

            pagination.pageCountProperty().bindBidirectional(dataPagination.totalPageProperty());

            pagination.setPageFactory(pageIndex -> {
                searchList.setItems(FXCollections.observableList(dataPagination.getCurrentPageDataList(pageIndex)));
                return searchList;
            });

        }
    }

    /**
     * 新增任务
     * @param actionEvent
     */
    public void handleNewTaskAction(ActionEvent actionEvent) {
        Task task = new Task();
        boolean okClicked = showAddTaskDialog(task);
        if (okClicked) {

            boolean exist = false;
            try {
                int count = SQLExecutor.intQuery("select count(1) from  TASK where URL = ? ",  task.getUrl());
                exist = (count > 0);
            } catch (Exception e) {

            }

            if ( exist ) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", new ButtonType("取消", ButtonBar.ButtonData.NO), new ButtonType("确定", ButtonBar.ButtonData.YES));
                alert.initOwner(stage());
                alert.setTitle("信息确认");
                alert.setHeaderText("此页面已经采集过, 需要重新采集?");
                alert.setContentText("标题: " + task.getTitle() + "\nURL: " + task.getUrl());

                Optional<ButtonType> _buttonType = alert.showAndWait();

                if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.NO)){
                    return;
                }
            }

            try {
                SQLExecutor.insert(task);
                taskTable.getItems().add(0, task);
                taskTable.getSelectionModel().select(task);
                ItemSupplement.loadSearchItems(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean showAddTaskDialog(Task task) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/TaskAddDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("选择采集页面");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLTaskAddDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bindTask(task);
            dialogStage.setMaximized(true);
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 删除任务
     * @param actionEvent
     */
    public void handleDelTaskAction(ActionEvent actionEvent) {

        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if ( selectedTask==null ) {
            return;
        }


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", new ButtonType("取消", ButtonBar.ButtonData.NO), new ButtonType("确定", ButtonBar.ButtonData.YES));
        alert.initOwner(stage());
        alert.setTitle("删除确认");
        alert.setHeaderText("确定删除采集任务[" + selectedTask.getTitle() +"]?");

        Optional<ButtonType> _buttonType = alert.showAndWait();

        if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES)){

            try {
                SQLExecutor.delete(selectedTask);
                taskTable.getItems().remove(selectedTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handleSearchAction(ActionEvent actionEvent) {

        String _keyword = keyword.getText();


        List<Task> tasks = null;
        if (StringUtils.isBlank(_keyword)) {
            try {
                tasks = SQLExecutor.executeQueryBeanList("select * from TASK order by UPDATETIME desc", Task.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                tasks = SQLExecutor.executeQueryBeanList("select * from TASK where TITLE like ? order by UPDATETIME desc ", Task.class, "%" + _keyword.trim()  + "%");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if ( tasks==null ) {
            tasks = Collections.EMPTY_LIST;
        }

        taskTable.setItems(FXCollections.observableArrayList(tasks));

    }
}
