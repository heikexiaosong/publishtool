package com.gavel.application.controller;

import com.gavel.HttpUtils;
import com.gavel.application.model.Task;
import com.gavel.crawler.GraingerProductParser;
import com.gavel.crawler.SkuItem;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Product;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.stage.Stage;
import javafx.concurrent.Worker.State;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class FXMLWebpageController {




    private static final Pattern CODE_PATTERN = Pattern.compile("/([a-zA-Z])-([^\\.]*).html", Pattern.CASE_INSENSITIVE);

    @FXML
    private AnchorPane root;

    @FXML
    private TextArea textArea;

    @FXML
    private WebView webView;

    @FXML
    private TextField address;

    private WebEngine webEngine;


    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }

    private ObservableList<Task> taskList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        webEngine = webView.getEngine();

        webEngine.setUserAgent(HttpUtils.USERAGENT);

        webEngine.locationProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                address.setText(newValue);
            }
        });

//        webView.getEngine().onStatusChangedProperty().set(new EventHandler<WebEvent<String>>() {
//            @Override
//            public void handle(WebEvent<String> event) {
//
//                textArea.appendText(event.getEventType().getName() + ": " + event.getEventType().getClass() + "\n");
//                textArea.appendText(event.getData() + "\n");
//            }
//        });


        // Listening to the status of worker
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

            @Override
            public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
                textArea.appendText("Loading state: " + newValue.toString() + "\n");
                if (newValue == Worker.State.SUCCEEDED) {
                    textArea.appendText( webEngine.getLocation() +  " Finish!\n" );


                    Document doc = webEngine.getDocument();
                    try {
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                        StringWriter stringWriter = new StringWriter();

                        transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));


                        //textArea.appendText(stringWriter.getBuffer().toString() + "\n");



                        ///
                        org.jsoup.nodes.Document document = Jsoup.parse(stringWriter.getBuffer().toString());

                        Element cpz = document.selectFirst("font.cpz");
                        Element total = document.selectFirst("font.total");

                        if ( cpz!=null ) {
                            textArea.appendText("产品组: " + cpz.text() + "; 产品: " + total.text() + "\n");

                            Elements brands = document.select("div.allbrand dd");

                            for (Element brand : brands) {
                                textArea.appendText(brand.attr("brandcode") + ": " + brand.selectFirst("a").text() + "\n");
                            }

                            textArea.appendText("\n");

                            int pageCur = 0;
                            int pageTotal = 0;

                            Elements labels = document.select("div.pagination > label");
                            if ( labels.size()==2 ) {
                                pageCur = Integer.parseInt(labels.get(0).text());
                                pageTotal = Integer.parseInt(labels.get(1).text());
                            }

                            textArea.appendText("当前页: " + pageCur);
                            textArea.appendText("总页数: " + pageTotal);

                            textArea.appendText("\n");


                            List<Product> products = new ArrayList<>();
                            List<SkuItem>  skuItems = new ArrayList<>();


                            int i = 0;
                            Elements elements = document.select("div.proUL li");
                            for (Element element : elements) {

                                Product graingerBrand = new Product();

                                graingerBrand.setName(element.attr("title"));

                                Element item = element.selectFirst("a");

                                String href = item.attr("href");

                                graingerBrand.setCode(href);
                                graingerBrand.setType("");
                                graingerBrand.setUrl("https://www.grainger.cn" + href);

                                Matcher matcher = CODE_PATTERN.matcher(href);
                                if (matcher.find()) {
                                    graingerBrand.setCode(matcher.group(2));
                                    graingerBrand.setType(matcher.group(1));
                                }


                                textArea.appendText("\t" + (++i) + ". [" +  graingerBrand.getUrl() + "]" + graingerBrand.getCode() + "....\n");


                                // System.out.println(graingerBrand);
                            }

                            System.out.println("Total: " + products.size() );
                        }



                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

    }

    public void handleGoAction(ActionEvent actionEvent) {

        String url = address.getText();
        if ( url==null || url.trim().length()==0 ) {
            url = "https://www.grainger.cn/";
        }
        address.setText(url);

        webView.getEngine().load(url);

        textArea.appendText("Load: " + url + "\n");

    }
}
