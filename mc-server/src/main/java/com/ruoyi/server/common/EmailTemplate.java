package com.ruoyi.server.common;

/**
 * ClassName: EmailTemplate <br>
 * Description:
 * date: 2024/1/13 16:27 <br>
 *
 * @author zhangchanggeng <br>
 * @since JDK 1.8
 */
public class EmailTemplate {
    public static String TITLE = "白名单审核结果通知";

    public static String getEmailHtml(String content) {
        return "<div>\n" +
                "    <includetail>\n" +
                "        <style>\n" +
                "            /* CLIENT-SPECIFIC STYLES */\n" +
                "            body, table, td, a {\n" +
                "                -webkit-text-size-adjust: 100%;\n" +
                "                -ms-text-size-adjust: 100%;\n" +
                "            }\n" +
                "\n" +
                "            table, td {\n" +
                "                mso-table-lspace: 0pt;\n" +
                "                mso-table-rspace: 0pt;\n" +
                "            }\n" +
                "\n" +
                "            img {\n" +
                "                -ms-interpolation-mode: bicubic;\n" +
                "            }\n" +
                "\n" +
                "            .hidden {\n" +
                "                display: none !important;\n" +
                "                visibility: hidden !important;\n" +
                "            }\n" +
                "\n" +
                "            /* iOS BLUE LINKS */\n" +
                "            a[x-apple-data-detectors] {\n" +
                "                color: inherit !important;\n" +
                "                text-decoration: none !important;\n" +
                "                font-size: inherit !important;\n" +
                "                font-family: inherit !important;\n" +
                "                font-weight: inherit !important;\n" +
                "                line-height: inherit !important;\n" +
                "            }\n" +
                "\n" +
                "            /* ANDROID MARGIN HACK */\n" +
                "            body {\n" +
                "                margin: 0 !important;\n" +
                "            }\n" +
                "\n" +
                "            div[style*=\"margin: 16px 0\"] {\n" +
                "                margin: 0 !important;\n" +
                "            }\n" +
                "\n" +
                "            @media only screen and (max-width: 639px) {\n" +
                "                body, #body {\n" +
                "                    min-width: 320px !important;\n" +
                "                }\n" +
                "\n" +
                "                table.wrapper {\n" +
                "                    width: 100% !important;\n" +
                "                    min-width: 320px !important;\n" +
                "                }\n" +
                "            }\n" +
                "        </style>\n" +
                "        <style>\n" +
                "            body {\n" +
                "                -webkit-text-size-adjust: 100%;\n" +
                "                -ms-text-size-adjust: 100%;\n" +
                "            }\n" +
                "\n" +
                "            img {\n" +
                "                -ms-interpolation-mode: bicubic;\n" +
                "            }\n" +
                "\n" +
                "            body {\n" +
                "                margin: 0 !important;\n" +
                "            }\n" +
                "        </style>\n" +
                "\n" +
                "\n" +
                "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"body\"\n" +
                "               style=\"text-align: center; min-width: 640px; width: 100%; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; mso-table-lspace: 0pt; mso-table-rspace: 0pt; margin: 0; padding: 0;\"\n" +
                "               bgcolor=\"#fafafa\">\n" +
                "            <tbody>\n" +
                "            <tr class=\"header\">\n" +
                "                <td style=\"font-family: 'Helvetica Neue',Helvetica,Arial,sans-serif; font-size: 13px; line-height: 1.6; color: #5c5c5c; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; mso-table-lspace: 0pt; mso-table-rspace: 0pt; padding: 25px 0;\">\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td style=\"font-family: 'Helvetica Neue',Helvetica,Arial,sans-serif; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; mso-table-lspace: 0pt; mso-table-rspace: 0pt;\">\n" +
                "                    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"wrapper\"\n" +
                "                           style=\"width: 640px; border-collapse: separate; border-spacing: 0; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; mso-table-lspace: 0pt; mso-table-rspace: 0pt; margin: 0 auto;\">\n" +
                "                        <tbody>\n" +
                "                        <tr>\n" +
                "                            <td style=\"font-family: 'Helvetica Neue',Helvetica,Arial,sans-serif; border-radius: 3px; overflow: hidden; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; mso-table-lspace: 0pt; mso-table-rspace: 0pt; padding: 18px 25px; border: 1px solid #ededed;\"\n" +
                "                                align=\"left\" bgcolor=\"#ffffff\">\n" +
                "                                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"content\"\n" +
                "                                       style=\"width: 100%; border-collapse: separate; border-spacing: 0; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; mso-table-lspace: 0pt; mso-table-rspace: 0pt;\">\n" +
                "                                    <tbody>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"font-family: 'Helvetica Neue',Helvetica,Arial,sans-serif; color: #333333; font-size: 15px; font-weight: 400; line-height: 1.4; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; mso-table-lspace: 0pt; mso-table-rspace: 0pt; padding: 15px 5px;\"\n" +
                "                                            align=\"center\">\n" +
                "                                            <h1 style=\"font-family: 'Helvetica Neue',Helvetica,Arial,sans-serif; color: #333333; font-size: 18px; font-weight: 400; line-height: 1.4; margin: 0; padding: 0;\"\n" +
                "                                                align=\"center\">\n" +
                "                                                " + content + "\n" +
                "                                            </h1>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    </tbody>\n" +
                "                                </table>\n" +
                "                            </td>\n" +
                "                        </tr>\n" +
                "                        </tbody>\n" +
                "                    </table>\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "            </tbody>\n" +
                "        </table>\n" +
                "    </includetail>\n" +
                "</div>";
    }
}
