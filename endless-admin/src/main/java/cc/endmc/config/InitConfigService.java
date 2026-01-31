package cc.endmc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class InitConfigService {
    private static final Logger log = LoggerFactory.getLogger(InitConfigService.class);
    private static final String CONFIG_DIR = "config";
    private static final String[] CONFIG_FILES = {
            "application.yml",
            "application-druid.yml"
    };

    public void initializeConfigs() {
        try {
            createConfigDirectory();
            for (String configFile : CONFIG_FILES) {
                if ("application.yml".equals(configFile)) {
                    handleApplicationYml(configFile);
                } else {
                    handleOtherConfig(configFile);
                }
            }
        } catch (Exception e) {
            log.error("配置文件初始化失败", e);
            throw new RuntimeException("配置文件初始化失败", e);
        }
    }

    private void createConfigDirectory() throws IOException {
        Path configPath = Paths.get(CONFIG_DIR);
        if (!Files.exists(configPath)) {
            Files.createDirectory(configPath);
            log.info("创建配置目录: {}", configPath.toAbsolutePath());
        }
    }

    private void handleApplicationYml(String configFile) throws IOException {
        Path targetPath = Paths.get(CONFIG_DIR, configFile);

        // 读取jar包中的配置
        List<String> jarConfigLines = readConfigFromJar(configFile);

        if (!Files.exists(targetPath)) {
            // 如果本地配置文件不存在，创建不包含endless节点的配置文件
            List<String> filteredLines = filterOutEndlessSection(jarConfigLines);
            Files.write(targetPath, filteredLines);
            log.info("创建新配置文件(不包含endless节点): {}", targetPath.toAbsolutePath());
        } else {
            // 如果本地配置文件存在，合并新配置
            mergeConfigurations(targetPath, jarConfigLines);
        }
    }

    /**
     * 读取jar包中的配置文件
     */
    private List<String> readConfigFromJar(String configFile) throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStream is = ResourceUtils.getURL("classpath:" + configFile).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * 合并配置文件，将jar包中的新配置项添加到外部配置文件中（支持深度合并）
     */
    private void mergeConfigurations(Path targetPath, List<String> jarConfigLines) throws IOException {
        // 读取现有的外部配置文件
        List<String> existingLines = Files.readAllLines(targetPath);

        // 从jar配置中过滤掉endless节点
        List<String> jarFilteredLines = filterOutEndlessSection(jarConfigLines);

        // 解析两个配置文件的完整路径结构
        Map<String, YamlNode> existingStructure = parseYamlStructure(existingLines);
        Map<String, YamlNode> jarStructure = parseYamlStructure(jarFilteredLines);

        // 找出需要添加的新配置路径
        List<String> newPaths = findNewConfigPaths(existingStructure, jarStructure);

        if (!newPaths.isEmpty()) {
            // 执行深度合并，将新配置插入到正确的位置
            List<String> mergedLines = deepMergeYaml(existingLines, jarFilteredLines, newPaths);

            // 写回文件
            Files.write(targetPath, mergedLines);
            log.info("配置文件已更新，新增 {} 个配置路径: {}", newPaths.size(), targetPath.toAbsolutePath());
            log.debug("新增配置路径: {}", newPaths);
        } else {
            log.info("配置文件无需更新: {}", targetPath.toAbsolutePath());
        }
    }

    /**
     * 深度合并YAML配置，将新配置插入到现有配置的正确位置
     */
    private List<String> deepMergeYaml(List<String> existingLines, List<String> jarLines, List<String> newPaths) {
        List<String> result = new ArrayList<>(existingLines);

        // 按路径深度排序，先处理顶级节点，再处理子节点
        List<String> sortedPaths = new ArrayList<>(newPaths);
        sortedPaths.sort((a, b) -> {
            int depthA = a.split("\\.").length;
            int depthB = b.split("\\.").length;
            return Integer.compare(depthA, depthB);
        });

        // 记录已处理的顶级节点，避免重复处理
        Set<String> processedTopLevelKeys = new HashSet<>();

        for (String path : sortedPaths) {
            String[] pathParts = path.split("\\.");
            String topLevelKey = pathParts[0];

            if (pathParts.length == 1) {
                // 顶级节点不存在，直接追加
                if (!processedTopLevelKeys.contains(topLevelKey)) {
                    List<String> newSection = extractTopLevelSection(jarLines, topLevelKey);
                    if (!newSection.isEmpty()) {
                        if (!result.isEmpty() && !result.getLast().trim().isEmpty()) {
                            result.add("");
                        }
                        result.add("# 新增配置项: " + path + " (" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) + ")");
                        result.addAll(newSection);
                        processedTopLevelKeys.add(topLevelKey);
                    }
                }
            } else {
                // 子节点不存在，需要插入到父节点内部
                if (!processedTopLevelKeys.contains(topLevelKey)) {
                    result = insertNestedConfig(result, jarLines, pathParts);
                    processedTopLevelKeys.add(topLevelKey);
                }
            }
        }

        return result;
    }

    /**
     * 将嵌套配置插入到现有配置的正确位置
     */
    private List<String> insertNestedConfig(List<String> existingLines, List<String> jarLines, String[] pathParts) {
        List<String> result = new ArrayList<>();
        String topLevelKey = pathParts[0];

        // 从jar配置中提取需要新增的子配置
        Map<String, List<String>> newSubConfigs = extractMissingSubConfigs(existingLines, jarLines, pathParts);

        if (newSubConfigs.isEmpty()) {
            return existingLines;
        }

        boolean inTargetSection = false;
        int targetIndent = -1;
        boolean inserted = false;
        int lastContentLineIndex = -1; // 记录目标节点内最后一行实际内容的位置

        for (int i = 0; i < existingLines.size(); i++) {
            String line = existingLines.get(i);
            String trimmed = line.trim();
            int indent = getIndentLevel(line);

            // 找到目标顶级节点
            if (!inTargetSection && trimmed.startsWith(topLevelKey + ":")) {
                inTargetSection = true;
                targetIndent = indent;
                result.add(line);
                lastContentLineIndex = result.size() - 1;
                continue;
            }

            if (inTargetSection && !inserted) {
                // 遇到同级或更低级的非空非注释节点，说明目标节点结束
                if (indent <= targetIndent && !trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    // 在最后一行内容后插入新配置
                    List<String> temp = new ArrayList<>();
                    temp.add("  # 新增子配置 (" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ")");
                    for (List<String> subConfig : newSubConfigs.values()) {
                        temp.addAll(subConfig);
                    }

                    // 在lastContentLineIndex之后插入
                    result.addAll(lastContentLineIndex + 1, temp);
                    inserted = true;
                    inTargetSection = false;
                    result.add(line);
                } else {
                    result.add(line);
                    // 如果是目标节点内的实际内容（非空非注释），更新最后内容行位置
                    if (indent > targetIndent && !trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        lastContentLineIndex = result.size() - 1;
                    }
                }
            } else {
                result.add(line);
            }
        }

        // 如果到文件末尾还没插入（目标节点在文件末尾）
        if (inTargetSection && !inserted) {
            result.add("  # 新增子配置 (" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) + ")");
            for (List<String> subConfig : newSubConfigs.values()) {
                result.addAll(subConfig);
            }
        }

        return result;
    }

    /**
     * 提取jar配置中缺失的子配置
     */
    private Map<String, List<String>> extractMissingSubConfigs(List<String> existingLines,
                                                               List<String> jarLines,
                                                               String[] pathParts) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        String topLevelKey = pathParts[0];

        // 解析现有配置的子节点
        Set<String> existingSubKeys = parseSubKeys(existingLines, topLevelKey);

        // 从jar配置中提取顶级节点的所有子配置
        List<String> jarSection = extractTopLevelSection(jarLines, topLevelKey);

        // 提取jar配置中缺失的子节点
        boolean inTopLevel = false;
        int baseIndent = -1;
        List<String> currentSubConfig = new ArrayList<>();
        String currentSubKey = null;

        for (String line : jarSection) {
            String trimmed = line.trim();
            int indent = getIndentLevel(line);

            if (trimmed.startsWith(topLevelKey + ":")) {
                inTopLevel = true;
                baseIndent = indent;
                continue;
            }

            if (inTopLevel) {
                // 检查是否是直接子节点（非注释、非空行）
                if (indent == baseIndent + 2 && trimmed.contains(":") && !trimmed.startsWith("#")) {
                    // 保存上一个子配置
                    if (currentSubKey != null && !existingSubKeys.contains(currentSubKey)) {
                        result.put(currentSubKey, new ArrayList<>(currentSubConfig));
                    }

                    // 开始新的子配置
                    currentSubKey = trimmed.split(":")[0].trim();
                    currentSubConfig = new ArrayList<>();
                    currentSubConfig.add(line);
                } else if (currentSubKey != null) {
                    // 在当前子节点内部
                    if (indent > baseIndent + 2 || (indent == baseIndent + 2 && (trimmed.isEmpty() || trimmed.startsWith("#")))) {
                        // 子节点的子内容或注释
                        currentSubConfig.add(line);
                    } else if (indent <= baseIndent + 2 && !trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        // 遇到同级或更低级的非注释内容，当前子配置结束
                        break;
                    }
                }
            }
        }

        // 保存最后一个子配置
        if (currentSubKey != null && !existingSubKeys.contains(currentSubKey)) {
            result.put(currentSubKey, currentSubConfig);
        }

        return result;
    }

    /**
     * 解析指定顶级节点下的直接子节点键
     */
    private Set<String> parseSubKeys(List<String> lines, String topLevelKey) {
        Set<String> subKeys = new HashSet<>();
        boolean inTargetSection = false;
        int baseIndent = -1;

        for (String line : lines) {
            String trimmed = line.trim();
            int indent = getIndentLevel(line);

            if (trimmed.startsWith(topLevelKey + ":")) {
                inTargetSection = true;
                baseIndent = indent;
                continue;
            }

            if (inTargetSection) {
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                // 遇到同级或更低级的节点，结束
                if (indent <= baseIndent) {
                    break;
                }

                // 直接子节点
                if (indent == baseIndent + 2 && trimmed.contains(":")) {
                    String key = trimmed.split(":")[0].trim();
                    subKeys.add(key);
                }
            }
        }

        return subKeys;
    }

    /**
     * 解析YAML文件的完整树形结构
     */
    private Map<String, YamlNode> parseYamlStructure(List<String> lines) {
        Map<String, YamlNode> rootNodes = new LinkedHashMap<>();
        Stack<YamlNode> nodeStack = new Stack<>();

        for (String line : lines) {
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                continue;
            }

            int indent = getIndentLevel(line);
            String trimmed = line.trim();

            if (!trimmed.contains(":")) {
                continue;
            }

            String[] parts = trimmed.split(":", 2);
            String key = parts[0].trim();
            boolean hasValue = parts.length > 1 && !parts[1].trim().isEmpty();

            YamlNode node = new YamlNode(key, indent, hasValue);

            // 弹出栈中缩进级别大于等于当前节点的节点
            while (!nodeStack.isEmpty() && nodeStack.peek().indent >= indent) {
                nodeStack.pop();
            }

            if (nodeStack.isEmpty()) {
                // 顶级节点
                rootNodes.put(key, node);
            } else {
                // 子节点
                nodeStack.peek().children.put(key, node);
            }

            nodeStack.push(node);
        }

        return rootNodes;
    }

    /**
     * 获取行的缩进级别
     */
    private int getIndentLevel(String line) {
        int indent = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                indent++;
            } else if (c == '\t') {
                indent += 4; // 制表符算4个空格
            } else {
                break;
            }
        }
        return indent;
    }

    /**
     * 找出jar配置中存在但本地配置中不存在的配置路径
     */
    private List<String> findNewConfigPaths(Map<String, YamlNode> existing, Map<String, YamlNode> jar) {
        List<String> newPaths = new ArrayList<>();
        findNewPathsRecursive("", existing, jar, newPaths);
        return newPaths;
    }

    /**
     * 递归查找新配置路径
     */
    private void findNewPathsRecursive(String parentPath, Map<String, YamlNode> existing,
                                       Map<String, YamlNode> jar, List<String> newPaths) {
        for (Map.Entry<String, YamlNode> entry : jar.entrySet()) {
            String key = entry.getKey();
            YamlNode jarNode = entry.getValue();
            String currentPath = parentPath.isEmpty() ? key : parentPath + "." + key;

            if (!existing.containsKey(key)) {
                // 整个节点都是新的
                newPaths.add(currentPath);
            } else {
                // 节点存在，检查子节点
                YamlNode existingNode = existing.get(key);
                if (!jarNode.children.isEmpty()) {
                    findNewPathsRecursive(currentPath, existingNode.children, jarNode.children, newPaths);
                }
            }
        }
    }

    /**
     * 从jar配置中提取新配置项的完整内容（已废弃）
     */
    @Deprecated
    private List<String> extractNewConfigs(List<String> lines, List<String> newPaths) {
        List<String> result = new ArrayList<>();
        Set<String> addedTopLevelKeys = new HashSet<>();

        for (String path : newPaths) {
            String[] pathParts = path.split("\\.");
            String topLevelKey = pathParts[0];

            // 如果是顶级节点的新增，提取整个节点
            if (pathParts.length == 1) {
                if (!addedTopLevelKeys.contains(topLevelKey)) {
                    List<String> section = extractTopLevelSection(lines, topLevelKey);
                    if (!section.isEmpty()) {
                        if (!result.isEmpty()) {
                            result.add("");
                        }
                        result.addAll(section);
                        addedTopLevelKeys.add(topLevelKey);
                    }
                }
            } else {
                // 如果是子节点的新增，提取该子节点
                if (!addedTopLevelKeys.contains(topLevelKey)) {
                    List<String> section = extractNestedSection(lines, pathParts);
                    if (!section.isEmpty()) {
                        if (!result.isEmpty()) {
                            result.add("");
                        }
                        result.addAll(section);
                        addedTopLevelKeys.add(topLevelKey);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 提取顶级配置节点
     */
    private List<String> extractTopLevelSection(List<String> lines, String key) {
        List<String> result = new ArrayList<>();
        boolean inSection = false;
        int baseIndent = -1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();

            if (!inSection && trimmed.startsWith(key + ":")) {
                inSection = true;
                baseIndent = getIndentLevel(line);
                result.add(line);
                continue;
            }

            if (inSection) {
                int indent = getIndentLevel(line);

                // 空行或注释
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    // 检查下一行是否还在当前节点内
                    if (i + 1 < lines.size()) {
                        String nextLine = lines.get(i + 1);
                        String nextTrimmed = nextLine.trim();
                        int nextIndent = getIndentLevel(nextLine);

                        // 如果下一行是同级或更低级的非空非注释行，说明当前节点结束
                        if (!nextTrimmed.isEmpty() && !nextTrimmed.startsWith("#") && nextIndent <= baseIndent) {
                            break;
                        }
                    }
                    result.add(line);
                } else if (indent > baseIndent) {
                    // 子节点内容
                    result.add(line);
                } else {
                    // 遇到同级或更低级的节点，结束
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 提取嵌套配置节点（已废弃）
     */
    @Deprecated
    private List<String> extractNestedSection(List<String> lines, String[] pathParts) {
        // 对于嵌套节点，提取整个顶级节点
        return extractTopLevelSection(lines, pathParts[0]);
    }

    /**
     * 解析YAML文件的顶级键（已废弃，保留用于兼容）
     */
    @Deprecated
    private Set<String> parseYamlKeys(List<String> lines) {
        Set<String> keys = new HashSet<>();
        Pattern keyPattern = Pattern.compile("^([a-zA-Z][a-zA-Z0-9_-]*):.*");

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                var matcher = keyPattern.matcher(line);
                if (matcher.matches()) {
                    keys.add(matcher.group(1));
                }
            }
        }
        return keys;
    }

    /**
     * 提取指定键的配置段落（已废弃，保留用于兼容）
     */
    @Deprecated
    private List<String> extractConfigSections(List<String> lines, Set<String> targetKeys) {
        List<String> result = new ArrayList<>();
        boolean inTargetSection = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();

            // 跳过注释和空行
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                if (inTargetSection) {
                    result.add(line);
                }
                continue;
            }

            // 检查是否是顶级键
            if (!line.startsWith(" ") && !line.startsWith("\t") && line.contains(":")) {
                String key = line.split(":")[0].trim();

                if (targetKeys.contains(key)) {
                    inTargetSection = true;
                    result.add(line);
                } else {
                    inTargetSection = false;
                }
            } else if (inTargetSection) {
                // 在目标段落内的缩进行
                result.add(line);
            }
        }

        return result;
    }

    /**
     * YAML节点类，用于表示配置树结构
     */
    private static class YamlNode {
        int indent;
        Map<String, YamlNode> children = new LinkedHashMap<>();

        YamlNode(String key, int indent, boolean hasValue) {
            this.indent = indent;
        }
    }

    /**
     * 过滤掉endless节点，保留其他配置
     */
    private List<String> filterOutEndlessSection(List<String> lines) {
        List<String> filteredLines = new ArrayList<>();
        boolean inEndlessSection = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            // 遇到endless节点，开始跳过
            if (line.trim().startsWith("endless:")) {
                inEndlessSection = true;
                continue;
            }

            // 在endless节点内部，跳过所有缩进的行
            if (inEndlessSection) {
                if (line.startsWith(" ") || line.startsWith("\t") || line.trim().isEmpty()) {
                    // 检查是否是endless节点的最后一行
                    if (i + 1 < lines.size()) {
                        String nextLine = lines.get(i + 1);
                        if (!nextLine.startsWith(" ") && !nextLine.startsWith("\t") && !nextLine.trim().isEmpty()) {
                            inEndlessSection = false;
                        }
                    } else {
                        inEndlessSection = false;
                    }
                    continue;
                } else {
                    inEndlessSection = false;
                }
            }

            // 保留非endless节点的内容
            filteredLines.add(line);
        }

        return filteredLines;
    }

    private void handleOtherConfig(String configFile) throws IOException {
        Path targetPath = Paths.get(CONFIG_DIR, configFile);
        // 只在配置文件不存在时创建
        if (!Files.exists(targetPath)) {
            try (InputStream is = ResourceUtils.getURL("classpath:" + configFile).openStream();
                 OutputStream os = Files.newOutputStream(targetPath)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
            log.info("创建配置文件: {}", targetPath.toAbsolutePath());
        }
    }
}