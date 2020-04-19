package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

@TableMeta(name = "GRAINGERPRODUCT", title = "固安捷产品")
public class Product {

    @FieldMeta(name = "CODE", title = "产品编码", length = 32, primary = true)
    private String code;

    @FieldMeta(name = "TYPE", title = "产品类型", length = 32, primary = true)
    private String type;

    @FieldMeta(name = "NAME", title = "名称", length = 128)
    private String name;

    @FieldMeta(name = "BRAND", title = "品牌", length = 64)
    private String brand;

    @FieldMeta(name = "CATEGORY", title = "类目", length = 64)
    private String category;

    @FieldMeta(name = "PIC", title = "图片", length = 1024)
    private String pic;

    @FieldMeta(name = "URL", title = "产品URL", length = 1024)
    private String url;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}
