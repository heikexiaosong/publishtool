package com.gavel.database;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import com.gavel.entity.*;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SQLExecutor {

    public static int intQuery(String query, Object... params) throws Exception {

        QueryRunner runner = new QueryRunner(DataSourceHolder.dataSource());
        Map<String, Object> res = runner.query(query, new MapHandler(), params);
        if ( res==null || res.size()<=0 ) {
            return 0;
        }

        Object obj = res.values().iterator().next();
        if ( obj==null ) {
            return 0;
        }

        if ( obj instanceof  Number ) {
            return  ((Number) obj).intValue();
        }

        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void insert(Object record) throws Exception {

        if ( record==null ){
            return;
        }

        Class clz = record.getClass();
        TableMeta tableMeta = (TableMeta)clz.getAnnotation(TableMeta.class);
        if ( tableMeta==null ){
            System.out.println("非实体类");
            return;
        }

        String table = tableMeta.name();
        // INSERT INTO jingsu.itemparameter (CATEGORYCODE, PARATEMPLATECODE, PARATEMPLATEDESC, PARCODE, PARNAME, PARTYPE, PARUNIT, ISMUST, DATATYPE, OPTIONS) VALUES ('R9002886', 'basic', '基本参数模板', 'cmModel', '商品型号', '3', '', 'X', null, 'null');
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(table).append(" (");

        List<Object> paramObjs = new ArrayList<>();

        StringBuilder params = new StringBuilder();
        Field[] fs = clz.getDeclaredFields();
        for (Field f : fs) {

            FieldMeta fieldMeta =  f.getAnnotation(FieldMeta.class);
            if ( fieldMeta!=null ){
                //System.out.println( fieldMeta.name() + ", " + fieldMeta.length() + ", " + f.getType().getName());
                builder.append(fieldMeta.name()).append(",");
                params.append("?,");
                boolean access =  f.isAccessible();
                f.setAccessible(true);
                paramObjs.add(f.get(record));
                f.setAccessible(access);
            }
        }
        builder.deleteCharAt(builder.length()-1);
        params.deleteCharAt(params.length()-1);
        builder.append(") VALUES (").append(params).append(")");

        QueryRunner runner = new QueryRunner(DataSourceHolder.dataSource());
        try {
            runner.execute(builder.toString(), paramObjs.toArray(new Object[paramObjs.size()]));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

//        QueryRunner runner = new QueryRunner(DataSourceHolder.dataSource());
//        runner.update(builder.toString());

    }

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
                        builder.append(fieldMeta.name()).append(" MEDIUMTEXT");
                    } else if (  fieldMeta.length() > 2000 )  {
                        builder.append(fieldMeta.name()).append(" TEXT");
                    } else {
                        builder.append(fieldMeta.name()).append(" NVARCHAR(").append(fieldMeta.length()).append(")");
                    }
                } else if ( f.getType().equals(String.class) ) {
                    builder.append(fieldMeta.name()).append(" FLOAT");
                } else if ( f.getType().equals(int.class) ) {
                    builder.append(fieldMeta.name()).append(" BIGINT");
                } else if ( f.getType().equals(Date.class) ) {
                    builder.append(fieldMeta.name()).append(" TIMESTAMP");
                } else {
                    builder.append(fieldMeta.name()).append(" NVARCHAR(").append(fieldMeta.length()).append(")");
                }

                if ( fieldMeta.primary() ) {
                    builder.append(" primary key");
                }
                builder.append(",\n");
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
        return runner.query(sql, new BeanListHandler<T>(clz), params);
    }

    public static  <T> T executeQueryBean(String sql, Class<T> clz, Object... params) throws Exception {
        QueryRunner runner = new QueryRunner(DataSourceHolder.dataSource());
        return runner.query(sql, new BeanHandler<T>(clz), params);
    }

    public static  int execute(String sql,  Object... params) throws Exception {
        QueryRunner runner = new QueryRunner(DataSourceHolder.dataSource());
        return runner.execute(sql, params);
    }

    public static void update(Object record) throws Exception {
        if ( record==null ){
            return;
        }

        Class clz = record.getClass();
        TableMeta tableMeta = (TableMeta)clz.getAnnotation(TableMeta.class);
        if ( tableMeta==null ){
            System.out.println("非实体类");
            return;
        }

        String table = tableMeta.name();
        // UPDATE image set URL = ?  where id = ?;
        StringBuilder builder = new StringBuilder("UPDATE ").append(table).append(" SET ");

        List<Object> paramObjs = new ArrayList<>();
        Field[] fs = clz.getDeclaredFields();
        for (Field f : fs) {
            FieldMeta fieldMeta =  f.getAnnotation(FieldMeta.class);
            if ( fieldMeta!=null && !fieldMeta.primary() ){
                //System.out.println( fieldMeta.name() + ", " + fieldMeta.length() + ", " + f.getType().getName());
                builder.append(" ").append(fieldMeta.name()).append(" = ?,");
                boolean access =  f.isAccessible();
                f.setAccessible(true);
                paramObjs.add(f.get(record));
                f.setAccessible(access);
            }
        }
        builder.deleteCharAt(builder.length()-1);

        builder.append(" where ");

        for (Field f : fs) {
            FieldMeta fieldMeta =  f.getAnnotation(FieldMeta.class);
            if ( fieldMeta!=null && fieldMeta.primary() ){
                //System.out.println( fieldMeta.name() + ", " + fieldMeta.length() + ", " + f.getType().getName());
                builder.append(" ").append(fieldMeta.name()).append(" = ? and");
                boolean access =  f.isAccessible();
                f.setAccessible(true);
                paramObjs.add(f.get(record));
                f.setAccessible(access);
            }
        }

        builder.delete(builder.length()-3, builder.length());

        QueryRunner runner = new QueryRunner(DataSourceHolder.dataSource());
        try {
            runner.execute(builder.toString(), paramObjs.toArray(new Object[paramObjs.size()]));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public static void delete(Object record) throws Exception {
        if ( record==null ){
            return;
        }

        Class clz = record.getClass();
        TableMeta tableMeta = (TableMeta)clz.getAnnotation(TableMeta.class);
        if ( tableMeta==null ){
            System.out.println("非实体类");
            return;
        }

        String table = tableMeta.name();
        // UPDATE image set URL = ?  where id = ?;
        StringBuilder builder = new StringBuilder("DELETE from ").append(table).append(" where ");

        boolean hasPrimary = false;

        List<Object> paramObjs = new ArrayList<>();
        Field[] fs = clz.getDeclaredFields();
        for (Field f : fs) {
            FieldMeta fieldMeta =  f.getAnnotation(FieldMeta.class);
            if ( fieldMeta!=null && fieldMeta.primary() ){
                hasPrimary = true;
                builder.append(" ").append(fieldMeta.name()).append(" = ? and");
                boolean access =  f.isAccessible();
                f.setAccessible(true);
                paramObjs.add(f.get(record));
                f.setAccessible(access);
            }
        }

        if ( !hasPrimary ) {
            return;
        }

        builder.delete(builder.length()-3, builder.length());
        QueryRunner runner = new QueryRunner(DataSourceHolder.dataSource());
        try {
            runner.execute(builder.toString(), paramObjs.toArray(new Object[paramObjs.size()]));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }



    public static void main(String[] args) throws Exception {
        createTable(Brand.class);
        createTable(Category.class);
        createTable(GraingerBrand.class);
        createTable(GraingerCategory.class);
        createTable(Product.class);
        createTable(Itemparameter.class);
        createTable(HtmlCache.class);

        createTable(ImageCache.class);

        createTable(BrandMapping.class);
        createTable(CategoryMapping.class);

        createTable(Proxy.class);
        createTable(Task.class);
        createTable(SearchItem.class);
        createTable(Item.class);
    }
}
