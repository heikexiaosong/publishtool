import com.gavel.database.SQLExecutor;
import com.gavel.entity.jd.JDBrand;
import com.gavel.utils.MD5Utils;
import com.google.gson.Gson;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.domain.Prop;
import com.jd.open.api.sdk.domain.Sku;
import com.jd.open.api.sdk.domain.Ware;
import com.jd.open.api.sdk.request.list.PopVenderCenerVenderBrandQueryRequest;
import com.jd.open.api.sdk.request.ware.WareWriteAddRequest;
import com.jd.open.api.sdk.response.list.PopVenderCenerVenderBrandQueryResponse;
import com.jd.open.api.sdk.response.list.VenderBrandPubInfo;
import com.jd.open.api.sdk.response.ware.WareWriteAddResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        {


            Ware ware = new Ware();
            ware.setJdPrice(new BigDecimal(50));
            ware.setBrandId(548835L);
            ware.setCategoryId(14358751L);

            List<Sku> skuList = new ArrayList<Sku>();
//sku构建
            Sku sku = new Sku();
//            sku.setVenderId(venderId);
            sku.setJdPrice(new BigDecimal(50));
            sku.setStockNum(1L);
            Set<Prop> saleAttrs = new HashSet<>();
//颜色
            Prop prop = new Prop();
            prop.setAttrId("1000000041");
            String [] valueAlias = {"红色"};
            prop.setAttrValueAlias(valueAlias);
            prop.setIndex(1);
            String[] attrValues = {"1001415136"};
            prop.setAttrValues(attrValues);
//尺码
// Prop prop1 = new Prop();
// prop1.setAttrId("1000000046");
// String [] valueAlias1 = {"S"};
// prop1.setAttrValueAlias(valueAlias1);
// prop.setIndex(1);
// String[] attrValues1 = {"1001415126"};
// prop1.setAttrValues(attrValues1);
            saleAttrs.add(prop);
// saleAttrs.add(prop1);
            sku.setSaleAttrs(saleAttrs);
//sku构建
            Sku sku2 = new Sku();
    //        sku2.setVenderId(venderId);
            sku2.setJdPrice(new BigDecimal(50));
            sku2.setStockNum(1L);
            Set<Prop> saleAttrs2 = new HashSet<>();
//颜色
            Prop prop22 = new Prop();
            prop22.setAttrId("1000000041");
            String [] valueAlias22 = {"深红色"};
            prop22.setAttrValueAlias(valueAlias22);
            prop22.setIndex(2);
            String[] attrValues22 = {"1001415137"};
            prop22.setAttrValues(attrValues22);
//尺码
// Prop prop23 = new Prop();
// prop23.setAttrId("1000000046");
// String [] valueAlias23 = {"M"};
// prop23.setAttrValueAlias(valueAlias23);
// prop23.setIndex(2);
// String[] attrValues23 = {"1001415127"};
// prop23.setAttrValues(attrValues23);
            saleAttrs2.add(prop22);
// saleAttrs2.add(prop23);
            sku2.setSaleAttrs(saleAttrs2);

            skuList.add(sku);
            skuList.add(sku2);
            ware.setSkus(skuList);
            WareWriteAddRequest request =new WareWriteAddRequest();
            request.setWare(ware);
            request.setSkus(ware.getSkus());
            WareWriteAddResponse response=client.execute(request);


            System.out.println(new Gson().toJson(response));

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
