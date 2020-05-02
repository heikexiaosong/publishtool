package com.gavel.crawler;

public class SkuItem {

    private String code;

    private String product;

    private String html;

    public SkuItem() {
    }

    public SkuItem(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "SkuItem{" +
                "code='" + code + '\'' +
                ", product='" + product + '\'' +
                '}';
    }
}
