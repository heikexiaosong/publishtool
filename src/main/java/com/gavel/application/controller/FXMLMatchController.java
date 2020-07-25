package com.gavel.application.controller;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.ItemA;
import com.gavel.utils.ExcelTool;
import com.gavel.utils.MD5Utils;
import com.gavel.utils.StringUtils;
import com.monitorjbl.xlsx.StreamingReader;
import javafx.beans.property.SimpleStringProperty;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FXMLMatchController {

    @FXML
    private AnchorPane root;

       // 产品SKU列表
    @FXML
    private TableView<ItemA> itemList;
    @FXML
    private TableColumn<ItemA, String> noCol;
    @FXML
    private TableColumn<ItemA, String> codeCol;
    @FXML
    private TableColumn<ItemA, String> titleCol;
    @FXML
    private TableColumn<ItemA, String> priceCol;
    @FXML
    private TableColumn<ItemA, String> cateCol;
    @FXML
    private TableColumn<ItemA, String> brandCol;

    @FXML
    private TextField filename;


    @FXML
    private void initialize() {

        noCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNum())));
        codeCol.setCellValueFactory(cellData -> cellData.getValue().codeProperty());
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        cateCol.setCellValueFactory(cellData -> cellData.getValue().cateProperty());
        brandCol.setCellValueFactory(cellData -> cellData.getValue().brandProperty());
    }



    private Stage stage() {
        return (Stage) root.getScene().getWindow();
    }



    public void handleItemPrice(ActionEvent actionEvent) {


    }

    public void handleFileChoose(ActionEvent actionEvent) {


    }


    private static Workbook build(String path) {

        Workbook workbook = null;

        FileInputStream ins = null;
        try {
            ins = new FileInputStream(path);
            workbook = new HSSFWorkbook(ins);
        } catch (Exception e) {

        } finally {
            if ( ins!=null ) {
                try {
                    ins.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if ( workbook==null ) {
            try {
                ins = new FileInputStream(path);
                workbook = new XSSFWorkbook(ins);
            } catch (Exception e) {

            } finally {
                if ( ins!=null ) {
                    try {
                        ins.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        if ( workbook==null ) {
            NPOIFSFileSystem fs = null;
            try {
                fs = new NPOIFSFileSystem(new File(path));
                workbook = WorkbookFactory.create(fs);
            } catch (Exception e) {

            } finally {
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
        return workbook;
    }

    public void handleItemImport(ActionEvent actionEvent) {


        FileChooser fileChooser = new FileChooser();//构建一个文件选择器实例
        fileChooser.setTitle("选择Excel文件");
        File selectedFile = fileChooser.showOpenDialog(stage());

        String path = selectedFile.getPath();

        String id = MD5Utils.md5Hex(path);




        try {

            FileInputStream in = new FileInputStream(path);
            Workbook wk = StreamingReader.builder()
                    .rowCacheSize(100)  //缓存到内存中的行数，默认是10
                    .bufferSize(4096)  //读取资源时，缓存到内存的字节大小，默认是1024
                    .open(in);  //打开资源，必须，可以是InputStream或者是File，注意：只能打开XLSX格式的文件
            Sheet sheet = wk.getSheetAt(0);
            //遍历所有的行
            for (Row row : sheet) {
                System.out.println("Row: " + row.getRowNum() + "行数据：");


                if ( row.getRowNum()==0 ) {
                    continue;
                }

                System.out.println("row: " + row.getRowNum());

                if ( StringUtils.isBlank(ExcelTool.getCellValue(row.getCell(0))) ) {
                    continue;
                }

                System.out.println(row.getRowNum()  + ": " + ExcelTool.getCellValue(row.getCell(0)));

                ItemA priceItem = new ItemA();
                priceItem.setId(MD5Utils.md5Hex(path + "_" + row.getRowNum()));
                priceItem.setNum(row.getRowNum());
                priceItem.setCode(ExcelTool.getCellValue(row.getCell(0)));
                priceItem.setTitle(ExcelTool.getCellValue(row.getCell(1)));
                priceItem.setPrice(ExcelTool.getCellValue(row.getCell(2)));
                priceItem.setCate(ExcelTool.getCellValue(row.getCell(4)));
                priceItem.setBrand(ExcelTool.getCellValue(row.getCell(5)));


                try {
                    SQLExecutor.insert(priceItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void handleCustomCateImport(ActionEvent actionEvent) {

    }

    public static void main(String[] args) throws FileNotFoundException {
        String path = "E:\\hhhh\\111.xlsx";

        FileInputStream in = new FileInputStream(path);
        Workbook wk = StreamingReader.builder()
                .rowCacheSize(100)  //缓存到内存中的行数，默认是10
                .bufferSize(4096)  //读取资源时，缓存到内存的字节大小，默认是1024
                .open(in);  //打开资源，必须，可以是InputStream或者是File，注意：只能打开XLSX格式的文件
        Sheet sheet = wk.getSheetAt(0);
        //遍历所有的行
        for (Row row : sheet) {
            System.out.println("开始遍历第" + row.getRowNum() + "行数据：");
            //遍历所有的列
            for (Cell cell : row) {
                System.out.print(cell.getStringCellValue() + " ");
            }
            System.out.println(" ");
        }
    }

}
