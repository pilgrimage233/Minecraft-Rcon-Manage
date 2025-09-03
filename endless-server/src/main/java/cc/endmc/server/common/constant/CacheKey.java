package cc.endmc.server.common.constant;

/**
 * 缓存键常量
 *
 * @author Memory
 */
public class CacheKey {

    public static final String SERVER_MANAGER_KEY = "server_manager:"; // 服务器管理总前缀

    public static final String ERROR_COUNT_KEY = "server_manager:error_count"; // 登录错误次数前缀

    public static final String SERVER_INFO_KEY = "server_manager:server_info"; // 服务器信息前缀

    public static final String SERVER_INFO_MAP_KEY = "server_manager:server_info_map"; // 服务器信息前缀

    public static final String SERVER_INFO_UPDATE_TIME_KEY = "server_manager:server_info_updateTime"; // 服务器信息更新时间前缀

    public static final String COMMAND_INFO_KEY = "server_manager:command_info"; // 指令信息前缀

    public static final String ERROR_COMMAND_CACHE_KEY = "server_manager:error_command_cache"; // 错误指令缓存前缀

    public static final String ONLINE_PLAYER_KEY = "server_manager:online_player"; // 在线玩家前缀

    public static final String SERVER_PLAYER_KEY = "server_manager:server_player:"; // 服务器玩家前缀

    public static final String WHITE_LIST_KEY = "server_manager:white_list"; // 白名单前缀

    public static final String VERIFICATION_KEY = "verification:"; // 通用验证码前缀

    public static final String VERIFY_KEY = "server_manager:verify:"; // 通用验证码前缀

    public static final String VERIFY_FOR_BOT_KEY = "server_manager:verify:bot:"; // 机器人验证码前缀

    public static final String VERIFY_FOR_BATCH_KEY = "server_manager:verify:batch:"; // 批量操作验证码前缀

    public static final String COMMAND_USE_KEY = "server_manager:command:use:"; // 指令使用前缀

    public static final String NODE_SERVER_MAP_KEY = "server_manager:node:server_map:"; // 节点服务器映射前缀

    public static final String PASS_KEY = "server_manager:pass:"; // 通用密码前缀

    public static final String UPDATE_CHECK_KEY = "update_check:"; // 更新检查前缀

    public static final String QUIZ_SUBMISSION_KEY = "quiz_submission:"; // 答题记录前缀

    public static final String QUIZ_SUBMISSION_DETAIL_KEY = "quiz_submission:detail:"; // 答题详情前缀

    public static final String PLAYER_INFO_KEY = "player_info:"; // 玩家信息前缀
}
