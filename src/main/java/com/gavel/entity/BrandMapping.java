package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

@TableMeta(name = "BRANDMAPPING", title = "品牌映射")
public class BrandMapping {


    @FieldMeta(name = "TASKID", title = "任务ID", length = 32)
    private String taskid;

    @FieldMeta(name = "GRAINGERCODE", title = "固安捷编码", length = 32, primary = true)
    private String graingercode;

    @FieldMeta(name = "NAME1", title = "名称1", length = 32)
    private String name1;

    @FieldMeta(name = "NAME2", title = "名称2", length = 32)
    private String name2;

    @FieldMeta(name = "REPLACENAME_ZH", title = "替换中文品牌", length = 32)
    private String replacename_zh;

    @FieldMeta(name = "REPLACENAME_EN", title = "替换英文品牌", length = 32)
    private String replacename_en;

    @FieldMeta(name = "BRAND", title = "苏宁品牌", length = 32)
    private String brand;

    @FieldMeta(name = "BRANDNAME", title = "苏宁品牌", length = 32)
    private String brandname;

    @FieldMeta(name = "LOGO", title = "LOGO", length = 1024)
    private String logo;

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getGraingercode() {
        return graingercode;
    }

    public void setGraingercode(String graingercode) {
        this.graingercode = graingercode;
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

    public String getReplacename_zh() {
        return replacename_zh;
    }

    public void setReplacename_zh(String replacename_zh) {
        this.replacename_zh = replacename_zh;
    }

    public String getReplacename_en() {
        return replacename_en;
    }

    public void setReplacename_en(String replacename_en) {
        this.replacename_en = replacename_en;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBrandname() {
        return brandname;
    }

    public void setBrandname(String brandname) {
        this.brandname = brandname;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
