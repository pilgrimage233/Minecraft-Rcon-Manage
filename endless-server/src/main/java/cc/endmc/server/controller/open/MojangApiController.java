package cc.endmc.server.controller.open;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.common.utils.http.HttpUtils;
import cc.endmc.common.utils.sign.Base64;
import cc.endmc.server.annotation.SignVerify;
import cc.endmc.server.common.constant.MojangApi;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: MojangApiController <br>
 * Description:
 * date: 2024/6/30 上午1:43 <br>
 *
 * @author Administrator <br>
 * @since JDK 1.8
 */
@RestController
@RequestMapping("/mojang/")
public class MojangApiController extends BaseController {
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 查询mojang api
     *
     * @param id
     */
    @SignVerify
    @GetMapping("/user/{id}")
    public AjaxResult getMojangApi(@PathVariable String id) {

        if (StringUtils.isEmpty(id)) {
            return error("id is null");
        }
        Map<String, Object> map = new HashMap<>();

        final String s = HttpUtils.sendGet(MojangApi.UUID_API + id);

        final JSONObject uuidJson = JSONObject.parseObject(s);

        if (uuidJson == null) {
            return error("user not found");
        } else if (uuidJson.containsKey("errorMessage")) {
            return error("user not found");
        }
        final String uuid = uuidJson.getString("id");
        map.put("uuid", uuid);
        // 格式化uuid
        final String formatUuid = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
        map.put("uuid_format", formatUuid);

        map.put("name", uuidJson.getString("name"));
        map.put("legacy", uuidJson.getOrDefault("legacy", false));
        map.put("demo", uuidJson.getOrDefault("demo", false));

        // 取得玩家的外觀和披風
        final String skin = HttpUtils.sendGet(MojangApi.SKIN_API + uuid);
        final JSONObject properties = JSONObject.parseObject(skin);
        properties.getJSONArray("properties").forEach(o -> {
            JSONObject jsonObject = (JSONObject) o;
            if (jsonObject.getString("name").equals("textures")) {
                final String value = jsonObject.getString("value");
                final String decode = new String(Base64.decode(value));
                final JSONObject textures = JSONObject.parseObject(decode);
                final JSONObject texturesJson = textures.getJSONObject("textures");
                final JSONObject skinJson = texturesJson.getJSONObject("SKIN");
                final JSONObject capeJson = texturesJson.getJSONObject("CAPE");
                map.put("skin", skinJson);
                map.put("cape", capeJson);
            }
        });

        map.put("query_time", DateUtils.getTime());
        return success(map);
    }

    @GetMapping("/texture")
    public void getTexture(@RequestParam String url, HttpServletResponse response) {
        try {
            // 获取皮肤数据
            ResponseEntity<Resource> responseEntity = restTemplate.getForEntity(url, Resource.class);
            Resource resource = responseEntity.getBody();

            if (resource == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 设置响应头
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=86400"); // 缓存24小时

            // 复制图片数据到响应流
            try (InputStream inputStream = resource.getInputStream();
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
