package com.gavel.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface FieldMeta {

	String name() default "";  //字段名称

	String title() default "";  //中文名称

	int length() default 0;  //数据库字段长度

	boolean primary() default false;   //主键
}