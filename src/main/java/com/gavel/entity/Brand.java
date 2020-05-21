package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@TableMeta(name = "BRAND", title = "苏宁品牌")
public class Brand {

    @FieldMeta(name = "SUPPLIERCODE", title = "供应商编码", length = 32)
    private final StringProperty supplierCode = new SimpleStringProperty();

    @FieldMeta(name = "CODE", title = "品牌编码", length = 32, primary = true)
    private final StringProperty code = new SimpleStringProperty();

    @FieldMeta(name = "NAME", title = "品牌名称", length = 64)
    private final StringProperty name = new SimpleStringProperty();

    @FieldMeta(name = "CATEGORYCODE", title = "类目编码", length = 32)
    private final StringProperty categoryCode = new SimpleStringProperty();

    public String getSupplierCode() {
        return supplierCode.get();
    }

    public StringProperty supplierCodeProperty() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode.set(supplierCode);
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

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getCategoryCode() {
        return categoryCode.get();
    }

    public StringProperty categoryCodeProperty() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode.set(categoryCode);
    }
}
