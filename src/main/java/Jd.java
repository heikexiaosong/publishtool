import com.gavel.database.SQLExecutor;
import com.gavel.entity.jd.JDBrand;
import com.gavel.utils.MD5Utils;
import com.google.gson.Gson;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.request.list.PopVenderCenerVenderBrandQueryRequest;
import com.jd.open.api.sdk.response.list.PopVenderCenerVenderBrandQueryResponse;
import com.jd.open.api.sdk.response.list.VenderBrandPubInfo;

public class Jd {


    public static void main(String[] args) throws Exception {


        String serverUrl = "https://api.jd.com/routerjson";
        String appKey = "819A568C76FEBC54945536AF7B7BBCBE";
        String appSecret = "374c67fe9ff2410f876686282de33dac";
        String accessToken = "bf84301e0479422588d33c76a3772250mwuw";
        JdClient client=new DefaultJdClient(serverUrl,accessToken,appKey,appSecret);

//        VenderShopcategoryFindShopCategoriesByVenderIdRequest request=new VenderShopcategoryFindShopCategoriesByVenderIdRequest();
//        VenderShopcategoryFindShopCategoriesByVenderIdResponse response=client.execute(request);

        {

            PopVenderCenerVenderBrandQueryRequest request=new PopVenderCenerVenderBrandQueryRequest();
            //request.setName("test");
            PopVenderCenerVenderBrandQueryResponse response=client.execute(request);
            System.out.println(new Gson().toJson(response.getBrandList()));


            String shopid= "10216927";
            int i = 1;
            for (VenderBrandPubInfo venderBrandPubInfo : response.getBrandList()) {
                 System.out.println( (i++) + ". " + new Gson().toJson(venderBrandPubInfo));


                JDBrand jdBrand = new Gson().fromJson(new Gson().toJson(venderBrandPubInfo), JDBrand.class);

                jdBrand.setShopid(shopid);
                jdBrand.setId(MD5Utils.md5Hex(jdBrand.getShopid() + "_" + jdBrand.getErpBrandId()));

                SQLExecutor.insert(jdBrand);
            }
        }


//        {
//            VenderShopcategoryFindShopCategoriesByVenderIdRequest request=new VenderShopcategoryFindShopCategoriesByVenderIdRequest();
//            VenderShopcategoryFindShopCategoriesByVenderIdResponse response=client.execute(request);
//
//            int i = 1;
//            for (VenderShopCategory venderShopCategory : response.getShopCategoryResult().getShopCategoryList()) {
//                System.out.println( (i++) + ". " + new Gson().toJson(venderShopCategory));
//
//
//                Shopcategory shopcategory = new Gson().fromJson(new Gson().toJson(venderShopCategory), Shopcategory.class);
//
//                shopcategory.setId(MD5Utils.md5Hex(shopcategory.getVenderId() + "_" + shopcategory.getCid()));
//
//                SQLExecutor.insert(shopcategory);
//
//            }
//
//        }

//        SellerVenderInfoGetRequest request=new SellerVenderInfoGetRequest();
//        request.setExtJsonParam("test");
//        SellerVenderInfoGetResponse response=client.execute(request);

//        PopOrderSearchRequest request=new PopOrderSearchRequest();
//        request.setStartDate("2020-01-01 10:00:00");
//        request.setEndDate("2020-11-22 12:00:00");
//        request.setOrderState("TRADE_CANCELED");
//        request.setOptionalFields("itemInfoList,orderId,isShipmenttype,scDT,idSopShipmenttype,orderStartTime");
//        request.setPage("1");
//        request.setPageSize("20");
//        request.setSortType(1);
//        request.setDateType(0);
//        PopOrderSearchResponse response=client.execute(request);








    }
}
