package cc.endmc.web.controller.setup;

import cc.endmc.common.annotation.Anonymous;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Set;

/**
 * 配置向导接口
 */
@Anonymous
@RestController
@RequestMapping("/setup")
public class SetupConfigController
{
    private static final Logger log = LoggerFactory.getLogger(SetupConfigController.class);
    private static final Path CONFIG_DIR = Paths.get("config");
    private static final Set<String> ALLOWED_FILES = Set.of("application.yml", "application-druid.yml");

    @Value("${setup.allow-remote:false}")
    private boolean allowRemote;

    @GetMapping("/config")
    public AjaxResult getConfig(@RequestParam(value = "file", defaultValue = "application.yml") String file,
                                HttpServletRequest request)
    {
        if (!isRequestAllowed(request))
        {
            return AjaxResult.error("仅允许本机访问配置向导，如需远程访问请设置 setup.allow-remote=true");
        }
        Path configPath = resolveConfigPath(file);
        if (configPath == null)
        {
            return AjaxResult.error("不支持的配置文件: " + file);
        }
        if (!Files.exists(configPath))
        {
            return AjaxResult.error("配置文件不存在: " + file);
        }
        try
        {
            String content = Files.readString(configPath, StandardCharsets.UTF_8);
            HashMap<String, Object> data = new HashMap<>();
            data.put("file", file);
            data.put("content", content);
            return AjaxResult.success(data);
        }
        catch (IOException e)
        {
            log.error("读取配置文件失败: {}", configPath.toAbsolutePath(), e);
            return AjaxResult.error("读取配置文件失败");
        }
    }

    @PostMapping(value = "/config", consumes = MediaType.TEXT_PLAIN_VALUE)
    public AjaxResult saveConfig(@RequestParam(value = "file", defaultValue = "application.yml") String file,
                                 @RequestBody(required = false) String content,
                                 HttpServletRequest request)
    {
        if (!isRequestAllowed(request))
        {
            return AjaxResult.error("仅允许本机访问配置向导，如需远程访问请设置 setup.allow-remote=true");
        }
        Path configPath = resolveConfigPath(file);
        if (configPath == null)
        {
            return AjaxResult.error("不支持的配置文件: " + file);
        }
        if (StringUtils.isBlank(content))
        {
            return AjaxResult.error("配置内容不能为空");
        }
        try
        {
            Files.createDirectories(CONFIG_DIR);
            Files.writeString(configPath, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            log.info("配置文件已保存: {}", configPath.toAbsolutePath());
            return AjaxResult.success("保存成功，请重启服务使配置生效");
        }
        catch (IOException e)
        {
            log.error("保存配置文件失败: {}", configPath.toAbsolutePath(), e);
            return AjaxResult.error("保存配置文件失败");
        }
    }

    private Path resolveConfigPath(String file)
    {
        if (!ALLOWED_FILES.contains(file))
        {
            return null;
        }
        return CONFIG_DIR.resolve(file).normalize();
    }

    private boolean isRequestAllowed(HttpServletRequest request)
    {
        return allowRemote || isLocalAddress(request.getRemoteAddr());
    }

    private boolean isLocalAddress(String address)
    {
        if (address == null)
        {
            return false;
        }
        return "127.0.0.1".equals(address)
                || "0:0:0:0:0:0:0:1".equals(address)
                || "::1".equals(address);
    }
}
