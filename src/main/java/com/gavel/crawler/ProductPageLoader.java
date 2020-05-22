package com.gavel.crawler;

import com.gavel.database.SQLExecutor;
import com.gavel.entity.HtmlCache;
import com.gavel.entity.Item;
import com.gavel.entity.SearchItem;
import com.gavel.grainger.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProductPageLoader {

    private static final ProductPageLoader loader = new ProductPageLoader();

    public static ProductPageLoader getInstance() {
        return loader;
    }


    private ProductPageLoader() {
    }


    public List<Item> loadPage(SearchItem searchItem) throws Exception {

        List<Item> items = new ArrayList<>();
        if ( searchItem==null ) {
            System.out.println("SearchItem is null");
            return items;
        }

        if ( "g".equalsIgnoreCase(searchItem.getType()) ) {
            Document doc = null;
            HtmlCache cache = null;
            cache = HtmlPageLoader.getInstance().loadHtmlPage(searchItem.getUrl(), true, true);
            if ( cache != null && cache.getHtml()!=null ) {
                doc = Jsoup.parse(cache.getHtml());
                if ( doc.title().equalsIgnoreCase("403 Forbidden") ) {
                    SQLExecutor.delete(cache);
                    cache = null;
                } else if ( doc.title().equalsIgnoreCase("Error") ) {
                    SQLExecutor.delete(cache);
                    cache = null;
                } else {
                    Elements fonts = doc.select("div.loadMoreBox div.rightTxt span font");
                    if (  fonts.size() == 2) {
                        int load = Integer.parseInt(fonts.get(0).text());
                        int total = Integer.parseInt(fonts.get(1).text());
                        if ( load < total ) {
                            SQLExecutor.delete(cache);
                            cache = null;
                        }
                    }
                }
            }

            if ( cache == null ) {
                cache = DriverHtmlLoader.getInstance().loadHtmlPage(searchItem.getUrl(), true);
            }
            if ( cache != null ) {
                if ( cache.getUpdatetime()==null ) {
                    cache.setUpdatetime(Calendar.getInstance().getTime());
                    SQLExecutor.insert(cache);
                }

                doc = Jsoup.parse(cache.getHtml());
                Elements skuList = doc.select("div.leftTable2 tr.trsku2");
                System.out.println("\tSKU: " + skuList.size());
                int i = 0;
                for (Element sku : skuList) {
                    String code = sku.child(0).attr("title");
                    Item item1 = SQLExecutor.executeQueryBean("select * from ITEM where code = ?", Item.class, code);
                    if ( item1!=null ) {
                        items.add(item1);
                        continue;
                    }


                    String url = "https://www.grainger.cn/u-" + code.trim() + ".html";
                    HtmlCache skuCache = null;

                    int tryTimes = 0;
                    while ( tryTimes < 5  ) {
                        skuCache = HtmlPageLoader.getInstance().loadSkuHtmlPage(url, true, tryTimes);
                        if ( skuCache != null && skuCache.getHtml()!=null ) {
                            doc = Jsoup.parse(skuCache.getHtml());
                            if ( doc.title().equalsIgnoreCase("403 Forbidden") ) {
                                SQLExecutor.delete(skuCache);
                                skuCache = null;
                            } else if ( doc.title().equalsIgnoreCase("Error") ) {
                                SQLExecutor.delete(skuCache);
                                skuCache = null;
                            }
                        }
                        if ( skuCache!=null ) {
                            break;
                        }
                        tryTimes++;

                        System.out.println("\t[Sku: " + code.trim() + "][Html]第 " + tryTimes + "次重试。。。" );
                    }

                    try {
                        item1 = parseSku(code, skuCache);
                        if ( item1!=null ) {
                            items.add(item1);
                            SQLExecutor.insert(item1);
                        }
                        if ( skuCache.getUpdatetime()==null ) {
                            skuCache.setUpdatetime(Calendar.getInstance().getTime());
                            SQLExecutor.insert(skuCache);
                        }
                    } catch (Exception e) {
                        System.out.println("\t" + sku.child(0).attr("title") + ": " + e.getMessage());
                        if ( skuCache!=null ) {
                            SQLExecutor.delete(skuCache);
                        }
                    }

                }

            }
        }


        return items;
    }

    public static Item parseSku(String code, HtmlCache cache) throws Exception {

        if ( code==null || code.trim().length()==0 ) {
            return null;
        }

        Item skuItem = new Item();
        skuItem.setCode(code);

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

        // 4级类目 + 产品组ID + ID
        Elements elements = doc.select("div.crumbs  a");
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

    public static void main(String[] args) throws Exception {

        SearchItem searchItem = new SearchItem();
        searchItem.setType("g");
        searchItem.setCode("368841");
        searchItem.setUrl("https://www.grainger.cn/g-368841.html");
        List<Item>  skus = ProductPageLoader.getInstance().loadPage(searchItem);
        System.out.println("SKU: " + skus.size());
        for (Item item : skus) {
            System.out.println(item);
        }
    }
}
