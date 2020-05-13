package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

@TableMeta(name = "CATEGORYMAPPING", title = "类目映射")
public class CategoryMapping {

    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    @FieldMeta(name = "CODE", title = "固安捷类目编码", length = 32, primary = true)
    private String code;

    @FieldMeta(name = "NAME", title = "固安捷类目名称", length = 32)
    private String name;

    @FieldMeta(name = "PARENT", title = "固安捷父类目编码", length = 32)
    private String parent;

    @FieldMeta(name = "GRADE", title = "固安捷类目层级", length = 8)
    private String grade;

    @FieldMeta(name = "TASKID", title = "任务ID", length = 32)
    private String taskid;

    @FieldMeta(name = "CATEGORYCODE", title = "苏宁类目编码", length = 32)
    private String categoryCode;

    @FieldMeta(name = "CATEGORYNAME", title = "苏宁类目名称", length = 32)
    private String categoryName;

    @FieldMeta(name = "DESCPATH", title = "采购目录层级描述", length = 128)
    private String descPath;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescPath() {
        return descPath;
    }

    public void setDescPath(String descPath) {
        this.descPath = descPath;
    }
}
