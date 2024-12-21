package com.ruoyi.server.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.common.utils.sign.Base64;
import com.ruoyi.server.async.AsyncManager;
import com.ruoyi.server.common.constant.MojangApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

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

    /**
     * 查询mojang api
     *
     * @param id
     */
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
}
