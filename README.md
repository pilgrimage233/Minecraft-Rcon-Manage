# 若依框架 Minecraft 服务器管理系统使用教程

# Minecraft 服务器管理系统

基于 RuoYi-Vue 框架开发的 Minecraft 服务器管理系统，提供白名单管理、OP管理、玩家管理等功能。

## 功能特性

### 1. 服务器管理

- 多服务器统一管理
- 服务器状态监控
- 实时在线玩家查看
- RCON 远程控制
- 服务器指令管理
- 在线Web终端

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
- 封禁管理

### 4. 管理员功能

- OP 权限管理
- 封禁系统
- 指令执行记录
- 操作日志记录
- 数据统计报表

### 5. 定时指令

- 定时指令执行
- 定时指令记录
- 定时指令统计

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

- JDK 1.8
- Maven 3.2
- MySQL 5.7+
- Redis 5.0+
- Node.js 16

### 开发环境部署

#### 1. 克隆项目

```bash
git clone [项目地址]
```

#### 2. 初始化数据库

```bash
# 创建数据库并执行SQL脚本
create database minecraft_manager character set utf8mb4 collate utf8mb4_general_ci;
```

然后导入项目根目录下的 `sql` 文件夹中的脚本

#### 3. 修改配置

修改后端配置文件：

```yaml
# 修改 ruoyi-admin/src/main/resources/application.yml 中的项目配置
ruoyi:
   # 文件路径 示例（Windows配置D:/ruoyi/uploadPath，Linux配置 /home/ruoyi/uploadPath）
   profile: D:/ruoyi/uploadPath

# 修改 ruoyi-admin/src/main/resources/application-druid.yml 中的数据库连接信息
spring:
  datasource:
     druid:
        master:
           url: jdbc:mysql://localhost:3306/minecraft_manager?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=Asia/Shanghai
           username: your_username
           password: your_password

# 修改 Redis 配置
spring:
   redis:
      host: 127.0.0.1
      port: 6379
      password: 
```

修改前端配置文件：

```javascript
// 修改 ruoyi-ui/vue.config.js 中的target API接口地址
  devServer: {
    host: '0.0.0.0', // 如果要支持ipv6，可以设置成 ::，这样既支持ipv4又支持ipv6
    port: port,
    open: true,
    proxy: {
      // detail: https://cli.vuejs.org/config/#devserver-proxy
      [process.env.VUE_APP_BASE_API]: {
        target: `http://localhost:8080`,
        changeOrigin: true,
        pathRewrite: {
          ['^' + process.env.VUE_APP_BASE_API]: ''
        }
      }
    },
    disableHostCheck: true
  }
```

#### 4. 启动服务

后端服务：

```bash
# 进入项目根目录
cd 项目根目录

# Windows环境使用
ry.bat

# Linux环境使用
./ry.sh
```

前端服务：

```bash
# 进入前端项目目录
cd ruoyi-ui

# 安装依赖
npm install

# 建议不要直接使用 cnpm 安装依赖，会有各种诡异的 bug。可以通过如下操作解决 npm 下载速度慢的问题
npm install --registry=https://registry.npmmirror.com

# 启动服务
npm run dev
```

启动成功后浏览器访问 http://localhost:80

### 生产环境部署

#### 1. 打包项目

后端打包：

```bash
# 在项目根目录执行
mvn clean package
```

前端打包：

```bash
# 在 ruoyi-ui 目录下执行
npm run build:prod
```

#### 2. 部署项目

后端部署：

```bash
# 将打包好的jar包上传到服务器
java -jar ruoyi-admin.jar
```

前端部署：

```bash
# 将打包好的dist目录上传到Nginx的html目录下
# 配置Nginx
server {
    listen       80;
    server_name  localhost;

    location / {
        root   /usr/share/nginx/html/dist;
        try_files $uri $uri/ /index.html;
        index  index.html index.htm;
    }
    
    location /prod-api/ {
        proxy_pass http://localhost:8080/;
    }
}
```

白名单申请前端部署：
页面已独立于其他仓库，具体请移步：https://github.com/pilgrimage233/whitelist-vue

```javascript
// 此前端需要切换到Node.js 18版本
// 此前端需要切换到Node.js 18版本
// 此前端需要切换到Node.js 18版本
// 如果觉得麻烦可以选择cloudflare pages部署 免费 免费 免费
// 修改接口地址 whitelist/.env   这个地方可以写后端地址，但我更推荐写后台代理后的地址，就是Ruoyi_ui部署后的域名+prod-api，比如我的是http://localhost:80/prod-api
VITE_API_URL = http://localhost:8081
```

## 系统使用指南

### 1. 系统登录

- 默认管理员账号：admin
- 默认密码：admin123
- 首次登录建议修改默认密码

### 2. 服务器配置

1. 进入【系统管理】-【服务器管理】
2. 点击【新增】按钮添加Minecraft服务器
3. 填写服务器信息：
   - 服务器名称
   - 服务器IP
   - 服务器端口
   - RCON端口
   - RCON密码

### 3. 白名单管理

1. 进入【白名单管理】-【申请列表】
2. 可以查看所有白名单申请
3. 点击【审核】按钮进行审核
4. 审核通过后，系统会自动将玩家添加到服务器白名单

### 4. 玩家管理

1. 进入【玩家管理】-【玩家列表】
2. 可以查看所有玩家信息
3. 点击【详情】按钮查看玩家详细信息
4. 可以进行封禁、解封等操作

### 5. 定时任务

1. 进入【系统管理】-【定时任务】
2. 可以添加定时执行的Minecraft指令
3. 支持cron表达式配置执行时间

## 常见问题

### 1. 无法连接到数据库

- 检查数据库连接配置是否正确
- 确保MySQL服务已启动
- 检查数据库用户权限

### 2. Redis连接失败

- 检查Redis服务是否启动
- 确认Redis连接配置是否正确
- 检查Redis密码是否正确

### 3. RCON连接失败

- 确保Minecraft服务器已开启RCON功能
- 检查RCON端口是否正确
- 验证RCON密码是否正确

### 4. 前端页面无法访问

- 检查前端服务是否启动
- 确认API接口地址配置是否正确
- 检查浏览器控制台是否有错误信息

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

### 1. RCON 连接

- 确保 Minecraft 服务器已开启 RCON 功能
- 在 server.properties 中设置：

```properties
enable-rcon=true
rcon.password=your_password
rcon.port=25575
```

### 2. 邮件通知

- 配置邮件服务器信息
- 支持阿里云邮件推送服务

### 3. 若依框架相关

本项目基于若依框架开发，若需了解更多若依框架的使用方法，请参考：

- [若依官方文档](http://doc.ruoyi.vip/)
- [若依开发手册](http://doc.ruoyi.vip/ruoyi-vue/)

### 4. 目录结构

```
├── endless-admin        // 主启动模块
├── endless-common       // 通用模块
├── endless-framework    // 核心框架
├── endless-generator    // 代码生成
├── endless-quartz       // 定时任务
├── endless-system       // 系统管理
├── endless-ui           // 前端UI
└── mc-server            // Minecraft服务器管理模块
```

## 贡献指南

欢迎提交 Issue 和 Pull Request

## 许可证

本项目使用 MIT 许可证

## 联系方式

Email: admin@mcpeach.cc

QQ群：702055554

## 致谢

- [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)
- [Element UI](https://element.eleme.cn)
