package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

import java.util.Date;

@TableMeta(name = "SHELVESITEM", title = "上架产品")
public class ShelvesItem {

    private Boolean selected = Boolean.FALSE;

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private String id;

    @FieldMeta(name = "TASKID", title = "任务ID", length = 32)
    private String taskid;

    @FieldMeta(name = "XH", title = "序号")
    private int xh;

    @FieldMeta(name = "ITEMCODE", title = "供应商商品编码", length = 32)
    private String itemCode;

    @FieldMeta(name = "PRODUCTNAME", title = "商品名称", length = 128)
    private String productName;

    @FieldMeta(name = "CMTITLE", title = "商品标题", length = 100)
    private String cmTitle;

    @FieldMeta(name = "SELLINGPOINTS", title = "商品卖点", length = 100)
    private String sellingPoints;

    @FieldMeta(name = "CATEGORYCODE", title = "类目", length = 64)
    private String categoryCode;

    @FieldMeta(name = "CATEGORYNAME", title = "类目", length = 64)
    private String categoryname;

    @FieldMeta(name = "BRANDCODE", title = "品牌", length = 64)
    private String brandCode;

    @FieldMeta(name = "BRANDNAME", title = "品牌", length = 64)
    private String brandname;

    @FieldMeta(name = "MAPPINGCATEGORYCODE", title = "上架类目", length = 64)
    private String mappingcategorycode;

    @FieldMeta(name = "MAPPINGBRANDCODE", title = "上架品牌", length = 64)
    private String mappingbrandcode;

    @FieldMeta(name = "MAPPINGCATEGORYNAME", title = "上架类目", length = 64)
    private String mappingcategoryname;

    @FieldMeta(name = "MAPPINGBRANDNAME", title = "上架品牌", length = 64)
    private String mappingbrandname;

    @FieldMeta(name = "INTRODUCTION", title = "商品描述", length = 4000)
    private String introduction;

    @FieldMeta(name = "STATUS", title = "状态", length = 20)
    private String status;

    @FieldMeta(name = "MESSAGE", title = "消息", length = 20)
    private String msg;

    @FieldMeta(name = "UPDATETIME", title = "更新时间")
    private Date updatetime;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public int getXh() {
        return xh;
    }

    public void setXh(int xh) {
        this.xh = xh;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCmTitle() {
        return cmTitle;
    }

    public void setCmTitle(String cmTitle) {
        this.cmTitle = cmTitle;
    }

    public String getSellingPoints() {
        return sellingPoints;
    }

    public void setSellingPoints(String sellingPoints) {
        this.sellingPoints = sellingPoints;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryname() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
    }

    public String getBrandCode() {
        return brandCode;
    }

    public void setBrandCode(String brandCode) {
        this.brandCode = brandCode;
    }

    public String getBrandname() {
        return brandname;
    }

    public void setBrandname(String brandname) {
        this.brandname = brandname;
    }

    public String getMappingcategorycode() {
        return mappingcategorycode;
    }

    public void setMappingcategorycode(String mappingcategorycode) {
        this.mappingcategorycode = mappingcategorycode;
    }

    public String getMappingbrandcode() {
        return mappingbrandcode;
    }

    public void setMappingbrandcode(String mappingbrandcode) {
        this.mappingbrandcode = mappingbrandcode;
    }

    public String getMappingcategoryname() {
        return mappingcategoryname;
    }

    public void setMappingcategoryname(String mappingcategoryname) {
        this.mappingcategoryname = mappingcategoryname;
    }

    public String getMappingbrandname() {
        return mappingbrandname;
    }

    public void setMappingbrandname(String mappingbrandname) {
        this.mappingbrandname = mappingbrandname;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}
