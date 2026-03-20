package cc.endmc.web.controller.system;

import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

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

    @Value("${app.front-end.path:}")
    private String frontEndPath;

    @Resource
    @Qualifier("threadPoolTaskExecutor")
    private Executor taskExecutor;

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
            // 在 assets 中查找 JAR 文件和前端 ZIP 文件
            String jarDownloadUrl = null;
            String jarFileName = null;
            String frontEndZipUrl = null;
            String frontEndZipFileName = null;
            
            JSONArray assets = json.getJSONArray("assets");
            if (assets != null) {
                for (int i = 0; i < assets.size(); i++) {
                    JSONObject asset = assets.getJSONObject(i);
                    String name = asset.getStr("name");
                    if (name != null) {
                        if (name.endsWith(".jar")) {
                            jarDownloadUrl = asset.getStr("browser_download_url");
                            jarFileName = name;
                        } else if (name.endsWith(".zip") && name.toLowerCase().contains("ui")) {
                            frontEndZipUrl = asset.getStr("browser_download_url");
                            frontEndZipFileName = name;
                        }
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
            final String finalFrontEndZipUrl = frontEndZipUrl;
            final String finalFrontEndZipFileName = frontEndZipFileName;

            // 异步下载（不阻塞响应）
            CompletableFuture.runAsync(() -> {
                try {
                    UpdateProgressController.sendProgress("start", "开始下载更新...", 0);
                    
                    // 下载新 JAR 到临时目录
                    File tempDir = new File(System.getProperty("java.io.tmpdir"));
                    File downloadedJar = new File(tempDir, finalJarFileName);

                    UpdateProgressController.sendProgress("downloading_backend", "正在下载后端文件...", 5);
                    
                    // 尝试从镜像源下载
                    boolean downloadSuccess = downloadWithMirrors(finalJarDownloadUrl, downloadedJar, "backend");

                    if (!downloadSuccess) {
                        log.error("从所有镜像源下载失败");
                        UpdateProgressController.sendComplete(false, "后端文件下载失败");
                        return;
                    }

                    // 二次验证文件完整性
                    if (!downloadedJar.exists() || downloadedJar.length() < 1024 * 1024) {
                        log.error("下载的 JAR 文件不完整或太小: {} 字节", downloadedJar.length());
                        UpdateProgressController.sendComplete(false, "下载的文件不完整，请重试");
                        FileUtil.del(downloadedJar);
                        return;
                    }

                    log.info("新 JAR 已下载到: {}，文件大小: {} 字节",
                            downloadedJar.getAbsolutePath(), downloadedJar.length());
                    UpdateProgressController.sendProgress("backend_downloaded",
                            String.format("后端文件下载完成 (%s)", formatFileSize(downloadedJar.length())), 40);

                    // 备份当前 JAR
                    File currentJar = new File(currentJarPath);
                    File backupJar = new File(currentJar.getParent(), currentJar.getName() + ".backup");
                    Files.copy(currentJar.toPath(), backupJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    log.info("当前 JAR 已备份到: {}", backupJar.getAbsolutePath());
                    UpdateProgressController.sendProgress("backend_backup", "后端文件已备份", 45);

                    // 替换当前 JAR
                    Files.copy(downloadedJar.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    log.info("已用新版本替换当前 JAR");
                    UpdateProgressController.sendProgress("backend_replaced", "后端文件已更新", 50);

                    // 清理临时文件
                    FileUtil.del(downloadedJar);

                    // 如果配置了前端路径且有前端 ZIP 文件，则更新前端
                    if (frontEndPath != null && !frontEndPath.trim().isEmpty()
                            && finalFrontEndZipUrl != null && finalFrontEndZipFileName != null) {
                        UpdateProgressController.sendProgress("updating_frontend", "开始更新前端文件...", 55);
                        updateFrontEnd(finalFrontEndZipUrl, finalFrontEndZipFileName, tempDir);
                    } else {
                        UpdateProgressController.sendProgress("skip_frontend", "跳过前端更新", 90);
                    }

                    UpdateProgressController.sendProgress("preparing_restart", "准备重启应用...", 95);
                    
                    // 延迟重启（给响应时间返回）
                    Thread.sleep(2000);

                    UpdateProgressController.sendComplete(true, "更新完成，应用即将重启");

                    Thread.sleep(1000);
                    log.info("正在重启应用程序...");
                    restartApplication();

                } catch (Exception e) {
                    log.error("更新应用程序失败", e);
                    UpdateProgressController.sendComplete(false, "更新失败: " + e.getMessage());
                }
            }, taskExecutor);

            return success("更新下载已开始，应用程序将自动重启");

        } catch (Exception e) {
            log.error("下载更新失败", e);
            return error("下载更新失败: " + e.getMessage());
        }
    }

    /**
     * 使用镜像源下载文件（支持自动切换和进度回调）
     *
     * @param originalUrl 原始 GitHub 下载地址
     * @param targetFile  目标保存文件
     * @param fileType    文件类型（用于进度提示）
     * @return 下载成功返回 true，失败返回 false
     */
    private boolean downloadWithMirrors(String originalUrl, File targetFile, String fileType) {
        for (int i = 0; i < DOWNLOAD_MIRRORS.length; i++) {
            String mirror = DOWNLOAD_MIRRORS[i];
            String downloadUrl = mirror.isEmpty() ? originalUrl : mirror + originalUrl;

            try {
                log.info("尝试从以下地址下载: {}", downloadUrl);

                // 设置超时并下载，带进度回调
                long startTime = System.currentTimeMillis();
                AtomicLong lastProgressTime = new AtomicLong(startTime);
                AtomicLong expectedSize = new AtomicLong(-1);
                AtomicLong downloadedSize = new AtomicLong(0);

                HttpUtil.downloadFile(downloadUrl, targetFile, 600000, new StreamProgress() {
                    private long lastReportedPercent = 0;

                    @Override
                    public void start() {
                        log.info("开始下载文件: {}", downloadUrl);
                    }

                    @Override
                    public void progress(long total, long progressSize) {
                        // 记录预期大小和已下载大小
                        expectedSize.set(total);
                        downloadedSize.set(progressSize);
                        
                        // 每秒最多更新一次进度
                        long now = System.currentTimeMillis();
                        if (now - lastProgressTime.get() < 1000) {
                            return;
                        }
                        lastProgressTime.set(now);

                        if (total > 0) {
                            long percent = (progressSize * 100) / total;
                            // 只在进度变化超过5%时报告
                            if (percent - lastReportedPercent >= 5) {
                                lastReportedPercent = percent;
                                int adjustedPercent = calculateAdjustedPercent(fileType, (int) percent);
                                String message = String.format("正在下载%s: %d%% (%s/%s)",
                                        getFileTypeLabel(fileType),
                                        percent,
                                        formatFileSize(progressSize),
                                        formatFileSize(total));
                                UpdateProgressController.sendProgress("downloading_" + fileType, message, adjustedPercent);
                                log.info(message);
                            }
                        }
                    }

                    @Override
                    public void finish() {
                        log.info("文件下载完成");
                    }
                });
                
                long duration = System.currentTimeMillis() - startTime;

                // 验证文件是否下载完整
                if (!targetFile.exists()) {
                    log.warn("从 {} 下载后文件不存在", downloadUrl);
                    continue;
                }

                long actualFileSize = targetFile.length();
                long expected = expectedSize.get();

                if (actualFileSize == 0) {
                    log.warn("从 {} 下载的文件为空", downloadUrl);
                    FileUtil.del(targetFile);
                    continue;
                }

                // 如果知道预期大小，验证文件完整性
                if (expected > 0) {
                    if (actualFileSize < expected) {
                        log.warn("从 {} 下载不完整: 预期 {} 字节，实际 {} 字节 ({:.2f}%)",
                                downloadUrl, expected, actualFileSize, (actualFileSize * 100.0 / expected));
                        FileUtil.del(targetFile);
                        continue;
                    }
                }

                log.info("成功从 {} 下载，耗时 {}ms，文件大小: {} 字节",
                        downloadUrl, duration, actualFileSize);
                return true;

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
     * 计算调整后的进度百分比（根据文件类型分配不同的进度区间）
     */
    private int calculateAdjustedPercent(String fileType, int percent) {
        if ("backend".equals(fileType)) {
            // 后端下载占 5%-40%
            return 5 + (percent * 35 / 100);
        } else if ("frontend".equals(fileType)) {
            // 前端下载占 55%-85%
            return 55 + (percent * 30 / 100);
        }
        return percent;
    }

    /**
     * 获取文件类型标签
     */
    private String getFileTypeLabel(String fileType) {
        return switch (fileType) {
            case "backend" -> "后端文件";
            case "frontend" -> "前端文件";
            default -> "文件";
        };
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        }
    }

    /**
     * 更新前端文件
     *
     * @param frontEndZipUrl      前端 ZIP 文件下载地址
     * @param frontEndZipFileName 前端 ZIP 文件名
     * @param tempDir             临时目录
     */
    private void updateFrontEnd(String frontEndZipUrl, String frontEndZipFileName, File tempDir) {
        try {
            log.info("开始更新前端文件...");

            // 下载前端 ZIP 文件
            File downloadedZip = new File(tempDir, frontEndZipFileName);
            boolean downloadSuccess = downloadWithMirrors(frontEndZipUrl, downloadedZip, "frontend");

            if (!downloadSuccess) {
                log.error("前端 ZIP 文件下载失败");
                UpdateProgressController.sendProgress("frontend_download_failed", "前端文件下载失败", 85);
                return;
            }

            // 验证 ZIP 文件完整性
            if (!downloadedZip.exists() || downloadedZip.length() < 1024) {
                log.error("下载的前端 ZIP 文件不完整或太小: {} 字节", downloadedZip.length());
                UpdateProgressController.sendProgress("frontend_download_failed", "前端文件下载不完整", 85);
                FileUtil.del(downloadedZip);
                return;
            }

            log.info("前端 ZIP 已下载到: {}，文件大小: {} 字节",
                    downloadedZip.getAbsolutePath(), downloadedZip.length());
            UpdateProgressController.sendProgress("frontend_downloaded",
                    String.format("前端文件下载完成 (%s)", formatFileSize(downloadedZip.length())), 85);

            // 检查前端路径
            File frontEndDir = new File(frontEndPath);
            if (!frontEndDir.exists()) {
                log.error("前端路径不存在: {}", frontEndPath);
                FileUtil.del(downloadedZip);
                UpdateProgressController.sendProgress("frontend_path_error", "前端路径不存在", 85);
                return;
            }

            // 判断前端目录结构：检查是否包含 dist 目录或 index.html
            File distDir = new File(frontEndDir, "dist");
            File indexHtml = new File(frontEndDir, "index.html");

            File targetDir;
            if (distDir.exists() && distDir.isDirectory()) {
                // 前端目录包含 dist 子目录
                targetDir = distDir;
                log.info("检测到 dist 目录，将更新到: {}", targetDir.getAbsolutePath());
            } else if (indexHtml.exists()) {
                // 前端目录直接包含 index.html
                targetDir = frontEndDir;
                log.info("检测到 index.html，将更新到: {}", targetDir.getAbsolutePath());
            } else {
                log.error("前端目录结构不正确，未找到 dist 目录或 index.html");
                FileUtil.del(downloadedZip);
                UpdateProgressController.sendProgress("frontend_structure_error", "前端目录结构不正确", 85);
                return;
            }

            UpdateProgressController.sendProgress("frontend_backing_up", "正在备份前端文件...", 86);

            // 备份当前前端文件
            File backupDir = new File(frontEndDir.getParent(), frontEndDir.getName() + "_backup_" + System.currentTimeMillis());
            try {
                FileUtil.copyContent(targetDir, backupDir, true);
                log.info("前端文件已备份到: {}", backupDir.getAbsolutePath());
            } catch (Exception e) {
                log.warn("备份前端文件失败: {}", e.getMessage());
            }

            UpdateProgressController.sendProgress("frontend_extracting", "正在解压前端文件...", 87);

            // 解压 ZIP 文件到临时目录
            File unzipTempDir = new File(tempDir, "frontend_unzip_" + System.currentTimeMillis());
            unzipTempDir.mkdirs();

            try {
                cn.hutool.core.util.ZipUtil.unzip(downloadedZip, unzipTempDir);
                log.info("前端 ZIP 已解压到: {}", unzipTempDir.getAbsolutePath());
                UpdateProgressController.sendProgress("frontend_extracted", "前端文件解压完成", 88);

                // 查找解压后的 dist 目录
                File unzippedDist = new File(unzipTempDir, "dist");
                File sourceDir;

                if (unzippedDist.exists() && unzippedDist.isDirectory()) {
                    // ZIP 中包含 dist 目录
                    sourceDir = unzippedDist;
                } else {
                    // ZIP 直接包含前端文件
                    sourceDir = unzipTempDir;
                }

                UpdateProgressController.sendProgress("frontend_replacing", "正在替换前端文件...", 89);

                // 清空目标目录
                FileUtil.clean(targetDir);
                log.info("已清空目标目录: {}", targetDir.getAbsolutePath());

                // 复制新文件到目标目录
                FileUtil.copyContent(sourceDir, targetDir, true);
                log.info("前端文件已更新到: {}", targetDir.getAbsolutePath());
                UpdateProgressController.sendProgress("frontend_replaced", "前端文件已更新", 90);

            } finally {
                // 清理临时文件
                FileUtil.del(unzipTempDir);
                FileUtil.del(downloadedZip);
            }

            log.info("前端更新完成");

        } catch (Exception e) {
            log.error("更新前端文件失败", e);
            UpdateProgressController.sendProgress("frontend_error", "前端更新失败: " + e.getMessage(), 85);
        }
    }

    /**
     * 重启应用程序
     */
    private void restartApplication() {
        try {
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            String currentJar = getCurrentJarPath();

            if (currentJar == null) {
                log.error("无法获取当前 JAR 路径，无法重启");
                System.exit(1);
                return;
            }

            // 构建重启命令
            ProcessBuilder builder = new ProcessBuilder();

            // 获取当前 JVM 参数
            RuntimeMXBean runtimeMxBean =
                    ManagementFactory.getRuntimeMXBean();
            List<String> jvmArgs = runtimeMxBean.getInputArguments();

            List<String> command = new ArrayList<>();
            command.add(javaBin);

            // 添加 JVM 参数（过滤掉一些不需要的参数）
            for (String arg : jvmArgs) {
                if (!arg.contains("-agentlib") && !arg.contains("-javaagent")) {
                    command.add(arg);
                }
            }

            command.add("-jar");
            command.add(currentJar);

            builder.command(command);
            builder.directory(new File(System.getProperty("user.dir")));

            // 继承当前进程的环境变量
            builder.inheritIO();

            log.info("重启命令: {}", String.join(" ", command));

            // 启动新进程
            builder.start();

            // 延迟退出当前进程，确保新进程启动
            Thread.sleep(2000);
            System.exit(0);

        } catch (Exception e) {
            log.error("重启应用程序失败", e);
            System.exit(1);
        }
    }

    /**
     * 获取当前运行的 JAR 文件路径
     */
    private String getCurrentJarPath() {
        try {
            // 尝试从 ProtectionDomain 获取路径
            String path = null;
            if (SysUpdateController.class.getProtectionDomain() != null
                    && SysUpdateController.class.getProtectionDomain().getCodeSource() != null
                    && SysUpdateController.class.getProtectionDomain().getCodeSource().getLocation() != null) {
                path = SysUpdateController.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI()
                        .getPath();
            }

            // 检查路径是否为 JAR 文件
            if (path != null && path.endsWith(".jar")) {
                return path;
            }

            // 如果在 IDE 或从 classes 目录运行，尝试在父目录中查找 JAR
            File currentDir = new File(System.getProperty("user.dir"));
            File[] jarFiles = currentDir.listFiles((dir, name) -> name.endsWith(".jar") && name.contains("endless-manager"));

            if (jarFiles != null && jarFiles.length > 0) {
                return jarFiles[0].getAbsolutePath();
            }

            log.warn("无法定位 JAR 文件，可能在开发环境中运行");
            return null;
        } catch (Exception e) {
            log.error("获取当前 JAR 路径失败", e);
            return null;
        }
    }
}
