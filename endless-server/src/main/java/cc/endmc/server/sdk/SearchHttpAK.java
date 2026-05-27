/**
 * 默认：
 */

package cc.endmc.server.sdk;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 百度IP定位API
 * 通过IP获取位置信息
 */
@Slf4j
public class SearchHttpAK {

    public static String URL = "https://api.map.baidu.com/location/ip?";

    public static String AK = "";

    public JSONObject requestGetAK(String strUrl, Map<String, String> param) throws Exception {

        if (strUrl == null || strUrl.isEmpty() || param == null || param.isEmpty()) {
            return null;
        }

        StringBuilder queryString = new StringBuilder(strUrl);
        for (Map.Entry<?, ?> pair : param.entrySet()) {
            queryString.append(pair.getKey()).append("=");
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8")).append("&");
        }

        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        URL url = new URL(queryString.toString());
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        try {
            httpConnection.connect();

            StringBuilder buffer = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(httpConnection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
            }

            JSONObject jsonObject = JSONObject.parseObject(buffer.toString());
            log.debug("IP定位: {}", jsonObject);
            return jsonObject;
        } finally {
            httpConnection.disconnect();
        }
    }
}