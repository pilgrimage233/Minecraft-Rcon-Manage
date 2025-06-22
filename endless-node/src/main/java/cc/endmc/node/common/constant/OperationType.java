package cc.endmc.node.common.constant;

/**
 * ClassName: OperationType <br>
 * Description:
 * date: 2025/6/22 09:12 <br>
 *
 * @author Memory <br>
 * @since JDK 1.8
 */
public class OperationType {

    public static final String ADD_NODE = "1"; // 新增节点
    public static final String UPDATE_NODE = "2"; // 修改节点
    public static final String DELETE_NODE = "3"; // 删除节点
    public static final String DOWNLOAD_LOG = "4"; // 下载日志
    public static final String START_GAME_SERVER = "5"; // 启动游戏服务器
    public static final String STOP_GAME_SERVER = "6"; // 停止游戏服务器
    public static final String RESTART_GAME_SERVER = "7"; // 重启游戏服务器
    public static final String FORCE_TERMINATE_GAME_SERVER = "8"; // 强制终止游戏服务器
    public static final String ADD_GAME_SERVER = "9"; // 新增游戏服务器
    public static final String UPDATE_GAME_SERVER = "10"; // 修改游戏服务器
    public static final String DELETE_GAME_SERVER = "11"; // 删除游戏服务器
}
