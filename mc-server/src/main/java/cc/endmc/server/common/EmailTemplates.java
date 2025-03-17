package cc.endmc.server.common;

import cc.endmc.common.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EmailTemplates {

    public static final String TITLE = "白名单审核通知";

    public static final String SUCCESS_CONTENT = "您好：%s，您的白名单已于：%s 日通过审核，审核人：%s 请您遵守相关规定，祝您游戏愉快！";

    public static final String FAIL_CONTENT = "您好：%s，您的白名单已于：%s 日未通过审核，审核人：%s 原因：%s";

    public static final String REMOVED_CONTENT = "您好：%s，您的白名单已于：%s 日被移除，移除人：%s 原因：%s";

    public static final String SUCCESS_TITLE = "白名单审核通过";

    public static final String FAIL_TITLE = "白名单审核未通过";

    public static final String REMOVE_TITLE = "白名单移除通知";

    public static final String BAN_TITLE = "封禁通知";

    public static final String UN_BAN_TITLE = "解禁通知";

    public static final String APPLY_SUCCESS = "提交申请成功！请留意填写信息的QQ邮箱，如审核通过会发送邮件或可以二次提交重复信息查看审核状态~";

    public static final String APPLY_ERROR = "提交申请错误,请联系管理员!";

    public static final String REMOVE_REASON = "破坏游戏环境！";

    public static final String BAN_TIME_TITTLE = "封禁时间：";

    public static final String UN_BAN_TIME_TITTLE = "解禁时间：";

    public static final String REMOVE_TIME_TITTLE = "移除时间：";

    public static final String FAIL_TIME_TITTLE = "拒审时间：";

    public static final String QQ_EMAIL = "@qq.com";

    public static final String EMAIL_VERIFY_TITLE = "邮箱验证";

    public static final String EMAIL_VERIFY_TEMPLATE = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>邮箱验证</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "            font-family: 'Helvetica Neue', Arial, sans-serif;\n" +
            "            background-color: #f5f5f5;\n" +
            "        }\n" +
            "        .container {\n" +
            "            max-width: 600px;\n" +
            "            margin: 20px auto;\n" +
            "            background: #ffffff;\n" +
            "            border-radius: 16px;\n" +
            "            overflow: hidden;\n" +
            "            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);\n" +
            "        }\n" +
            "        .header {\n" +
            "            background: linear-gradient(135deg, #1e2f45, #2c3e50);\n" +
            "            color: white;\n" +
            "            padding: 30px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .header h1 {\n" +
            "            margin: 0;\n" +
            "            font-size: 24px;\n" +
            "            font-weight: 600;\n" +
            "        }\n" +
            "        .content {\n" +
            "            padding: 30px;\n" +
            "            color: #2c3e50;\n" +
            "        }\n" +
            "        .verify-button {\n" +
            "            display: block;\n" +
            "            width: 200px;\n" +
            "            margin: 30px auto;\n" +
            "            padding: 15px 25px;\n" +
            "            background: #409EFF;\n" +
            "            color: white;\n" +
            "            text-decoration: none;\n" +
            "            text-align: center;\n" +
            "            border-radius: 8px;\n" +
            "            font-weight: 500;\n" +
            "            font-size: 16px;\n" +
            "            transition: all 0.3s ease;\n" +
            "        }\n" +
            "        .verify-button:hover {\n" +
            "            background: #66b1ff;\n" +
            "            transform: translateY(-2px);\n" +
            "        }\n" +
            "        .verify-code {\n" +
            "            text-align: center;\n" +
            "            font-size: 24px;\n" +
            "            font-weight: bold;\n" +
            "            color: #409EFF;\n" +
            "            margin: 20px 0;\n" +
            "            letter-spacing: 5px;\n" +
            "        }\n" +
            "        .footer {\n" +
            "            background: #f8f9fa;\n" +
            "            padding: 20px;\n" +
            "            text-align: center;\n" +
            "            color: #909399;\n" +
            "            font-size: 14px;\n" +
            "        }\n" +
            "        .warning {\n" +
            "            color: #E6A23C;\n" +
            "            font-size: 14px;\n" +
            "            margin-top: 20px;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>邮箱验证</h1>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"content\">\n" +
            "            <p>亲爱的用户：</p>\n" +
            "            <p>您好！感谢您游玩我们的服务器。请点击下面的按钮验证您的邮箱：</p>\n" +
            "            \n" +
            "            <a href=\"{verifyLink}\" class=\"verify-button\">验证邮箱</a>\n" +
            "            \n" +
            "            <p>如果按钮无法点击，请复制以下链接到浏览器地址栏访问：</p>\n" +
            "            <p style=\"word-break: break-all; color: #409EFF;\">{verifyLink}</p>\n" +
            "            \n" +
            "            <p class=\"warning\">注意：该验证链接将在30分钟后失效，请尽快完成验证。</p>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"footer\">\n" +
            "            <p>此邮件由系统自动发送，请勿回复</p>\n" +
            "            <p>© 2024 Minecraft. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";

    public static final String WHITELIST_NOTIFICATION_TEMPLATE = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>白名单审核通知</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "            font-family: 'Helvetica Neue', Arial, sans-serif;\n" +
            "            background-color: #f5f5f5;\n" +
            "        }\n" +
            "        .container {\n" +
            "            max-width: 600px;\n" +
            "            margin: 20px auto;\n" +
            "            background: #ffffff;\n" +
            "            border-radius: 16px;\n" +
            "            overflow: hidden;\n" +
            "            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);\n" +
            "        }\n" +
            "        .header {\n" +
            "            background: linear-gradient(135deg, #1e2f45, #2c3e50);\n" +
            "            color: white;\n" +
            "            padding: 30px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .header h1 {\n" +
            "            margin: 0;\n" +
            "            font-size: 24px;\n" +
            "            font-weight: 600;\n" +
            "        }\n" +
            "        .content {\n" +
            "            padding: 30px;\n" +
            "            color: #2c3e50;\n" +
            "        }\n" +
            "        .status {\n" +
            "            text-align: center;\n" +
            "            margin: 20px 0;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 8px;\n" +
            "            font-weight: 600;\n" +
            "            font-size: 18px;\n" +
            "        }\n" +
            "        .status.approved {\n" +
            "            background-color: #f0f9eb;\n" +
            "            color: #67C23A;\n" +
            "            border: 1px solid #e1f3d8;\n" +
            "        }\n" +
            "        .status.rejected {\n" +
            "            background-color: #fef0f0;\n" +
            "            color: #f56c6c;\n" +
            "            border: 1px solid #fde2e2;\n" +
            "        }\n" +
            "        .info-box {\n" +
            "            background: #f8f9fa;\n" +
            "            border-radius: 8px;\n" +
            "            padding: 20px;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        .info-item {\n" +
            "            margin: 10px 0;\n" +
            "            display: flex;\n" +
            "            justify-content: space-between;\n" +
            "        }\n" +
            "        .info-label {\n" +
            "            color: #606266;\n" +
            "            font-weight: 500;\n" +
            "        }\n" +
            "        .info-value {\n" +
            "            color: #409EFF;\n" +
            "            font-weight: 500;\n" +
            "        }\n" +
            "        .footer {\n" +
            "            background: #f8f9fa;\n" +
            "            padding: 20px;\n" +
            "            text-align: center;\n" +
            "            color: #909399;\n" +
            "            font-size: 14px;\n" +
            "        }\n" +
            "        .button {\n" +
            "            display: inline-block;\n" +
            "            padding: 12px 24px;\n" +
            "            background: #409EFF;\n" +
            "            color: white;\n" +
            "            text-decoration: none;\n" +
            "            border-radius: 24px;\n" +
            "            margin: 20px 0;\n" +
            "            font-weight: 500;\n" +
            "            transition: all 0.3s ease;\n" +
            "        }\n" +
            "        .button:hover {\n" +
            "            background: #66b1ff;\n" +
            "            transform: translateY(-2px);\n" +
            "        }\n" +
            "        .minecraft-style {\n" +
            "            font-family: 'Minecraft', 'Helvetica Neue', Arial, sans-serif;\n" +
            "            text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.2);\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1 class=\"minecraft-style\">✨ 白名单审核通知 ✨</h1>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"content\">\n" +
            "            <p>亲爱的 {username}：</p>\n" +
            "            <!-- 审核通过模板 -->\n" +
            "            <!-- 审核拒绝模板 -->\n" +
            "            <div class=\"info-box\">\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">游戏ID：</span>\n" +
            "                    <span class=\"info-value\">{gameId}</span>\n" +
            "                </div>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">申请时间：</span>\n" +
            "                    <span class=\"info-value\">{applyTime}</span>\n" +
            "                </div>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">审核时间：</span>\n" +
            "                    <span class=\"info-value\">{reviewTime}</span>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "\n" +
            "            <!-- 通过时显示 -->\n" +
            "            <p>服务器信息：</p>\n" +
            "            <div class=\"info-box\">\n" +
            "                   {info}                    " +
            "            </div>\n" +
            "            \n" +
            "            <p>温馨提示：</p>\n" +
            "            <ul>\n" +
            "                <li>请遵守服务器规则，与其他玩家和谐共处</li>\n" +
            "                <li>如遇问题可以联系管理员寻求帮助</li>\n" +
            "                <li>祝您游戏愉快！</li>\n" +
            "            </ul>\n" +
            "\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"footer\">\n" +
            "            <p>此邮件由系统自动发送，请勿回复</p>\n" +
            "            <p>© 2024 Minecraft. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";


    public static final String WHITELIST_NOTIFICATION_TEMPLATE_BAN = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>白名单移除通知</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "            font-family: 'Helvetica Neue', Arial, sans-serif;\n" +
            "            background-color: #f5f5f5;\n" +
            "        }\n" +
            "        .container {\n" +
            "            max-width: 600px;\n" +
            "            margin: 20px auto;\n" +
            "            background: #ffffff;\n" +
            "            border-radius: 16px;\n" +
            "            overflow: hidden;\n" +
            "            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);\n" +
            "        }\n" +
            "        .header {\n" +
            "            background: linear-gradient(135deg, #1e2f45, #2c3e50);\n" +
            "            color: white;\n" +
            "            padding: 30px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .header h1 {\n" +
            "            margin: 0;\n" +
            "            font-size: 24px;\n" +
            "            font-weight: 600;\n" +
            "        }\n" +
            "        .content {\n" +
            "            padding: 30px;\n" +
            "            color: #2c3e50;\n" +
            "        }\n" +
            "        .status {\n" +
            "            text-align: center;\n" +
            "            margin: 20px 0;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 8px;\n" +
            "            font-weight: 600;\n" +
            "            font-size: 18px;\n" +
            "        }\n" +
            "        .status.approved {\n" +
            "            background-color: #f0f9eb;\n" +
            "            color: #67C23A;\n" +
            "            border: 1px solid #e1f3d8;\n" +
            "        }\n" +
            "        .status.rejected {\n" +
            "            background-color: #fef0f0;\n" +
            "            color: #f56c6c;\n" +
            "            border: 1px solid #fde2e2;\n" +
            "        }\n" +
            "        .info-box {\n" +
            "            background: #f8f9fa;\n" +
            "            border-radius: 8px;\n" +
            "            padding: 20px;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        .info-item {\n" +
            "            margin: 10px 0;\n" +
            "            display: flex;\n" +
            "            justify-content: space-between;\n" +
            "        }\n" +
            "        .info-label {\n" +
            "            color: #606266;\n" +
            "            font-weight: 500;\n" +
            "        }\n" +
            "        .info-value {\n" +
            "            color: #409EFF;\n" +
            "            font-weight: 500;\n" +
            "        }\n" +
            "        .footer {\n" +
            "            background: #f8f9fa;\n" +
            "            padding: 20px;\n" +
            "            text-align: center;\n" +
            "            color: #909399;\n" +
            "            font-size: 14px;\n" +
            "        }\n" +
            "        .button {\n" +
            "            display: inline-block;\n" +
            "            padding: 12px 24px;\n" +
            "            background: #409EFF;\n" +
            "            color: white;\n" +
            "            text-decoration: none;\n" +
            "            border-radius: 24px;\n" +
            "            margin: 20px 0;\n" +
            "            font-weight: 500;\n" +
            "            transition: all 0.3s ease;\n" +
            "        }\n" +
            "        .button:hover {\n" +
            "            background: #66b1ff;\n" +
            "            transform: translateY(-2px);\n" +
            "        }\n" +
            "        .minecraft-style {\n" +
            "            font-family: 'Minecraft', 'Helvetica Neue', Arial, sans-serif;\n" +
            "            text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.2);\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1 class=\"minecraft-style\">🚫 白名单移除通知 🚫</h1>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"content\">\n" +
            "            <p>你好 {username}：</p>\n" +
            "            \n" +
            "            <!-- 移除模板 -->\n" +
            "            <!-- 解禁模板 -->\n" +
            "            <!-- 封禁模板 -->\n" +
            "            \n" +
            "            <div class=\"info-box\">\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">游戏ID：</span>\n" +
            "                    <span class=\"info-value\">{gameId}</span>\n" +
            "                </div>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">申请时间：</span>\n" +
            "                    <span class=\"info-value\">{applyTime}</span>\n" +
            "                </div>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">{timeTittle}：</span>\n" +
            "                    <span class=\"info-value\">{time}</span>\n" +
            "                </div>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">原因：</span>\n" +
            "                    <span class=\"info-value\">{removeReason}</span>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "\n" +
            "            <p>温馨提示：</p>\n" +
            "            <ul>\n" +
            "                <li>请遵守服务器规则，与其他玩家和谐共处</li>\n" +
            "                <li>如遇问题可以联系管理员寻求帮助</li>\n" +
            "            </ul>\n" +
            "\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"footer\">\n" +
            "            <p>此邮件由系统自动发送，请勿回复</p>\n" +
            "            <p>© 2024 Minecraft. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";

    public static final String WHITELIST_NOTIFICATION_TEMPLATE_UNBAN = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>封禁解除通知</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "            font-family: 'Helvetica Neue', Arial, sans-serif;\n" +
            "            background-color: #f5f5f5;\n" +
            "        }\n" +
            "        .container {\n" +
            "            max-width: 600px;\n" +
            "            margin: 20px auto;\n" +
            "            background: #ffffff;\n" +
            "            border-radius: 16px;\n" +
            "            overflow: hidden;\n" +
            "            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);\n" +
            "        }\n" +
            "        .header {\n" +
            "            background: linear-gradient(135deg, #1e2f45, #2c3e50);\n" +
            "            color: white;\n" +
            "            padding: 30px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .header h1 {\n" +
            "            margin: 0;\n" +
            "            font-size: 24px;\n" +
            "            font-weight: 600;\n" +
            "        }\n" +
            "        .content {\n" +
            "            padding: 30px;\n" +
            "            color: #2c3e50;\n" +
            "        }\n" +
            "        .status {\n" +
            "            text-align: center;\n" +
            "            margin: 20px 0;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 8px;\n" +
            "            font-weight: 600;\n" +
            "            font-size: 18px;\n" +
            "        }\n" +
            "        .status.approved {\n" +
            "            background-color: #f0f9eb;\n" +
            "            color: #67C23A;\n" +
            "            border: 1px solid #e1f3d8;\n" +
            "        }\n" +
            "        .status.rejected {\n" +
            "            background-color: #fef0f0;\n" +
            "            color: #f56c6c;\n" +
            "            border: 1px solid #fde2e2;\n" +
            "        }\n" +
            "        .info-box {\n" +
            "            background: #f8f9fa;\n" +
            "            border-radius: 8px;\n" +
            "            padding: 20px;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        .info-item {\n" +
            "            margin: 10px 0;\n" +
            "            display: flex;\n" +
            "            justify-content: space-between;\n" +
            "        }\n" +
            "        .info-label {\n" +
            "            color: #606266;\n" +
            "            font-weight: 500;\n" +
            "        }\n" +
            "        .info-value {\n" +
            "            color: #409EFF;\n" +
            "            font-weight: 500;\n" +
            "        }\n" +
            "        .footer {\n" +
            "            background: #f8f9fa;\n" +
            "            padding: 20px;\n" +
            "            text-align: center;\n" +
            "            color: #909399;\n" +
            "            font-size: 14px;\n" +
            "        }\n" +
            "        .button {\n" +
            "            display: inline-block;\n" +
            "            padding: 12px 24px;\n" +
            "            background: #409EFF;\n" +
            "            color: white;\n" +
            "            text-decoration: none;\n" +
            "            border-radius: 24px;\n" +
            "            margin: 20px 0;\n" +
            "            font-weight: 500;\n" +
            "            transition: all 0.3s ease;\n" +
            "        }\n" +
            "        .button:hover {\n" +
            "            background: #66b1ff;\n" +
            "            transform: translateY(-2px);\n" +
            "        }\n" +
            "        .minecraft-style {\n" +
            "            font-family: 'Minecraft', 'Helvetica Neue', Arial, sans-serif;\n" +
            "            text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.2);\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1 class=\"minecraft-style\">🎉 封禁解除通知 🎉</h1>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"content\">\n" +
            "            <p>你好 {username}：</p>\n" +
            "            \n" +
            "            <!-- 解禁模板 -->\n" +
            "            \n" +
            "            <div class=\"info-box\">\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">游戏ID：</span>\n" +
            "                    <span class=\"info-value\">{gameId}</span>\n" +
            "                </div>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">封禁时间：</span>\n" +
            "                    <span class=\"info-value\">{banTime}</span>\n" +
            "                </div>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">解禁时间：</span>\n" +
            "                    <span class=\"info-value\">{unBanTime}</span>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "\n" +
            "            <p>温馨提示：</p>\n" +
            "            <ul>\n" +
            "                <li>请遵守服务器规则，与其他玩家和谐共处</li>\n" +
            "                <li>如遇问题可以联系管理员寻求帮助</li>\n" +
            "            </ul>\n" +
            "\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"footer\">\n" +
            "            <p>此邮件由系统自动发送，请勿回复</p>\n" +
            "            <p>© 2024 Minecraft. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";

    // 待审核
    public static final String WHITELIST_NOTIFICATION_TEMPLATE_PENDING = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>白名单已提交</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "            font-family: 'Helvetica Neue', Arial, sans-serif;\n" +
            "            background-color: #f5f5f5;\n" +
            "        }\n" +
            "        .container {\n" +
            "            max-width: 600px;\n" +
            "            margin: 20px auto;\n" +
            "            background: #ffffff;\n" +
            "            border-radius: 16px;\n" +
            "            overflow: hidden;\n" +
            "            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);\n" +
            "        }\n" +
            "        .header {\n" +
            "            background: linear-gradient(135deg, #1e2f45, #2c3e50);\n" +
            "            color: white;\n" +
            "            padding: 30px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .header h1 {\n" +
            "            margin: 0;\n" +
            "            font-size: 24px;\n" +
            "            font-weight: 600;\n" +
            "        }\n" +
            "        .content {\n" +
            "            padding: 30px;\n" +
            "            color: #2c3e50;\n" +
            "        }\n" +
            "        .status {\n" +
            "            text-align: center;\n" +
            "            margin: 20px 0;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 8px;\n" +
            "            font-weight: 600;\n" +
            "            font-size: 18px;\n" +
            "        }\n" +
            "        .status.approved {\n" +
            "            background-color: #f0f9eb;\n" +
            "            color: #67C23A;\n" +
            "            border: 1px solid #e1f3d8;\n" +
            "        }\n" +
            "        .status.rejected {\n" +
            "            background-color: #fef0f0;\n" +
            "            color: #f56c6c;\n" +
            "            border: 1px solid #fde2e2;\n" +
            "        }\n" +
            "        .info-box {\n" +
            "            background: #f8f9fa;\n" +
            "            border-radius: 8px;\n" +
            "            padding: 20px;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        .info-item {\n" +
            "            margin: 10px 0;\n" +
            "            display: flex;\n" +
            "            justify-content: space-between;\n" +
            "        }\n" +
            "        .info-label {\n" +
            "            color: #606266;\n" +
            "            font-weight: 500;\n" +
            "        }\n" +
            "        .info-value {\n" +
            "            color: #409EFF;\n" +
            "            font-weight: 500;\n" +
            "        }\n" +
            "        .footer {\n" +
            "            background: #f8f9fa;\n" +
            "            padding: 20px;\n" +
            "            text-align: center;\n" +
            "            color: #909399;\n" +
            "            font-size: 14px;\n" +
            "        }\n" +
            "        .button {\n" +
            "            display: inline-block;\n" +
            "            padding: 12px 24px;\n" +
            "            background: #409EFF;\n" +
            "            color: white;\n" +
            "            text-decoration: none;\n" +
            "            border-radius: 24px;\n" +
            "            margin: 20px 0;\n" +
            "            font-weight: 500;\n" +
            "            transition: all 0.3s ease;\n" +
            "        }\n" +
            "        .button:hover {\n" +
            "            background: #66b1ff;\n" +
            "            transform: translateY(-2px);\n" +
            "        }\n" +
            "        .minecraft-style {\n" +
            "            font-family: 'Minecraft', 'Helvetica Neue', Arial, sans-serif;\n" +
            "            text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.2);\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1 class=\"minecraft-style\">🎉 您的白名单已成功提交 🎉</h1>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"content\">\n" +
            "            <p>你好 {username}：</p>\n" +
            "            \n" +
            "            <!-- 提交模板 -->\n" +
            "            \n" +
            "            <div class=\"info-box\">\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">游戏ID：</span>\n" +
            "                    <span class=\"info-value\">{gameId}</span>\n" +
            "                </div>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">提交时间：</span>\n" +
            "                    <span class=\"info-value\">{applyTime}</span>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "\n" +
            "            <p>温馨提示：</p>\n" +
            "            <ul>\n" +
            "                <li>如遇问题可以联系管理员寻求帮助</li>\n" +
            "            </ul>\n" +
            "\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"footer\">\n" +
            "            <p>此邮件由系统自动发送，请勿回复</p>\n" +
            "            <p>© 2024 Minecraft. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";

    public static final String ALERT_TITLE = "系统异常告警";

    public static final String ALERT_TEMPLATE = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>系统异常告警</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "            font-family: 'Helvetica Neue', Arial, sans-serif;\n" +
            "            background-color: #f5f5f5;\n" +
            "        }\n" +
            "        .container {\n" +
            "            max-width: 600px;\n" +
            "            margin: 20px auto;\n" +
            "            background: #ffffff;\n" +
            "            border-radius: 16px;\n" +
            "            overflow: hidden;\n" +
            "            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);\n" +
            "        }\n" +
            "        .header {\n" +
            "            background: linear-gradient(135deg, #ff4d4d, #ff1a1a);\n" +
            "            color: white;\n" +
            "            padding: 30px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .header h1 {\n" +
            "            margin: 0;\n" +
            "            font-size: 24px;\n" +
            "            font-weight: 600;\n" +
            "        }\n" +
            "        .content {\n" +
            "            padding: 30px;\n" +
            "            color: #2c3e50;\n" +
            "        }\n" +
            "        .alert-info {\n" +
            "            background: #fff3f3;\n" +
            "            border-left: 4px solid #ff4d4d;\n" +
            "            padding: 15px;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        .info-box {\n" +
            "            background: #f8f9fa;\n" +
            "            border-radius: 8px;\n" +
            "            padding: 20px;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        .info-item {\n" +
            "            margin: 10px 0;\n" +
            "            display: flex;\n" +
            "            align-items: center;\n" +
            "        }\n" +
            "        .info-label {\n" +
            "            font-weight: 600;\n" +
            "            min-width: 120px;\n" +
            "            color: #666;\n" +
            "        }\n" +
            "        .info-value {\n" +
            "            color: #333;\n" +
            "        }\n" +
            "        .footer {\n" +
            "            text-align: center;\n" +
            "            padding: 20px;\n" +
            "            background: #f8f9fa;\n" +
            "            color: #666;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>系统异常告警</h1>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"content\">\n" +
            "            <div class=\"alert-info\">\n" +
            "                <p>系统检测到异常情况，请及时处理！</p>\n" +
            "            </div>\n" +
            "            \n" +
            "            <div class=\"info-box\">\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">异常时间：</span>\n" +
            "                    <span class=\"info-value\">{time}</span>\n" +
            "                </div>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">异常次数：</span>\n" +
            "                    <span class=\"info-value\">{count}</span>\n" +
            "                </div>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">异常类型：</span>\n" +
            "                    <span class=\"info-value\">{type}</span>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "            \n" +
            "            <div class=\"info-box\">\n" +
            "                <h3>服务器信息</h3>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">服务器名称：</span>\n" +
            "                    <span class=\"info-value\">{serverName}</span>\n" +
            "                </div>\n" +
            "                <div class=\"info-item\">\n" +
            "                    <span class=\"info-label\">服务器地址：</span>\n" +
            "                    <span class=\"info-value\">{serverAddress}</span>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "            \n" +
            "            <p>温馨提示：</p>\n" +
            "            <ul>\n" +
            "                <li>请及时检查系统运行状态</li>\n" +
            "                <li>如遇问题可以联系系统管理员</li>\n" +
            "            </ul>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"footer\">\n" +
            "            <p>此邮件由系统自动发送，请勿回复</p>\n" +
            "            <p>© 2024 Minecraft. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";

    // 替换模板中的变量
    public static String getWhitelistNotification(String username, String gameId, String applyTime,
                                                  String reviewTime, String titte, String appUrl, List<Map<String, Object>> infoList) {
        String template = WHITELIST_NOTIFICATION_TEMPLATE;

        // 定义单个服务器信息的HTML模板
        String serverInfoTemplate =
                "<div class=\"server-info-block\" style=\"margin-bottom: 20px; padding: 15px; background: #f8f9fa; border-radius: 8px; border: 1px solid #e9ecef;\">\n" +
                        "    <div class=\"info-item\">\n" +
                        "        <span class=\"info-label\">服务器名称：</span>\n" +
                        "        <span class=\"info-value\">{name}</span>\n" +
                        "    </div>\n" +
                        "    <div class=\"info-item\">\n" +
                        "        <span class=\"info-label\">服务器地址：</span>\n" +
                        "        <span class=\"info-value\">{serverAddress}:{port}</span>\n" +
                        "    </div>\n" +
                        "    <div class=\"info-item\">\n" +
                        "        <span class=\"info-label\">游戏版本：</span>\n" +
                        "        <span class=\"info-value\">{core}-{version}</span>\n" +
                        "    </div>\n" +
                        "</div>";

        // 替换基本变量
        template = template.replace("{username}", username)
                .replace("{gameId}", gameId)
                .replace("{applyTime}", applyTime)
                .replace("{reviewTime}", reviewTime);

        // 处理服务器信息
        if (infoList == null || infoList.isEmpty()) {
            // 如果没有服务器信息，提供链接查看
            String URL = appUrl + "/#/player-servers/" + gameId;
            String linkHtml = "<div style='text-align: center;'>" +
                    "<a href=\"" + URL + "\" class=\"button\" " +
                    "style=\"display: inline-block; padding: 12px 24px; background: #409EFF; " +
                    "color: white; text-decoration: none; border-radius: 24px; margin: 20px 0; " +
                    "font-weight: 500; transition: all 0.3s ease; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);\" " +
                    "onmouseover=\"this.style.background='#66b1ff'; this.style.transform='translateY(-2px)';\" " +
                    "onmouseout=\"this.style.background='#409EFF'; this.style.transform='translateY(0)';\">" +
                    "查看服务器信息</a></div>";
            template = template.replace("{info}", linkHtml);
        } else {
            // 有服务器信息，生成服务器信息HTML
            StringBuilder serverInfoHtml = new StringBuilder();
            for (Map<String, Object> infoMap : infoList) {
                String serverInfo = serverInfoTemplate;
                serverInfo = serverInfo.replace("{name}", (String) infoMap.get("name"))
                        .replace("{serverAddress}", (String) infoMap.get("serverAddress"))
                        .replace("{port}", String.valueOf(infoMap.get("port")))
                        .replace("{core}", (String) infoMap.get("core"))
                        .replace("{version}", (String) infoMap.get("version"));
                serverInfoHtml.append(serverInfo);
            }
            template = template.replace("{info}", serverInfoHtml.toString());
        }

        // 根据审核结果显示不同状态
        switch (titte) {
            case FAIL_TITLE:
                template = template.replace(
                        "<!-- 审核拒绝模板 -->",
                        "<div class=\"status rejected\"><span>😢 很抱歉，您的白名单申请未通过</span></div>"
                ).replace(
                        "<!-- 审核通过模板 -->",
                        ""
                );
                break;
            case SUCCESS_TITLE:
                template = template.replace(
                        "<!-- 审核通过模板 -->",
                        "<div class=\"status approved\"><span>🎉 恭喜，您的白名单申请已通过！</span></div>"
                ).replace(
                        "<!-- 审核拒绝模板 -->",
                        ""
                );
                break;
        }
        return template;
    }

    public static String getWhitelistNotificationBan(String username, String gameId, String applyTime,
                                                     String time, String timeTittle, String removeReason, String titte) {
        String template = WHITELIST_NOTIFICATION_TEMPLATE_BAN;

        if (StringUtils.isEmpty(removeReason)) {
            removeReason = REMOVE_REASON;
        }

        // 替换变量
        template = template.replace("{username}", username)
                .replace("{gameId}", gameId)
                .replace("{applyTime}", applyTime)
                .replace("{timeTittle}", timeTittle)
                .replace("{time}", time)
                .replace("{removeReason}", removeReason);

        // 根据审核结果显示不同状态
        switch (titte) {
            case BAN_TITLE:
                template = template.replace(
                        "<!-- 封禁模板 -->",
                        "<div class=\"status rejected\"><span>🚫 你已被封禁！🚫</span></div>"
                ).replace(
                        "<!-- 移除模板 -->",
                        ""
                );
                break;
            case REMOVE_TITLE:
                template = template.replace(
                        "<!-- 移除模板 -->",
                        "<div class=\"status rejected\"><span>😢 很抱歉，您的白名单已被移除</span></div>"
                ).replace(
                        "<!-- 封禁模板 -->",
                        ""
                );
                break;
            case FAIL_TITLE:
                template = template.replace(
                        "<!-- 移除模板 -->",
                        "<div class=\"status rejected\"><span>😢 很抱歉，您的白名单申请未通过</span></div>"
                ).replace(
                        "<!-- 封禁模板 -->",
                        ""
                );
                break;
        }
        return template;
    }

    public static String getWhitelistNotificationUnBan(String username, String gameId, String banTime, String unBanTime) {
        String template = WHITELIST_NOTIFICATION_TEMPLATE_UNBAN;

        // 替换变量
        template = template.replace("{username}", username)
                .replace("{gameId}", gameId)
                .replace("{banTime}", banTime)
                .replace("{unBanTime}", unBanTime);

        // 模板替换
        template = template.replace(
                "<!-- 解禁模板 -->",
                "<div class=\"status approved\"><span>🎉 恭喜，您的封禁已解除！</span></div>"
        );

        return template;
    }

    public static String getWhitelistNotificationPending(String username, String gameId, String applyTime) {
        String template = WHITELIST_NOTIFICATION_TEMPLATE_PENDING;

        // 替换变量
        template = template.replace("{username}", username)
                .replace("{gameId}", gameId)
                .replace("{applyTime}", applyTime);

        // 模板
        template = template.replace(
                "<!-- 提交模板 -->",
                "<div class=\"status approved\"><span> 您的白名单已成功提交 </span></div>"
        ).replace(
                "<!-- 提交模板 -->",
                ""
        );

        return template;
    }

    // 获取邮箱验证模板
    public static String getEmailVerifyTemplate(String verifyLink) {
        return EMAIL_VERIFY_TEMPLATE.replace("{verifyLink}", verifyLink);
    }

    public static String getAlertNotification(String time, int count, String type,
                                              String serverName, String serverAddress) {
        String template = ALERT_TEMPLATE;

        // 替换变量
        template = template.replace("{time}", time)
                .replace("{count}", String.valueOf(count))
                .replace("{type}", type)
                .replace("{serverName}", serverName)
                .replace("{serverAddress}", serverAddress);

        return template;
    }
}