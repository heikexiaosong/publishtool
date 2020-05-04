package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

import java.util.Date;

@TableMeta(name = "SHELVESITEM", title = "上架产品")
public class ShelvesItem {

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private String id;

    @FieldMeta(name = "TASKID", title = "任务ID", length = 32)
    private String taskid;

    @FieldMeta(name = "XH", title = "序号")
    private int xh;

    @FieldMeta(name = "CODE", title = "SKU编码", length = 32)
    private String code;

    @FieldMeta(name = "NAME", title = "名称", length = 128)
    private String name;

    @FieldMeta(name = "GRAINGERBRAND", title = "固安捷品牌", length = 64)
    private String graingerbrand;

    @FieldMeta(name = "GRAINGERBRANDNAME", title = "固安捷品牌名称", length = 64)
    private String graingerbrandname;

    @FieldMeta(name = "GRAINGERCATEGORY", title = "固安捷类目", length = 64)
    private String graingercategory;

    @FieldMeta(name = "GRAINGERCATEGORYNAME", title = "固安捷类目名称", length = 64)
    private String graingercategoryname;

    @FieldMeta(name = "BRAND", title = "品牌", length = 64)
    private String brand;

    @FieldMeta(name = "BRANDNAME", title = "品牌名称", length = 64)
    private String brandname;

    @FieldMeta(name = "CATEGORY", title = "类目", length = 64)
    private String category;

    @FieldMeta(name = "CATEGORYNAME", title = "类目名称", length = 64)
    private String categoryname;

    @FieldMeta(name = "STATUS", title = "状态", length = 20)
    private String status;

    @FieldMeta(name = "MESSAGE", title = "状态", length = 20)
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

    public String getGraingerbrand() {
        return graingerbrand;
    }

    public void setGraingerbrand(String graingerbrand) {
        this.graingerbrand = graingerbrand;
    }

    public String getGraingerbrandname() {
        return graingerbrandname;
    }

    public void setGraingerbrandname(String graingerbrandname) {
        this.graingerbrandname = graingerbrandname;
    }

    public String getGraingercategory() {
        return graingercategory;
    }

    public void setGraingercategory(String graingercategory) {
        this.graingercategory = graingercategory;
    }

    public String getGraingercategoryname() {
        return graingercategoryname;
    }

    public void setGraingercategoryname(String graingercategoryname) {
        this.graingercategoryname = graingercategoryname;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBrandname() {
        return brandname;
    }

    public void setBrandname(String brandname) {
        this.brandname = brandname;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryname() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
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
