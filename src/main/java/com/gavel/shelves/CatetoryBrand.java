package com.gavel.shelves;

public class CatetoryBrand {

    private final String brandCode;

    private final String brandZh;

    private final String brandEn;

    private final String categoryCode;

    private final String category;

    public CatetoryBrand(String brandCode, String brandZh, String brandEn, String categoryCode, String category) {
        this.brandCode = brandCode;
        this.brandZh = brandZh;
        this.brandEn = brandEn;
        this.categoryCode = categoryCode;
        this.category = category;
    }

    public String getBrandCode() {
        return brandCode;
    }

    public String getBrandZh() {
        return brandZh;
    }

    public String getBrandEn() {
        return brandEn;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getCategory() {
        return category;
    }
}
