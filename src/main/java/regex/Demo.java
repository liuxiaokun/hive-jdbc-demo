package regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Demo {

    public static void main(String[] args) {

        String patternString = "/(.+)/(.+)/properties/report";
        String content = "/prd_id/device_id/properties/report";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(content);
        boolean b = matcher.find();
        System.out.println(b);
        String productKey = matcher.group(1);
        System.out.println("productKey:" + productKey);
        String deviceKey = matcher.group(2);
        System.out.println("deviceKey:" + deviceKey);
    }
}
