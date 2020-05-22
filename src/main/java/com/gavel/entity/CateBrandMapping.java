package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import com.gavel.database.SQLExecutor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.atomic.AtomicLong;

@TableMeta(name = "BRAND_CATE_MAPPING", title = "品牌类目映射")
public class CateBrandMapping {

    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private final StringProperty id = new SimpleStringProperty();

    @FieldMeta(name = "SHOPID", title = "店铺ID", length = 50)
    private final StringProperty shopid = new SimpleStringProperty();

    @FieldMeta(name = "BRANDCODE", title = "固安捷品牌编码", length = 32)
    private final StringProperty brandcode = new SimpleStringProperty();

    @FieldMeta(name = "BRANDNAME", title = "固安捷品牌名称", length = 50)
    private final StringProperty brandname = new SimpleStringProperty();

    @FieldMeta(name = "CATECODE", title = "固安捷类目编码", length = 32)
    private final StringProperty catecode = new SimpleStringProperty();

    @FieldMeta(name = "CATENAME", title = "固安捷类目名称", length = 50)
    private final StringProperty catename = new SimpleStringProperty();


    @FieldMeta(name = "CATEGORYCODE", title = "苏宁类目编码", length = 32)
    private final StringProperty categorycode = new SimpleStringProperty();

    @FieldMeta(name = "CATEGORYNAME", title = "苏宁类目名称", length = 50)
    private final StringProperty categoryname = new SimpleStringProperty();

    @FieldMeta(name = "DESCPATH", title = "采购目录层级描述", length = 128)
    private final StringProperty descpath = new SimpleStringProperty();

    @FieldMeta(name = "SBRANDCODE", title = "苏宁品牌编码", length = 32)
    private final StringProperty sbrandcode = new SimpleStringProperty();

    @FieldMeta(name = "SBRANDNAME", title = "苏宁品牌名称", length = 50)
    private final StringProperty sbrandname = new SimpleStringProperty();

    private final AtomicLong count = new AtomicLong(1);

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getShopid() {
        return shopid.get();
    }

    public StringProperty shopidProperty() {
        return shopid;
    }

    public void setShopid(String shopid) {
        this.shopid.set(shopid);
    }


    public String getBrandcode() {
        return brandcode.get();
    }

    public StringProperty brandcodeProperty() {
        return brandcode;
    }

    public void setBrandcode(String brandcode) {
        this.brandcode.set(brandcode);
    }

    public String getBrandname() {
        return brandname.get();
    }

    public StringProperty brandnameProperty() {
        return brandname;
    }

    public void setBrandname(String brandname) {
        this.brandname.set(brandname);
    }

    public String getCatecode() {
        return catecode.get();
    }

    public StringProperty catecodeProperty() {
        return catecode;
    }

    public void setCatecode(String catecode) {
        this.catecode.set(catecode);
    }

    public String getCatename() {
        return catename.get();
    }

    public StringProperty catenameProperty() {
        return catename;
    }

    public void setCatename(String catename) {
        this.catename.set(catename);
    }

    public String getCategorycode() {
        return categorycode.get();
    }

    public StringProperty categorycodeProperty() {
        return categorycode;
    }

    public void setCategorycode(String categorycode) {
        this.categorycode.set(categorycode);
    }

    public String getCategoryname() {
        return categoryname.get();
    }

    public StringProperty categorynameProperty() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname.set(categoryname);
    }

    public String getDescpath() {
        return descpath.get();
    }

    public StringProperty descpathProperty() {
        return descpath;
    }

    public void setDescpath(String descpath) {
        this.descpath.set(descpath);
    }

    public String getSbrandcode() {
        return sbrandcode.get();
    }

    public StringProperty sbrandcodeProperty() {
        return sbrandcode;
    }

    public void setSbrandcode(String sbrandcode) {
        this.sbrandcode.set(sbrandcode);
    }

    public String getSbrandname() {
        return sbrandname.get();
    }

    public StringProperty sbrandnameProperty() {
        return sbrandname;
    }

    public void setSbrandname(String sbrandname) {
        this.sbrandname.set(sbrandname);
    }


    public AtomicLong getCount() {
        return count;
    }

    public static void main(String[] args) throws Exception {

        SQLExecutor.createTable(CateBrandMapping.class);

    }
}
