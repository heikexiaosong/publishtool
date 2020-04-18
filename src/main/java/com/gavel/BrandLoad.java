package com.gavel;

import com.gavel.database.DataSourceHolder;
import com.gavel.suning.SuningClient;
import com.google.gson.Gson;
import com.suning.api.DefaultSuningClient;
import com.suning.api.SelectSuningResponse;
import com.suning.api.entity.item.BrandQueryRequest;
import com.suning.api.entity.item.BrandQueryResponse;
import com.suning.api.entity.item.CategoryQueryRequest;
import com.suning.api.entity.item.CategoryQueryResponse;
import com.suning.api.exception.SuningApiException;

import java.sql.*;

public class BrandLoad {

    public static void main(String[] args) throws Exception {

        Connection conn = DataSourceHolder.dataSource().getConnection();

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM CATEGORY");
        //遍历结果集

        int i = 0;
        while (rs.next()) {
            System.out.println(rs.getString("categoryCode") + "," + rs.getString("categoryName")+ "," + rs.getString("grade"));

            queryBrand(conn, rs.getString("categoryCode"));

            i += 1;
            System.out.println("Handle: " + i + "\n");

        }
        //释放资源
        stmt.close();
        //关闭连接
        conn.close();

    }

    private static void queryBrand(Connection conn, String categoryCode) throws Exception {



        final int PAGE_SIZE = 50;
        int pageNo = 1;

        BrandQueryRequest request = new BrandQueryRequest();
        request.setPageSize(PAGE_SIZE);
        request.setPageNo(pageNo);
        request.setCategoryCode(categoryCode);
//        request.setCategoryName("家装建材及五金");
        //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
        request.setCheckParam(true);

        DefaultSuningClient client = new DefaultSuningClient(SuningClient.SERVER_URL, SuningClient.APPKEY, SuningClient.APPSECRET, "json");
        try {
            BrandQueryResponse response = client.excute(request);
            System.out.println("CategoryQueryRequest :" + response.getBody());

            SelectSuningResponse.SnHead head = response.getSnhead();

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO BRAND VALUES(?, ?, ?)");

            while ( response.getSnbody()!=null && response.getSnbody().getBrandQueries()!=null && response.getSnbody().getBrandQueries().size() == 50 ) {
                for (BrandQueryResponse.BrandQuery brandQuery : response.getSnbody().getBrandQueries()) {
                    System.out.println(new Gson().toJson(brandQuery));

                    //新增
                    stmt.setObject(1, brandQuery.getBrandCode());
                    stmt.setObject(2, brandQuery.getBrandName());
                    stmt.setObject(3, categoryCode);

                    stmt.addBatch();
                }

                stmt.executeBatch();

                pageNo += 1;
                request.setPageNo(pageNo);
                response = client.excute(request);
            }

            if (response.getSnbody()!=null && response.getSnbody().getBrandQueries()!=null && response.getSnbody().getBrandQueries().size() > 0 ) {
                for (BrandQueryResponse.BrandQuery brandQuery : response.getSnbody().getBrandQueries()) {
                    System.out.println(new Gson().toJson(brandQuery));

                    //新增
                    stmt.setObject(1, brandQuery.getBrandCode());
                    stmt.setObject(2, brandQuery.getBrandName());
                    stmt.setObject(3, categoryCode);

                    stmt.addBatch();
                }

                stmt.executeBatch();
            }

            stmt.close();



        } catch (SuningApiException e) {
            e.printStackTrace();
        }


    }
}
