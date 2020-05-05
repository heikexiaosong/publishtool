package com.gavel.shelves.suning;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.Itemparameter;
import com.gavel.shelves.ParameterLoader;

import java.util.ArrayList;
import java.util.List;

public class SuningParameterLoader implements ParameterLoader {

    @Override
    public List<Parameter> loadParameters(String categoryCode) {

        List<Parameter> parameters = new ArrayList<>();
        try {
            List<Itemparameter> itemparameters = SQLExecutor.executeQueryBeanList("select  * from itemparameter where PARATEMPLATECODE <> 'common' and CATEGORYCODE = ?", Itemparameter.class, categoryCode);
            for (Itemparameter itemparameter : itemparameters) {
                parameters.add(new Parameter(itemparameter.getParCode(), itemparameter.getParam()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parameters;
    }

    @Override
    public List<Parameter> loadCommonParameters(String categoryCode) {
        List<Parameter> parameters = new ArrayList<>();
        try {
            List<Itemparameter> itemparameters = SQLExecutor.executeQueryBeanList("select  * from itemparameter where PARATEMPLATECODE = 'common' and CATEGORYCODE = ?", Itemparameter.class, categoryCode);
            for (Itemparameter itemparameter : itemparameters) {
                parameters.add(new Parameter(itemparameter.getParCode(), itemparameter.getParam()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parameters;
    }

}
