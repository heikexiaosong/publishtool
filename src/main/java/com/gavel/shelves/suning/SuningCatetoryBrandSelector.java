package com.gavel.shelves.suning;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.BrandMapping;
import com.gavel.entity.CategoryMapping;
import com.gavel.shelves.CatetoryBrand;
import com.gavel.shelves.CatetoryBrandSelector;

public class SuningCatetoryBrandSelector implements CatetoryBrandSelector {

    @Override
    public CatetoryBrand selectCatetoryBrand(String _category, String _brand) {

        String brandCode = null;
        String brandZh = null;
        String brandEn = null;
        String categoryCode = null;
        String category = null;

        try {
            CategoryMapping categoryMapping = SQLExecutor.executeQueryBean("select * from CATEGORYMAPPING where  code = ? ", CategoryMapping.class, _category);
            if ( categoryMapping!=null ) {
                categoryCode = categoryMapping.getCategoryCode();
                category = categoryMapping.getCategoryName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BrandMapping brandMapping = SQLExecutor.executeQueryBean("select * from BRANDMAPPING where  graingercode = ? ", BrandMapping.class, _brand);
            if ( brandMapping!=null ) {
                brandCode = brandMapping.getBrand();
                brandZh = brandMapping.getBrandname();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CatetoryBrand(brandCode, brandZh, brandEn, categoryCode, category);
    }
}
