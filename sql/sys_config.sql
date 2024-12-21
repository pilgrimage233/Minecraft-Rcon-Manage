create table sys_config
(
    config_id    int auto_increment comment '参数主键'
        primary key,
    config_name  varchar(100) default '' null comment '参数名称',
    config_key   varchar(100) default '' null comment '参数键名',
    config_value varchar(500) default '' null comment '参数键值',
    config_type  char         default 'N' null comment '系统内置（Y是 N否）',
    create_by    varchar(64)  default '' null comment '创建者',
    create_time  datetime                null comment '创建时间',
    update_by    varchar(64)  default '' null comment '更新者',
    update_time  datetime                null comment '更新时间',
    remark       varchar(500)            null comment '备注'
)
    comment '参数配置表';

INSERT INTO ruoyi.sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time,
                              update_by, update_time, remark)
VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2023-12-26 16:54:02', '',
        null, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO ruoyi.sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time,
                              update_by, update_time, remark)
VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2023-12-26 16:54:02', '', null,
        '初始化密码 123456');
INSERT INTO ruoyi.sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time,
                              update_by, update_time, remark)
VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2023-12-26 16:54:02', '', null,
        '深色主题theme-dark，浅色主题theme-light');
INSERT INTO ruoyi.sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time,
                              update_by, update_time, remark)
VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2023-12-26 16:54:02', '', null,
        '是否开启验证码功能（true开启，false关闭）');
INSERT INTO ruoyi.sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time,
                              update_by, update_time, remark)
VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2023-12-26 16:54:02',
        '', null, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO ruoyi.sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time,
                              update_by, update_time, remark)
VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2023-12-26 16:54:02', '', null,
        '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
