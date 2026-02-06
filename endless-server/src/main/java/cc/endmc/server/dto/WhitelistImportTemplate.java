package cc.endmc.server.dto;

import cc.endmc.common.annotation.Excel;
import lombok.Data;

/**
 * Excel导入模板对象
 */
@Data
public class WhitelistImportTemplate {
    @Excel(name = "QQ号")
    private String qqNum;

    @Excel(name = "游戏昵称")
    private String userName;

    @Excel(name = "是否正版")
    private String isOnline;

    @Excel(name = "备注")
    private String remark;
}