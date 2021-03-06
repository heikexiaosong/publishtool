import com.gavel.database.SQLExecutor;
import com.gavel.entity.Itemparameter;
import com.gavel.utils.MD5Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception {


       List<Itemparameter> items = SQLExecutor.executeQueryBeanList("select * from ITEMPARAMETER ", Itemparameter.class);

       System.out.println(items.size());


       if ( items==null || items.size() ==0 ) {
           return;
       }


       Set<String> idSet = new HashSet<>();

        for (Itemparameter item : items) {

            item.setId( MD5Utils.md5Hex(item.getSupplierCode() + "_" + item.getCategoryCode().trim() + "_" + item.getParCode()));

            if ( idSet.add(item.getId()) ) {
                SQLExecutor.delete(item);

                SQLExecutor.insert(item);
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
