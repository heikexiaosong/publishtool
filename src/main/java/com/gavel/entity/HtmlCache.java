package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import com.gavel.utils.StringUtils;
import com.gavel.utils.ZipUtil;

import java.io.IOException;
import java.util.Date;

@TableMeta(name = "HTMLCACHE", title = "html缓存")
public class HtmlCache {

    @FieldMeta(name = "URL", title = "URL", length = 1024, primary = true)
    private String url;

    private String html;

    @FieldMeta(name = "COMPRESS", title = "压缩文本")
    private String compress;

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

        if (StringUtils.isBlank(html) && StringUtils.isNotBlank(compress)) {
            try {
                this.html = ZipUtil.uncompress(compress);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return html;
    }

    public void setHtml(String html) {
        this.html = html;
        if (StringUtils.isNotBlank(html)) {
            try {
                this.compress = ZipUtil.compress(html);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
