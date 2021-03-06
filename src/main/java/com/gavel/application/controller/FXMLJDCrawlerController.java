package com.gavel.application.controller;


import com.gavel.HttpUtils;
import com.gavel.application.DataPagination;
import com.gavel.application.MainApp;
import com.gavel.crawler.DriverHtmlLoader;
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
import java.io.StringWriter;
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


    public static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private BlockingQueue<Task> taskQueue = new LinkedBlockingDeque<>();

    @FXML
    private void initialize() {

        title.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        url.setCellValueFactory(cellData -> cellData.getValue().urlProperty());
        status.setCellValueFactory(cellData -> cellData.getValue().statusProperty());


        // Clear person details.
        showTaskDetails(null);

        // Listen for selection changes and show the person details when changed.
        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTaskDetails(newValue));

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


        List<Task> tasks = refresh();

        //
        if ( tasks!=null ) {
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

    private List<Task> refresh(){

        Task selected =  taskTable.getSelectionModel().getSelectedItem();

        List<Task> tasks = null;
        try {
            tasks = SQLExecutor.executeQueryBeanList("select * from TASK  where TYPE = 'JD' order by UPDATETIME desc", Task.class);
        } catch (Exception e) {
            e.printStackTrace();
            tasks = Collections.EMPTY_LIST;
        }

        taskTable.setItems(FXCollections.observableArrayList(tasks));
        if ( selected!=null ) {
            for (Task task : taskTable.getItems()) {
                if ( task.getId().equalsIgnoreCase(selected.getId()) ) {
                    Platform.runLater(() -> taskTable.getSelectionModel().select(task));
                    break;
                }
            }
        }
        return taskTable.getItems();

    }



    private void loadSkus() throws Exception {

        System.out.println("Task: " + taskQueue.size());

        if ( taskQueue.isEmpty() ) {
            List<Task> tasks = refresh();
            if ( tasks!=null ) {
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

        }

        Task task = taskQueue.poll();
        if ( task==null ) {
            return;
        }

        Platform.runLater(() -> taskTable.getSelectionModel().select(task));

        int pageCur = 1;
        int pageTotal = 1;

        while ( pageCur <= pageTotal ) {

            int pageParams = (pageCur*2 - 1);
            if ( task.getUrl()!=null && task.getUrl().contains("i-list.jd.com") ) {
                pageParams = pageCur;
            }

            String html = DriverHtmlLoader.getInstance().loadHtml(task.getUrl() + "&page=" + pageParams);

            Document doc = Jsoup.parse(html);

            pageCur = Integer.parseInt(doc.selectFirst("div.f-pager .fp-text b").text());
            pageTotal = Integer.parseInt(doc.selectFirst("div.f-pager .fp-text i").text());

            int count = SQLExecutor.intQuery("select count(1) from  SEARCHITEM where TASKID = ? and  PAGENUM = ?", task.getId(), pageCur);
            if ( count > 0 ) {
                pageCur++;
                continue;
            }


            // 根据上面的商品结果，为您推荐的相似商品。 删除
            Element diviner = doc.selectFirst("div#J_goodsList ul.J_diviner");
            if ( diviner!=null ) {
                diviner.remove();
            }

            Elements items = doc.select("div#J_goodsList li.gl-item");
            if ( items==null || items.size()==0 ) {
                items = doc.select("div#plist li.gl-item div.j-sku-item");
            }

            List<SearchItem> searchItems = new ArrayList<>();

            if ( items!=null && items.size() > 0 ) {

                System.out.println(doc.selectFirst("div.f-pager .fp-text i"));
                System.out.println(doc.selectFirst("div.f-result-sum span.num"));

                int xh = 1;
                for (Element item : items) {


                    String own = "N";
                    Elements icons = item.select("div.J-pro-icons i");
                    if ( icons!=null && icons.size() > 0 ) {
                        for (Element icon : icons) {
                            if ( "自营".equalsIgnoreCase(icon.text()) ) {
                                own = "Y";
                                break;
                            }
                        }
                    }

                    icons = item.select("div.p-icons i");
                    if ( icons!=null && icons.size() > 0 ) {
                        for (Element icon : icons) {
                            if ( "自营".equalsIgnoreCase(icon.text()) ) {
                                own = "Y";
                                break;
                            }
                        }
                    }

                    System.out.println("[" + pageCur + "][" + xh +"]" + item.selectFirst("div.p-name em").text() + "\t" + own);

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
                    searchItem.setId(task.getId() + "-" + pageCur + "-" + xh);
                    searchItem.setTaskid(task.getId());
                    searchItem.setPagenum(pageCur);
                    searchItem.setXh(xh++);
                    searchItem.setCode(item.attr("data-sku"));
                    searchItem.setUrl(url);
                    searchItem.setTitle(item.selectFirst("div.p-name em").text());
                    searchItem.setSkunum(1);
                    searchItem.setActual(0);
                    searchItem.setType("u");

                    searchItem.setOwn(own);



                    Element shopname = item.selectFirst("a.hd-shopname");
                    if ( shopname==null ) {
                        shopname = item.selectFirst("a.hd-shopname");
                    }
                    if ( shopname!=null ) {
                        String _shop = shopname.attr("title");
                        if ( StringUtils.isBlank(_shop) ) {
                            _shop = shopname.text();
                        }
                        searchItem.setShop(_shop);
                    }


                    Element stock = item.selectFirst("div.p-stock");
                    if ( stock!=null ) {
                        searchItem.setStock(stock.text());
                    }

                    SQLExecutor.insert(searchItem);

                    searchItems.add(searchItem);
                }
            }

            Map<String, JsonObject> pricesMap = new HashMap<>();

            if ( searchItems!=null && searchItems.size() > 0 ) {

                StringBuilder query = new StringBuilder("https://p.3.cn/prices/mgets?pin=hailinking1984&type=1&skuIds=");

                int cnt = 1;
                StringBuilder skuids = new StringBuilder();


                for (int i1 = 0; i1 < searchItems.size(); i1++) {
                    SearchItem searchItem = searchItems.get(i1);
                    skuids.append("J_").append(searchItem.getCode()).append("%2C");
                    if ( cnt++ >= 100 || i1==searchItems.size()-1 ) {
                        cnt = 1;

                        System.out.println(query.toString() + skuids.toString());

                        String text = HttpUtils.get(query.toString() + skuids.toString(), task.getUrl() + "&page=" + pageParams);
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
                item.setCategory(task.getCategory());
                item.setCategoryname(task.getCategory());
                item.setBrand(task.getBrand());
                item.setBrandname(task.getBrand());
                item.setOwn(searchItem.getOwn());
                item.setShop(searchItem.getShop());
                item.setStock(searchItem.getStock());

                JsonObject priceObj = pricesMap.get("J_" + searchItem.getCode());
                if ( priceObj!=null && priceObj.has("op") ) {
                    item.setPrice(priceObj.get("op").getAsFloat());
                }

                SQLExecutor.insert(item);

            }
            pageCur++;
        }



        List<SearchItem> searchItemList =   SQLExecutor.executeQueryBeanList("select * from  SEARCHITEM where TASKID = ? and SKUNUM <> ACTUAL order by PAGENUM, XH ", SearchItem.class, task.getId());

        System.out.println("[" + task.getTitle() + "][SearchItem][待处理]: " + searchItemList.size());

        if ( searchItemList==null || searchItemList.size() ==0 ) {
            task.setStatus("success");

            Platform.runLater(() ->  refresh());

            try {
                SQLExecutor.update(task);
            } catch (Exception e) {

            }
            return;
        }

        int total = searchItemList.size();
        boolean success = true;
        for (int i = 0; i < total; i++) {

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

        if ( success ) {
            task.setStatus("success");
            Platform.runLater(() ->  refresh());
            try {
                SQLExecutor.update(task);
            } catch (Exception e) {

            }
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
        Task task1 = new Task();

        StringWriter writer = new StringWriter();

        boolean okClicked = showAddTaskDialog(task1, writer);
        if (okClicked) {


            List<Task> tasks = new ArrayList<>();
            okClicked = showSelectBrandTaskDialog(tasks, task1.getUrl(), writer);

            if ( okClicked && tasks.size() > 0 ) {

                for (Task task : tasks) {
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
                        taskQueue.put(task);
                        //ItemSupplement.loadSearchItems(task);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean showAddTaskDialog(Task task, StringWriter writer) {
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
            controller.bind(writer);
            dialogStage.setMaximized(true);
            controller.setStartPage("https://www.jd.com/");
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean showSelectBrandTaskDialog(List<Task> tasks, String url, StringWriter writer) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/fxml/BrandTaskSelectedDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("采集任务选择");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            FXMLBrandTaskSelectedController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.bindItems(tasks);
            controller.setPage(url, writer);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
