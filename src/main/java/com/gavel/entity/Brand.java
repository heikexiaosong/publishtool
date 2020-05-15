package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

@TableMeta(name = "BRAND", title = "苏宁品牌")
public class Brand {

    @FieldMeta(name = "SUPPLIERCODE", title = "供应商编码", length = 32)
    private String supplierCode;

    @FieldMeta(name = "CODE", title = "品牌编码", length = 32, primary = true)
    private String code;

    @FieldMeta(name = "NAME", title = "品牌名称", length = 64)
    private String name;

    @FieldMeta(name = "CATEGORYCODE", title = "类目编码", length = 32)
    private String categoryCode;

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
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

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }
}
