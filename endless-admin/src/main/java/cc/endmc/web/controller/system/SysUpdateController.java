package cc.endmc.web.controller.system;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/system/update")
public class SysUpdateController extends BaseController {

    // GitHub API 镜像源列表（按优先级排序）
    private static final String[] API_MIRRORS = {
            "https://api.github.com/repos/pilgrimage233/Minecraft-Rcon-Manage/releases/latest"  // 官方 API
    };

    // GitHub 下载镜像源列表（按优先级排序）
    private static final String[] DOWNLOAD_MIRRORS = {
            "",  // 原始 GitHub 地址
            "https://gh-proxy.com/",
            "https://gh.ddlc.top/"
    };

    @Value("${endless.version}")
    private String currentVersion;

    @GetMapping("/check")
    public AjaxResult checkUpdate() {
        JSONObject json = null;
        Exception lastException = null;

        // 尝试从多个 API 镜像源获取版本信息
        for (String apiUrl : API_MIRRORS) {
            try {
                log.info("尝试从以下地址获取版本信息: {}", apiUrl);

                String response = HttpUtil.createGet(apiUrl)
                        .header("Accept", "application/vnd.github.v3+json")
                        .timeout(10000)
                        .execute()
                        .body();

                json = JSONUtil.parseObj(response);
                log.info("成功从 {} 获取版本信息", apiUrl);
                break; // 成功获取，跳出循环

            } catch (Exception e) {
                log.warn("从 {} 获取失败: {}", apiUrl, e.getMessage());
                lastException = e;
                // 继续尝试下一个镜像源
            }
        }

        // 如果所有镜像源都失败
        if (json == null) {
            log.error("从所有 API 镜像源检查更新失败", lastException);
            return error("无法连接到更新服务器，请检查网络连接或稍后重试");
        }

        try {
            String latestVersion = json.getStr("tag_name");
            String releaseNotes = json.getStr("body");
            String downloadUrl = json.getStr("html_url");

            // 在 assets 中查找 JAR 文件
            String jarDownloadUrl = null;
            JSONArray assets = json.getJSONArray("assets");
            if (assets != null) {
                for (int i = 0; i < assets.size(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    String name = asset.getStr("name");
                    if (name != null && name.endsWith(".jar")) {
                        jarDownloadUrl = asset.getStr("browser_download_url");
                        break;
                    }
                }
            }

            boolean hasUpdate = !currentVersion.equals(latestVersion.replace("v", ""));

            return success()
                    .put("currentVersion", currentVersion)
                    .put("latestVersion", latestVersion.replace("v", ""))
                    .put("hasUpdate", hasUpdate)
                    .put("releaseNotes", releaseNotes)
                    .put("downloadUrl", downloadUrl)
                    .put("jarDownloadUrl", jarDownloadUrl);

        } catch (Exception e) {
            log.error("解析版本信息失败", e);
            return error("解析版本信息失败: " + e.getMessage());
        }
    }

    @PostMapping("/download")
    public AjaxResult downloadUpdate() {
        JSONObject json = null;
        Exception lastException = null;

        // 尝试从多个 API 镜像源获取版本信息
        for (String apiUrl : API_MIRRORS) {
            try {
                log.info("尝试从以下地址获取版本信息: {}", apiUrl);

                String response = HttpUtil.createGet(apiUrl)
                        .header("Accept", "application/vnd.github.v3+json")
                        .timeout(10000)
                        .execute()
                        .body();

                json = JSONUtil.parseObj(response);
                log.info("成功从 {} 获取版本信息", apiUrl);
                break;

            } catch (Exception e) {
                log.warn("从 {} 获取失败: {}", apiUrl, e.getMessage());
                lastException = e;
            }
        }

        if (json == null) {
            log.error("从所有 API 镜像源获取版本信息失败", lastException);
            return error("无法连接到更新服务器，请检查网络连接");
        }

        try {
            // 在 assets 中查找 JAR 文件
            String jarDownloadUrl = null;
            String jarFileName = null;
            JSONArray assets = json.getJSONArray("assets");
            if (assets != null) {
                for (int i = 0; i < assets.size(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    String name = asset.getStr("name");
                    if (name != null && name.endsWith(".jar")) {
                        jarDownloadUrl = asset.getStr("browser_download_url");
                        jarFileName = name;
                        break;
                    }
                }
            }

            if (jarDownloadUrl == null) {
                return error("在最新版本中未找到 JAR 文件");
            }

            // 查找当前运行的 JAR
            String currentJarPath = getCurrentJarPath();
            if (currentJarPath == null) {
                return error("无法定位当前运行的 JAR 文件");
            }

            log.info("当前 JAR 路径: {}", currentJarPath);
            log.info("原始下载地址: {}", jarDownloadUrl);

            final String finalJarDownloadUrl = jarDownloadUrl;
            final String finalJarFileName = jarFileName;

            // 异步下载（不阻塞响应）
            CompletableFuture.runAsync(() -> {
                try {
                    // 下载新 JAR 到临时目录
                    File tempDir = new File(System.getProperty("java.io.tmpdir"));
                    File downloadedJar = new File(tempDir, finalJarFileName);

                    // 尝试从镜像源下载
                    boolean downloadSuccess = downloadWithMirrors(finalJarDownloadUrl, downloadedJar);

                    if (!downloadSuccess) {
                        log.error("从所有镜像源下载失败");
                        return;
                    }

                    log.info("新 JAR 已下载到: {}", downloadedJar.getAbsolutePath());

                    // 备份当前 JAR
                    File currentJar = new File(currentJarPath);
                    File backupJar = new File(currentJar.getParent(), currentJar.getName() + ".backup");
                    Files.copy(currentJar.toPath(), backupJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    log.info("当前 JAR 已备份到: {}", backupJar.getAbsolutePath());

                    // 替换当前 JAR
                    Files.copy(downloadedJar.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    log.info("已用新版本替换当前 JAR");

                    // 清理临时文件
                    FileUtil.del(downloadedJar);

                    // 延迟重启（给响应时间返回）
                    Thread.sleep(2000);
                    log.info("正在重启应用程序...");
                    System.exit(0);

                } catch (Exception e) {
                    log.error("更新应用程序失败", e);
                }
            });

            return success("更新下载已开始，应用程序将自动重启");

        } catch (Exception e) {
            log.error("下载更新失败", e);
            return error("下载更新失败: " + e.getMessage());
        }
    }

    /**
     * 使用镜像源下载文件（支持自动切换）
     *
     * @param originalUrl 原始 GitHub 下载地址
     * @param targetFile  目标保存文件
     * @return 下载成功返回 true，失败返回 false
     */
    private boolean downloadWithMirrors(String originalUrl, File targetFile) {
        for (String mirror : DOWNLOAD_MIRRORS) {
            String downloadUrl = mirror.isEmpty() ? originalUrl : mirror + originalUrl;

            try {
                log.info("尝试从以下地址下载: {}", downloadUrl);

                // 设置超时并下载
                long startTime = System.currentTimeMillis();
                HttpUtil.downloadFile(downloadUrl, targetFile, 300000); // 5分钟超时
                long duration = System.currentTimeMillis() - startTime;

                if (targetFile.exists() && targetFile.length() > 0) {
                    log.info("成功从 {} 下载，耗时 {}ms，文件大小: {} 字节",
                            downloadUrl, duration, targetFile.length());
                    return true;
                }

                log.warn("从 {} 下载的文件为空", downloadUrl);

            } catch (Exception e) {
                log.warn("从 {} 下载失败: {}", downloadUrl, e.getMessage());
                if (targetFile.exists()) {
                    FileUtil.del(targetFile);
                }
            }
        }

        return false;
    }

    /**
     * 获取当前运行的 JAR 文件路径
     */
    private String getCurrentJarPath() {
        try {
            String path = SysUpdateController.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            if (path.endsWith(".jar")) {
                return path;
            }

            // 如果在 IDE 或从 classes 目录运行，尝试在父目录中查找 JAR
            File currentDir = new File(System.getProperty("user.dir"));
            File[] jarFiles = currentDir.listFiles((dir, name) -> name.endsWith(".jar") && name.contains("endless-manager"));

            if (jarFiles != null && jarFiles.length > 0) {
                return jarFiles[0].getAbsolutePath();
            }

            return null;
        } catch (Exception e) {
            log.error("获取当前 JAR 路径失败", e);
            return null;
        }
    }
}
