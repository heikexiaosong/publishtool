package com.gavel.shelves.suning;

import com.gavel.shelves.CatetoryBrand;
import com.gavel.shelves.CatetoryBrandSelector;

public class SuningCatetoryBrandSelector implements CatetoryBrandSelector {
    @Override
    public CatetoryBrand selectCatetoryBrand() {
        return new CatetoryBrand("285R", "mhc", "", "R9002847", "螺丝批、旋具头");
    }
}
