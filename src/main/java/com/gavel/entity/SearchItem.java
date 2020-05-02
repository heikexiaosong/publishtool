package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

@TableMeta(name = "SEARCHITEM", title = "搜索结果表")
public class SearchItem {

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private String id;

    @FieldMeta(name = "TASKID", title = "任务ID", length = 32)
    private String taskid;

    @FieldMeta(name = "PAGENUM", title = "页数")
    private int pagenum;

    @FieldMeta(name = "XH", title = "页面序号")
    private int xh;

    @FieldMeta(name = "TYPE", title = "类型", length = 8)
    private String type;

    @FieldMeta(name = "CODE", title = "编码", length = 20)
    private String code;

    @FieldMeta(name = "TITLE", title = "标题", length = 100)
    private String title;

    @FieldMeta(name = "URL", title = "URL", length = 1024)
    private String url;

    @FieldMeta(name = "SKUNUM", title = "包含sku数")
    private int skunum;

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

    public int getPagenum() {
        return pagenum;
    }

    public void setPagenum(int pagenum) {
        this.pagenum = pagenum;
    }

    public int getXh() {
        return xh;
    }

    public void setXh(int xh) {
        this.xh = xh;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSkunum() {
        return skunum;
    }

    public void setSkunum(int skunum) {
        this.skunum = skunum;
    }
}
