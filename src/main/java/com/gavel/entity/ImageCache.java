package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

import java.util.Date;

@TableMeta(name = "IMAGE", title = "图片")
public class ImageCache {

    @FieldMeta(name = "ID", title = "ID", length = 128, primary = true)
    private String id;

    @FieldMeta(name = "URL", title = "图片URL", length = 1024)
    private String url;

    @FieldMeta(name = "FILEPATH", title = "图片路径", length = 64)
    private String filepath;

    @FieldMeta(name = "PICURL", title = "苏宁图片URL", length = 1024)
    private String picurl;

    @FieldMeta(name = "UPDATETIME", title = "更新时间")
    private Date updatetime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getPicurl() {
        return picurl;
    }

    public void setPicurl(String picurl) {
        this.picurl = picurl;
    }
}
