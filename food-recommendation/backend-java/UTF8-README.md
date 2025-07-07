# 中文编码问题解决方案

本项目已经配置了完整的UTF-8编码支持，解决Windows环境下的中文乱码问题。

## 启动脚本说明

### 1. start-utf8.bat（推荐开发使用）
- 专门为解决中文编码问题设计
- 自动设置UTF-8环境
- 适用于开发调试

### 2. start-prod.bat（生产环境）
- 直接运行已编译的JAR文件
- 优化的JVM参数
- 适用于生产部署

### 3. start.bat（原始脚本）
- 已更新支持UTF-8编码
- 兼容原有启动方式

## 使用方法

### 开发环境
```batch
# 直接双击运行或在cmd中执行
start-utf8.bat
```

### 生产环境
```batch
# 首先构建项目
mvn clean package

# 然后启动服务
start-prod.bat
```

## 配置说明

### 1. Logback配置
- 创建了 `logback-spring.xml` 配置文件
- 设置控制台和文件输出均为UTF-8编码
- 配置了日志轮转和保留策略

### 2. Maven配置
- 在 `pom.xml` 中添加了完整的编码配置
- 设置项目源码、编译输出、报告均为UTF-8
- 配置Maven编译器和Spring Boot插件的JVM参数

### 3. Spring Boot配置
- 在 `application.properties` 中配置HTTP编码
- 设置数据库连接字符集为UTF-8
- 配置日志输出编码

## 验证方法

启动应用后，中文日志应该正常显示，例如：
```
2025-07-07 16:32:11.813  INFO 29308 --- [nio-3001-exec-2] c.a.service.RecommendationService : 推荐参数 - 健康目标: [维持], 饮食偏好: [high-protein], 过敏信息: 无
```

## 故障排除

如果仍然出现乱码：

1. **检查命令行编码**
   ```batch
   chcp 65001
   ```

2. **检查JAVA_TOOL_OPTIONS环境变量**
   ```batch
   set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
   ```

3. **使用PowerShell替代CMD**
   PowerShell对UTF-8支持更好

4. **检查IDE设置**
   确保IDE（如IntelliJ IDEA、Eclipse）的文件编码设置为UTF-8
