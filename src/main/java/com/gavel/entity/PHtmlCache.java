package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.PartitionMeta;
import com.gavel.annotation.TableMeta;
import com.gavel.database.SQLExecutor;

import java.util.Date;

@TableMeta(name = "HTMLCACHE", title = "html缓存")
@PartitionMeta
public class PHtmlCache {

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private String id;

    @FieldMeta(name = "URL", title = "URL", length = 1024)
    private String url;

    @FieldMeta(name = "HTML", title = "压缩文本")
    private String html;

    @FieldMeta(name = "CONTENTLEN", title = "内容长度")
    private int contentlen;

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

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public int getContentlen() {
        return contentlen;
    }

    public void setContentlen(int contentlen) {
        this.contentlen = contentlen;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public static void main(String[] args) throws Exception {
        SQLExecutor.createTable(PHtmlCache.class);
    }
}
