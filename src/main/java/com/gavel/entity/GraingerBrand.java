package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

@TableMeta(name = "GRAINGERBRAND", title = "固安捷品牌")
public class GraingerBrand {

    @FieldMeta(name = "CODE", title = "品牌编码", length = 32, primary = true)
    private String code;

    @FieldMeta(name = "NAME1", title = "名称1", length = 32)
    private String name1;

    @FieldMeta(name = "NAME2", title = "名称2", length = 32)
    private String name2;

    @FieldMeta(name = "LOGO", title = "LOGO", length = 1024)
    private String logo;

    @FieldMeta(name = "URL", title = "URL", length = 1024)
    private String url;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
