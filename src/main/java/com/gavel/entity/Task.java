package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

import java.util.Date;

@TableMeta(name = "TASK", title = "任务表")
public class Task {


    public static class Status {
        public static final String INIT = "init";
        public static final String RUNNING = "running";
        public static final String COMPLETE = "complete";
    }


    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private String id;

    @FieldMeta(name = "TITLE", title = "任务名称", length = 50)
    private String title;

    @FieldMeta(name = "URL", title = "爬取URL", length = 1024)
    private String url;

    @FieldMeta(name = "REMARK", title = "说明", length = 1024)
    private String remark;

    @FieldMeta(name = "PAGENUM", title = "页数")
    private int pagenum;

    @FieldMeta(name = "PRODUCTNUM", title = "产品组")
    private int productnum;

    @FieldMeta(name = "SKUNUM", title = "产品数")
    private int skunum;

    @FieldMeta(name = "STATUS", title = "状态", length = 20)
    private String status;

    @FieldMeta(name = "UPDATETIME", title = "创建时间")
    private Date updatetime;

    public Task() {
        id = String.valueOf(System.currentTimeMillis());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getPagenum() {
        return pagenum;
    }

    public void setPagenum(int pagenum) {
        this.pagenum = pagenum;
    }

    public int getProductnum() {
        return productnum;
    }

    public void setProductnum(int productnum) {
        this.productnum = productnum;
    }

    public int getSkunum() {
        return skunum;
    }

    public void setSkunum(int skunum) {
        this.skunum = skunum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}
