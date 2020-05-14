package com.gavel.database;

import com.alibaba.druid.pool.DruidDataSource;
import com.gavel.config.APPConfig;

import javax.sql.DataSource;

public class DataSourceHolder {

    private static DataSourceHolder  INSTANCE = new DataSourceHolder();


    private DataSource dataSource = null;

    public DataSourceHolder() {

        APPConfig appConfig = APPConfig.getInstance();

        DruidDataSource druidDataSource = new DruidDataSource();

        druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(appConfig.getProperty("jdbc.driverClassName", "com.mysql.jdbc.Driver"));
        druidDataSource.setUsername(appConfig.getProperty("jdbc.username","root"));
        druidDataSource.setPassword(appConfig.getProperty("jdbc.password","root"));
        druidDataSource.setUrl(appConfig.getProperty("jdbc.url", "jdbc:mysql://127.0.0.1:3306/jingsu?useUnicode=true&characterEncoding=utf-8&useSSL=false"));
        druidDataSource.setInitialSize(5);
        druidDataSource.setMinIdle(1);
        druidDataSource.setMaxActive(10);
        // 启用监控统计功能  dataSource.setFilters("stat");
        // for mysql  dataSource.setPoolPreparedStatements(false);
        druidDataSource.setPoolPreparedStatements(false);

        dataSource = druidDataSource;
    }

    public static DataSourceHolder getInstance() {
        return INSTANCE;
    }

    public static DataSource dataSource(){
        return INSTANCE.dataSource;
    }
}
