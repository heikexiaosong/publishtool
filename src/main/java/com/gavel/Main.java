package com.gavel;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.Itemparameter;
import com.gavel.utils.MD5Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception {


       List<Itemparameter> items = SQLExecutor.executeQueryBeanList("select * from ITEMPARAMETER where id is null ", Itemparameter.class);

       System.out.println(items.size());


       if ( items==null || items.size() ==0 ) {
           return;
       }


       Set<String> idSet = new HashSet<>();

       int i = 0;
        for (Itemparameter item : items) {

            item.setId( MD5Utils.md5Hex(item.getSupplierCode() + "_" + item.getCategoryCode().trim() + "_" + item.getParCode()));

            System.out.print("\r" + (i++));
            if ( idSet.add(item.getId()) ) {
               // SQLExecutor.delete(item);


                try {
                    SQLExecutor.update(item);
                } catch (Exception e) {

                }
            }
        }





    }

    private static String escape(String text) {
        String res = text;
        if ( text!=null && text.contains(",") ) {
            res = "\"" + text + "\"";
        }

        return res;
    }
}