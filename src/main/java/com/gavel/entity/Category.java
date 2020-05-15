package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

@TableMeta(name = "CATEGORY", title = "苏宁类目")
public class Category {

    @FieldMeta(name = "SUPPLIERCODE", title = "供应商编码", length = 32)
    private String supplierCode;

    @FieldMeta(name = "CATEGORYCODE", title = "类目编码", length = 32)
    private String categoryCode;

    @FieldMeta(name = "CATEGORYNAME", title = "类目名称", length = 32)
    private String categoryName;

    @FieldMeta(name = "GRADE", title = "目录层级", length = 8)
    private String grade;   // 当前采购目录层级。

    @FieldMeta(name = "ISBOTTOM", title = "类目编码", length = 8)
    private String isBottom;        // “X”表示无下级目录；“N”表示有下级目录。

    @FieldMeta(name = "DESCPATH", title = "采购目录层级描述", length = 128)
    private String descPath;

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getIsBottom() {
        return isBottom;
    }

    public void setIsBottom(String isBottom) {
        this.isBottom = isBottom;
    }

    public String getDescPath() {
        return descPath;
    }

    public void setDescPath(String descPath) {
        this.descPath = descPath;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryCode='" + categoryCode + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", grade='" + grade + '\'' +
                ", isBottom='" + isBottom + '\'' +
                ", descPath='" + descPath + '\'' +
                '}';
    }
}
