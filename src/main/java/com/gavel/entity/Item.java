package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

@TableMeta(name = "ITEM", title = "固安捷SKU")
public class Item {


    @FieldMeta(name = "CODE", title = "SKU编码", length = 32, primary = true)
    private String code;

    @FieldMeta(name = "PRODUCTCODE", title = "产品编码", length = 32)
    private String productcode;

    @FieldMeta(name = "NAME", title = "名称", length = 128)
    private String name;

    @FieldMeta(name = "SUBNAME", title = "副标题", length = 128)
    private String subname;

    @FieldMeta(name = "BRAND", title = "品牌", length = 64)
    private String brand;

    @FieldMeta(name = "CATEGORY", title = "类目", length = 64)
    private String category;

    @FieldMeta(name = "URL", title = "SKUURL", length = 1024)
    private String url;


    @FieldMeta(name = "HTML", title = "html", length = 99999)
    private String html;

    public String getProductcode() {
        return productcode;
    }

    public void setProductcode(String productcode) {
        this.productcode = productcode;
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

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
