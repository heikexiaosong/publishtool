package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.TableMeta;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@TableMeta(name = "CUSTOMCATE", title = "")
public class CustomCate {

    //商品编码	商品名称	供价	 _	类目	品牌

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private final StringProperty id = new SimpleStringProperty();

    @FieldMeta(name = "NUM", title = "ROW")
    private int num;

    @FieldMeta(name = "CTAE1", title = "类目1", length = 128)
    private final StringProperty cate1 = new SimpleStringProperty();

    @FieldMeta(name = "CTAENAME1", title = "类目1", length = 128)
    private final StringProperty catename1 = new SimpleStringProperty();

    @FieldMeta(name = "CTAE2", title = "类目2", length = 128)
    private final StringProperty cate2 = new SimpleStringProperty();

    @FieldMeta(name = "CTAENAME2", title = "类目2", length = 128)
    private final StringProperty catename2 = new SimpleStringProperty();

    @FieldMeta(name = "CTAE3", title = "类目3", length = 128)
    private final StringProperty cate3 = new SimpleStringProperty();

    @FieldMeta(name = "CTAENAME3", title = "类目3", length = 128)
    private final StringProperty catename3 = new SimpleStringProperty();


    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getCate1() {
        return cate1.get();
    }

    public StringProperty cate1Property() {
        return cate1;
    }

    public void setCate1(String cate1) {
        this.cate1.set(cate1);
    }

    public String getCatename1() {
        return catename1.get();
    }

    public StringProperty catename1Property() {
        return catename1;
    }

    public void setCatename1(String catename1) {
        this.catename1.set(catename1);
    }

    public String getCate2() {
        return cate2.get();
    }

    public StringProperty cate2Property() {
        return cate2;
    }

    public void setCate2(String cate2) {
        this.cate2.set(cate2);
    }

    public String getCatename2() {
        return catename2.get();
    }

    public StringProperty catename2Property() {
        return catename2;
    }

    public void setCatename2(String catename2) {
        this.catename2.set(catename2);
    }

    public String getCate3() {
        return cate3.get();
    }

    public StringProperty cate3Property() {
        return cate3;
    }

    public void setCate3(String cate3) {
        this.cate3.set(cate3);
    }

    public String getCatename3() {
        return catename3.get();
    }

    public StringProperty catename3Property() {
        return catename3;
    }

    public void setCatename3(String catename3) {
        this.catename3.set(catename3);
    }
}
