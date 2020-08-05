package zkh360;

import com.gavel.crawler.HtmlPageLoader;
import com.gavel.entity.HtmlCache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Zkh360SkuParser {


    public static void main(String[] args) throws Exception {


        HtmlCache htmlCache = HtmlPageLoader.getInstance().loadHtmlPage("https://web.zkh360.com/view/home/new_zkh_product.html?proSkuNo=AM1952&proSkuId=5525217", true);
        //SQLExecutor.insert(htmlCache);



        parse(htmlCache.getHtml());

    }

    private static void parse(String html) {

        Document doc = Jsoup.parse(html);

        // 类目
        Elements crumbs = doc.select("ul#BreadcrumbId li.crumbs > a.crumbsA ");
        for (Element crumb : crumbs) {
            System.out.println(crumb.text());
        }

        // 主图
        Elements image_list = doc.select("ul#image_list img");
        for (Element image : image_list) {
            System.out.println(image.attr("src"));
        }


        Element product_detail = doc.selectFirst("div#product_detail");

        // 标题
        Element proview_name = product_detail.selectFirst("div.proview_name");
        System.out.println("Title: " + proview_name.text());

        // 价格
        Element price = doc.selectFirst("input#product-price");
        System.out.println("price: " + price.attr("value"));

        // 属性
        Elements list = product_detail.select("ul.product_info_list span");
        for (Element attr : list) {
            if ( attr.childrenSize() > 1 ) {
                continue;
            }
            System.out.println(attr.text());
        }

    }
}
