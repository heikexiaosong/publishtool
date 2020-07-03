package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;

@TableMeta(name = "TASK", title = "任务表")
public class Task {


    public static class Status {
        public static final String READY = "ready";
        public static final String INIT = "init";
        public static final String RUNNING = "running";
        public static final String COMPLETE = "complete";
    }

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

    @FieldMeta(name = "TITLE", title = "任务名称", length = 50)
    private final StringProperty title = new SimpleStringProperty();


    @FieldMeta(name = "URL", title = "爬取URL", length = 1024)
    private final StringProperty url = new SimpleStringProperty();

    @FieldMeta(name = "REMARK", title = "说明", length = 1024)
    private final StringProperty remark = new SimpleStringProperty();

    @FieldMeta(name = "PAGENUM", title = "页数")
    private int pagenum;

    @FieldMeta(name = "PRODUCTNUM", title = "产品组")
    private int productnum;

    @FieldMeta(name = "SKUNUM", title = "产品数")
    private int skunum;

    @FieldMeta(name = "STATUS", title = "状态", length = 20)
    private final StringProperty status = new SimpleStringProperty();

    @FieldMeta(name = "UPDATETIME", title = "创建时间")
    private Date updatetime;

    @FieldMeta(name = "TYPE", title = "类型", length = 20)
    private final StringProperty type = new SimpleStringProperty();


    public Task() {
        id.set(String.valueOf(System.currentTimeMillis()));
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

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public String getRemark() {
        return remark.get();
    }

    public StringProperty remarkProperty() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark.set(remark);
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
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }
}
