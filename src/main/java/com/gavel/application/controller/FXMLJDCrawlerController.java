package com.gavel.application.controller;


import com.gavel.HttpUtils;
import com.gavel.application.DataPagination;
import com.gavel.application.MainApp;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Item;
import com.gavel.entity.SearchItem;
import com.gavel.entity.Task;
import com.gavel.jd.SkuPageLoader;
import com.gavel.utils.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.*;
import java.util.concurrent.*;

public class FXMLJDCrawlerController {

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
            tasks = SQLExecutor.executeQueryBeanList("select * from TASK  where TYPE = 'JD'  order by UPDATETIME desc", Task.class);
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
                List<Task> tasks = SQLExecutor.executeQueryBeanList("select * from TASK  where TYPE = 'JD' and STATUS <> 'success'  order by UPDATETIME desc", Task.class);
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

        Task task = taskQueue.poll();
        if ( task==null ) {
            return;
        }


        int pageNum = task.getPagenum();
        if ( pageNum > 0 ) {
            for (int i = 1; i <= pageNum; i++) {
               int count = SQLExecutor.intQuery("select count(1) from  SEARCHITEM where TASKID = ? and  PAGENUM = ?", task.getId(), i);
               if ( count > 0 ) {
                   continue;
               }

                String html = HttpUtils.get(task.getUrl() + "&page=" + i, "");


                Document doc = Jsoup.parse(html);
                Elements items = doc.select("div#plist li.gl-item div.j-sku-item");


                List<SearchItem> searchItems = new ArrayList<>();

                if ( items!=null && items.size() > 0 ) {

                    System.out.println(doc.selectFirst("div.f-pager .fp-text i"));
                    System.out.println(doc.selectFirst("div.f-result-sum span.num"));

                    int xh = 1;
                    for (Element item : items) {
                        System.out.println(item.selectFirst("div.p-name em").text());

                        System.out.print(item.attr("data-sku"));
                        System.out.print("\t" + item.attr("venderid"));
                        System.out.print("\t" + item.attr("jdzy_shop_id"));
                        System.out.println("\t" + item.attr("brand_id"));

                        String url = item.selectFirst("div.p-name a").attr("href");
                        if ( url.startsWith("//") ) {
                            url = "https:" + url;
                        }

                        System.out.println(item.selectFirst("div.p-price"));

                        SearchItem searchItem = new SearchItem();
                        searchItem.setId(task.getId() + "-" + i + "-" + xh);
                        searchItem.setTaskid(task.getId());
                        searchItem.setPagenum(i);
                        searchItem.setXh(xh++);
                        searchItem.setCode(item.attr("data-sku"));
                        searchItem.setUrl(url);
                        searchItem.setTitle(item.selectFirst("div.p-name em").text());
                        searchItem.setSkunum(1);
                        searchItem.setActual(0);
                        searchItem.setType("u");

                        SQLExecutor.insert(searchItem);

                        searchItems.add(searchItem);
                    }
                }

                Map<String, JsonObject> pricesMap = new HashMap<>();

                if ( searchItems!=null && searchItems.size() > 0 ) {

                    StringBuilder query = new StringBuilder("https://p.3.cn/prices/mgets?type=1&area=12_988_40034_0&pdbp=0&pdtk=&pdpin=hailinking1984&pin=hailinking1984&source=list_pc_front&skuIds=");

                    int cnt = 1;
                    StringBuilder skuids = new StringBuilder();


                    for (int i1 = 0; i1 < searchItems.size(); i1++) {
                        SearchItem searchItem = searchItems.get(i1);
                        skuids.append("J_").append(searchItem.getCode()).append("%2C");
                        if ( cnt++ >= 30 || i1==searchItems.size()-1 ) {
                            cnt = 1;

                            System.out.println(query.toString() + skuids.toString());

                            String text = HttpUtils.get(query.toString() + skuids.toString(), task.getUrl() + "&page=" + i);
                            JsonArray arrays = new JsonParser().parse(text).getAsJsonArray();
                            if ( arrays!=null && arrays.size() > 0 ) {
                                for (JsonElement array : arrays) {
                                    JsonObject object = (JsonObject)array;
                                    pricesMap.put(object.get("id").toString().replace("\"", ""), object);
                                }
                            }
                        }

                    }

                }

                for (SearchItem searchItem : searchItems) {

                    Item item = SQLExecutor.executeQueryBean("select * from ITEM  where CODE = ? ", Item.class, searchItem.getCode());
                    if ( item!=null ){
                        continue;
                    }

                    item = new Item();

                    item.setCode(searchItem.getCode());
                    item.setProductcode(searchItem.getCode());
                    item.setName(searchItem.getTitle());
                    item.setProductname(searchItem.getTitle());
                    item.setType("JD");
                    item.setUrl(searchItem.getUrl());

                    JsonObject priceObj = pricesMap.get("J_" + searchItem.getCode());
                    if ( priceObj!=null && priceObj.has("p") ) {
                        item.setPrice(priceObj.get("p").getAsFloat());
                    }

                    SQLExecutor.insert(item);

                }
            }
        }




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

        int total = searchItemList.size();
        for (int i = 0; i < total; i++) {
            boolean success = true;
            try {
                SearchItem searchItem = searchItemList.get(i);
                System.out.println("\r" + (i+1) + "/" + total + ". " + searchItem.getUrl() + ": " + searchItem.getSkunum());

                searchItem.setActual(searchItem.getSkunum());

                Item item =  SkuPageLoader.getInstance().loadPage(searchItem);
                searchItem.setStatus(SearchItem.Status.SUCCESS);
                if ( item==null ) {
                    searchItem.setActual(0);
                    searchItem.setStatus(SearchItem.Status.EXCEPTION);
                    searchItem.setRemarks("");
                    System.out.println("\t...... load failed!");
                    success = false;
                }

                SQLExecutor.update(searchItem);
            } catch (Exception e) {

            }
        }


        task.setStatus("success");
        SQLExecutor.update(task);
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
                //ItemSupplement.loadSearchItems(task);
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
            controller.setStartPage("https://imall.jd.com/");
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
