# 部署指南

## 开发环境部署

### 1. 环境要求
- Java 8+
- Maven 3.6+
- MySQL 8.0
- curl (用于API测试)

### 2. 快速启动步骤

1. **配置数据库**
```bash
# 启动MySQL服务
# Windows: net start mysql
# Linux/Mac: sudo systemctl start mysql

# 创建数据库并导入数据
mysql -u root -p
CREATE DATABASE food_recommendation;
exit;

mysql -u root -p food_recommendation < ../database.sql
```

2. **配置应用**
```bash
# 复制配置文件模板
cp src/main/resources/application.properties.example src/main/resources/application.properties

# 编辑配置文件，修改数据库密码和AI服务令牌
```

3. **启动应用**
```bash
# Windows
start.bat

# Linux/Mac
chmod +x start.sh
./start.sh
```

4. **测试API**
```bash
# Windows
test-api.bat

# Linux/Mac
chmod +x test-api.sh
./test-api.sh
```

## 生产环境部署

### 1. 打包应用
```bash
mvn clean package -DskipTests
```

### 2. 部署到服务器
```bash
# 复制JAR文件到服务器
scp target/food-recommendation-0.0.1-SNAPSHOT.jar user@server:/opt/food-recommendation/

# 在服务器上启动
java -jar /opt/food-recommendation/food-recommendation-0.0.1-SNAPSHOT.jar
```

### 3. 使用Docker部署 (可选)
```dockerfile
# Dockerfile
FROM openjdk:8-jre-slim
COPY target/food-recommendation-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 3000
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
# 构建Docker镜像
docker build -t food-recommendation .

# 运行容器
docker run -p 3000:3000 -e SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/food_recommendation food-recommendation
```

## 监控和维护

### 1. 健康检查
```bash
curl http://localhost:3000/api/health
```

### 2. 应用日志
日志文件位置：`logs/spring.log` (如果配置了文件输出)

### 3. 性能监控
可以集成Spring Boot Actuator进行更详细的监控。

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 验证数据库用户名密码
   - 确认数据库名称正确

2. **AI服务调用失败**
   - 检查AI服务是否可访问
   - 验证API令牌是否正确
   - 查看网络连接

3. **图片资源访问失败**
   - 确认images目录路径正确
   - 检查文件权限

### 调试模式
```bash
# 启用详细日志
java -jar app.jar --logging.level.com.aishipin=DEBUG
```
