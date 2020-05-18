package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;

@TableMeta(name = "SHELVESTASK", title = "上架任务")
public class ShelvesTask {

    public static class Status {
        public static final String INIT = "init";
        public static final String RUNNING = "running";
        public static final String COMPLETE = "complete";
    }

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private final StringProperty id = new SimpleStringProperty();


    @FieldMeta(name = "SHOPID", title = "店铺ID", length = 50)
    private final StringProperty shopid = new SimpleStringProperty();

    @FieldMeta(name = "TITLE", title = "任务名称", length = 50)
    private final StringProperty title = new SimpleStringProperty();

    @FieldMeta(name = "REMARK", title = "说明", length = 1024)
    private final StringProperty remark = new SimpleStringProperty();

    @FieldMeta(name = "MOQ", title = "起订量")
    private int moq;

    @FieldMeta(name = "SKUNUM", title = "产品数")
    private int skunum;

    @FieldMeta(name = "SUCCESS", title = "上架成功数")
    private int success;

    @FieldMeta(name = "FAILED", title = "上架失败数")
    private int failed;

    @FieldMeta(name = "STATUS", title = "状态", length = 20)
    private final StringProperty status = new SimpleStringProperty();

    @FieldMeta(name = "UPDATETIME", title = "创建时间")
    private Date updatetime;

    @FieldMeta(name = "PIC", title ="默认图片", length = 1024)
    private final StringProperty pic = new SimpleStringProperty();

    public ShelvesTask() {
        setId(String.valueOf(System.currentTimeMillis()));
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

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
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

    public int getMoq() {
        return moq;
    }

    public void setMoq(int moq) {
        this.moq = moq;
    }

    public int getSkunum() {
        return skunum;
    }

    public void setSkunum(int skunum) {
        this.skunum = skunum;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
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

    public String getPic() {
        return pic.get();
    }

    public StringProperty picProperty() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic.set(pic);
    }
}
