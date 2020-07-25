package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import com.gavel.database.SQLExecutor;

import java.util.Date;

@TableMeta(name = "ITEM_IMAGE", title = "商品图片")
public class ImageInfo {

    @FieldMeta(name = "ID", title = "ID", length = 128, primary = true)
    private String id;

    @FieldMeta(name = "REFID", title = "关联ID", length = 128)
    private String refid;

    @FieldMeta(name = "CODE", title = "SKU编码", length = 32)
    private String code;

    @FieldMeta(name = "TYPE", title = "图片类别", length = 8) // M: 主图  D: 详情
    private String type;

    @FieldMeta(name = "XH", title = "图片序号")
    private int xh;

    @FieldMeta(name = "PICURL", title = "原始图片URL", length = 1024)
    private String picurl;

    @FieldMeta(name = "FILEPATH", title = "本地路径", length = 1024)
    private String filepath;

    @FieldMeta(name = "SUNINGURL", title = "苏宁图片URL", length = 1024)
    private String suningurl;

    @FieldMeta(name = "STATUS", title = "苏宁图片有效", length = 1024)
    private int status;

    @FieldMeta(name = "UPDATETIME", title = "更新时间")
    private Date updatetime;

    public ImageInfo() {
        this.status = Integer.MAX_VALUE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRefid() {
        return refid;
    }

    public void setRefid(String refid) {
        this.refid = refid;
    }

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

    public int getXh() {
        return xh;
    }

    public void setXh(int xh) {
        this.xh = xh;
    }

    public String getPicurl() {
        return picurl;
    }

    public void setPicurl(String picurl) {
        this.picurl = picurl;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getSuningurl() {
        return suningurl;
    }

    public void setSuningurl(String suningurl) {
        this.suningurl = suningurl;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static void main(String[] args) throws Exception {
        SQLExecutor.createTable(ImageInfo.class);
    }
}
