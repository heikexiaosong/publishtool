package com.gavel.application.controller;

import com.gavel.application.DataPagination;
import com.gavel.application.MainApp;
import com.gavel.crawler.ItemSupplement;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.SearchItem;
import com.gavel.entity.Task;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;


public class FXMLCollectionController {

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
    private Label taskRemark;
    @FXML
    private Label taskPagenum;
    @FXML
    private Label taskProductnum;
    @FXML
    private Label taskSkunum;
    @FXML
    private Label taskStatus;

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
            tasks = SQLExecutor.executeQueryBeanList("select * from TASK", Task.class);
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
    }

    private void showTaskDetails(Task task) {
        if ( task==null ) {
            pagination.setPageCount(0);
        } else {

            taskId.setText(task.getId());
            taskTitle.setText(task.getTitle());
            taskUrl.setText(task.getUrl());
            taskRemark.setText(task.getRemark());
            taskPagenum.setText(String.valueOf(task.getPagenum()));
            taskProductnum.setText(String.valueOf(task.getProductnum()));
            taskSkunum.setText(String.valueOf(task.getSkunum()));
            taskStatus.setText(task.getStatus());



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
            try {
                SQLExecutor.insert(task);
                taskTable.getItems().add(task);
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
    }
}
