# 使用支持 Java 8 的 Maven 镜像
FROM maven:3.9.9-eclipse-temurin-8 AS build

# 设置工作目录
WORKDIR /app

# 拷贝所有项目文件
COPY . .

# 编译并打包
RUN mvn clean package

FROM openjdk:8-jdk
LABEL maintainer="EndlessManager"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建工作目录
WORKDIR /app

# 复制jar包
COPY --from=build /app/endless-admin/target/endless-manager.jar ./app.jar

# 暴露端口
EXPOSE 8080

# 直接启动，不指定外部配置文件位置
ENTRYPOINT ["sh", "-c", "java -Xmx1G $JAVA_OPTS -jar app.jar"]