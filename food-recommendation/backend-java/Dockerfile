# Java后端Dockerfile
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 安装必要的包
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制JAR文件
COPY target/food-recommendation-*.jar app.jar

# 创建非root用户
RUN adduser --disabled-password --gecos '' appuser
USER appuser

# 暴露端口
EXPOSE 3001

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:3001/api/health || exit 1

# JVM参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# 启动命令
CMD java $JAVA_OPTS -jar app.jar
