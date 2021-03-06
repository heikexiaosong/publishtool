package com.gavel.crawler;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Item;
import com.gavel.entity.PHtmlCache;
import com.gavel.entity.SearchItem;
import com.gavel.grainger.StringUtils;
import com.gavel.utils.MD5Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Calendar;

public class SkuPageLoader {

    private static final SkuPageLoader loader = new SkuPageLoader();

    public static SkuPageLoader getInstance() {
        return loader;
    }


    public Item loadPage(SearchItem searchItem) throws Exception {
        if ( searchItem==null ) {
            System.out.println("SearchItem is null");
            return null;
        }


        Item item = null;
        if ( "u".equalsIgnoreCase(searchItem.getType()) ) {

            Item exist = SQLExecutor.executeQueryBean("select * from ITEM  where CODE = ? ", Item.class, searchItem.getCode());
            if ( exist!=null ){
                return exist;
            }

            Document doc = null;

            String id = MD5Utils.md5Hex(searchItem.getUrl());
            String suffix =  id.substring(id.length()-1);

            HtmlCache cache = SQLExecutor.executeQueryBean("select * from htmlcache_"+ com.gavel.utils.StringUtils.trim(suffix) + "  where ID = ? limit 1 ", HtmlCache.class, id);
            if ( cache!=null ) {
                try {
                    item = parseSku(searchItem.getCode(), cache.getHtml());
                } catch (Exception e) {
                    SQLExecutor.execute("delete from htmlcache_"+ com.gavel.utils.StringUtils.trim(suffix) + "  where ID = ? ", id);
                    cache = null;
                }
            }


            if ( item == null ) {
                cache = DriverHtmlLoader.getInstance().loadHtmlPage(searchItem.getUrl());

                if ( cache != null ) {
                    try {
                        item = parseSku(searchItem.getCode(), cache.getHtml());

                        if ( cache.getUpdatetime()==null ) {
                            cache.setUpdatetime(Calendar.getInstance().getTime());

                            PHtmlCache _cache = new PHtmlCache();
                            _cache.setId(MD5Utils.md5Hex(cache.getUrl().trim()));
                            _cache.setUrl(cache.getUrl().trim());
                            _cache.setHtml(cache.getHtml());
                            _cache.setContentlen(cache.getContentlen());
                            _cache.setUpdatetime(Calendar.getInstance().getTime());
                            SQLExecutor.insert(_cache, _cache.getId().substring(_cache.getId().length()-1));
                        }

                        if ( item!=null ) {
                            SQLExecutor.insert(item);
                        }

                    } catch (Exception e) {

                    }
                }


            }
        }


        return item;
    }

    private static Item parseSku(String code, String html) throws Exception {

        if ( code==null || code.trim().length()==0 ) {
            return null;
        }

        Item skuItem = new Item();
        skuItem.setCode(code);

        String url = code;
        if ( html==null ||  html.trim().length()==0 ) {
            throw new Exception("[" + url + "]获取Html页面异常");
        }


        Document doc = Jsoup.parse(html);

        Element err = doc.selectFirst("div.err-notice");
        if ( err!=null ) {
            throw new Exception("[" + url + "]页面未找到");
        }

        // 品牌 + 标题
        Element proDetailCon = doc.selectFirst("div.proDetailCon");
        if ( proDetailCon==null ) {
            throw new Exception("[" + url + "]Html内容有异常: " + doc.title());
        }

        // 4级类目 + 产品组ID + ID
        Elements elements = doc.select("div.crumbs  a");
        if ( elements==null || elements.size() < 5 ) {
            throw new Exception("[" + url + "]Html内容有异常: " + doc.title());
        }

        Element c1 = elements.get(1);
        Element c2 = elements.get(2);
        Element c3 = elements.get(3);
        Element c4 = elements.get(4);

        StringBuilder catename = new StringBuilder();
        catename.append(c1.text()).append("|").append(c2.text()).append("|").append(c3.text()).append("|").append(c4.text());
        skuItem.setCategoryname(catename.toString());
        skuItem.setCategory(StringUtils.getCode(c4.attr("href")));


        // 产品组
        Element c5 = elements.get(5);
        skuItem.setProductcode(StringUtils.getCode(c5.attr("href")));

        // 产品
        Element c6 = elements.get(6);
        skuItem.setUrl("https://www.grainger.cn" + c6.attr("href"));


        // 标题
        Element title = proDetailCon.selectFirst("h3 > a");
        skuItem.setName(title.text());

        // 副标题
        Element subTitle = proDetailCon.select("h4 > a").last();
        skuItem.setSubname(subTitle.text());



        Element brand = proDetailCon.selectFirst("div font a");
        skuItem.setBrand(StringUtils.getCode(brand.attr("href")));
        skuItem.setBrandname(brand.text());
        /**
         * 订 货 号：5W8061
                * 品   牌：霍尼韦尔 Honeywell
         * 制造商型号： SHSL00202-42
                * 包装内件数：1双
                * 预计发货日： 停止销售
         *

         String code = fonts.get(0).text();
         String brand = fonts.get(1).text();
         String model = fonts.get(2).text();
         String number = fonts.get(3).text();
         String fahuori = fonts.get(4).text();
                */



        return skuItem;
    }
}
