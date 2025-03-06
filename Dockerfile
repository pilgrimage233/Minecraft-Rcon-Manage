FROM openjdk:8-jdk
LABEL maintainer="EndlessManager"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建工作目录
WORKDIR /app

# 复制jar包
COPY ./ruoyi-admin/target/endless-manager.jar ./app.jar

# 暴露端口
EXPOSE 8080

# 直接启动，不指定外部配置文件位置
ENTRYPOINT ["java", "-jar", "app.jar"]