import com.gavel.HttpUtils;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.*;
import com.gavel.grainger.GraingerProductLoad;
import com.gavel.utils.ImageLoader;
import com.gavel.utils.MD5Utils;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    private static final String PIC_DIR = "D:\\images";

    public static void main(String[] args) throws Exception {

        List<Brand> brands = SQLExecutor.executeQueryBeanList("select * from brand", Brand.class);


       List<BrandMapping> brandMappings = SQLExecutor.executeQueryBeanList("select * from brandmapping", BrandMapping.class);

        for (BrandMapping brandMapping : brandMappings) {
            Set<String> brandSet = new HashSet<>();
            System.out.println(brandMapping.getGraingercode() + ": " + brandMapping.getName1() + "/" + brandMapping.getName2());

            for (Brand brand: brands) {
                if ( brand.getName().equals(brandMapping.getName1()) || brand.getName().equals(brandMapping.getName2())
                        || brand.getName().contains(brandMapping.getName1()) || brand.getName().contains(brandMapping.getName2())   ){
                    if ( brandSet.add(brand.getCode()) ) {
                        System.out.println("\t" + brand.getCode() + ": " + brand.getName() + " - " + brand.getCategoryCode());
                    }

                    brandMapping.setBrand(brand.getCode());
                }
            }
            if ( brandMapping.getBrand()!=null ) {
                SQLExecutor.update(brandMapping);
            }


            System.out.println("");
        }

        System.out.println("Total: " + brandMappings.size());
    }
}
