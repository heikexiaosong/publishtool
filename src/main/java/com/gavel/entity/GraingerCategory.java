package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

@TableMeta(name = "GRAINGERCATEGORY", title = "固安捷类目")
public class GraingerCategory {

    @FieldMeta(name = "CODE", title = "类目编码", length = 32, primary = true)
    private String code;

    @FieldMeta(name = "NAME", title = "类目名称", length = 32)
    private String name;

    @FieldMeta(name = "PARENT", title = "父类目编码", length = 32)
    private String parent;

    @FieldMeta(name = "GRADE", title = "类目层级", length = 8)
    private String grade;

    @FieldMeta(name = "URL", title = "URL", length = 32)
    private String url;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "GraingerCategory{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", parent='" + parent + '\'' +
                ", grade='" + grade + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
