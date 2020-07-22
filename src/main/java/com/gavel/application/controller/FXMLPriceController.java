package com.gavel.application.controller;

import com.gavel.HttpUtils;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Item;
import com.gavel.entity.PHtmlCache;
import com.gavel.utils.ExcelTool;
import com.gavel.utils.MD5Utils;
import com.gavel.utils.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FXMLPriceController {

    @FXML
    private AnchorPane root;

       // 产品SKU列表
    @FXML
    private TableView<PriceItem> itemList;
    @FXML
    private TableColumn<PriceItem, String> noCol;
    @FXML
    private TableColumn<PriceItem, String> idCol;
    @FXML
    private TableColumn<PriceItem, String> skuidCol;
    @FXML
    private TableColumn<PriceItem, String> titleCol;
    @FXML
    private TableColumn<PriceItem, String> cateCol;
    @FXML
    private TableColumn<PriceItem, String> brandCol;
    @FXML
    private TableColumn<PriceItem, String> sellPointsCol;
    @FXML
    private TableColumn<PriceItem, String> stepCol;
    @FXML
    private TableColumn<PriceItem, String> statusCol;
    @FXML
    private TableColumn<PriceItem, String> typeCol;
    @FXML
    private TableColumn<PriceItem, String> masterCol;

    @FXML
    private TableColumn<PriceItem, String> platformCol;
    @FXML
    private TableColumn<PriceItem, String> priceCol;
    @FXML
    private TableColumn<PriceItem, String> price1Col;
    @FXML
    private TableColumn<PriceItem, String> price2Col;

    @FXML
    private TextField filename;


    @FXML
    private void initialize() {

        noCol.setCellValueFactory(cellData -> cellData.getValue().rownumProperty());
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        skuidCol.setCellValueFactory(cellData -> cellData.getValue().skuidProperty());
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        cateCol.setCellValueFactory(cellData -> cellData.getValue().cateProperty());
        brandCol.setCellValueFactory(cellData -> cellData.getValue().brandProperty());
        sellPointsCol.setCellValueFactory(cellData -> cellData.getValue().sellPointsProperty());
        stepCol.setCellValueFactory(cellData -> cellData.getValue().stepProperty());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        masterCol.setCellValueFactory(cellData -> cellData.getValue().masterProperty());
        platformCol.setCellValueFactory(cellData -> cellData.getValue().platformProperty());
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        price1Col.setCellValueFactory(cellData -> cellData.getValue().price1Property());
        price2Col.setCellValueFactory(cellData -> cellData.getValue().price2Property());
    }



    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }



    public void handleItemPrice(ActionEvent actionEvent) {

        Map<String, JsonObject> pricesMap = new HashMap<>();


        Map<String, PriceItem> priceItemMap = new HashMap<>();

        List<String> jdSkus = new ArrayList<>();
        for (PriceItem priceItem : itemList.getItems()) {

            try {
                priceItemMap.put(priceItem.getId(), priceItem);

                if ( StringUtils.isBlank(priceItem.getSkuid()) ) {
                    continue;
                }

                System.out.println("SKU: " +  priceItem.getSkuid());
                Item item = SQLExecutor.executeQueryBean("select * from ITEM where CODE = ? ", Item.class, priceItem.getSkuid());
                if ( item==null ) {
                    continue;
                }
                if ( "JD".equalsIgnoreCase(item.getType()) ) {
                    priceItem.setPlatform("京东");
                    if ( StringUtils.isNotBlank(priceItem.getSkuid()) && !jdSkus.contains(priceItem.getSkuid()) ) {
                        jdSkus.add(priceItem.getSkuid());
                    }
                } else {
                    priceItem.setPlatform("固安捷");
                }


           } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (jdSkus != null && jdSkus.size() > 0) {

            StringBuilder query = new StringBuilder("https://p.3.cn/prices/mgets?type=1&pdbp=0&pdtk=&pdpin=hailinking1984&pin=hailinking1984&source=list_pc_front&skuIds=");

            int cnt = 1;
            StringBuilder skuids = new StringBuilder();


            for (int i1 = 0; i1 < jdSkus.size(); i1++) {
                String skuCode = jdSkus.get(i1);
                skuids.append("J_").append(skuCode).append("%2C");
                if (cnt++ >= 100 || i1 == jdSkus.size() - 1) {
                    cnt = 1;
                    String text = HttpUtils.get(query.toString() + skuids.toString(), "");

//                    String html = DriverHtmlLoader.getInstance().loadHtml(query.toString() + skuids.toString());
//
//                    String text = Jsoup.parse(html).selectFirst("pre").text();

                    JsonArray arrays = new JsonParser().parse(text).getAsJsonArray();
                    System.out.println("arrays: " +  ( arrays==null ? 0 : arrays.size() ));
                    if (arrays != null && arrays.size() > 0) {
                        for (JsonElement array : arrays) {
                            JsonObject object = (JsonObject) array;
                            pricesMap.put(object.get("id").toString().replace("\"", "").replace("J_", ""), object);
                        }
                    }


                    skuids.delete(0, skuids.length()-1);
                }
            }
        }

        for (PriceItem _priceItem : itemList.getItems()) {
            PriceItem priceItem = _priceItem;
            try {
                if ( StringUtils.isBlank(priceItem.getType()) ||  "通码商品".equalsIgnoreCase(priceItem.getType()) ) {
                    continue;
                }

                if ( StringUtils.isBlank(priceItem.getSkuid()) && StringUtils.isNotBlank(priceItem.getMaster()) ) {
                    priceItem = priceItemMap.get(_priceItem.getMaster());
                }

                if ( priceItem==null ||  StringUtils.isBlank(priceItem.getSkuid()) ) {
                    continue;
                }

                JsonObject priceObj = pricesMap.get(priceItem.getSkuid());
                if ( priceObj!=null ) {

                    if (priceObj.has("op") ) {
                        _priceItem.setPrice(priceObj.get("op").getAsString());
                    }

                    if (priceObj.has("p") ) {
                        _priceItem.setPrice1(priceObj.get("p").getAsString());
                    }
                } else {
                    System.out.println("[PriceJsonObject]" + priceItem.getSkuid() + ": " + null);
                }


                if ( _priceItem.getPrice()!=null && !_priceItem.getPrice().equalsIgnoreCase(_priceItem.getPrice1()) ) {

                    PHtmlCache pHtmlCache = null;
                    boolean jd = true;
                    try {

                        String url = "https://item.jd.com/" + priceItem.getSkuid().trim()  + ".html";
                        String id = MD5Utils.md5Hex(url);
                        if ( id!=null && id.trim().length() > 0 ) {
                            pHtmlCache = SQLExecutor.executeQueryBean("select * from HTMLCACHE_" + id.charAt(id.length()-1) + " where ID = ? ", PHtmlCache.class, id);
                        }
                    } catch (Exception e) {

                    }

                    if ( pHtmlCache==null ) {
                        String url = "https://i-item.jd.com/"  + priceItem.getSkuid().trim() +".html";
                        String id = MD5Utils.md5Hex(url);
                        if ( id!=null && id.trim().length() > 0 ) {
                            pHtmlCache = SQLExecutor.executeQueryBean("select * from HTMLCACHE_" + id.charAt(id.length()-1) + " where ID = ? ", PHtmlCache.class, id);
                        }
                    }

                    if ( pHtmlCache==null ) {
                        String url = "https://www.grainger.cn/u-" + priceItem.getSkuid().trim() + ".html";
                        String id = MD5Utils.md5Hex(url);
                        if ( id!=null && id.trim().length() > 0 ) {
                            pHtmlCache = SQLExecutor.executeQueryBean("select * from HTMLCACHE_" + id.charAt(id.length()-1) + " where ID = ? ", PHtmlCache.class, id);
                            jd = false;
                        }
                    }


                    _priceItem.setPrice2(_priceItem.getPrice());


                    if ( pHtmlCache!=null && pHtmlCache.getHtml()!=null ) {
                        Document doc = Jsoup.parse(pHtmlCache.getHtml());
                        if ( jd ) {
                            Element hx_price = doc.selectFirst("del#page_hx_price");
                            if ( hx_price!=null ) {
                                // op 是京东原价
                                // p 是 京东优惠价
                                System.out.println(_priceItem.getRownum() +  " 有优惠价 ==> " + _priceItem.getSkuid() );
                            } else {
                                // p 是京东价
                                _priceItem.setPrice(_priceItem.getPrice1());
                            }
                        } else {
                            Elements prices = doc.select("div.price b");
                            if ( prices!=null && prices.size()==2 ) {
                                // delPrice 是固安捷原价
                                _priceItem.setPrice(prices.get(0).text().replace("¥", ""));
                                _priceItem.setPrice1(prices.get(1).text().replace("¥", ""));
                            } else if (prices!=null && prices.size()==1){
                                _priceItem.setPrice(prices.get(0).text().replace("¥", ""));
                                _priceItem.setPrice1(prices.get(0).text().replace("¥", ""));
                            }

                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if ( filename.getText()!=null && filename.getText().trim().length() > 0 ) {
            FileInputStream ins = null;
            Workbook workbook = null;
            try {
                ins = new FileInputStream(filename.getText());
                try {
                    workbook = new XSSFWorkbook(ins);
                } catch (Exception e){
                    try {
                        workbook = new HSSFWorkbook(ins);
                    } catch (Exception e1) {
                        try {
                            NPOIFSFileSystem fs = new NPOIFSFileSystem(new File(filename.getText()));
                            workbook = WorkbookFactory.create(fs);
                        } catch (IOException e2) {
                            e2.printStackTrace();

                            filename.setText("文件打开失败, 请检查文件格式");
                        }
                    }
                } finally {
                    if ( ins!=null ) {
                        try {
                            ins.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }



                if ( workbook!=null ) {

                    // 获取数据sheet
                    int activeSheetIndex = workbook.getActiveSheetIndex();
                    Sheet dataSheet = workbook.getSheetAt(activeSheetIndex);
                    if ( dataSheet==null ) {
                        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                            if ( workbook.isSheetHidden(i) ) {
                                continue;
                            }

                            dataSheet = workbook.getSheetAt(i);
                            break;
                        }
                    }

                    if ( dataSheet!=null ) {
                        for (PriceItem _priceItem : itemList.getItems()) {
                            try {

                              int rowNum = Integer.parseInt( _priceItem.getRownum());

                               Row row = dataSheet.getRow(rowNum);
                               if ( row != null ) {
                                   Cell cell = row.getCell(19);
                                   if ( cell==null ) {
                                       cell = row.createCell(19);
                                   }
                                   cell.setCellValue(_priceItem.getPrice());

                                   cell = row.getCell(20);
                                   if ( cell==null ) {
                                       cell = row.createCell(20);
                                   }
                                   cell.setCellValue(_priceItem.getPrice1());

                                   cell = row.getCell(21);
                                   if ( cell==null ) {
                                       cell = row.createCell(21);
                                   }
                                   cell.setCellValue(_priceItem.getPrice2());
                               }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {

                if ( workbook!=null) {
                    try {
                        workbook.write(new FileOutputStream(filename.getText()));
                        workbook.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void handleFileChoose(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();//构建一个文件选择器实例
        fileChooser.setTitle("选择Excel文件");
        File selectedFile = fileChooser.showOpenDialog(stage());

        String path = selectedFile.getPath();
        filename.setText(path);


        FileInputStream ins = null;
        Workbook workbook = null;
        try {
            ins = new FileInputStream(path);
            try {
                workbook = new XSSFWorkbook(ins);
            } catch (Exception e){
                try {
                    workbook = new HSSFWorkbook(ins);
                } catch (Exception e1) {
                    try {
                        NPOIFSFileSystem fs = new NPOIFSFileSystem(new File(path));
                        workbook = WorkbookFactory.create(fs);
                    } catch (IOException e2) {
                        e2.printStackTrace();

                        filename.setText("文件打开失败, 请检查文件格式");
                    }
                }
            } finally {
                if ( ins!=null ) {
                    try {
                        ins.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }



            if ( workbook!=null ) {

                // 获取数据sheet
                int activeSheetIndex = workbook.getActiveSheetIndex();
                Sheet dataSheet = workbook.getSheetAt(activeSheetIndex);
                if ( dataSheet==null ) {
                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                        if ( workbook.isSheetHidden(i) ) {
                            continue;
                        }

                        Sheet sheet = workbook.getSheetAt(i);
                        if ( sheet.getSheetName().equalsIgnoreCase("module") )  {
                            continue;
                        }

                        int lastRowNum = sheet.getLastRowNum();
                        if (  lastRowNum <=1 ) {
                            continue;
                        }

                        dataSheet = sheet;
                        break;
                    }
                }


                List<PriceItem> priceItems = new ArrayList<>();

                if ( dataSheet!=null ) {

                    for (Row row : dataSheet) {
                        if ( row.getRowNum()==0 ) {
                            continue;
                        }

                        if ( StringUtils.isBlank(ExcelTool.getCellValue(row.getCell(0))) ) {
                            continue;
                        }

                        System.out.println(row.getRowNum()  + ": " + ExcelTool.getCellValue(row.getCell(0)));

                        PriceItem priceItem = new PriceItem(row.getRowNum());
                        priceItem.setId(ExcelTool.getCellValue(row.getCell(0)));
                        priceItem.setSkuid(ExcelTool.getCellValue(row.getCell(1)));
                        priceItem.setTitle(ExcelTool.getCellValue(row.getCell(2)));
                        priceItem.setCate(ExcelTool.getCellValue(row.getCell(4)));
                        priceItem.setBrand(ExcelTool.getCellValue(row.getCell(5)));
                        priceItem.setSellPoints(ExcelTool.getCellValue(row.getCell(8)));
                        priceItem.setStep(ExcelTool.getCellValue(row.getCell(10)));
                        priceItem.setStatus(ExcelTool.getCellValue(row.getCell(11)));
                        priceItem.setType(ExcelTool.getCellValue(row.getCell(12)));
                        priceItem.setMaster(ExcelTool.getCellValue(row.getCell(13)));

                        priceItems.add(priceItem);
                    }

                }

                itemList.setItems(FXCollections.observableArrayList(priceItems));
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {

            if ( workbook!=null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }




    }


    static class PriceItem {

        private final StringProperty rownum = new SimpleStringProperty(); // 0

        private final StringProperty id = new SimpleStringProperty(); // 0

        private final StringProperty skuid = new SimpleStringProperty(); // 1

        private final StringProperty title = new SimpleStringProperty(); // 2

        private final StringProperty cate = new SimpleStringProperty(); // 4

        private final StringProperty brand = new SimpleStringProperty(); // 5

        private final StringProperty sellPoints = new SimpleStringProperty(); // 8

        private final StringProperty step = new SimpleStringProperty(); // 10

        private final StringProperty status = new SimpleStringProperty(); // 11

        private final StringProperty type = new SimpleStringProperty(); // 12

        private final StringProperty master = new SimpleStringProperty(); // 13


        private final StringProperty platform = new SimpleStringProperty(); // 平台

        private final StringProperty price = new SimpleStringProperty(); // 价格

        private final StringProperty price1 = new SimpleStringProperty(); // 促销价格

        private final StringProperty price2 = new SimpleStringProperty(); // 当前价格


        public PriceItem(int rownum) {
            this.rownum.set(String.valueOf(rownum));
        }

        public String getRownum() {
            return rownum.get();
        }

        public StringProperty rownumProperty() {
            return rownum;
        }

        public void setRownum(String rownum) {
            this.rownum.set(rownum);
        }

        public String getId() {
            return id.get();
        }

        public StringProperty idProperty() {
            return id;
        }

        public void setId(String id) {
            this.id.set(id);
        }

        public String getSkuid() {
            return skuid.get();
        }

        public StringProperty skuidProperty() {
            return skuid;
        }

        public void setSkuid(String skuid) {
            this.skuid.set(skuid);
        }

        public String getTitle() {
            return title.get();
        }

        public StringProperty titleProperty() {
            return title;
        }

        public void setTitle(String title) {
            this.title.set(title);
        }

        public String getCate() {
            return cate.get();
        }

        public StringProperty cateProperty() {
            return cate;
        }

        public void setCate(String cate) {
            this.cate.set(cate);
        }

        public String getBrand() {
            return brand.get();
        }

        public StringProperty brandProperty() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand.set(brand);
        }

        public String getSellPoints() {
            return sellPoints.get();
        }

        public StringProperty sellPointsProperty() {
            return sellPoints;
        }

        public void setSellPoints(String sellPoints) {
            this.sellPoints.set(sellPoints);
        }

        public String getStep() {
            return step.get();
        }

        public StringProperty stepProperty() {
            return step;
        }

        public void setStep(String step) {
            this.step.set(step);
        }

        public String getStatus() {
            return status.get();
        }

        public StringProperty statusProperty() {
            return status;
        }

        public void setStatus(String status) {
            this.status.set(status);
        }

        public String getType() {
            return type.get();
        }

        public StringProperty typeProperty() {
            return type;
        }

        public void setType(String type) {
            this.type.set(type);
        }

        public String getMaster() {
            return master.get();
        }

        public StringProperty masterProperty() {
            return master;
        }

        public void setMaster(String master) {
            this.master.set(master);
        }

        public String getPlatform() {
            return platform.get();
        }

        public StringProperty platformProperty() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform.set(platform);
        }

        public String getPrice() {
            return price.get();
        }

        public StringProperty priceProperty() {
            return price;
        }

        public void setPrice(String price) {
            this.price.set(price);
        }

        public String getPrice1() {
            return price1.get();
        }

        public StringProperty price1Property() {
            return price1;
        }

        public void setPrice1(String price1) {
            this.price1.set(price1);
        }

        public String getPrice2() {
            return price2.get();
        }

        public StringProperty price2Property() {
            return price2;
        }

        public void setPrice2(String price2) {
            this.price2.set(price2);
        }
    }


    public static void main(String[] args) {

        Document doc = Jsoup.parse("<div><div class=\"price\">\n" +
                "                            价格:\n" +
                "                            <b id=\"bSalePrice\" class=\"bSalePrice\">\n" +
                "¥828.90                            </b>\n" +
                "                            元/套\n" +
                "                           \n" +
                "                        </div></div>");

        // price.selectFirst("b#bSalePrice del").text().replace("¥", "")
        Elements prices = doc.select("div.price b");
        System.out.println("123".charAt("123".length()));
    }
}
