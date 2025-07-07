# Food Recommendation Backend (Java)

这是食物推荐系统的Java后端实现，使用Spring Boot框架重写了原有的Node.js后端。

## 技术栈

- **Java 8+**
- **Spring Boot 2.7.12**
- **Spring Data JPA**
- **MySQL 8.0**
- **Maven**
- **Lombok**

## 项目结构

```
backend-java/
├── src/main/java/com/aishipin/
│   ├── FoodRecommendationApplication.java  # 主启动类
│   ├── controller/                         # 控制器层
│   │   └── FoodRecommendationController.java
│   ├── service/                           # 服务层
│   │   ├── RecommendationService.java     # 推荐服务
│   │   └── AiService.java                 # AI服务
│   ├── entity/                            # 实体类
│   │   ├── Food.java
│   │   ├── User.java
│   │   ├── UserHealthInfo.java
│   │   └── RecommendationHistory.java
│   ├── repository/                        # 数据访问层
│   │   ├── FoodRepository.java
│   │   ├── UserHealthInfoRepository.java
│   │   └── RecommendationHistoryRepository.java
│   ├── dto/                               # 数据传输对象
│   │   ├── RecommendationRequest.java
│   │   ├── FoodResponse.java
│   │   └── AiDto.java
│   └── config/                            # 配置类
│       └── WebConfig.java
├── src/main/resources/
│   └── application.properties             # 应用配置
├── pom.xml                               # Maven配置
└── README.md                             # 项目说明
```

## 主要功能

### 1. 健康检查 
- **端点**: `GET /api/health`
- **功能**: 检查服务状态和数据库连接

### 2. 智能食物推荐
- **端点**: `POST /api/recommendations`
- **功能**: 基于用户健康目标和饮食偏好，结合AI服务推荐最适合的食物

## 核心特性

### 智能推荐算法
1. **用户画像分析**: 解析用户健康目标（减脂、增肌、增强免疫力等）
2. **饮食偏好匹配**: 支持素食、低脂、低糖、高蛋白等多种偏好
3. **AI增强**: 集成外部AI服务，提供个性化推荐理由
4. **营养标签**: 自动生成食物营养标签（低热量、高蛋白等）
5. **过敏源规避**: 严格避免用户过敏的食物

### 推荐策略
- **多目标支持**: 支持用户同时选择多个健康目标
- **智能补充**: AI推荐不足时，基于规则的默认推荐进行补充
- **营养均衡**: 确保推荐结果涵盖多种食物类别
- **图片支持**: 自动构建食物图片URL

## 快速开始

### 1. 环境准备
```bash
# 确保已安装以下软件：
# - Java 8 或以上版本
# - Maven 3.6+
# - MySQL 8.0
```

### 2. 数据库设置
```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE food_recommendation;

# 2. 导入数据库结构和数据
mysql -u root -p food_recommendation < ../database.sql
```

### 3. 配置应用
编辑 `src/main/resources/application.properties`：
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/food_recommendation?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=你的密码

# AI服务配置
ai.service.url=http://localhost:8082/v1/chat-messages
ai.service.token=你的AI服务令牌
```

### 4. 启动应用
```bash
# 使用Maven启动
mvn spring-boot:run

# 或者编译后运行
mvn clean package
java -jar target/food-recommendation-0.0.1-SNAPSHOT.jar
```

### 5. 验证服务
```bash
# 健康检查
curl http://localhost:3000/api/health

# 测试推荐接口
curl -X POST http://localhost:3000/api/recommendations \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "filters": {
      "healthGoals": ["lose-weight"],
      "dietPreferences": ["low-fat"],
      "allergies": "无"
    }
  }'
```

## API文档

### 推荐请求示例
```json
{
  "userId": "user-123",
  "filters": {
    "healthGoals": ["lose-weight", "improve-immunity"],
    "dietPreferences": ["vegetarian", "low-fat"],
    "allergies": "花生,海鲜"
  }
}
```

### 推荐响应示例
```json
[
  {
    "id": 32,
    "name": "西兰花",
    "calories": 27.00,
    "protein": 3.70,
    "carbs": 4.90,
    "fat": 0.40,
    "image": "http://localhost:3000/images/蔬菜类/西兰花.jpeg",
    "tags": ["十字花科", "low-calorie", "low-fat", "vegetarian"],
    "description": "抗癌明星蔬菜，维生素C含量是柠檬2倍"
  }
]
```

## 与Node.js版本的对比

| 特性 | Node.js版本 | Java版本 |
|------|-------------|----------|
| 框架 | Express.js | Spring Boot |
| 数据库操作 | mysql2 | Spring Data JPA |
| 依赖注入 | 无 | Spring IOC |
| 配置管理 | 硬编码 | application.properties |
| 错误处理 | try-catch | Spring异常处理 |
| 代码组织 | 单文件 | 分层架构 |
| 类型安全 | JavaScript | Java强类型 |
| 性能 | 异步I/O | JVM优化 |

## 开发说明

### 添加新的健康目标
1. 在 `AiService.buildPrompt()` 方法中添加新目标的处理逻辑
2. 在 `RecommendationService.getDefaultRecommendations()` 中添加对应的默认推荐

### 添加新的饮食偏好
1. 在 `AiService.formatDietPreferences()` 中添加新偏好的中文映射
2. 在推荐逻辑中添加相应的过滤规则

### 自定义营养标签
在 `RecommendationService.formatFoodResponse()` 方法中修改标签生成逻辑。

## 日志监控

应用使用SLF4J + Logback进行日志记录：
- **INFO级别**: 记录请求处理流程
- **DEBUG级别**: 记录详细的AI交互信息
- **ERROR级别**: 记录异常和错误信息

## 性能优化

1. **数据库连接池**: 使用HikariCP连接池
2. **缓存**: 可添加Redis缓存常用推荐结果
3. **异步处理**: AI服务调用可改为异步执行
4. **分页**: 大量数据查询支持分页

## 扩展计划

- [ ] 添加用户认证和授权
- [ ] 实现推荐历史记录功能
- [ ] 添加食物收藏和评分
- [ ] 支持个人营养目标设定
- [ ] 集成更多AI服务提供商
- [ ] 添加API限流和监控
