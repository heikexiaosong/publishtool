package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import com.gavel.database.SQLExecutor;
import com.gavel.utils.MD5Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@TableMeta(name = "SHOPINFO", title = "店铺信息")
public class Shopinfo {

    public static class Platform {
        public static final String SUNNING = "A";
        public static final String JD = "B";
    }

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private final StringProperty id = new SimpleStringProperty();

    @FieldMeta(name = "CODE", title = "店铺编码", length = 100)
    private final StringProperty code = new SimpleStringProperty();

    @FieldMeta(name = "NAME", title = "店铺名称", length = 100)
    private final StringProperty name = new SimpleStringProperty();

    @FieldMeta(name = "PLATFORM", title = "店铺平台", length = 100)
    private final StringProperty platform = new SimpleStringProperty();

    @FieldMeta(name = "TYPE", title = "店铺类型", length = 100)
    private final StringProperty type = new SimpleStringProperty();

    @FieldMeta(name = "ENDPOINT", title = "接口地址", length = 256)
    private final StringProperty endpoint = new SimpleStringProperty();

    @FieldMeta(name = "APPKEY", title = "接口KEY", length = 256)
    private final StringProperty appkey = new SimpleStringProperty();

    @FieldMeta(name = "APPSECRET", title = "接口密钥", length = 256)
    private final StringProperty appsecret = new SimpleStringProperty();

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getCode() {
        return code.get();
    }

    public StringProperty codeProperty() {
        return code;
    }

    public void setCode(String code) {
        this.code.set(code);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getPlatform() {
        return platform.get();
    }

    public StringProperty platformProperty() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform.set(platform);
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

    public String getEndpoint() {
        return endpoint.get();
    }

    public StringProperty endpointProperty() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint.set(endpoint);
    }

    public String getAppkey() {
        return appkey.get();
    }

    public StringProperty appkeyProperty() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey.set(appkey);
    }

    public String getAppsecret() {
        return appsecret.get();
    }

    public StringProperty appsecretProperty() {
        return appsecret;
    }

    public void setAppsecret(String appsecret) {
        this.appsecret.set(appsecret);
    }

    public static void main(String[] args) throws Exception {
        //SQLExecutor.createTable(Shopinfo.class);

        Shopinfo shopinfo = new Shopinfo();
        shopinfo.setCode("10214063");
        shopinfo.setName("京苏易购电子商务（江苏）有限公司");
        shopinfo.setPlatform("苏宁");
        shopinfo.setType("B端店铺");

        shopinfo.setEndpoint("https://open.suning.com/api/http/sopRequest");
        shopinfo.setAppkey("59b8afae20d9a64886b4e31b64d06422");
        shopinfo.setAppsecret("c987b0b479dd2a00c6e01be0ccb916ca");

        shopinfo.setId(MD5Utils.md5Hex(shopinfo.getCode() + "_" + shopinfo.getAppkey()));

        SQLExecutor.insert(shopinfo);


        shopinfo = new Shopinfo();
        shopinfo.setCode("10148425");
        shopinfo.setName("帷易胜（江苏）工业品有限公司");
        shopinfo.setPlatform("苏宁");
        shopinfo.setType("B端店铺");

        shopinfo.setEndpoint("https://open.suning.com/api/http/sopRequest");
        shopinfo.setAppkey("7e8ab1a1444856e80c7a79650c3022cb");
        shopinfo.setAppsecret("48b64e40dcef582abfc4555b10ace5df");

        shopinfo.setId(MD5Utils.md5Hex(shopinfo.getCode() + "_" + shopinfo.getAppkey()));

        SQLExecutor.insert(shopinfo);





    }
}
