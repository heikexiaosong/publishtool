package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;

@TableMeta(name = "SHELVESITEM", title = "上架产品")
public class ShelvesItem {

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

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private final StringProperty id = new SimpleStringProperty();

    @FieldMeta(name = "TASKID", title = "任务ID", length = 32)
    private final StringProperty taskid = new SimpleStringProperty();

    @FieldMeta(name = "SKUCODE", title = "SKU编码")
    private final StringProperty skuCode= new SimpleStringProperty();

    @FieldMeta(name = "ITEMCODE", title = "供应商商品编码", length = 32)
    private final StringProperty itemCode= new SimpleStringProperty();

    @FieldMeta(name = "MODEL", title = "制造商型号", length = 100)
    private final StringProperty model= new SimpleStringProperty();

    @FieldMeta(name = "PRODUCTNAME", title = "商品名称", length = 128)
    private final StringProperty productName= new SimpleStringProperty();

    @FieldMeta(name = "CMTITLE", title = "商品标题", length = 100)
    private final StringProperty cmTitle= new SimpleStringProperty();

    @FieldMeta(name = "SELLINGPOINTS", title = "商品卖点", length = 100)
    private final StringProperty sellingPoints= new SimpleStringProperty();

    @FieldMeta(name = "CATEGORYCODE", title = "类目", length = 64)
    private final StringProperty categoryCode= new SimpleStringProperty();

    @FieldMeta(name = "CATEGORYNAME", title = "类目", length = 64)
    private final StringProperty categoryname= new SimpleStringProperty();

    @FieldMeta(name = "BRANDCODE", title = "品牌", length = 64)
    private final StringProperty brandCode= new SimpleStringProperty();

    @FieldMeta(name = "BRANDNAME", title = "品牌", length = 64)
    private final StringProperty brandname= new SimpleStringProperty();

    @FieldMeta(name = "MAPPINGCATEGORYCODE", title = "上架类目", length = 64)
    private final StringProperty mappingcategorycode= new SimpleStringProperty();

    @FieldMeta(name = "MAPPINGBRANDCODE", title = "上架品牌", length = 64)
    private final StringProperty mappingbrandcode= new SimpleStringProperty();

    @FieldMeta(name = "MAPPINGCATEGORYNAME", title = "上架类目", length = 64)
    private final StringProperty mappingcategoryname= new SimpleStringProperty();

    @FieldMeta(name = "MAPPINGBRANDNAME", title = "上架品牌", length = 64)
    private final StringProperty mappingbrandname= new SimpleStringProperty();

    @FieldMeta(name = "INTRODUCTION", title = "商品描述", length = 4000)
    private final StringProperty introduction= new SimpleStringProperty();

    @FieldMeta(name = "STATUS", title = "状态", length = 20)
    private final StringProperty status= new SimpleStringProperty();

    @FieldMeta(name = "MESSAGE", title = "消息", length = 4000)
    private final StringProperty message= new SimpleStringProperty();

    @FieldMeta(name = "UPDATETIME", title = "更新时间")
    private Date updatetime;

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getTaskid() {
        return taskid.get();
    }

    public StringProperty taskidProperty() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid.set(taskid);
    }

    public String getSkuCode() {
        return skuCode.get();
    }

    public StringProperty skuCodeProperty() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode.set(skuCode);
    }

    public String getItemCode() {
        return itemCode.get();
    }

    public StringProperty itemCodeProperty() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode.set(itemCode);
    }

    public String getModel() {
        return model.get();
    }

    public StringProperty modelProperty() {
        return model;
    }

    public void setModel(String model) {
        this.model.set(model);
    }

    public String getProductName() {
        return productName.get();
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    public String getCmTitle() {
        return cmTitle.get();
    }

    public StringProperty cmTitleProperty() {
        return cmTitle;
    }

    public void setCmTitle(String cmTitle) {
        this.cmTitle.set(cmTitle);
    }

    public String getSellingPoints() {
        return sellingPoints.get();
    }

    public StringProperty sellingPointsProperty() {
        return sellingPoints;
    }

    public void setSellingPoints(String sellingPoints) {
        this.sellingPoints.set(sellingPoints);
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

    public String getCategoryname() {
        return categoryname.get();
    }

    public StringProperty categorynameProperty() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname.set(categoryname);
    }

    public String getBrandCode() {
        return brandCode.get();
    }

    public StringProperty brandCodeProperty() {
        return brandCode;
    }

    public void setBrandCode(String brandCode) {
        this.brandCode.set(brandCode);
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

    public String getMappingcategorycode() {
        return mappingcategorycode.get();
    }

    public StringProperty mappingcategorycodeProperty() {
        return mappingcategorycode;
    }

    public void setMappingcategorycode(String mappingcategorycode) {
        this.mappingcategorycode.set(mappingcategorycode);
    }

    public String getMappingbrandcode() {
        return mappingbrandcode.get();
    }

    public StringProperty mappingbrandcodeProperty() {
        return mappingbrandcode;
    }

    public void setMappingbrandcode(String mappingbrandcode) {
        this.mappingbrandcode.set(mappingbrandcode);
    }

    public String getMappingcategoryname() {
        return mappingcategoryname.get();
    }

    public StringProperty mappingcategorynameProperty() {
        return mappingcategoryname;
    }

    public void setMappingcategoryname(String mappingcategoryname) {
        this.mappingcategoryname.set(mappingcategoryname);
    }

    public String getMappingbrandname() {
        return mappingbrandname.get();
    }

    public StringProperty mappingbrandnameProperty() {
        return mappingbrandname;
    }

    public void setMappingbrandname(String mappingbrandname) {
        this.mappingbrandname.set(mappingbrandname);
    }

    public String getIntroduction() {
        return introduction.get();
    }

    public StringProperty introductionProperty() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction.set(introduction);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getMessage() {
        return message.get();
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}
