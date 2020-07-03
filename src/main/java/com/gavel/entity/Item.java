package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

@TableMeta(name = "ITEM", title = "固安捷SKU")
public class Item {

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

    @FieldMeta(name = "CODE", title = "SKU编码", length = 32, primary = true)
    private String code;

    @FieldMeta(name = "NAME", title = "商品标题", length = 128)
    private String name;

    @FieldMeta(name = "PRODUCTCODE", title = "产品编码", length = 32)
    private String productcode;

    @FieldMeta(name = "PRODUCTNAME", title = "商品名称", length = 128)
    private String productname;

    @FieldMeta(name = "SUBNAME", title = "商品卖点", length = 128)
    private String subname;

    @FieldMeta(name = "BRAND", title = "品牌", length = 64)
    private String brand;

    @FieldMeta(name = "BRANDNAME", title = "品牌名称", length = 64)
    private String brandname;

    @FieldMeta(name = "CATEGORY", title = "类目", length = 64)
    private String category;

    @FieldMeta(name = "CATEGORYNAME", title = "类目名称", length = 64)
    private String categoryname;

    @FieldMeta(name = "CATEGORYDESC", title = "类目描述", length = 64)
    private String categorydesc;

    @FieldMeta(name = "URL", title = "SKUURL", length = 1024)
    private String url;

    @FieldMeta(name = "PICNUM", title = "图片数")
    private int picnum;

    @FieldMeta(name = "CATE1", title = "类目1", length = 32)
    private String cate1;

    @FieldMeta(name = "CATE1NAME", title = "类目1名称", length = 128)
    private String cate1name;

    @FieldMeta(name = "CATE2", title = "类目2", length = 32)
    private String cate2;

    @FieldMeta(name = "CATE2NAME", title = "类目2名称", length = 128)
    private String cate2name;


    @FieldMeta(name = "CATE3", title = "类目3", length = 32)
    private String cate3;

    @FieldMeta(name = "CATE3NAME", title = "类目3名称", length = 128)
    private String cate3name;

    @FieldMeta(name = "CATE4", title = "类目4", length = 32)
    private String cate4;

    @FieldMeta(name = "CATE4NAME", title = "类目4名称", length = 128)
    private String cate4name;


    @FieldMeta(name = "PIC1", title = "图片1")
    private int pic1;

    @FieldMeta(name = "PIC2", title = "图片2")
    private int pic2;

    @FieldMeta(name = "PIC3", title = "图片3")
    private int pic3;

    @FieldMeta(name = "PIC4", title = "图片4")
    private int pic4;

    @FieldMeta(name = "PIC5", title = "图片5")
    private int pic5;

    public String getProductcode() {
        return productcode;
    }

    public void setProductcode(String productcode) {
        this.productcode = productcode;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubname() {
        return subname;
    }

    public void setSubname(String subname) {
        this.subname = subname;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBrandname() {
        return brandname;
    }

    public void setBrandname(String brandname) {
        this.brandname = brandname;
    }

    public String getCategoryname() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
    }

    public String getCategorydesc() {
        return categorydesc;
    }

    public void setCategorydesc(String categorydesc) {
        this.categorydesc = categorydesc;
    }

    public int getPicnum() {
        return picnum;
    }

    public void setPicnum(int picnum) {
        this.picnum = picnum;
    }
}
