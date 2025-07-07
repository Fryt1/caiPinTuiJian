# 🚀 Food Recommendation 后端运行指南

## 📋 运行前准备

### 1. 环境要求检查
- ✅ Java 11 或更高版本
- ✅ Maven 3.6+ 
- ✅ MySQL 8.0+
- ✅ Git（可选）

### 2. 检查环境是否安装正确

打开命令行（CMD或PowerShell），运行以下命令：

```bash
# 检查Java版本
java -version

# 检查Maven版本  
mvn -version

# 检查MySQL服务状态
net start | findstr MySQL
```

## 🗄️ 数据库准备

### 1. 启动MySQL服务
```bash
# 启动MySQL服务
net start MySQL80
```

### 2. 创建数据库
```sql
-- 连接到MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE food_recommendation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE food_recommendation;

-- 退出MySQL
EXIT;
```

### 3. 导入数据（如果有数据文件）
```bash
# 如果有database.sql文件
mysql -u root -p food_recommendation < database.sql
```

## ⚙️ 配置检查

### 1. 数据库连接配置
检查 `src/main/resources/application.properties` 文件：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/food_recommendation?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&useUnicode=true
spring.datasource.username=root
spring.datasource.password=10086123  # 修改为你的MySQL密码
```

### 2. AI服务配置（可选）
```properties
ai.service.url=http://localhost:8082/v1/chat-messages
ai.service.token=app-uAGYYnfpXdB5t1EdYdNP7wgP
```

## 🏃‍♂️ 运行方式

### 方式1：使用UTF-8脚本（推荐）
```bash
# 双击运行或在命令行执行
start-utf8.bat
```

### 方式2：使用Maven命令
```bash
# 清理并编译
mvn clean compile

# 直接运行
mvn spring-boot:run
```

### 方式3：编译后运行JAR包
```bash
# 编译打包
mvn clean package

# 运行JAR包
start-prod.bat
```

### 方式4：手动Java命令
```bash
# 编译打包
mvn clean package

# 手动运行
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar target/food-recommendation-0.0.1-SNAPSHOT.jar
```

## 🔍 验证运行状态

### 1. 检查启动日志
正常启动后，应该看到类似输出：
```
2025-07-07 16:32:11.813  INFO --- [main] com.aishipin.FoodRecommendationApplication : Started FoodRecommendationApplication in 3.456 seconds
2025-07-07 16:32:11.814  INFO --- [main] .ConditionEvaluationReportLoggingListener : 

============================
CONDITIONS EVALUATION REPORT
============================
```

### 2. 测试API接口
打开浏览器或使用Postman测试：

**健康检查：**
```
GET http://localhost:3001/health
```

**获取所有食物：**
```
GET http://localhost:3001/api/foods
```

**获取推荐：**
```
POST http://localhost:3001/api/recommendations
Content-Type: application/json

{
  "userId": "user-123",
  "healthGoals": ["维持"],
  "dietaryPreferences": ["high-protein"],
  "allergies": []
}
```

## ❌ 常见问题解决

### 1. 端口被占用
```
Error: Port 3001 is already in use
```
**解决方案：**
- 修改 `application.properties` 中的 `server.port=3002`
- 或者杀死占用端口的进程

### 2. 数据库连接失败
```
Connection refused to localhost:3306
```
**解决方案：**
- 检查MySQL服务是否启动：`net start MySQL80`
- 检查用户名密码是否正确
- 检查数据库是否存在

### 3. 中文乱码
```
????????????
```
**解决方案：**
- 使用 `start-utf8.bat` 启动
- 或者运行 `test-encoding.bat` 检查编码

### 4. Maven依赖下载失败
**解决方案：**
```bash
# 清理并重新下载依赖
mvn clean
mvn dependency:resolve
```

## 🔧 开发调试

### 1. 开启调试模式
在 `application.properties` 中添加：
```properties
logging.level.com.aishipin=DEBUG
logging.level.org.springframework.web=DEBUG
```

### 2. 使用IDE运行
- 导入项目到IntelliJ IDEA或Eclipse
- 找到 `FoodRecommendationApplication.java`
- 右键选择 "Run" 或 "Debug"

### 3. 热重载（可选）
添加Spring Boot DevTools依赖实现代码热重载

## 📞 快速启动命令

```bash
# 一键启动（确保MySQL已启动）
cd d:\work\vscode\aiShiPin\food-recommendation\backend-java
start-utf8.bat
```

启动成功后，访问：http://localhost:3001
