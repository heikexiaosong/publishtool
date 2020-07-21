package com.gavel.application.controller;

import com.gavel.HttpUtils;
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

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
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

    public void setStartPage(final String url) {
        if ( url!=null && url.length() > 0 ) {
            webView.getEngine().load(url);
        }
    }

    private StringWriter writer;

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

        if ( url.contains("jd.com") ) {

            String html = null;

            if ( writer!=null ) {
                org.w3c.dom.Document doc = webView.getEngine().getDocument();

                try {
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);

                    StreamResult result = new StreamResult(writer);
                    transformer.transform(source, result);
                    html = writer.toString();
                } catch (TransformerConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (TransformerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if ( html==null || html.trim().length()<=0 ) {
                html = HttpUtils.get(url, "");
            }

            Document document = Jsoup.parse(html);

            System.out.println(document.selectFirst("div.f-pager .fp-text i"));
            System.out.println(document.selectFirst("div.f-result-sum span.num"));

            Elements items = document.select("div#plist li.gl-item div.j-sku-item");

            for (Element item : items) {
                System.out.println(item.selectFirst("div.p-name em").text());

                System.out.print(item.attr("data-sku"));
                System.out.print("\t" + item.attr("venderid"));
                System.out.print("\t" + item.attr("jdzy_shop_id"));
                System.out.println("\t" + item.attr("brand_id"));

                System.out.println(item.selectFirst("div.p-price"));
            }



            task.setTitle(document.title());

            Element total = document.selectFirst("div.f-result-sum span.num");
            if ( total==null ) {
                total = document.selectFirst("span.fp-total b");

            }
            System.out.println("产品: " + total.text());

            task.setProductnum(0);
            try {
                task.setSkunum(Integer.parseInt(total.text()));
            } catch (Exception e){
                e.printStackTrace();
            }

            int pageCur = Integer.parseInt(document.selectFirst("div.f-pager .fp-text b").text());
            int pageTotal = Integer.parseInt(document.selectFirst("div.f-pager .fp-text i").text());


            System.out.println("当前页: " + pageCur);
            System.out.println("总页数: " + pageTotal);

            task.setPagenum(pageTotal);

            task.setUrl(url);
            task.setStatus(Task.Status.READY);
            task.setType("JD");
            task.setUpdatetime(Calendar.getInstance().getTime());

        } else {
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

    public void bind(StringWriter _writer) {
        this.writer = _writer;
    }

    public void handleGoAction(ActionEvent actionEvent) {
        String url = address.getText().trim();
        if (StringUtils.isNotBlank(url) ) {
            webView.getEngine().load(url);
        }
    }
}
