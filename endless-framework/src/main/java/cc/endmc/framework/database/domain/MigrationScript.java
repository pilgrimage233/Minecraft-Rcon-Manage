package cc.endmc.framework.database.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数据库升级脚本实体类
 * 支持按应用版本分类的迁移脚本
 * 文件命名格式: V{version}__{description}.sql
 * 例如: V1_0_1__add_rcon_node_instance_relation.sql
 *
 * @author Memory
 * @since 2024-12-28
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class MigrationScript implements Serializable, Comparable<MigrationScript> {

    private static final long serialVersionUID = 1L;

    /**
     * 应用程序版本号
     */
    private String appVersion;

    /**
     * 脚本类型(migration)
     */
    private String scriptType;

    /**
     * 脚本名称
     */
    private String scriptName;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 脚本内容
     */
    private String content;

    /**
     * 描述
     */
    private String description;

    /**
     * 校验和
     */
    private String checksum;

    /**
     * 执行顺序
     */
    private Integer executionOrder;

    /**
     * 版本排序权重
     */
    private Integer sortWeight;

    /**
     * 构造函数
     */
    public MigrationScript() {
    }

    /**
     * 构造函数
     *
     * @param filePath 文件路径 (如: db/migration/V1_0_1__add_rcon_node_instance_relation.sql)
     * @param content  脚本内容
     */
    public MigrationScript(String filePath, String content) {
        this.content = content;
        parseFilePath(filePath);
        this.checksum = calculateChecksum(content);
    }

    /**
     * 解析文件路径获取版本信息
     * 路径格式: db/migration/V{version}__{description}.sql
     * 例如: db/migration/V1_0_1__add_rcon_node_instance_relation.sql
     *
     * @param filePath 文件路径
     */
    private void parseFilePath(String filePath) {
        if (filePath == null || !filePath.endsWith(".sql")) {
            throw new IllegalArgumentException("无效的迁移脚本文件路径: " + filePath);
        }

        // 标准化路径分隔符
        String normalizedPath = filePath.replace("\\", "/");
        String[] pathParts = normalizedPath.split("/");

        if (pathParts.length < 3) {
            throw new IllegalArgumentException("迁移脚本路径格式错误，应为: db/migration/V{version}__{description}.sql");
        }

        // 解析文件名
        this.fileName = pathParts[pathParts.length - 1];
        String nameWithoutExt = fileName.substring(0, fileName.length() - 4);

        // 解析版本和描述 (格式: V1_0_1__add_rcon_node_instance_relation)
        if (!nameWithoutExt.startsWith("V")) {
            throw new IllegalArgumentException("脚本文件名必须以 V 开头: " + fileName);
        }

        int doubleUnderscoreIndex = nameWithoutExt.indexOf("__");
        if (doubleUnderscoreIndex == -1) {
            throw new IllegalArgumentException("脚本文件名必须包含双下划线分隔版本和描述: " + fileName);
        }

        // 解析版本号 (V1_0_1 -> 1.0.1)
        String versionPart = nameWithoutExt.substring(1, doubleUnderscoreIndex);
        this.appVersion = versionPart.replace("_", ".");

        // 解析脚本名称和描述
        this.scriptName = nameWithoutExt.substring(doubleUnderscoreIndex + 2);
        this.description = this.scriptName.replace("_", " ");

        // 设置脚本类型为 migration (统一类型)
        this.scriptType = "migration";

        // 从版本号中提取执行顺序 (用于排序)
        this.executionOrder = extractExecutionOrder(versionPart);

        // 计算排序权重
        this.sortWeight = calculateSortWeight(this.appVersion);
    }

    /**
     * 从版本号中提取执行顺序
     * 例: V1_0_1 -> 1001, V1_1_0 -> 1100
     *
     * @param versionPart 版本部分 (如: 1_0_1)
     * @return 执行顺序
     */
    private Integer extractExecutionOrder(String versionPart) {
        String[] parts = versionPart.split("_");
        int order = 0;

        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            try {
                int part = Integer.parseInt(parts[i]);
                order += part * (int) Math.pow(100, 2 - i);
            } catch (NumberFormatException e) {
                // 如果版本号包含非数字字符，使用0
                order += 0;
            }
        }

        return order;
    }

    /**
     * 计算版本排序权重
     * 例: 3.9.1 -> 3009001, 3.10.0 -> 3010000
     *
     * @param version 版本号
     * @return 排序权重
     */
    private Integer calculateSortWeight(String version) {
        String[] parts = version.split("\\.");
        int weight = 0;

        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            try {
                int part = Integer.parseInt(parts[i]);
                weight += part * (int) Math.pow(1000, 2 - i);
            } catch (NumberFormatException e) {
                // 如果版本号包含非数字字符，使用字符串哈希值
                weight += Math.abs(parts[i].hashCode()) % 1000 * (int) Math.pow(1000, 2 - i);
            }
        }

        return weight;
    }

    /**
     * 计算内容校验和
     *
     * @param content 内容
     * @return MD5校验和
     */
    private String calculateChecksum(String content) {
        if (content == null) {
            return null;
        }

        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(content.hashCode());
        }
    }

    /**
     * 获取脚本唯一标识
     *
     * @return 唯一标识
     */
    public String getScriptKey() {
        return String.format("%s:%s:%s", appVersion, scriptType, scriptName);
    }

    /**
     * 比较脚本用于排序
     * 排序规则：
     * 1. 按应用版本排序
     * 2. 同版本内，按执行顺序排序
     *
     * @param other 另一个脚本
     * @return 比较结果
     */
    @Override
    public int compareTo(MigrationScript other) {
        if (other == null) {
            return 1;
        }

        // 1. 按版本权重排序
        int weightCompare = Integer.compare(this.sortWeight, other.sortWeight);
        if (weightCompare != 0) {
            return weightCompare;
        }

        // 2. 同版本内，按执行顺序排序
        int orderCompare = Integer.compare(this.executionOrder, other.executionOrder);
        if (orderCompare != 0) {
            return orderCompare;
        }

        // 3. 最后按文件名排序
        return this.fileName.compareTo(other.fileName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrationScript that = (MigrationScript) o;
        return Objects.equals(appVersion, that.appVersion) &&
                Objects.equals(scriptType, that.scriptType) &&
                Objects.equals(scriptName, that.scriptName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appVersion, scriptType, scriptName);
    }

    @Override
    public String toString() {
        return String.format("MigrationScript{appVersion='%s', scriptType='%s', scriptName='%s', fileName='%s'}",
                appVersion, scriptType, scriptName, fileName);
    }
}