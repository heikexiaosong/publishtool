package com.gavel.shelves;

import java.util.List;

public interface ParameterLoader {


    List<Parameter> loadParameters(String categoryCode);

    List<Parameter> loadCommonParameters(String categoryCode);



    public static class Parameter {
        private final String code;
        private final String value;

        public Parameter(String code, String value) {
            this.code = code;
            this.value = value;
        }

        public String code() {
            return code;
        }

        public String value() {
            return value;
        }
    }

}
