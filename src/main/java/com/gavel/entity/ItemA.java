package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@TableMeta(name = "ITEMA", title = "")
public class ItemA {

    //商品编码	商品名称	供价	 _	类目	品牌

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private final StringProperty id = new SimpleStringProperty();

    @FieldMeta(name = "NUM", title = "ROW")
    private int num;

    @FieldMeta(name = "CODE", title = "商品编码", length = 128)
    private final StringProperty code = new SimpleStringProperty();


    @FieldMeta(name = "TITLE", title = "商品名称", length = 128)
    private final StringProperty title = new SimpleStringProperty();


    @FieldMeta(name = "PRICE", title = "供价", length = 128)
    private final StringProperty price = new SimpleStringProperty();


    @FieldMeta(name = "CATE", title = "类目", length = 128)
    private final StringProperty cate = new SimpleStringProperty();


    @FieldMeta(name = "BRAND", title = "品牌", length = 128)
    private final StringProperty brand = new SimpleStringProperty();

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getCode() {
        return code.get();
    }

    public StringProperty codeProperty() {
        return code;
    }

    public void setCode(String code) {
        this.code.set(code);
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

    public String getPrice() {
        return price.get();
    }

    public StringProperty priceProperty() {
        return price;
    }

    public void setPrice(String price) {
        this.price.set(price);
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
}
