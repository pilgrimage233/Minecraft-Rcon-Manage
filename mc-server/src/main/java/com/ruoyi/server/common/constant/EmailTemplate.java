package com.ruoyi.server.common.constant;

/**
 * ClassName: EmailTemplate <br>
 * Description:
 * date: 2024/1/13 16:27 <br>
 *
 * @author zhangchanggeng <br>
 * @since JDK 1.8
 */
public class EmailTemplate {
    public static final String TITLE = "白名单审核结果通知";

    public static final String SUCCESS_CONTENT = "您好：%s，您的白名单已于：%s 日通过审核，审核人：%s 请您遵守相关规定，祝您游戏愉快！";

    public static final String FAIL_CONTENT = "您好：%s，您的白名单已于：%s 日未通过审核，审核人：%s 原因：%s";

    public static final String REMOVED_CONTENT = "您好：%s，您的白名单已于：%s 日被移除，移除人：%s 原因：%s";

    public static final String SUCCESS_TITLE = "白名单审核通过通知";

    public static final String FAIL_TITLE = "白名单审核未通过通知";

    public static final String APPLY_SUCCESS = "提交申请成功！请留意填写信息的QQ邮箱，如审核通过会发送邮件或可以二次提交重复信息查看审核状态~";

    public static final String APPLY_ERROR = "提交申请错误,请联系管理员!";

    public static final String QQ_EMAIL = "@qq.com";

}
