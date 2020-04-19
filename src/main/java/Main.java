import com.gavel.database.SQLExecutor;
import com.gavel.entity.GraingerCategory;
import com.gavel.grainger.GraingerProductLoad;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {


      List<GraingerCategory> graingerCategoryList =  SQLExecutor.executeQueryBeanList("select * from graingercategory where grade = '4'", GraingerCategory.class);


        for (GraingerCategory graingerCategory : graingerCategoryList) {
            try {
                System.out.println(graingerCategory);
                //GraingerProductLoad.load(graingerCategory.getCode());
            } catch (Exception e) {
                System.out.println(graingerCategory + " 失败: " + e.getMessage());
            }
        }

        System.out.println("Total: " + graingerCategoryList.size());

    }
}
