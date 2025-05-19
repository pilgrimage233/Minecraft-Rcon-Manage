# 编译静态文件
FROM node:16 AS build

# 设置工作目录
WORKDIR /app

# 复制 package.json
COPY package*.json ./

# 安装依赖
RUN npm install

# 复制所有源码
COPY . .

# 编译 Vue 项目
RUN npm run build:prod

FROM nginx:stable-alpine
LABEL maintainer="EndlessManager"

# 删除默认的 nginx 主页
RUN rm -rf /usr/share/nginx/html/*

# 复制编译后的文件到 nginx 的 html 目录
COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
