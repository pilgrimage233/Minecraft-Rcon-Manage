# Minecraft 服务器管理系统

基于 RuoYi-Vue 框架开发的 Minecraft 服务器管理系统，提供白名单管理、OP管理、玩家管理等功能。

## 功能特性

### 1. 服务器管理

- 多服务器统一管理
- 服务器状态监控
- 实时在线玩家查看
- RCON 远程控制
- 服务器指令管理

### 2. 白名单管理

- 白名单申请与审核
- 在线/离线账号支持
- 自动验证正版账号
- 邮件通知功能
- IP 限流保护

### 3. 玩家管理

- 玩家信息统计
- 游戏时长记录
- 历史名称记录
- 玩家行为追踪
- 地理位置统计

### 4. 管理员功能

- OP 权限管理
- 封禁系统
- 指令执行记录
- 操作日志记录
- 数据统计报表

## 技术栈

### 后端技术

- Spring Boot
- MyBatis
- Redis
- MySQL
- JWT

### 前端技术

- Vue.js
- Element UI
- Axios
- Vue Router
- Vuex

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.0+
- MySQL 5.7+
- Redis 5.0+
- Node.js 12+

### 开发环境部署

1. 克隆项目

```bash
git clone [项目地址]
```

2. 初始化数据库

```bash
# 创建数据库并执行SQL脚本
create database minecraft_manager character set utf8mb4 collate utf8mb4_general_ci;
```

3. 修改配置

```yaml
# 修改 application.yml 中的数据库连接信息
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/minecraft_manager
    username: your_username
    password: your_password
```

4. 启动服务

```bash
# 后端服务
cd ruoyi-admin
mvn spring-boot:run

# 前端服务
cd ruoyi-ui
npm install
npm run dev
```

## 系统截图

### 前台申请

#### 白名单申请

![申请表单](img/img_2.png)

#### 成员列表

![成员列表](img/img_3.png)

#### 玩家详情

![玩家详情](img/img_4.png)

### 后台管理

#### 首页

![首页](img/img.png)

#### 白名单管理

![白名单管理](img/img_1.png)

#### 玩家管理

![封禁管理](img/img_6.png)

#### IP 限流

![IP限流](img/img_5.png)

## 特别说明

1. RCON 连接

- 确保 Minecraft 服务器已开启 RCON 功能
- 在 server.properties 中设置：

```properties
enable-rcon=true
rcon.password=your_password
rcon.port=25575
```

2. 邮件通知

- 配置邮件服务器信息
- 支持阿里云邮件推送服务

## 贡献指南

欢迎提交 Issue 和 Pull Request

## 许可证

本项目使用 MIT 许可证

## 联系方式

Email: admin@mcpeach.cc
QQ: 996111175

## 致谢

- [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)
- [Element UI](https://element.eleme.cn)