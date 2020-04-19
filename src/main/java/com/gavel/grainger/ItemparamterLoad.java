package com.gavel.grainger;

import com.gavel.HttpUtils;
import com.gavel.database.DataSourceHolder;
import com.gavel.database.SQLExecutor;
import com.gavel.entity.Category;
import com.gavel.entity.GraingerCategory;
import com.gavel.entity.Itemparameter;
import com.gavel.suning.SuningClient;
import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.SuningResponse;
import com.suning.api.entity.item.ItemparametersQueryRequest;
import com.suning.api.entity.item.ItemparametersQueryResponse;
import com.suning.api.entity.master.CityQueryRequest;
import com.suning.api.entity.master.CityQueryResponse;
import com.suning.api.exception.SuningApiException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class ItemparamterLoad {

    private  static  final DefaultSuningClient client = new DefaultSuningClient(SuningClient.SERVER_URL, SuningClient.APPKEY, SuningClient.APPSECRET, "json");

    public static void main(String[] args) throws Exception {

        List<Category> graingerCategoryList =  SQLExecutor.executeQueryBeanList("select * from category where grade = '4'", Category.class);


        Connection conn = DataSourceHolder.dataSource().getConnection();

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO itemparameter VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        for (Category category : graingerCategoryList) {
            System.out.println(category);

            List<Itemparameter>  itemparameters = loadItemparameters(category.getCategoryCode());

            for (Itemparameter itemparameter : itemparameters) {
                //新增
                stmt.setObject(1, itemparameter.getCategoryCode());
                stmt.setObject(2, itemparameter.getParaTemplateCode());
                stmt.setObject(3, itemparameter.getParaTemplateDesc());
                stmt.setObject(4, itemparameter.getParCode());
                stmt.setObject(5, itemparameter.getParName());
                stmt.setObject(6, itemparameter.getParType());
                stmt.setObject(7, itemparameter.getParUnit());
                stmt.setObject(8, itemparameter.getIsMust());
                stmt.setObject(9, itemparameter.getDataType());
                stmt.setObject(10, itemparameter.getOptions());

                stmt.addBatch();
            }



            stmt.executeBatch();

            stmt.clearBatch();

        }
        System.out.println("Total: " + graingerCategoryList.size());

        //释放资源
        stmt.close();
        //关闭连接
        conn.close();


    }

    private static List<Itemparameter> loadItemparameters(String categoryCode) {

        List<Itemparameter> itemparameters = new ArrayList<>();
        if ( categoryCode==null || categoryCode.trim().length()<=0 ) {
            return itemparameters;
        }

        ItemparametersQueryRequest request = new ItemparametersQueryRequest();
        request.setCategoryCode(categoryCode);


        int next = 1;
        int total = 1;

        while ( next <= total ) {
            request.setPageNo(next);
            request.setPageSize(20);
            //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
            request.setCheckParam(true);

            try {
                ItemparametersQueryResponse response = client.excute(request);
                System.out.println("NationQueryRequest :" + response.getBody());

                SuningResponse.SnError error = response.getSnerror();
                if ( error!=null ) {
                    System.out.println(error.getErrorCode() + " ==> " + error.getErrorMsg());
                } else {

                    next = response.getSnhead().getPageNo() + 1;
                    total = response.getSnhead().getPageTotal();

                    for (ItemparametersQueryResponse.ItemparametersQuery item : response.getSnbody().getItemparametersQueries()) {

                        Itemparameter itemparameter = new Itemparameter();

                        itemparameter.setCategoryCode(categoryCode.trim());
                        itemparameter.setParaTemplateCode(item.getParaTemplateCode());
                        itemparameter.setParaTemplateDesc(item.getParaTemplateDesc());
                        itemparameter.setParCode(item.getParCode());
                        itemparameter.setParName(item.getParName());
                        itemparameter.setParType(item.getParType());
                        itemparameter.setParUnit(item.getParUnit());
                        itemparameter.setDataType(item.getDataType());
                        itemparameter.setIsMust(item.getIsMust());
                        itemparameter.setOptions(new Gson().toJson(item.getParOption()));

                        System.out.println(new Gson().toJson(item));

                        itemparameters.add(itemparameter);
                    }
                }



            } catch (SuningApiException e) {
                e.printStackTrace();
            }

        }



        return itemparameters;
    }
}
