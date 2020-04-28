package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;

@TableMeta(name = "CATEGORYMAPPING", title = "类目映射")
public class CategoryMapping {

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

}
