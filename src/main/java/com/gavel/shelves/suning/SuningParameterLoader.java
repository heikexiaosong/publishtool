package com.gavel.shelves.suning;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.Itemparameter;
import com.gavel.shelves.ParameterLoader;
import com.gavel.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SuningParameterLoader implements ParameterLoader {

    private final Map<String, String> attrs;


    public SuningParameterLoader(Map<String, String> attrs) {
        this.attrs = attrs;
    }

    @Override
    public List<Parameter> loadParameters(String categoryCode) {

        List<Parameter> parameters = new ArrayList<>();
        try {
            List<Itemparameter> itemparameters = SQLExecutor.executeQueryBeanList("select  * from itemparameter where PARATEMPLATECODE <> 'common' and CATEGORYCODE = ?", Itemparameter.class, categoryCode);
            for (Itemparameter itemparameter : itemparameters) {
                String value = itemparameter.getParam();
                if ( attrs!=null && attrs.containsKey(itemparameter.getParName()) ) {
                    String actul = attrs.get(itemparameter.getParName()).trim();
                    System.out.println(itemparameter.getParName() + " => " + actul);
                    switch ( itemparameter.getParType() ) {
                        case "1":
                        case "2":
                            for (Itemparameter.ParOption option : itemparameter.getParOption()) {
                                if ( actul.equalsIgnoreCase(option.getParOptionDesc().trim())  ) {
                                    value = option.getParOptionCode();
                                    if (StringUtils.isBlank(option.getParOptionCode())) {
                                        value = option.getParOptionDesc();
                                    }
                                }
                            }
                            break;

                        case "3":
                            value = actul;
                            break;
                    }
                    System.out.println(itemparameter.getParName() + " => " + value);
                }
                parameters.add(new Parameter(itemparameter.getParCode(), value));
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
