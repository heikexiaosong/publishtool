package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

import java.util.Date;

@TableMeta(name = "PROXY", title = "代理信息")
public class Proxy {

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private String id;

    @FieldMeta(name = "IP", title = "IP地址", length = 15)
    private String ip;

    @FieldMeta(name = "PORT", title = "端口")
    private int port;

    @FieldMeta(name = "TYPE", title = "代理类型", length = 8)
    private String type;

    @FieldMeta(name = "UPDATETIME", title = "更新时间")
    private Date updatetime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}
