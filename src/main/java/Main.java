import com.gavel.BrandLoad;
import com.gavel.database.DataSourceHolder;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        {
        Pattern pattern = Pattern.compile("/([a-zA-Z]-)?(\\d*).html", Pattern.CASE_INSENSITIVE);

        String str = "/c-207146.html";

        System.out.println(str);

        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            System.out.println(matcher.group(2));
        }


    }

        {

            Pattern pattern = Pattern.compile("([^（]*).*", Pattern.CASE_INSENSITIVE);

            String str= "调心球轴承（1524）";

            System.out.println(str);

            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                System.out.println(matcher.group(1));
            }

        }

        QueryRunner runner = new QueryRunner(DataSourceHolder.getInstance().dataSource());


    }
}
