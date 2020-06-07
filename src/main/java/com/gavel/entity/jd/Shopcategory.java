package com.gavel.entity.jd;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import com.gavel.database.SQLExecutor;

import java.util.Date;


@TableMeta(name = "SHOPCATEGORY", title = "京东类目")
public class Shopcategory {


    /**
     * cid : 14363728
     * venderId : 10358817
     * shopId : 10216927
     * parentCid : 14358810
     * orderNo : 9
     * name : 液压辅助元件
     * isOpen : false
     * isHomeShow : false
     * status : 1
     * createTime : Apr 27, 2020 12:00:00 AM
     * modifyTime : Apr 27, 2020 12:00:00 AM
     */

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private String id;

    @FieldMeta(name = "CID", title = "分类编号", length = 20)
    private int cid;

    @FieldMeta(name = "VENDERID", title = "商家编号", length = 20)
    private int venderId;

    @FieldMeta(name = "SHOPID", title = "店铺编号", length = 20)
    private int shopId;

    @FieldMeta(name = "PARENTCID", title = "父分类编号", length = 20)
    private int parentCid;

    @FieldMeta(name = "ORDERNO", title = "分类排序", length = 20)
    private int orderNo;

    @FieldMeta(name = "NAME", title = "分类名称", length = 100)
    private String name;

    @FieldMeta(name = "ISOPEN", title = "是否展开子分类（false，不展开；true，展开）", length = 20)
    private boolean isOpen;

    @FieldMeta(name = "ISHOMESHOW", title = "是否在首页展示分类（false，前台不展示，true前台展示", length = 20)
    private boolean isHomeShow;

    @FieldMeta(name = "STATUS", title = "状态(-1：删除 1：正常)", length = 20)
    private int status;

    @FieldMeta(name = "CREATETIME", title = "创建时间", length = 20)
    private Date createTime;

    @FieldMeta(name = "MODIFYTIME", title = "修改时间。每修改一次此值都会发生变化。", length = 20)
    private Date modifyTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getVenderId() {
        return venderId;
    }

    public void setVenderId(int venderId) {
        this.venderId = venderId;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public int getParentCid() {
        return parentCid;
    }

    public void setParentCid(int parentCid) {
        this.parentCid = parentCid;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIsOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public boolean isIsHomeShow() {
        return isHomeShow;
    }

    public void setIsHomeShow(boolean isHomeShow) {
        this.isHomeShow = isHomeShow;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean isHomeShow() {
        return isHomeShow;
    }

    public void setHomeShow(boolean homeShow) {
        isHomeShow = homeShow;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public static void main(String[] args) throws Exception {
        SQLExecutor.createTable(Shopcategory.class);
    }
}
