import com.gavel.database.SQLExecutor;
import com.gavel.entity.Itemparameter;

import java.util.List;

public class ItemparatersHandle {

    public static void main(String[] args) throws Exception {


       List<Itemparameter> items = SQLExecutor.executeQueryBeanList("select * from  ITEMPARAMETER where PARAM is null and PARTYPE in ('1', '2')", Itemparameter.class);

        System.out.println(items.size());


        for (int i = 0; i < items.size(); i++) {
            Itemparameter itemparameter = items.get(i);

            System.out.println(itemparameter.getParCode() + " - " + itemparameter.getParName() + " - " + itemparameter.getParaTemplateDesc());

            try {

                List<Itemparameter.ParOption> options = itemparameter.getParOption();
                if ( options!=null && options.size()>=1 ) {
                    itemparameter.setParam(options.get(0).getParOptionCode());
                    try {
                        SQLExecutor.update(itemparameter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();
                System.out.println(": " + e.getMessage());
            }


        }

    }

}
