package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

@TableMeta(name = "ITEMPARAMETER", title = "类目参数")
public class Itemparameter {

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private String id;

    @FieldMeta(name = "CATEGORYCODE", title = "类目编码", length = 32)
    private String categoryCode;

    @FieldMeta(name = "SUPPLIERCODE", title = "供应商编码", length = 32)
    private String supplierCode;

    @FieldMeta(name = "PARATEMPLATECODE", title = "参数模板代码", length = 32)
    private String paraTemplateCode;

    @FieldMeta(name = "PARATEMPLATEDESC", title = "参数模板名称", length = 256)
    private String paraTemplateDesc;

    @FieldMeta(name = "PARCODE", title = "参数代码", length = 32, primary = true)
    private String parCode;

    @FieldMeta(name = "PARNAME", title = "参数名称", length = 32)
    private String parName;

    @FieldMeta(name = "PARTYPE", title = "参数类型", length = 8)
    private String parType; // 参数类型。1：单选；2：多选；3：手工输入；

    @FieldMeta(name = "PARUNIT", title = "单位", length = 20)
    private String parUnit;

    @FieldMeta(name = "ISMUST", title = "是否必填", length = 8)
    private String isMust;

    // 数值类型。CURR 货币型。 DATE 日期型 。TIME 时间型。 CHAR 字符型 长度100。 NUM 数字型 长度13.（最多两位小数）。
    @FieldMeta(name = "DATATYPE", title = "数值类型", length = 8)
    private String dataType;

    @FieldMeta(name = "OPTIONS", title = "选项", length = 4000)
    private String options;

    @FieldMeta(name = "PARAM", title = "默认值", length = 1024)
    private String param;

    private List<ParOption> parOption = new ArrayList<>();

    public static class ParOption {
        private String parOptionCode;
        private String parOptionDesc;

        public ParOption() {
        }

        public String getParOptionCode() {
            return this.parOptionCode;
        }

        public void setParOptionCode(String parOptionCode) {
            this.parOptionCode = parOptionCode;
        }

        public String getParOptionDesc() {
            return this.parOptionDesc;
        }

        public void setParOptionDesc(String parOptionDesc) {
            this.parOptionDesc = parOptionDesc;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getParaTemplateCode() {
        return paraTemplateCode;
    }

    public void setParaTemplateCode(String paraTemplateCode) {
        this.paraTemplateCode = paraTemplateCode;
    }

    public String getParaTemplateDesc() {
        return paraTemplateDesc;
    }

    public void setParaTemplateDesc(String paraTemplateDesc) {
        this.paraTemplateDesc = paraTemplateDesc;
    }

    public String getParCode() {
        return parCode;
    }

    public void setParCode(String parCode) {
        this.parCode = parCode;
    }

    public String getParName() {
        return parName;
    }

    public void setParName(String parName) {
        this.parName = parName;
    }

    public String getParType() {
        return parType;
    }

    public void setParType(String parType) {
        this.parType = parType;
    }

    public String getParUnit() {
        return parUnit;
    }

    public void setParUnit(String parUnit) {
        this.parUnit = parUnit;
    }

    public String getIsMust() {
        return isMust;
    }

    public void setIsMust(String isMust) {
        this.isMust = isMust;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public List<ParOption> getParOption() {
        if ( options==null || options.trim().length() <= 0 ){
            return parOption;
        }

        if ( parOption==null || parOption.size() == 0 ) {
            parOption = new Gson().fromJson(options, new TypeToken<List<ParOption>>() {}.getType());
        }

        return parOption;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}
