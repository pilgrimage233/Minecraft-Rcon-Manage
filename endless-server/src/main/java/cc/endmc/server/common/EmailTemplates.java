package cc.endmc.server.common;

import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.cache.EmailTempCache;
import cc.endmc.server.domain.email.CustomEmailTemplates;
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

    public static final String WHITELIST_LOGIN_CODE_TITLE = "白名单登录验证码";

    public static final String EMAIL_VERIFY_TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>邮箱验证</title>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: 'Helvetica Neue', Arial, sans-serif;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 16px;
                        overflow: hidden;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #1e2f45, #2c3e50);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 30px;
                        color: #2c3e50;
                    }
                    .verify-button {
                        display: block;
                        width: 200px;
                        margin: 30px auto;
                        padding: 15px 25px;
                        background: #409EFF;
                        color: white;
                        text-decoration: none;
                        text-align: center;
                        border-radius: 8px;
                        font-weight: 500;
                        font-size: 16px;
                        transition: all 0.3s ease;
                    }
                    .verify-button:hover {
                        background: #66b1ff;
                        transform: translateY(-2px);
                    }
                    .verify-code {
                        text-align: center;
                        font-size: 24px;
                        font-weight: bold;
                        color: #409EFF;
                        margin: 20px 0;
                        letter-spacing: 5px;
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        color: #909399;
                        font-size: 14px;
                    }
                    .warning {
                        color: #E6A23C;
                        font-size: 14px;
                        margin-top: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>邮箱验证</h1>
                    </div>
                   \s
                    <div class="content">
                        <p>亲爱的用户：</p>
                        <p>您好！感谢您游玩我们的服务器。请点击下面的按钮验证您的邮箱：</p>
                       \s
                        <a href="{verifyLink}" class="verify-button">验证邮箱</a>
                       \s
                        <p>如果按钮无法点击，请复制以下链接到浏览器地址栏访问：</p>
                        <p style="word-break: break-all; color: #409EFF;">{verifyLink}</p>
                       \s
                        <p class="warning">注意：该验证链接将在30分钟后失效，请尽快完成验证。</p>
                    </div>
                   \s
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿回复</p>
                        <p>© 2025 Minecraft. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>""";

    public static final String WHITELIST_LOGIN_CODE_TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>白名单登录验证码</title>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: 'Helvetica Neue', Arial, sans-serif;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 16px;
                        overflow: hidden;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #1e2f45, #2c3e50);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 30px;
                        color: #2c3e50;
                    }
                    .verify-code {
                        text-align: center;
                        font-size: 28px;
                        font-weight: bold;
                        color: #409EFF;
                        margin: 24px 0;
                        letter-spacing: 6px;
                    }
                    .warning {
                        color: #E6A23C;
                        font-size: 14px;
                        margin-top: 20px;
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        color: #909399;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>白名单登录验证码</h1>
                    </div>
                    <div class="content">
                        <p>亲爱的用户：</p>
                        <p>您正在设置白名单登录账号，请使用以下验证码完成验证：</p>
                        <div class="verify-code">{code}</div>
                        <p class="warning">注意：验证码将在10分钟后失效，请尽快完成验证。</p>
                    </div>
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿回复</p>
                        <p>© 2025 Minecraft. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>""";

    public static final String WHITELIST_NOTIFICATION_TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>白名单审核通知</title>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: 'Helvetica Neue', Arial, sans-serif;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 16px;
                        overflow: hidden;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #1e2f45, #2c3e50);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 30px;
                        color: #2c3e50;
                    }
                    .status {
                        text-align: center;
                        margin: 20px 0;
                        padding: 15px;
                        border-radius: 8px;
                        font-weight: 600;
                        font-size: 18px;
                    }
                    .status.approved {
                        background-color: #f0f9eb;
                        color: #67C23A;
                        border: 1px solid #e1f3d8;
                    }
                    .status.rejected {
                        background-color: #fef0f0;
                        color: #f56c6c;
                        border: 1px solid #fde2e2;
                    }
                    .info-box {
                        background: #f8f9fa;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 20px 0;
                    }
                    .info-item {
                        margin: 10px 0;
                        display: flex;
                        justify-content: space-between;
                    }
                    .info-label {
                        color: #606266;
                        font-weight: 500;
                    }
                    .info-value {
                        color: #409EFF;
                        font-weight: 500;
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        color: #909399;
                        font-size: 14px;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 24px;
                        background: #409EFF;
                        color: white;
                        text-decoration: none;
                        border-radius: 24px;
                        margin: 20px 0;
                        font-weight: 500;
                        transition: all 0.3s ease;
                    }
                    .button:hover {
                        background: #66b1ff;
                        transform: translateY(-2px);
                    }
                    .minecraft-style {
                        font-family: 'Minecraft', 'Helvetica Neue', Arial, sans-serif;
                        text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.2);
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 class="minecraft-style">✨ 白名单审核通知 ✨</h1>
                    </div>
                   \s
                    <div class="content">
                        <p>亲爱的 {username}：</p>
                        <!-- 审核通过模板 -->
                        <!-- 审核拒绝模板 -->
                        <div class="info-box">
                            <div class="info-item">
                                <span class="info-label">游戏ID：</span>
                                <span class="info-value">{gameId}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">申请时间：</span>
                                <span class="info-value">{applyTime}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">审核时间：</span>
                                <span class="info-value">{reviewTime}</span>
                            </div>
                        </div>
            
                        <!-- 通过时显示 -->
                        <p>服务器信息：</p>
                        <div class="info-box">
                               {info}                    \
                        </div>
                       \s
                        <p>温馨提示：</p>
                        <ul>
                            <li>请遵守服务器规则，与其他玩家和谐共处</li>
                            <li>如遇问题可以联系管理员寻求帮助</li>
                            <li>祝您游戏愉快！</li>
                        </ul>
            
                    </div>
                   \s
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿回复</p>
                        <p>© 2025 Minecraft. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>""";


    public static final String WHITELIST_NOTIFICATION_TEMPLATE_BAN = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>白名单移除通知</title>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: 'Helvetica Neue', Arial, sans-serif;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 16px;
                        overflow: hidden;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #1e2f45, #2c3e50);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 30px;
                        color: #2c3e50;
                    }
                    .status {
                        text-align: center;
                        margin: 20px 0;
                        padding: 15px;
                        border-radius: 8px;
                        font-weight: 600;
                        font-size: 18px;
                    }
                    .status.approved {
                        background-color: #f0f9eb;
                        color: #67C23A;
                        border: 1px solid #e1f3d8;
                    }
                    .status.rejected {
                        background-color: #fef0f0;
                        color: #f56c6c;
                        border: 1px solid #fde2e2;
                    }
                    .info-box {
                        background: #f8f9fa;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 20px 0;
                    }
                    .info-item {
                        margin: 10px 0;
                        display: flex;
                        justify-content: space-between;
                    }
                    .info-label {
                        color: #606266;
                        font-weight: 500;
                    }
                    .info-value {
                        color: #409EFF;
                        font-weight: 500;
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        color: #909399;
                        font-size: 14px;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 24px;
                        background: #409EFF;
                        color: white;
                        text-decoration: none;
                        border-radius: 24px;
                        margin: 20px 0;
                        font-weight: 500;
                        transition: all 0.3s ease;
                    }
                    .button:hover {
                        background: #66b1ff;
                        transform: translateY(-2px);
                    }
                    .minecraft-style {
                        font-family: 'Minecraft', 'Helvetica Neue', Arial, sans-serif;
                        text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.2);
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 class="minecraft-style">🚫 白名单移除通知 🚫</h1>
                    </div>
                   \s
                    <div class="content">
                        <p>你好 {username}：</p>
                       \s
                        <!-- 移除模板 -->
                        <!-- 解禁模板 -->
                        <!-- 封禁模板 -->
                       \s
                        <div class="info-box">
                            <div class="info-item">
                                <span class="info-label">游戏ID：</span>
                                <span class="info-value">{gameId}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">申请时间：</span>
                                <span class="info-value">{applyTime}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">{timeTittle}：</span>
                                <span class="info-value">{time}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">原因：</span>
                                <span class="info-value">{removeReason}</span>
                            </div>
                        </div>
            
                        <p>温馨提示：</p>
                        <ul>
                            <li>请遵守服务器规则，与其他玩家和谐共处</li>
                            <li>如遇问题可以联系管理员寻求帮助</li>
                        </ul>
            
                    </div>
                   \s
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿回复</p>
                        <p>© 2025 Minecraft. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>""";

    public static final String WHITELIST_NOTIFICATION_TEMPLATE_UNBAN = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>封禁解除通知</title>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: 'Helvetica Neue', Arial, sans-serif;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 16px;
                        overflow: hidden;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #1e2f45, #2c3e50);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 30px;
                        color: #2c3e50;
                    }
                    .status {
                        text-align: center;
                        margin: 20px 0;
                        padding: 15px;
                        border-radius: 8px;
                        font-weight: 600;
                        font-size: 18px;
                    }
                    .status.approved {
                        background-color: #f0f9eb;
                        color: #67C23A;
                        border: 1px solid #e1f3d8;
                    }
                    .status.rejected {
                        background-color: #fef0f0;
                        color: #f56c6c;
                        border: 1px solid #fde2e2;
                    }
                    .info-box {
                        background: #f8f9fa;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 20px 0;
                    }
                    .info-item {
                        margin: 10px 0;
                        display: flex;
                        justify-content: space-between;
                    }
                    .info-label {
                        color: #606266;
                        font-weight: 500;
                    }
                    .info-value {
                        color: #409EFF;
                        font-weight: 500;
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        color: #909399;
                        font-size: 14px;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 24px;
                        background: #409EFF;
                        color: white;
                        text-decoration: none;
                        border-radius: 24px;
                        margin: 20px 0;
                        font-weight: 500;
                        transition: all 0.3s ease;
                    }
                    .button:hover {
                        background: #66b1ff;
                        transform: translateY(-2px);
                    }
                    .minecraft-style {
                        font-family: 'Minecraft', 'Helvetica Neue', Arial, sans-serif;
                        text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.2);
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 class="minecraft-style">🎉 封禁解除通知 🎉</h1>
                    </div>
                   \s
                    <div class="content">
                        <p>你好 {username}：</p>
                       \s
                        <!-- 解禁模板 -->
                       \s
                        <div class="info-box">
                            <div class="info-item">
                                <span class="info-label">游戏ID：</span>
                                <span class="info-value">{gameId}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">封禁时间：</span>
                                <span class="info-value">{banTime}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">解禁时间：</span>
                                <span class="info-value">{unBanTime}</span>
                            </div>
                        </div>
            
                        <p>温馨提示：</p>
                        <ul>
                            <li>请遵守服务器规则，与其他玩家和谐共处</li>
                            <li>如遇问题可以联系管理员寻求帮助</li>
                        </ul>
            
                    </div>
                   \s
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿回复</p>
                        <p>© 2025 Minecraft. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>""";

    // 待审核
    public static final String WHITELIST_NOTIFICATION_TEMPLATE_PENDING = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>白名单已提交</title>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: 'Helvetica Neue', Arial, sans-serif;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 16px;
                        overflow: hidden;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #1e2f45, #2c3e50);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 30px;
                        color: #2c3e50;
                    }
                    .status {
                        text-align: center;
                        margin: 20px 0;
                        padding: 15px;
                        border-radius: 8px;
                        font-weight: 600;
                        font-size: 18px;
                    }
                    .status.approved {
                        background-color: #f0f9eb;
                        color: #67C23A;
                        border: 1px solid #e1f3d8;
                    }
                    .status.rejected {
                        background-color: #fef0f0;
                        color: #f56c6c;
                        border: 1px solid #fde2e2;
                    }
                    .info-box {
                        background: #f8f9fa;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 20px 0;
                    }
                    .info-item {
                        margin: 10px 0;
                        display: flex;
                        justify-content: space-between;
                    }
                    .info-label {
                        color: #606266;
                        font-weight: 500;
                    }
                    .info-value {
                        color: #409EFF;
                        font-weight: 500;
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 20px;
                        text-align: center;
                        color: #909399;
                        font-size: 14px;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 24px;
                        background: #409EFF;
                        color: white;
                        text-decoration: none;
                        border-radius: 24px;
                        margin: 20px 0;
                        font-weight: 500;
                        transition: all 0.3s ease;
                    }
                    .button:hover {
                        background: #66b1ff;
                        transform: translateY(-2px);
                    }
                    .minecraft-style {
                        font-family: 'Minecraft', 'Helvetica Neue', Arial, sans-serif;
                        text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.2);
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 class="minecraft-style">🎉 您的白名单已成功提交 🎉</h1>
                    </div>
                   \s
                    <div class="content">
                        <p>你好 {username}：</p>
                       \s
                        <!-- 提交模板 -->
                       \s
                        <div class="info-box">
                            <div class="info-item">
                                <span class="info-label">游戏ID：</span>
                                <span class="info-value">{gameId}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">提交时间：</span>
                                <span class="info-value">{applyTime}</span>
                            </div>
                        </div>
            
                        <p>温馨提示：</p>
                        <ul>
                            <li>如遇问题可以联系管理员寻求帮助</li>
                        </ul>
            
                    </div>
                   \s
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿回复</p>
                        <p>© 2025 Minecraft. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>""";

    public static final String ALERT_TITLE = "系统异常告警";

    public static final String ALERT_TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>系统异常告警</title>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: 'Helvetica Neue', Arial, sans-serif;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 16px;
                        overflow: hidden;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #ff4d4d, #ff1a1a);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 30px;
                        color: #2c3e50;
                    }
                    .alert-info {
                        background: #fff3f3;
                        border-left: 4px solid #ff4d4d;
                        padding: 15px;
                        margin: 20px 0;
                    }
                    .info-box {
                        background: #f8f9fa;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 20px 0;
                    }
                    .info-item {
                        margin: 10px 0;
                        display: flex;
                        align-items: center;
                    }
                    .info-label {
                        font-weight: 600;
                        min-width: 120px;
                        color: #666;
                    }
                    .info-value {
                        color: #333;
                    }
                    .footer {
                        text-align: center;
                        padding: 20px;
                        background: #f8f9fa;
                        color: #666;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>系统异常告警</h1>
                    </div>
                   \s
                    <div class="content">
                        <div class="alert-info">
                            <p>系统检测到异常情况，请及时处理！</p>
                        </div>
                       \s
                        <div class="info-box">
                            <div class="info-item">
                                <span class="info-label">异常时间：</span>
                                <span class="info-value">{time}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">异常次数：</span>
                                <span class="info-value">{count}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">异常类型：</span>
                                <span class="info-value">{type}</span>
                            </div>
                        </div>
                       \s
                        <div class="info-box">
                            <h3>服务器信息</h3>
                            <div class="info-item">
                                <span class="info-label">服务器名称：</span>
                                <span class="info-value">{serverName}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">服务器地址：</span>
                                <span class="info-value">{serverAddress}</span>
                            </div>
                        </div>
                       \s
                        <p>温馨提示：</p>
                        <ul>
                            <li>请及时检查系统运行状态</li>
                            <li>如遇问题可以联系系统管理员</li>
                        </ul>
                    </div>
                   \s
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿回复</p>
                        <p>© 2025 Minecraft. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>""";

    // 替换模板中的变量
    public static String getWhitelistNotification(String username, String gameId, String applyTime,
                                                  String reviewTime, String titte, String appUrl, List<Map<String, Object>> infoList, String server) {
        String template = WHITELIST_NOTIFICATION_TEMPLATE;
        String key = "default";

        if (StringUtils.isNotEmpty(server)) {
            key = server;
        }

        boolean custom = false;

        // 根据不同状态获取自定义模板
        template = switch (titte) {
            case FAIL_TITLE -> {
                if (!EmailTempCache.isEmpty() && EmailTempCache.get(key) != null) {
                    final CustomEmailTemplates defaultTemp = EmailTempCache.get(key);
                    template = defaultTemp.getRefuseTemp() != null ? defaultTemp.getRefuseTemp() : WHITELIST_NOTIFICATION_TEMPLATE;
                    // custom = defaultTemp.getRefuseTemp() != null;
                }
                yield template.replace(
                        "<!-- 审核拒绝模板 -->",
                        "<div class=\"status rejected\"><span>😢 很抱歉，您的白名单申请未通过</span></div>"
                ).replace(
                        "<!-- 审核通过模板 -->",
                        ""
                );
            }
            case SUCCESS_TITLE -> {
                if (!EmailTempCache.isEmpty() && EmailTempCache.get(key) != null) {
                    final CustomEmailTemplates defaultTemp = EmailTempCache.get(key);
                    template = defaultTemp.getPassTemp() != null ? defaultTemp.getPassTemp() : WHITELIST_NOTIFICATION_TEMPLATE;
                    custom = defaultTemp.getPassTemp() != null;
                }
                yield template.replace(
                        "<!-- 审核通过模板 -->",
                        "<div class=\"status approved\"><span>🎉 恭喜，您的白名单申请已通过！</span></div>"
                ).replace(
                        "<!-- 审核拒绝模板 -->",
                        ""
                );
            }
            default -> template;
        };

        // 替换基本变量
        template = template.replace("{username}", username)
                .replace("{gameId}", gameId)
                .replace("{applyTime}", applyTime)
                .replace("{reviewTime}", reviewTime);

        // 服务器查询地址
        String URL = appUrl + "/player-servers/" + gameId;
        template = template.replace("{url}", URL);
        if (custom) {
            if (infoList.size() <= 1) {
                final Map<String, Object> infoMap = infoList.getFirst();
                template = template.replace("{name}", (String) infoMap.get("name"))
                        .replace("{serverAddress}", (String) infoMap.get("serverAddress"))
                        .replace("{port}", String.valueOf(infoMap.get("port")))
                        .replace("{core}", (String) infoMap.get("core"))
                        .replace("{version}", (String) infoMap.get("version"));
            }
        } else {
            // 定义单个服务器信息的HTML模板
            String serverInfoTemplate =
                    """
                            <div class="server-info-block" style="margin-bottom: 20px; padding: 15px; background: #f8f9fa; border-radius: 8px; border: 1px solid #e9ecef;">
                                <div class="info-item">
                                    <span class="info-label">服务器名称：</span>
                                    <span class="info-value">{name}</span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">服务器地址：</span>
                                    <span class="info-value">{serverAddress}:{port}</span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">游戏版本：</span>
                                    <span class="info-value">{core}-{version}</span>
                                </div>
                            </div>""";

            // 处理服务器信息
            if (infoList == null || infoList.isEmpty()) {
                // 如果没有服务器信息，提供链接查看
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
        }

        return template;
    }

    // 获取白名单移除/封禁通知模板
    public static String getWhitelistNotificationBan(String username, String gameId, String applyTime,
                                                     String time, String timeTittle, String removeReason, String titte, String server) {
        String template = WHITELIST_NOTIFICATION_TEMPLATE;
        String key = "default";

        if (StringUtils.isNotEmpty(server)) {
            key = server;
        }
        // 根据不同状态获取自定义模板
        if (!EmailTempCache.isEmpty() && EmailTempCache.get(key) != null) {
            final CustomEmailTemplates defaultTemp = EmailTempCache.get(key);
            template = switch (titte) {
                case BAN_TITLE -> {
                    template = defaultTemp.getBanTemp() != null ? defaultTemp.getBanTemp() : WHITELIST_NOTIFICATION_TEMPLATE;
                    yield template.replace(
                            "<!-- 封禁模板 -->",
                            "<div class=\"status rejected\"><span>🚫 你已被封禁！🚫</span></div>"
                    ).replace(
                            "<!-- 移除模板 -->",
                            ""
                    );
                }
                case REMOVE_TITLE -> {
                    template = defaultTemp.getRemoveTemp() != null ? defaultTemp.getRemoveTemp() : WHITELIST_NOTIFICATION_TEMPLATE;
                    yield template.replace(
                            "<!-- 移除模板 -->",
                            "<div class=\"status rejected\"><span>😢 很抱歉，您的白名单申请未通过</span></div>"
                    ).replace(
                            "<!-- 封禁模板 -->",
                            ""
                    );
                }
                case FAIL_TITLE -> {
                    template = defaultTemp.getRefuseTemp() != null ? defaultTemp.getRefuseTemp() : WHITELIST_NOTIFICATION_TEMPLATE;
                    yield template.replace(
                            "<!-- 移除模板 -->",
                            "<div class=\"status rejected\"><span>😢 很抱歉，您的白名单申请未通过</span></div>"
                    ).replace(
                            "<!-- 封禁模板 -->",
                            ""
                    );
                }
                default -> template;
            };
        }

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

        return template;
    }

    // 获取白名单解禁通知模板
    public static String getWhitelistNotificationUnBan(String username, String gameId, String banTime, String unBanTime, String server) {
        String template = WHITELIST_NOTIFICATION_TEMPLATE_UNBAN;
        String key = "default";

        if (StringUtils.isNotEmpty(server)) {
            key = server;
        }

        if (EmailTempCache.isEmpty() && EmailTempCache.get(key) != null) {
            final CustomEmailTemplates defaultTemp = EmailTempCache.get(key);
            if (defaultTemp.getPardonTemp() != null) {
                template = defaultTemp.getPardonTemp();
            }
        }

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

    // 获取白名单待审核通知模板
    public static String getWhitelistNotificationPending(String username, String gameId, String applyTime, boolean autoPass, String server) {
        String template = WHITELIST_NOTIFICATION_TEMPLATE_PENDING;

        String key = "default";

        if (StringUtils.isNotEmpty(server)) {
            key = server;
        }

        if (!EmailTempCache.isEmpty() && EmailTempCache.get(key) != null) {
            final CustomEmailTemplates defaultTemp = EmailTempCache.get(key);
            if (autoPass) {
                if (defaultTemp.getPassTemp() != null) {
                    template = defaultTemp.getPassTemp();
                } else if (defaultTemp.getPendingTemp() != null) {
                    template = defaultTemp.getPendingTemp();
                }
            }
        }

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
        String key = "default";

        if (!EmailTempCache.isEmpty() && EmailTempCache.get(key) != null) {
            final CustomEmailTemplates defaultTemp = EmailTempCache.get(key);
            if (defaultTemp.getVerifyTemp() != null) {
                return defaultTemp.getVerifyTemp().replace("{verifyLink}", verifyLink);
            }
        }

        return EMAIL_VERIFY_TEMPLATE.replace("{verifyLink}", verifyLink);
    }

    public static String getWhitelistLoginCodeTemplate(String code) {
        return WHITELIST_LOGIN_CODE_TEMPLATE.replace("{code}", code);
    }

    // 获取告警通知模板
    public static String getAlertNotification(String time, int count, String type,
                                              String serverName, String serverAddress) {

        String template = ALERT_TEMPLATE;
        String key = "default";

        // 如果有自定义模板则使用自定义模板
        if (!EmailTempCache.isEmpty() && EmailTempCache.get(key) != null) {
            final CustomEmailTemplates defaultTemp = EmailTempCache.get(key);
            if (defaultTemp.getWarningTemp() != null) {
                template = defaultTemp.getWarningTemp();
            }
        }

        // 替换变量
        template = template.replace("{time}", time)
                .replace("{count}", String.valueOf(count))
                .replace("{type}", type)
                .replace("{serverName}", serverName)
                .replace("{serverAddress}", serverAddress);

        return template;

    }

    public static String getReviewTemplate(String qq, String gameId, String applyTime, boolean status) {
        String ket = "default";
        if (!EmailTempCache.isEmpty() && EmailTempCache.get(ket) != null) {
            final CustomEmailTemplates defaultTemp = EmailTempCache.get(ket);
            if (defaultTemp.getReviewTemp() != null) {
                return defaultTemp.getReviewTemp()
                        .replace("{qq}", qq)
                        .replace("{gameId}", gameId)
                        .replace("{applyTime}", applyTime)
                        .replace("{status}", status ? "已自动通过" : "待审核");
            }
        }
        return null;
    }
}