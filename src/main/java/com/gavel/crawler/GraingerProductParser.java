package com.gavel.crawler;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.grainger.StringUtils;
import com.gavel.utils.ImageLoader;
import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GraingerProductParser {

    public static List<SkuItem> parse(HtmlCache product) throws Exception {

        List<SkuItem> skuItems = new ArrayList<>();

        if ( product==null || product.getHtml()==null || product.getHtml().trim().length()==0 ) {
            throw new Exception("Html页面内容为空");
        }

        Document doc = Jsoup.parse(product.getHtml());

        // 4级类目 + 产品组ID
        Elements elements = doc.select("div.crumbs  a");
        if ( elements.size() == 7 ) {
            skuItems.add(parseSku(StringUtils.getCode(elements.get(6).attr("href")), product));
            if ( product.getUpdatetime()==null ) {
                product.setUpdatetime(Calendar.getInstance().getTime());
                SQLExecutor.insert(product);
            }
            return skuItems;
        }

        // SKU 信息
        Elements skuList = doc.select("div.leftTable2 tr.trsku2");
        for (Element sku : skuList) {
            try {
                String code = sku.child(0).attr("title");
                String url = "https://www.grainger.cn/u-" + code.trim() + ".html";
                HtmlCache skuCache = HtmlPageLoader.getInstance().loadHtmlPage(url, true);
                skuItems.add(parseSku(code, skuCache));
                if ( skuCache.getUpdatetime()==null ) {
                    skuCache.setUpdatetime(Calendar.getInstance().getTime());
                    SQLExecutor.insert(skuCache);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }

        return skuItems;

    }


    private static SkuItem parseSku(String code, HtmlCache cache) throws Exception {

        if ( code==null || code.trim().length()==0 ) {
            return null;
        }

        SkuItem skuItem = new SkuItem(code);

        String url = cache.getUrl();
        if ( cache==null || cache.getHtml()==null || cache.getHtml().trim().length()==0 ) {
            throw new Exception("[" + url + "]获取Html页面异常");
        }


        Document doc = Jsoup.parse(cache.getHtml());

        Element err = doc.selectFirst("div.err-notice");
        if ( err!=null ) {
            throw new Exception("[" + url + "]页面未找到");
        }

        // 品牌 + 标题
        Element proDetailCon = doc.selectFirst("div.proDetailCon");
        if ( proDetailCon==null ) {
            throw new Exception("[" + url + "]Html内容有异常: " + doc.title());
        }

        // 4级类目 + 产品组ID
        Elements elements = doc.select("div.crumbs  a");
        Element c1 = elements.get(1);
        Element c2 = elements.get(2);
        Element c3 = elements.get(3);
        Element c4 = elements.get(4);
        Element c5 = elements.get(5);
//        System.out.print(StringUtils.getCode(c1.attr("href")) + " > ");
//        System.out.print(StringUtils.getCode(c2.attr("href")) + " > ");
//        System.out.print(StringUtils.getCode(c3.attr("href")) + " > ");
//        System.out.print(StringUtils.getCode(c4.attr("href")) + " > ");
//        System.out.println(StringUtils.getCode(c5.attr("href")));



        // 产品图片
        Elements imgs = doc.select("div.xiaotu > div.xtu > dl > dd > img");
        for (Element img : imgs) {
            String  src = img.attr("src");

            if ( src.equals("/Content/images/hp_np.png") ) {
                src = "https://www.grainger.cn/Content/images/hp_np.png";
            }

            if ( src!=null && src.startsWith("//") ) {
                src = "https:" + src;
            }

            src = src.replace("product_images_new/350/", "product_images_new/800/");
            //System.out.println(src);

            ImageLoader.loadIamge(src);
        }


        // 标题前 品牌
        String brand1 =  proDetailCon.selectFirst("h3 > span > a").html();
        // System.out.print("[" + StringUtils.getCode(proDetailCon.selectFirst("h3 > span > a").attr("href")) + "][" + brand1 + "]");
        // 标题
        // System.out.println(proDetailCon.selectFirst("h3 > a"));

        // 产品组
        Element g = proDetailCon.selectFirst(" > a");
        skuItem.setProduct(StringUtils.getCode(g.attr("href")));
        //  System.out.println(g.text());

        // 价格
        Element price = doc.selectFirst("b#bSalePrice");
        //  System.out.println(price);


        List<Pair<String, String>> attrs = new ArrayList<>();

        // 属性名
        Elements pxTRs = doc.select("div#rightTable1 tr.pxTR > td");

        // 属性值
        Elements attrTrs = doc.select("div#rightTable2 tr.trsku2");
        Element attrRecord = attrTrs.get(0);
        for (Element attrTr : attrTrs) {
            Element selected = attrTr.selectFirst("td > a > span.dweight");
            if ( selected==null ) {
                continue;
            }
            attrRecord = attrTr;
            break;
        }


        for (int i = 0; i < pxTRs.size(); i++) {
            Element element  = pxTRs.get(i);
            String tname = element.attr("title");
            if ( tname==null || tname.trim().length()==0 ) {
                tname = element.text();
            }

            Element attr = attrRecord.child(i);
            String tvalue = attr.attr("title");
            if ( tvalue==null || tvalue.trim().length()==0 ) {
                tvalue = attr.text();
            }


            attrs.add(new Pair(tname, tvalue));
            // System.out.println(tname + ": " + tvalue);
        }




        return skuItem;
    }

    public static void main(String[] args) throws Exception {
        String url = "https://www.grainger.cn/g-307598.html";


        HtmlCache htmlCache = HtmlPageLoader.getInstance().loadHtmlPage(url, true);

        if ( htmlCache==null || htmlCache.getHtml()==null || htmlCache.getHtml().trim().length()==0 ) {
            System.out.println("[URL: " + url + "]网页打开失败");
            return;
        }

        parse(htmlCache);
    }

}
