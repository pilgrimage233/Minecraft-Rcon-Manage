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
                "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css\">\n" +
                "    <style>\n" +
                "        * {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            box-sizing: border-box;\n" +
                "        }\n" +
                "        @keyframes gradient {\n" +
                "            0% { background-position: 0% 50%; }\n" +
                "            50% { background-position: 100% 50%; }\n" +
                "            100% { background-position: 0% 50%; }\n" +
                "        }\n" +
                "        @keyframes float {\n" +
                "            0% { transform: translateY(0px); }\n" +
                "            50% { transform: translateY(-10px); }\n" +
                "            100% { transform: translateY(0px); }\n" +
                "        }\n" +
                "        @keyframes pulse {\n" +
                "            0% { box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.4); }\n" +
                "            70% { box-shadow: 0 0 0 10px rgba(255, 255, 255, 0); }\n" +
                "            100% { box-shadow: 0 0 0 0 rgba(255, 255, 255, 0); }\n" +
                "        }\n" +
                "        html, body {\n" +
                "            height: 100%;\n" +
                "            width: 100%;\n" +
                "            overflow-x: hidden;\n" +
                "        }\n" +
                "        body {\n" +
                "            font-family: 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;\n" +
                "            background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);\n" +
                "            background-size: 400% 400%;\n" +
                "            animation: gradient 15s ease infinite;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            color: #ffffff;\n" +
                "            min-height: 100vh;\n" +
                "            padding: 20px;\n" +
                "            transition: all 1s cubic-bezier(0.4, 0, 0.2, 1);\n" +
                "            -webkit-font-smoothing: antialiased;\n" +
                "            -moz-osx-font-smoothing: grayscale;\n" +
                "        }\n" +
                "        .theme-transition {\n" +
                "            transition: background 1s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.5s ease;\n" +
                "        }\n" +
                "        .container {\n" +
                "            position: relative;\n" +
                "            width: 100%;\n" +
                "            max-width: 480px;\n" +
                "            text-align: center;\n" +
                "            padding: 2.5rem;\n" +
                "            background: rgba(255, 255, 255, 0.08);\n" +
                "            border-radius: 24px;\n" +
                "            backdrop-filter: blur(12px) saturate(180%);\n" +
                "            -webkit-backdrop-filter: blur(12px) saturate(180%);\n" +
                "            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);\n" +
                "            border: 1px solid rgba(255, 255, 255, 0.2);\n" +
                "            overflow: hidden;\n" +
                "            z-index: 1;\n" +
                "            animation: float 6s ease-in-out infinite;\n" +
                "            transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);\n" +
                "        }\n" +
                "        .container::before {\n" +
                "            content: '';\n" +
                "            position: absolute;\n" +
                "            top: -50%;\n" +
                "            left: -50%;\n" +
                "            width: 200%;\n" +
                "            height: 200%;\n" +
                "            background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);\n" +
                "            z-index: -1;\n" +
                "        }\n" +
                "        .logo {\n" +
                "            width: 80px;\n" +
                "            height: 80px;\n" +
                "            margin: 0 auto 1.5rem;\n" +
                "            background: rgba(255, 255, 255, 0.15);\n" +
                "            border-radius: 50%;\n" +
                "            position: relative;\n" +
                "            animation: pulse 2s infinite;\n" +
                "        }\n" +
                "        .logo::before {\n" +
                "            content: '';\n" +
                "            position: absolute;\n" +
                "            top: -4px;\n" +
                "            left: -4px;\n" +
                "            right: -4px;\n" +
                "            bottom: -4px;\n" +
                "            border-radius: 50%;\n" +
                "            border: 2px solid rgba(255, 99, 132, 0.6);\n" +
                "            animation: pulse 2s infinite;\n" +
                "        }\n" +
                "        .logo i {\n" +
                "            position: absolute;\n" +
                "            left: 50%;\n" +
                "            top: 50%;\n" +
                "            transform: translate(-50%, -50%);\n" +
                "            font-size: 2.5rem;\n" +
                "            color: #ffffff;\n" +
                "            text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);\n" +
                "            width: 2.5rem;\n" +
                "            height: 2.5rem;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            justify-content: center;\n" +
                "        }\n" +
                "        h1 {\n" +
                "            font-size: 2.2rem;\n" +
                "            font-weight: 700;\n" +
                "            margin-bottom: 1.5rem;\n" +
                "            background: linear-gradient(to right, #ffffff, #e0e0e0);\n" +
                "            -webkit-background-clip: text;\n" +
                "            -webkit-text-fill-color: transparent;\n" +
                "            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);\n" +
                "            letter-spacing: 1px;\n" +
                "        }\n" +
                "        .alert-box {\n" +
                "            background: rgba(255, 255, 255, 0.05);\n" +
                "            border-left: 4px solid rgba(255, 255, 255, 0.3);\n" +
                "            padding: 1.2rem;\n" +
                "            margin: 1.5rem 0;\n" +
                "            text-align: left;\n" +
                "            border-radius: 12px;\n" +
                "            backdrop-filter: blur(5px);\n" +
                "            -webkit-backdrop-filter: blur(5px);\n" +
                "        }\n" +
                "        p {\n" +
                "            font-size: 1.1rem;\n" +
                "            margin: 0.8rem 0;\n" +
                "            opacity: 0.95;\n" +
                "            line-height: 1.6;\n" +
                "            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        .info-item {\n" +
                "            background: rgba(255, 255, 255, 0.05);\n" +
                "            border-radius: 12px;\n" +
                "            padding: 0.8rem 1rem;\n" +
                "            margin: 0.8rem 0;\n" +
                "            text-align: left;\n" +
                "            transition: all 0.3s ease;\n" +
                "            backdrop-filter: blur(5px);\n" +
                "            -webkit-backdrop-filter: blur(5px);\n" +
                "        }\n" +
                "        .info-item:hover {\n" +
                "            background: rgba(255, 255, 255, 0.15);\n" +
                "            transform: translateY(-2px);\n" +
                "        }\n" +
                "        .version {\n" +
                "            margin-top: 2rem;\n" +
                "            font-size: 0.9rem;\n" +
                "            padding: 0.6rem 1.2rem;\n" +
                "            background: rgba(255, 255, 255, 0.12);\n" +
                "            border-radius: 30px;\n" +
                "            display: inline-block;\n" +
                "            backdrop-filter: blur(5px);\n" +
                "            -webkit-backdrop-filter: blur(5px);\n" +
                "            letter-spacing: 1px;\n" +
                "            border: 1px solid rgba(255, 255, 255, 0.15);\n" +
                "        }\n" +
                "        i {\n" +
                "            margin-right: 10px;\n" +
                "            width: 20px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .dark-mode-toggle {\n" +
                "            position: absolute;\n" +
                "            top: 15px;\n" +
                "            right: 15px;\n" +
                "            background: rgba(255, 255, 255, 0.05);\n" +
                "            border: none;\n" +
                "            color: white;\n" +
                "            width: 40px;\n" +
                "            height: 40px;\n" +
                "            border-radius: 50%;\n" +
                "            cursor: pointer;\n" +
                "            backdrop-filter: blur(5px);\n" +
                "            -webkit-backdrop-filter: blur(5px);\n" +
                "            transition: all 0.3s ease;\n" +
                "            padding: 0;\n" +
                "            display: grid;\n" +
                "            place-items: center;\n" +
                "            border: 1px solid rgba(255, 255, 255, 0.2);\n" +
                "        }\n" +
                "        .dark-mode-toggle i {\n" +
                "            font-size: 1.2rem;\n" +
                "            width: auto;\n" +
                "            height: auto;\n" +
                "            margin: 0;\n" +
                "            line-height: 1;\n" +
                "        }\n" +
                "        .dark-mode-toggle:hover {\n" +
                "            background: rgba(255, 255, 255, 0.25);\n" +
                "            transform: scale(1.05);\n" +
                "        }\n" +
                "        @media (max-width: 768px) {\n" +
                "            .container {\n" +
                "                padding: 2rem 1.5rem;\n" +
                "                max-width: 92%;\n" +
                "                background: rgba(255, 255, 255, 0.03);\n" +
                "                backdrop-filter: blur(8px);\n" +
                "                -webkit-backdrop-filter: blur(8px);\n" +
                "            }\n" +
                "            .alert-box {\n" +
                "                background: rgba(255, 255, 255, 0.03);\n" +
                "                backdrop-filter: blur(8px);\n" +
                "                -webkit-backdrop-filter: blur(8px);\n" +
                "            }\n" +
                "            .info-item {\n" +
                "                background: rgba(255, 255, 255, 0.03);\n" +
                "                backdrop-filter: blur(8px);\n" +
                "                -webkit-backdrop-filter: blur(8px);\n" +
                "            }\n" +
                "            .version {\n" +
                "                background: rgba(255, 255, 255, 0.03);\n" +
                "            }\n" +
                "            .dark-mode-toggle {\n" +
                "                background: rgba(255, 255, 255, 0.05);\n" +
                "            }\n" +
                "            body {\n" +
                "                padding: 15px;\n" +
                "                background-size: 200% 200%;\n" +
                "            }\n" +
                "        }\n" +
                "        @supports not ((-webkit-backdrop-filter: none) or (backdrop-filter: none)) {\n" +
                "            .container, .alert-box, .info-item {\n" +
                "                background: rgba(31, 41, 55, 0.4);\n" +
                "            }\n" +
                "            @media (max-width: 768px) {\n" +
                "                .container {\n" +
                "                    background: rgba(31, 41, 55, 0.3);\n" +
                "                }\n" +
                "                .alert-box, .info-item {\n" +
                "                    background: rgba(31, 41, 55, 0.2);\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <button class=\"dark-mode-toggle\" title=\"切换主题\">\n" +
                "            <i class=\"fas fa-moon\"></i>\n" +
                "        </button>\n" +
                "        <div class=\"logo\">\n" +
                "            <i class=\"fas fa-shield-alt\"></i>\n" +
                "        </div>\n" +
                "        <h1>" + ruoyiConfig.getName() + "</h1>\n" +
                "        <div class=\"alert-box\">\n" +
                "            <p><i class=\"fas fa-exclamation-circle\"></i>重要提示：请确保您已获得授权访问。</p>\n" +
                "        </div>\n" +
                "        <div class=\"info-item\">\n" +
                "            <p><i class=\"fas fa-check-circle\"></i>欢迎访问系统首页</p>\n" +
                "        </div>\n" +
                "        <div class=\"info-item\">\n" +
                "            <p><i class=\"fas fa-star\"></i>让每一次访问都充满期待</p>\n" +
                "        </div>\n" +
                "        <div class=\"version\">\n" +
                "            <i class=\"fas fa-code-branch\"></i>Version " + ruoyiConfig.getVersion() + "\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        document.querySelector('.dark-mode-toggle').addEventListener('click', function() {\n" +
                "            const icon = this.querySelector('i');\n" +
                "            const body = document.body;\n" +
                "            const container = document.querySelector('.container');\n" +
                "            \n" +
                "            // Add transition classes\n" +
                "            body.classList.add('theme-transition');\n" +
                "            container.style.opacity = '0.8';\n" +
                "            \n" +
                "            setTimeout(() => {\n" +
                "                if (icon.classList.contains('fa-moon')) {\n" +
                "                    icon.classList.remove('fa-moon');\n" +
                "                    icon.classList.add('fa-sun');\n" +
                "                    document.body.style.background = 'linear-gradient(-45deg, #2b5876, #4e4376, #2c3e50, #000428)';\n" +
                "                } else {\n" +
                "                    icon.classList.remove('fa-sun');\n" +
                "                    icon.classList.add('fa-moon');\n" +
                "                    document.body.style.background = 'linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab)';\n" +
                "                }\n" +
                "                \n" +
                "                // Add animation for icon transition\n" +
                "                icon.style.transition = 'transform 0.5s ease';\n" +
                "                icon.style.transform = 'rotate(360deg)';\n" +
                "                \n" +
                "                // Fade container back in\n" +
                "                setTimeout(() => {\n" +
                "                    container.style.opacity = '1';\n" +
                "                }, 100);\n" +
                "                \n" +
                "                // Reset rotation after animation completes\n" +
                "                setTimeout(() => {\n" +
                "                    icon.style.transform = 'rotate(0deg)';\n" +
                "                }, 500);\n" +
                "            }, 50);\n" +
                "            \n" +
                "            // Remove transition class after animation completes\n" +
                "            setTimeout(() => {\n" +
                "                body.classList.remove('theme-transition');\n" +
                "            }, 1000);\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
        return html;
    }
}
