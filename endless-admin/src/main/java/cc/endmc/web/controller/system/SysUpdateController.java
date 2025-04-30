package cc.endmc.web.controller.system;

import cc.endmc.common.core.domain.AjaxResult;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/update")
public class SysUpdateController {
    private static final Logger log = LoggerFactory.getLogger(SysUpdateController.class);
    private static final String GITHUB_API_URL = "https://api.github.com/repos/pilgrimage233/Minecraft-Rcon-Manage/releases/latest";

    @Value("${ruoyi.version}")
    private String currentVersion;

    @GetMapping("/check")
    public AjaxResult checkUpdate() {
        try {
            String response = HttpUtil.createGet(GITHUB_API_URL)
                    .header("Accept", "application/vnd.github.v3+json")
                    .execute()
                    .body();

            JSONObject json = JSONUtil.parseObj(response);

            String latestVersion = json.getStr("tag_name");
            String releaseNotes = json.getStr("body");
            String downloadUrl = json.getStr("html_url");

            boolean hasUpdate = !currentVersion.equals(latestVersion.replace("v", ""));

            return AjaxResult.success()
                    .put("currentVersion", currentVersion)
                    .put("latestVersion", latestVersion.replace("v", ""))
                    .put("hasUpdate", hasUpdate)
                    .put("releaseNotes", releaseNotes)
                    .put("downloadUrl", downloadUrl);

        } catch (Exception e) {
            log.error("Failed to check for updates", e);
            return AjaxResult.error("Failed to check for updates: " + e.getMessage());
        }
    }
}
