package cc.endmc.node.utils;

import cc.endmc.common.utils.StringUtils;
import cc.endmc.node.domain.NodeMinecraftServer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartScriptBuilder {

    public static String prepareStartScript(String providedScript, NodeMinecraftServer server) {
        String script = providedScript;
        if (StringUtils.isEmpty(script)) {
            script = server.getStartStr();
        }
        return processJvmArgs(script, server);
    }

    private static String processJvmArgs(String script, NodeMinecraftServer server) {
        if (script == null || script.isEmpty()) return script;

        String newXms = "-Xms" + server.getJvmXms() + "M";
        String newXmx = "-Xmx" + server.getJvmXmx() + "M";
        String otherArgs = server.getJvmArgs();
        String javaPath = server.getJavaPath();

        script = script.replaceAll("-Xms\\d+[MmGgKk]?\\s*", "");
        script = script.replaceAll("-Xmx\\d+[MmGgKk]?\\s*", "");

        if (StringUtils.isNotEmpty(otherArgs)) {
            String[] commonJvmArgs = {
                    "-XX:\\+UseG1GC", "-XX:\\+ParallelRefProcEnabled", "-XX:MaxGCPauseMillis=",
                    "-XX:G1HeapRegionSize=", "-XX:\\+UnlockExperimentalVMOptions", "-XX:\\+DisableExplicitGC",
                    "-XX:-OmitStackTraceInFastThrow", "-XX:G1NewSizePercent=", "-XX:G1MaxNewSizePercent=",
                    "-XX:G1HeapWastePercent=", "-XX:G1MixedGCCountTarget=", "-XX:InitiatingHeapOccupancyPercent=",
                    "-XX:G1MixedGCLiveThresholdPercent=", "-XX:G1RSetUpdatingPauseTimePercent=",
                    "-XX:SurvivorRatio=", "-XX:PerfDisableSharedMem", "-XX:MaxTenuringThreshold=",
                    "-Dusing.aikars.flags=", "-Daikars.new.flags="
            };
            for (String arg : commonJvmArgs) {
                if (arg.endsWith("=")) {
                    script = script.replaceAll(arg.replace("+", "\\+").replace(".", "\\.") + "\\d+\\s*", "");
                } else {
                    script = script.replace(arg + " ", "").replace(arg, "");
                }
            }
        }

        Pattern pattern = Pattern.compile("(['\"]?)([^\\s'\"]*[/\\\\])?java(\\.exe)?\\1(?=\\s|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(script);

        final boolean match = javaPath != null && javaPath.contains(" ") && !javaPath.startsWith("\"") && !javaPath.startsWith("'");
        if (matcher.find()) {
            int javaStart = matcher.start();
            int javaEnd = matcher.end();

            String javaCommand;
            if (StringUtils.isNotEmpty(javaPath)) {
                javaCommand = match ? "\"" + javaPath + "\"" : javaPath;
            } else {
                javaCommand = matcher.group(0);
            }

            int insertPos = javaEnd;
            while (insertPos < script.length() && script.charAt(insertPos) == ' ') insertPos++;

            StringBuilder sb = new StringBuilder();
            sb.append(script, 0, javaStart);
            sb.append(javaCommand);
            sb.append(" ").append(newXms).append(" ").append(newXmx);
            if (StringUtils.isNotEmpty(otherArgs)) sb.append(" ").append(otherArgs.trim());
            sb.append(" ");
            sb.append(script.substring(insertPos));
            script = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotEmpty(javaPath)) {
                sb.append(match ? "\"" + javaPath + "\"" : javaPath);
            } else {
                sb.append("java");
            }
            sb.append(" ").append(newXms).append(" ").append(newXmx);
            if (StringUtils.isNotEmpty(otherArgs)) sb.append(" ").append(otherArgs.trim());
            sb.append(" ").append(script);
            script = sb.toString();
        }

        return script.replaceAll("\\s+", " ").trim();
    }
}
