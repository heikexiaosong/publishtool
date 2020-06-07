package com.gavel.entity.jd;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import com.gavel.database.SQLExecutor;

@TableMeta(name = "JDBRAND", title = "京东品牌")
public class JDBrand {


    /**
     * erpBrandId : 544662
     * brandName : 英飞力
     */

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private String id;

    @FieldMeta(name = "SHOPID", title = "店铺ID", length = 20)
    private String shopid;

    @FieldMeta(name = "ERP品牌ID", title = "店铺ID", length = 20)
    private int erpBrandId;

    @FieldMeta(name = "品牌名称", title = "店铺ID", length = 20)
    private String brandName;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShopid() {
        return shopid;
    }

    public void setShopid(String shopid) {
        this.shopid = shopid;
    }

    public int getErpBrandId() {
        return erpBrandId;
    }

    public void setErpBrandId(int erpBrandId) {
        this.erpBrandId = erpBrandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public static void main(String[] args) throws Exception {
        SQLExecutor.createTable(JDBrand.class);
    }
}
