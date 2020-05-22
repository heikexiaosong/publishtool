package com.gavel.shelves.suning;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.CateBrandMapping;
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
            CateBrandMapping categoryMapping = SQLExecutor.executeQueryBean("select * from BRAND_CATE_MAPPING where BRANDCODE = ? and  CATECODE = ? ", CateBrandMapping.class, _brand, _category);
            if ( categoryMapping!=null ) {
                categoryCode = categoryMapping.getCategorycode();
                category = categoryMapping.getCategoryname();
                brandCode = categoryMapping.getSbrandcode();
                brandZh = categoryMapping.getSbrandname();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CatetoryBrand(brandCode, brandZh, brandEn, categoryCode, category);
    }
}
