package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

import java.util.Date;

@TableMeta(name = "HTML_CACHE", title = "html缓存")
public class HtmlCacheNew {

    @FieldMeta(name = "ID", title = "ID", length = 40, primary = true)
    private String id;

    @FieldMeta(name = "URL", title = "URL", length = 1024)
    private String url;

    @FieldMeta(name = "COMPRESS", title = "压缩文本")
    private String compress;

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

    public String getCompress() {
        return compress;
    }

    public void setCompress(String compress) {
        this.compress = compress;
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
