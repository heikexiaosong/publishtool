package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

import java.util.Date;

@TableMeta(name = "SHELVESTASK", title = "上架任务")
public class ShelvesTask {


    public static class Status {
        public static final String INIT = "init";
        public static final String RUNNING = "running";
        public static final String COMPLETE = "complete";
    }


    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private String id;

    @FieldMeta(name = "title", title = "任务名称", length = 50)
    private String title;

    @FieldMeta(name = "REMARK", title = "说明", length = 1024)
    private String remark;

    @FieldMeta(name = "MOQ", title = "起订量")
    private int moq;

    @FieldMeta(name = "SKUNUM", title = "产品数")
    private int skunum;

    @FieldMeta(name = "SUCCESS", title = "上架成功数")
    private int success;

    @FieldMeta(name = "FAILED", title = "上架失败数")
    private int failed;

    @FieldMeta(name = "STATUS", title = "状态", length = 20)
    private String status;

    @FieldMeta(name = "UPDATETIME", title = "创建时间")
    private Date updatetime;

    public ShelvesTask() {
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getSkunum() {
        return skunum;
    }

    public int getMoq() {
        return moq;
    }

    public void setMoq(int moq) {
        this.moq = moq;
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
