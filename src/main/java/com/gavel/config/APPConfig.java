package com.gavel.config;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.Shopinfo;
import com.gavel.utils.SafeProperties;
import com.gavel.utils.StringUtils;
import com.suning.api.DefaultSuningClient;

public class APPConfig {

    private static APPConfig INSTANCE = new APPConfig();

    private APPConfig() {

        this.shopinfo = new Shopinfo();
        init();
    }

    private SafeProperties properties;

    private final Shopinfo shopinfo ;


    private void init() {
        try {
            properties = SafeProperties.build("config.properties");
            System.out.println("[config.properties]Load 完成");
        } catch (Exception e){

        }
    }

    public static APPConfig getInstance() {
        return INSTANCE;
    }


    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public Shopinfo getShopinfo(){
        if (StringUtils.isBlank(shopinfo.getId())) {
            try {
                Shopinfo temp =  SQLExecutor.executeQueryBean("select * from SHOPINFO where CODE = ?  ", Shopinfo.class, "10214063");
                if ( temp !=null ) {
                    shopinfo.setId(temp.getId());
                    shopinfo.setCode(temp.getCode());
                    shopinfo.setName(temp.getName());
                    shopinfo.setPlatform(temp.getPlatform());
                    shopinfo.setType(temp.getType());
                    shopinfo.setEndpoint(temp.getEndpoint());
                    shopinfo.setAppkey(temp.getAppkey());
                    shopinfo.setAppsecret(temp.getAppsecret());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return shopinfo;
    }

    public DefaultSuningClient client() {
        final DefaultSuningClient client = new DefaultSuningClient(shopinfo.getEndpoint(), shopinfo.getAppkey(), shopinfo.getAppsecret());
        return client;
    }

}
