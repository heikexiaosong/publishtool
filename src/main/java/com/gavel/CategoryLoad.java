package com.gavel;

import com.gavel.config.APPConfig;
import com.gavel.database.DataSourceHolder;
import com.google.gson.Gson;
import com.suning.api.entity.item.CategoryQueryRequest;
import com.suning.api.entity.item.CategoryQueryResponse;
import com.suning.api.exception.SuningApiException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CategoryLoad {

    public static void main(String[] args) throws Exception {


        Connection conn = DataSourceHolder.dataSource().getConnection();

        final int PAGE_SIZE = 50;
        int pageNo = 1;

        CategoryQueryRequest request = new CategoryQueryRequest();
        request.setPageSize(PAGE_SIZE);
        request.setPageNo(pageNo);
//        request.setCategoryName("家装建材及五金");
        //api入参校验逻辑开关，当测试稳定之后建议设置为 false 或者删除该行
        request.setCheckParam(true);

        try {
            CategoryQueryResponse response = APPConfig.getInstance().client().excute(request);
            System.out.println("CategoryQueryRequest :" + response.getBody());

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO CATEGORY VALUES(?, ?, ?, ?, ?)");

            while ( response.getSnbody()!=null && response.getSnbody().getCategoryQueries()!=null && response.getSnbody().getCategoryQueries().size() == 50 ) {
                for (CategoryQueryResponse.CategoryQuery categoryQuery : response.getSnbody().getCategoryQueries()) {
                    System.out.println(new Gson().toJson(categoryQuery));

                    //新增

                    stmt.setObject(1, categoryQuery.getCategoryCode());
                    stmt.setObject(2, categoryQuery.getCategoryName());
                    stmt.setObject(3, categoryQuery.getGrade());
                    stmt.setObject(4, categoryQuery.getIsBottom());
                    stmt.setObject(5, categoryQuery.getDescPath());

                    stmt.addBatch();
                }

                stmt.executeBatch();

                pageNo += 1;
                request.setPageNo(pageNo);
                response = APPConfig.getInstance().client().excute(request);
            }

            if ( response.getSnbody()!=null && response.getSnbody().getCategoryQueries()!=null && response.getSnbody().getCategoryQueries().size() > 0 ) {
                for (CategoryQueryResponse.CategoryQuery categoryQuery : response.getSnbody().getCategoryQueries()) {
                    System.out.println(new Gson().toJson(categoryQuery));

                    //新增

                    stmt.setObject(1, categoryQuery.getCategoryCode());
                    stmt.setObject(2, categoryQuery.getCategoryName());
                    stmt.setObject(3, categoryQuery.getGrade());
                    stmt.setObject(4, categoryQuery.getIsBottom());
                    stmt.setObject(5, categoryQuery.getDescPath());

                    stmt.addBatch();
                }

                stmt.executeBatch();
            }

            stmt.close();



        } catch (SuningApiException e) {
            e.printStackTrace();
        }


        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM CATEGORY");
        //遍历结果集
        while (rs.next()) {
            System.out.println(rs.getString("categoryCode") + "," + rs.getString("categoryName")+ "," + rs.getString("grade"));
        }
        //释放资源
        stmt.close();
        //关闭连接
        conn.close();

    }
}
