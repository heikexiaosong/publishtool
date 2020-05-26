package com.gavel.application.controller;

import com.gavel.crawler.HtmlPageLoader;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Task;
import com.gavel.utils.StringUtils;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Calendar;

public class FXMLTaskAddDialogController {

    @FXML
    private AnchorPane root;

    @FXML
    private TextField address;

    @FXML
    private WebView webView;

    private Task task;


    private Stage dialogStage;
    private boolean okClicked = false;

    private FXMLShelvesController.EditTask editTask;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void initialize() {

        webView.getEngine().setUserAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36");

        webView.getEngine().load("https://www.grainger.cn");


        webView.getEngine().getLoadWorker().stateProperty()
                .addListener((obs, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        address.setText(webView.getEngine().getLocation());
                    }
                }); // addListener()
    }

    @FXML
    private void handleOk() {

        // TODO
        String url = webView.getEngine().getLocation();
        try {
            HtmlCache htmlCache = HtmlPageLoader.getInstance().loadHtmlPage(url, false);
            if ( htmlCache!=null && htmlCache.getHtml()!=null ) {

                Document document = Jsoup.parse(htmlCache.getHtml());

                task.setTitle(document.title());

                Element cpz = document.selectFirst("font.cpz");
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
                task.setUrl(url);
                task.setStatus(Task.Status.INIT);
                task.setUpdatetime(Calendar.getInstance().getTime());

            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public void handleSearchAction(ActionEvent actionEvent) {

    }

    public void bindTask(Task _task) {
        this.task = _task;
    }

    public void handleGoAction(ActionEvent actionEvent) {
        String url = address.getText().trim();
        if (StringUtils.isNotBlank(url) ) {
            webView.getEngine().load(url);
        }
    }
}
