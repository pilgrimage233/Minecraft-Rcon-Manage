/**
 * 默认：
 */

package cc.endmc.server.sdk;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 百度IP定位API
 * 通过IP获取位置信息
 */
public class SearchHttpAK {

    public static String URL = "https://api.map.baidu.com/location/ip?";

    public static String AK = "1ilMd2g27l7TgJwRZubialb5Bb4H6DZT";

    public static void main(String[] args) throws Exception {

        SearchHttpAK snCal = new SearchHttpAK();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("ip", "111.206.214.37");
        params.put("coor", "bd09ll");
        params.put("ak", AK);

        snCal.requestGetAK(URL, params);
    }

    /**
     * 默认ak
     * 选择了ak，使用IP白名单校验：
     * 根据您选择的AK已为您生成调用代码
     * 检测到您当前的ak设置了IP白名单校验
     * 您的IP白名单中的IP非公网IP，请设置为公网IP，否则将请求失败
     * 请在IP地址为xxxxxxx的计算发起请求，否则将请求失败
     */
    public JSONObject requestGetAK(String strUrl, Map<String, String> param) throws Exception {

        if (strUrl == null || strUrl.length() <= 0 || param == null || param.size() <= 0) {
            return null;
        }

        StringBuilder queryString = new StringBuilder();
        queryString.append(strUrl);
        for (Map.Entry<?, ?> pair : param.entrySet()) {
            queryString.append(pair.getKey()).append("=");
            //    第一种方式使用的 jdk 自带的转码方式  第二种方式使用的 spring 的转码方法 两种均可
            //    queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8").replace("+", "%20") + "&");
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8")).append("&");
        }

        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        java.net.URL url = new URL(queryString.toString());
        // System.out.println(queryString.toString());
        URLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.connect();

        InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        isr.close();
        JSONObject jsonObject = JSONObject.parseObject(buffer.toString());
        System.out.println("IP定位: " + jsonObject);
        return jsonObject;
    }
}