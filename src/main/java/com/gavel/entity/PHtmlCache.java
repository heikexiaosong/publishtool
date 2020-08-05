package com.gavel.entity;

import com.gavel.annotation.FieldMeta;
import com.gavel.annotation.PartitionMeta;
import com.gavel.annotation.TableMeta;
import com.gavel.database.SQLExecutor;

@TableMeta(name = "HTMLCACHE", title = "html缓存")
@PartitionMeta
public class PHtmlCache extends HtmlCache {

    @FieldMeta(name = "ID", title = "ID", length = 32, primary = true)
    private String id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static void main(String[] args) throws Exception {
        SQLExecutor.createTable(PHtmlCache.class);
    }
}
