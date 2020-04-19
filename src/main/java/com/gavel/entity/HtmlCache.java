package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

import java.util.Date;

@TableMeta(name = "HTMLCACHE", title = "html缓存")
public class HtmlCache {

    @FieldMeta(name = "URL", title = "URL", length = 1024, primary = true)
    private String url;

    @FieldMeta(name = "HTML", title = "html", length = 99999)
    private String html;

    @FieldMeta(name = "CONTENTLEN", title = "内容长度")
    private int contentlen;

    @FieldMeta(name = "UPDATETIME", title = "更新时间")
    private Date updatetime;


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
}
