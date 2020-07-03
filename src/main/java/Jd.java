import com.google.gson.Gson;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.domain.Image;
import com.jd.open.api.sdk.domain.Prop;
import com.jd.open.api.sdk.domain.Sku;
import com.jd.open.api.sdk.domain.Ware;
import com.jd.open.api.sdk.domain.list.CategoryAttrReadService.response.findAttrsByCategoryIdUnlimitCate.CategoryAttrUnlimit;
import com.jd.open.api.sdk.domain.list.CategoryAttrReadService.response.findAttrsByCategoryIdUnlimitCate.CategoryAttrValueUnlimit;
import com.jd.open.api.sdk.domain.list.CategoryReadService.response.findByPId.Category;
import com.jd.open.api.sdk.domain.seller.VenderShopCategoryJosService.response.findShopCategoriesByVenderId.VenderShopCategory;
import com.jd.open.api.sdk.request.list.CategoryReadFindAttrsByCategoryIdUnlimitCateRequest;
import com.jd.open.api.sdk.request.list.CategoryReadFindByPIdRequest;
import com.jd.open.api.sdk.request.list.CategoryReadFindValuesByAttrIdUnlimitRequest;
import com.jd.open.api.sdk.request.mall.NewWareBaseproductGetRequest;
import com.jd.open.api.sdk.request.seller.VenderShopcategoryFindShopCategoriesByVenderIdRequest;
import com.jd.open.api.sdk.request.ware.WareWriteAddRequest;
import com.jd.open.api.sdk.response.list.CategoryReadFindAttrsByCategoryIdUnlimitCateResponse;
import com.jd.open.api.sdk.response.list.CategoryReadFindByPIdResponse;
import com.jd.open.api.sdk.response.list.CategoryReadFindValuesByAttrIdUnlimitResponse;
import com.jd.open.api.sdk.response.mall.NewWareBaseproductGetResponse;
import com.jd.open.api.sdk.response.seller.VenderShopcategoryFindShopCategoriesByVenderIdResponse;
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
        String accessToken = "50a1b409ebd14e6b9c5999dfb7c75439lntb";
        JdClient client=new DefaultJdClient(serverUrl,accessToken,appKey,appSecret);

        long venid = 14621763407L;

        {

            // 取商家所有的店内分类

            VenderShopcategoryFindShopCategoriesByVenderIdRequest request=new VenderShopcategoryFindShopCategoriesByVenderIdRequest();
            VenderShopcategoryFindShopCategoriesByVenderIdResponse response=client.execute(request);

            for (VenderShopCategory venderShopCategory : response.getShopCategoryResult().getShopCategoryList()) {
                System.out.println(new Gson().toJson(venderShopCategory));
            }
            System.out.println("------------------ " + response.getShopCategoryResult().getShopCategoryList().size());


        }

        {
            //jingdong.category.read.findByPId ( 查找子类目列表 )
            CategoryReadFindByPIdRequest request=new CategoryReadFindByPIdRequest();
            request.setParentCid(14066L);
            request.setField("fid,id");
            CategoryReadFindByPIdResponse response=client.execute(request);


            for (Category category : response.getCategories()) {
                System.out.println(new Gson().toJson(category));
            }



            if ( 1==1 ) {
                return;
            }
        }



        {
            NewWareBaseproductGetRequest request=new NewWareBaseproductGetRequest();
            request.setIds("70656179398");
            request.setBasefields("jingdong,test");
            NewWareBaseproductGetResponse response=client.execute(request);
            System.out.println(new Gson().toJson(response));
        }

//        {
//
//
//
//
//        }
//
//        {
//
//            PopVenderCenerVenderBrandQueryRequest request=new PopVenderCenerVenderBrandQueryRequest();
//            //request.setName("test");
//            PopVenderCenerVenderBrandQueryResponse response=client.execute(request);
//            System.out.println(new Gson().toJson(response.getBrandList()));
//
//
//            String shopid= "10216927";
//            int i = 1;
//            for (VenderBrandPubInfo venderBrandPubInfo : response.getBrandList()) {
//                 System.out.println( (i++) + ". " + new Gson().toJson(venderBrandPubInfo));
//
//
//                JDBrand jdBrand = new Gson().fromJson(new Gson().toJson(venderBrandPubInfo), JDBrand.class);
//
//                jdBrand.setShopid(shopid);
//                jdBrand.setId(MD5Utils.md5Hex(jdBrand.getShopid() + "_" + jdBrand.getErpBrandId()));
//
//                SQLExecutor.insert(jdBrand);
//            }
//        }

        {
            CategoryReadFindAttrsByCategoryIdUnlimitCateRequest request1=new CategoryReadFindAttrsByCategoryIdUnlimitCateRequest();
            request1.setCid(14104L);
            //request1.setAttributeType(4);
            request1.setField("attrValueList,attrGroup");
            CategoryReadFindAttrsByCategoryIdUnlimitCateResponse response1=client.execute(request1);

            for (CategoryAttrUnlimit categoryAttrUnlimit : response1.getFindattrsbycategoryidunlimitcateResult()) {
                if (categoryAttrUnlimit.getIsRequired()) {
                    System.out.println("\t" + categoryAttrUnlimit.getInputType() + " --> " + new Gson().toJson(categoryAttrUnlimit));
                    System.out.println("\t\t" + new Gson().toJson(categoryAttrUnlimit.getAttrValueList()));
                }
            }
        }

        {
            Ware ware = new Ware();
            ware.setTitle("京东后台测试商品");

            ware.setCategoryId(14104L);

            List<Image> images = new ArrayList<>();
            images.add(new com.jd.open.api.sdk.domain.Image());

            ware.setImages(images);

            ware.setIntroduction("<span>京东后台测试商品</span>");
            ware.setMarketPrice(new BigDecimal(0.9));


            List<Sku> skus = new ArrayList<>();
            Sku sku = new Sku();
            skus.add(sku);
            sku.setJdPrice(new BigDecimal(0.8));


            Set<Prop> multiCateProps = new HashSet<>();

            //类型:131754],包装规格:],长度:],尺码:],颜色:],材质:],材质:],类别:],


            {
                Prop prop = new Prop();
                prop.setAttrId("165432");
                String [] valueAlias = {"JingSu"};
                prop.setAttrValueAlias(valueAlias);
                prop.setIndex(1);
                String[] attrValues = {"548835"};
                prop.setAttrValues(attrValues);
                multiCateProps.add(prop);
            }

            {
                Prop prop = new Prop();
                prop.setAttrId("165433");
                String [] valueAlias = {"通用劳保手套"};
                prop.setAttrValueAlias(valueAlias);
                prop.setIndex(1);
                String[] attrValues = {"699985"};
                prop.setAttrValues(attrValues);
                multiCateProps.add(prop);
            }

            {
                Prop prop = new Prop();
                prop.setAttrId("115987");
                String [] valueAlias = {"丁腈"};
                prop.setAttrValueAlias(valueAlias);
                prop.setIndex(1);
                String[] attrValues = {"699973"};
                prop.setAttrValues(attrValues);
                multiCateProps.add(prop);
            }
            {
                Prop prop = new Prop();
                prop.setAttrId("165434");
                String [] valueAlias = {"丁腈"};
                prop.setAttrValueAlias(valueAlias);
                prop.setIndex(1);
                String[] attrValues = {"699973"};
                prop.setAttrValues(attrValues);
                multiCateProps.add(prop);
            }
            {
                Prop prop = new Prop();
                prop.setAttrId("165435");
                String [] valueAlias = {"红色"};
                prop.setAttrValueAlias(valueAlias);
                prop.setIndex(1);
                String[] attrValues = {"1001415136"};
                prop.setAttrValues(attrValues);
                multiCateProps.add(prop);
            }


            {
                Prop prop = new Prop();
                prop.setAttrId("165436");
                String [] valueAlias = {"S"};
                prop.setAttrValueAlias(valueAlias);
                prop.setIndex(1);
                String[] attrValues = {"1001415126"};
                prop.setAttrValues(attrValues);
                multiCateProps.add(prop);
            }

            {
                Prop prop = new Prop();
                prop.setAttrId("165437");
                String [] valueAlias = {"M"};
                prop.setAttrValueAlias(valueAlias);
                prop.setIndex(1);
                String[] attrValues = {"1001415127"};
                prop.setAttrValues(attrValues);
                multiCateProps.add(prop);
            }

            {

                Prop prop = new Prop();
                prop.setAttrId("165438");
                String [] valueAlias = {"M"};
                prop.setAttrValueAlias(valueAlias);
                prop.setIndex(1);
                String[] attrValues = {"1001415127"};
                prop.setAttrValues(attrValues);
                multiCateProps.add(prop);


            }

            {

                Prop prop = new Prop();
                prop.setAttrId("131754");
                String [] valueAlias = {"通用劳保手套"};
                prop.setAttrValueAlias(valueAlias);
                prop.setIndex(1);
                String[] attrValues = {"699985"};
                prop.setAttrValues(attrValues);
                multiCateProps.add(prop);


            }





            sku.setMultiCateProps(multiCateProps);


            WareWriteAddRequest request =new WareWriteAddRequest();
            request.setWare(ware);
            request.setSkus(skus);

            System.out.println("\n\n");
            System.out.println(request.getAppJsonParams());
            WareWriteAddResponse response=client.execute(request);


            System.out.println("\n\n");
            System.out.println(new Gson().toJson(response));


            if ( 1==1 ) {
                return;
            }

        }

        {


            Ware ware = new Ware();
            ware.setTitle("京东后台测试商品");
            ware.setJdPrice(new BigDecimal(50));
            ware.setMarketPrice(new BigDecimal(55));
            ware.setOuterId("1");
            ware.setBrandId(548835L);
            ware.setCategoryId(14104L);

            List<Image> images = new ArrayList<>();
            com.jd.open.api.sdk.domain.Image image = new com.jd.open.api.sdk.domain.Image();

            images.add(image);

            ware.setImages(images);

            ware.setIntroduction("<h1>hello</h1>");
            ware.setMarketPrice(new BigDecimal(12.9));

            List<Sku> skuList = new ArrayList<Sku>();
            //sku构建
            Sku sku2 = new Sku();
            //sku2.setVenderId(venderId);
            sku2.setWareId(venid);
            sku2.setJdPrice(new BigDecimal(50));
            sku2.setStockNum(1L);

            Set<Prop> saleAttrs2 = new HashSet<>();


            CategoryReadFindAttrsByCategoryIdUnlimitCateRequest request1=new CategoryReadFindAttrsByCategoryIdUnlimitCateRequest();
            request1.setCid(14104L);
            //request1.setAttributeType(4);
            request1.setField("attrValueList,attrGroup");
            CategoryReadFindAttrsByCategoryIdUnlimitCateResponse response1=client.execute(request1);

            for (CategoryAttrUnlimit categoryAttrUnlimit : response1.getFindattrsbycategoryidunlimitcateResult()) {
                if (categoryAttrUnlimit.getIsRequired() )
                {
                    System.out.println("\t" + new Gson().toJson(categoryAttrUnlimit));
                    System.out.println("\t\t" + new Gson().toJson(categoryAttrUnlimit.getAttrValueList()));
                     Prop prop23 = new Prop();
                     prop23.setAttrId(String.valueOf(categoryAttrUnlimit.getId()));

                     if ( categoryAttrUnlimit.getInputType()==3 ) { // 3.可输入
//                         String [] valueAlias23 = {"M"};
//                         prop23.setAttrValueAlias(valueAlias23);
                         String[] attrValues23 = {"1001415127"};
                         prop23.setAttrValues(attrValues23);
                     } else { //  // 1..单选 2.多选

                         CategoryAttrValueUnlimit attr = categoryAttrUnlimit.getAttrValueList().get(0);

//                         String [] valueAlias23 = {attr.getName()};
//                         prop23.setAttrValueAlias(valueAlias23);
                         String[] attrValues23 = {String.valueOf(attr.getName())};
                         prop23.setAttrValues(attrValues23);
                     }

                     // 通用属性[类型:131754],包装规格:165438],长度:165437],尺码:165436],颜色:165435],材质:165434],材质:115987],类别:165433],品牌:165432],必填#



                    CategoryReadFindValuesByAttrIdUnlimitRequest request2=new CategoryReadFindValuesByAttrIdUnlimitRequest();
                    request2.setCategoryAttrId(categoryAttrUnlimit.getId());

                    CategoryReadFindValuesByAttrIdUnlimitResponse response2 = client.execute(request2);

                    for (com.jd.open.api.sdk.domain.list.CategoryAttrValueReadService.response.findValuesByAttrIdUnlimit.CategoryAttrValueUnlimit categoryAttrValueUnlimit : response2.getFindvaluesbyattridunlimitResult()) {
                        System.out.println("\t\t --> " + new Gson().toJson(categoryAttrValueUnlimit));
                    }



                    saleAttrs2.add(prop23);
                }
            }
            //sku2.setSaleAttrs(saleAttrs2);

            sku2.setMultiCateProps(saleAttrs2);

            skuList.add(sku2);
            ware.setSkus(skuList);
            WareWriteAddRequest request =new WareWriteAddRequest();
            request.setWare(ware);
            request.setSkus(ware.getSkus());

            System.out.println("\n\n");
            System.out.println(request.getAppJsonParams());
            WareWriteAddResponse response=client.execute(request);


            System.out.println("\n\n");
            System.out.println(new Gson().toJson(response.getMsg()));

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
