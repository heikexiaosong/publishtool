package com.gavel.database;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import com.gavel.entity.*;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class SQLExecutor {

    public static void createTable(Class clz) throws Exception {

        if ( clz==null ){
            return;
        }

        TableMeta tableMeta = (TableMeta)clz.getAnnotation(TableMeta.class);
        if ( tableMeta==null ){
            System.out.println("非实体类");
            return;
        }

        String table = tableMeta.name();

        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        builder.append(table).append("\n")
                .append("(").append("\n");

        Field[] fs = clz.getDeclaredFields();
        for (Field f : fs) {
            FieldMeta fieldMeta =  f.getAnnotation(FieldMeta.class);
            if ( fieldMeta!=null ){
                System.out.println( fieldMeta.name() + ", " + fieldMeta.length() + ", " + f.getType().getName());
                System.out.println( f.getType());
                if ( f.getType().equals(String.class) ) {
                    if (  fieldMeta.length() > 30000 ) {
                        builder.append(fieldMeta.name()).append(" MEDIUMTEXT,\n");
                    } else if (  fieldMeta.length() > 2000 )  {
                        builder.append(fieldMeta.name()).append(" TEXT,\n");
                    } else {
                        builder.append(fieldMeta.name()).append(" NVARCHAR(").append(fieldMeta.length()).append("),\n");
                    }
                } else if ( f.getType().equals(String.class) ) {
                    builder.append(fieldMeta.name()).append(" FLOAT,\n");
                } else if ( f.getType().equals(int.class) ) {
                    builder.append(fieldMeta.name()).append(" BIGINT,\n");
                } else if ( f.getType().equals(Date.class) ) {
                    builder.append(fieldMeta.name()).append(" TIMESTAMP,\n");
                } else {
                    builder.append(fieldMeta.name()).append(" NVARCHAR(").append(fieldMeta.length()).append("),\n");
                }
            }
        }

        builder.deleteCharAt(builder.length()-2);
        builder.append(");");

        System.out.println(builder.toString());

        QueryRunner runner = new QueryRunner(DataSourceHolder.dataSource());
        runner.update(builder.toString());

    }

    public static  <T> List<T> executeQueryBeanList(String sql, Class<T> clz, Object... params) throws Exception {
        QueryRunner runner = new QueryRunner(DataSourceHolder.dataSource());
        return runner.query(sql, new BeanListHandler<T>(clz));
    }

    public static void main(String[] args) throws Exception {
        createTable(Brand.class);
        createTable(Category.class);
        createTable(GraingerBrand.class);
        createTable(GraingerCategory.class);
        createTable(Product.class);
        createTable(Itemparameter.class);
        createTable(Item.class);
        createTable(HtmlCache.class);
    }

}
