FROM nginx:stable-alpine
LABEL maintainer="EndlessManager"

# 复制构建文件到nginx目录
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
