package com.ruoyi.web.controller.system;

import com.ruoyi.common.config.RuoYiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页
 *
 * @author ruoyi
 */
@RestController
public class SysIndexController {
    /**
     * 系统基础配置
     */
    @Autowired
    private RuoYiConfig ruoyiConfig;

    /**
     * 访问首页，提示语
     */
    @RequestMapping("/")
    public String index() {
        String html = "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>" + ruoyiConfig.getName() + "</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css\">\n" +
                "    <style>\n" +
                "        @keyframes gradient {\n" +
                "            0% { background-position: 0% 50%; }\n" +
                "            50% { background-position: 100% 50%; }\n" +
                "            100% { background-position: 0% 50%; }\n" +
                "        }\n" +
                "        @keyframes fadeIn {\n" +
                "            from { opacity: 0; transform: translateY(-20px); }\n" +
                "            to { opacity: 1; transform: translateY(0); }\n" +
                "        }\n" +
                "        @keyframes pulse {\n" +
                "            0% { transform: scale(1); }\n" +
                "            50% { transform: scale(1.05); }\n" +
                "            100% { transform: scale(1); }\n" +
                "        }\n" +
                "        body {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            font-family: 'Microsoft YaHei', sans-serif;\n" +
                "            background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);\n" +
                "            background-size: 400% 400%;\n" +
                "            animation: gradient 15s ease infinite;\n" +
                "            height: 100vh;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            color: #ffffff;\n" +
                "        }\n" +
                "        .container {\n" +
                "            text-align: center;\n" +
                "            padding: 3rem;\n" +
                "            background: rgba(255, 255, 255, 0.1);\n" +
                "            border-radius: 20px;\n" +
                "            backdrop-filter: blur(10px);\n" +
                "            box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);\n" +
                "            animation: fadeIn 1s ease-out;\n" +
                "            border: 1px solid rgba(255, 255, 255, 0.18);\n" +
                "            max-width: 600px;\n" +
                "            width: 90%;\n" +
                "        }\n" +
                "        .icon-wrapper {\n" +
                "            font-size: 4rem;\n" +
                "            margin-bottom: 2rem;\n" +
                "            animation: pulse 2s infinite;\n" +
                "            color: #ffffff;\n" +
                "            text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);\n" +
                "        }\n" +
                "        h1 {\n" +
                "            font-size: 3rem;\n" +
                "            margin-bottom: 1.5rem;\n" +
                "            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);\n" +
                "            color: #ffffff;\n" +
                "            letter-spacing: 2px;\n" +
                "        }\n" +
                "        .alert-box {\n" +
                "            background: rgba(255, 255, 255, 0.15);\n" +
                "            border-left: 4px solid #ffffff;\n" +
                "            padding: 1.5rem;\n" +
                "            margin: 1.5rem 0;\n" +
                "            text-align: left;\n" +
                "            border-radius: 8px;\n" +
                "            backdrop-filter: blur(5px);\n" +
                "        }\n" +
                "        p {\n" +
                "            font-size: 1.2rem;\n" +
                "            margin: 1rem 0;\n" +
                "            opacity: 0.9;\n" +
                "            line-height: 1.8;\n" +
                "        }\n" +
                "        .version {\n" +
                "            margin-top: 2rem;\n" +
                "            font-size: 1rem;\n" +
                "            padding: 0.8rem 1.5rem;\n" +
                "            background: rgba(255, 255, 255, 0.15);\n" +
                "            border-radius: 30px;\n" +
                "            display: inline-block;\n" +
                "            backdrop-filter: blur(5px);\n" +
                "            letter-spacing: 1px;\n" +
                "        }\n" +
                "        i {\n" +
                "            margin-right: 8px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"icon-wrapper\">\n" +
                "            <i class=\"fas fa-shield-alt\"></i>\n" +
                "        </div>\n" +
                "        <h1>" + ruoyiConfig.getName() + "</h1>\n" +
                "        <div class=\"alert-box\">\n" +
                "            <p><i class=\"fas fa-exclamation-circle\"></i>重要提示：请确保您已获得授权访问。</p>\n" +
                "        </div>\n" +
                "        <p><i class=\"fas fa-check-circle\"></i>欢迎访问系统首页</p>\n" +
                "        <p><i class=\"fas fa-star\"></i>让每一次访问都充满期待</p>\n" +
                "        <div class=\"version\">\n" +
                "            <i class=\"fas fa-code-branch\"></i>Version " + ruoyiConfig.getVersion() + "\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
        return html;
    }
}
