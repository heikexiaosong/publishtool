package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import com.gavel.database.SQLExecutor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@TableMeta(name = "BRAND_INFO", title = "品牌信息")
public class BrandInfo {

    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }


    @FieldMeta(name = "CODE", title = "品牌编码", length = 32, primary = true)
    private final StringProperty code = new SimpleStringProperty();

    @FieldMeta(name = "NAME1", title = "名称1", length = 32)
    private final StringProperty name1 = new SimpleStringProperty();

    @FieldMeta(name = "NAME2", title = "名称2", length = 32)
    private final StringProperty name2 = new SimpleStringProperty();

    @FieldMeta(name = "LOGO", title = "LOGO", length = 1024)
    private final StringProperty logo = new SimpleStringProperty();

    @FieldMeta(name = "URL", title = "URL", length = 1024)
    private final StringProperty url = new SimpleStringProperty();

    @FieldMeta(name = "PAGENUM", title = "页数")
    private int pagenum;

    @FieldMeta(name = "PRODUCTNUM", title = "产品组")
    private int productnum;

    @FieldMeta(name = "SKUNUM", title = "产品数")
    private int skunum;


    public String getCode() {
        return code.get();
    }

    public StringProperty codeProperty() {
        return code;
    }

    public void setCode(String code) {
        this.code.set(code);
    }

    public String getName1() {
        return name1.get();
    }

    public StringProperty name1Property() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1.set(name1);
    }

    public String getName2() {
        return name2.get();
    }

    public StringProperty name2Property() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2.set(name2);
    }

    public String getLogo() {
        return logo.get();
    }

    public StringProperty logoProperty() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo.set(logo);
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public int getPagenum() {
        return pagenum;
    }

    public void setPagenum(int pagenum) {
        this.pagenum = pagenum;
    }

    public int getProductnum() {
        return productnum;
    }

    public void setProductnum(int productnum) {
        this.productnum = productnum;
    }

    public int getSkunum() {
        return skunum;
    }

    public void setSkunum(int skunum) {
        this.skunum = skunum;
    }

    public static void main(String[] args) {
        try {
            SQLExecutor.createTable(BrandInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
