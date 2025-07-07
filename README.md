# 🍽️ AI食物推荐系统

一个基于AI的智能食物推荐系统，帮助用户根据个人健康目标、饮食偏好和过敏信息获得个性化的营养推荐。

## 📋 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术架构](#技术架构)
- [系统要求](#系统要求)
- [快速开始](#快速开始)
- [详细部署指南](#详细部署指南)
  - [数据库配置](#数据库配置)
  - [前端部署](#前端部署)
  - [后端部署（Node.js版本）](#后端部署nodejs版本)
  - [后端部署（Java版本）](#后端部署java版本)
- [配置说明](#配置说明)
- [API文档](#api文档)
- [常见问题](#常见问题)
- [贡献指南](#贡献指南)

## 🎯 项目简介

AI食物推荐系统是一个现代化的智能营养推荐平台，它结合了人工智能算法和营养学知识，为用户提供个性化的食物推荐。系统支持多种健康目标（减脂、增肌、维持体重等）和饮食偏好（素食、低脂、高蛋白等），并能智能避免用户的过敏食物。

## ✨ 功能特性

- 🤖 **AI智能推荐**：基于先进的AI算法，提供精准的食物推荐
- 🎯 **多目标支持**：支持减脂、增肌、增强免疫力、改善消化等多种健康目标
- 🥗 **营养均衡**：自动计算营养成分，确保推荐的食物搭配均衡
- 🚫 **过敏管理**：智能避免用户过敏的食物，保障食用安全
- 📱 **响应式设计**：完美适配PC、平板和手机等多种设备
- 🔄 **随机化推荐**：避免重复推荐，每次都有新鲜的食物组合
- 🏷️ **智能标签**：自动生成食物标签（低卡、高蛋白、素食等）
- 📊 **详细信息**：提供每种食物的详细营养成分和推荐理由

## 🏗️ 技术架构

### 前端技术栈
- **Vue.js 3** - 渐进式JavaScript框架
- **Vite** - 下一代前端构建工具
- **HTML5/CSS3** - 现代Web标准
- **Responsive Design** - 响应式设计

### 后端技术栈（双版本支持）

#### Node.js版本
- **Node.js + Express** - 高性能JavaScript运行时
- **MySQL2** - MySQL数据库连接器
- **Axios** - HTTP客户端
- **CORS** - 跨域资源共享

#### Java版本  
- **Spring Boot 2.7** - 企业级Java框架
- **Spring Data JPA** - 数据持久化
- **MySQL Connector** - 数据库连接
- **Jackson** - JSON处理
- **Maven** - 项目管理工具

### 数据库
- **MySQL 5.7+** - 关系型数据库
- **完整的营养数据库** - 包含详细的食物营养信息

## 💻 系统要求

### 基础环境
- **操作系统**：Windows 10/11, macOS 10.14+, Ubuntu 18.04+
- **内存**：最少4GB RAM，推荐8GB+
- **硬盘空间**：至少1GB可用空间

### 软件依赖
- **Node.js** >= 16.0.0 ([下载链接](https://nodejs.org/))
- **MySQL** >= 5.7 ([下载链接](https://dev.mysql.com/downloads/mysql/))
- **Java** >= 11 (可选，仅Java后端需要) ([下载链接](https://adoptium.net/))
- **Maven** >= 3.6 (可选，仅Java后端需要) ([下载链接](https://maven.apache.org/download.cgi))

### AI服务
- **Dify平台账号** - 用于AI推荐服务
- **有效的API密钥** - 需要配置AI服务访问凭证

## � 快速开始

### 1️⃣ 克隆项目
```bash
git clone <your-repository-url>
cd aiShiPin
```

### 2️⃣ 数据库初始化
```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 创建数据库
CREATE DATABASE food_recommendation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. 导入数据
USE food_recommendation;
SOURCE food-recommendation/backend/database.sql;
```

### 3️⃣ 选择后端版本部署

#### 选项A：Node.js后端（推荐新手）
```bash
cd food-recommendation/backend
npm install
npm start
```

#### 选项B：Java后端（推荐企业级部署）
```bash
cd food-recommendation/backend-java
mvn clean install
java -jar target/food-recommendation-0.0.1-SNAPSHOT.jar
```

### 4️⃣ 前端部署
```bash
cd food-recommendation/frontend
npm install
npm run dev
```

### 5️⃣ 访问应用
- 前端地址：http://localhost:5173
- 后端API：http://localhost:3000 (Node.js) 或 http://localhost:3001 (Java)

## 📖 详细部署指南

### � Docker容器化部署

#### 环境要求
- Docker >= 20.0
- Docker Compose >= 2.0

#### 一键部署
```bash
# 1. 克隆项目
git clone <your-repository-url>
cd aiShiPin

# 2. 启动所有服务（包含MySQL、后端、前端）
docker-compose up -d

# 3. 查看服务状态
docker-compose ps

# 4. 查看日志
docker-compose logs -f

# 5. 停止服务
docker-compose down
```

#### 选择性部署
```bash
# 只启动数据库和Node.js后端
docker-compose up -d mysql backend-node

# 或启动数据库和Java后端（需要先修改docker-compose.yml）
# 取消注释backend-java服务，注释backend-node服务
docker-compose up -d mysql backend-java

# 启动前端
docker-compose up -d frontend
```

#### 自定义配置
如需修改配置，可以：
1. 编辑 `docker-compose.yml` 文件
2. 修改环境变量
3. 挂载自定义配置文件

### �🗄️ 数据库配置

#### 安装MySQL
1. **Windows用户**：
   - 下载MySQL Installer从官网
   - 选择"Server only"或"Full"安装
   - 设置root密码（推荐：`10086123`）

2. **macOS用户**：
   ```bash
   # 使用Homebrew安装
   brew install mysql
   brew services start mysql
   ```

3. **Linux用户**：
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install mysql-server
   sudo systemctl start mysql
   
   # CentOS/RHEL
   sudo yum install mysql-server
   sudo systemctl start mysqld
   ```

#### 数据库初始化
```sql
-- 1. 创建数据库
CREATE DATABASE food_recommendation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 创建用户（可选，增强安全性）
CREATE USER 'food_app'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON food_recommendation.* TO 'food_app'@'localhost';
FLUSH PRIVILEGES;

-- 3. 使用数据库
USE food_recommendation;

-- 4. 导入表结构和数据
SOURCE food-recommendation/backend/database.sql;
```

### 🎨 前端部署

#### 开发环境部署
```bash
# 1. 进入前端目录
cd food-recommendation/frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

#### 生产环境部署
```bash
# 1. 构建生产版本
npm run build

# 2. 部署到Web服务器
# 将 dist/ 目录内容复制到Web服务器根目录
# 例如：Apache的htdocs或Nginx的html目录
```

#### Nginx配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:3000;  # 或3001（Java版本）
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 🟢 后端部署（Node.js版本）

#### 开发环境部署
```bash
# 1. 进入Node.js后端目录
cd food-recommendation/backend

# 2. 安装依赖
npm install

# 3. 配置环境变量（可选）
cp .env.example .env
# 编辑 .env 文件，配置数据库连接等

# 4. 启动开发服务器
npm run dev  # 开发模式（支持热重载）
# 或
npm start    # 生产模式
```

#### 生产环境部署
```bash
# 1. 使用PM2进程管理器
npm install -g pm2

# 2. 启动应用
pm2 start server.js --name "food-backend-node"

# 3. 设置开机自启
pm2 startup
pm2 save

# 4. 监控应用
pm2 status
pm2 logs food-backend-node
```

#### 配置说明
在 `food-recommendation/backend/server.js` 中修改数据库配置：
```javascript
const dbConfig = {
  host: 'localhost',      // 数据库地址
  user: 'root',          // 数据库用户名
  password: '10086123',   // 数据库密码
  database: 'food_recommendation'
};
```

### ☕ 后端部署（Java版本）

#### 开发环境部署
```bash
# 1. 进入Java后端目录
cd food-recommendation/backend-java

# 2. 检查Java版本
java -version  # 需要Java 11+

# 3. 编译项目
mvn clean compile

# 4. 运行应用
mvn spring-boot:run

# 或使用启动脚本（Windows）
.\start.ps1

# 或使用启动脚本（Linux/macOS）
./start.sh
```

#### 生产环境部署
```bash
# 1. 构建JAR包
mvn clean package -DskipTests

# 2. 运行JAR包
java -jar target/food-recommendation-0.0.1-SNAPSHOT.jar

# 3. 后台运行
nohup java -jar target/food-recommendation-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 4. 使用systemd服务（Linux）
sudo vim /etc/systemd/system/food-backend-java.service
```

#### Systemd服务配置示例
```ini
[Unit]
Description=Food Recommendation Java Backend
After=network.target

[Service]
Type=simple
User=your-user
WorkingDirectory=/path/to/food-recommendation/backend-java
ExecStart=/usr/bin/java -jar target/food-recommendation-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

#### 配置说明
在 `food-recommendation/backend-java/src/main/resources/application.properties` 中配置：
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/food_recommendation?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
spring.datasource.username=root
spring.datasource.password=10086123

# 服务器配置
server.port=3001

# AI服务配置
ai.service.url=http://localhost:8082/v1/chat-messages
ai.service.token=app-uAGYYnfpXdB5t1EdYdNP7wgP
```

## ⚙️ 配置说明

### 后端版本选择建议

| 特性 | Node.js版本 | Java版本 |
|------|-------------|----------|
| **部署难度** | ⭐⭐ 简单 | ⭐⭐⭐ 中等 |
| **性能** | ⭐⭐⭐ 良好 | ⭐⭐⭐⭐ 优秀 |
| **内存占用** | ⭐⭐⭐⭐ 较低 | ⭐⭐ 较高 |
| **企业级特性** | ⭐⭐⭐ 基础 | ⭐⭐⭐⭐⭐ 完整 |
| **适用场景** | 个人项目、快速原型 | 企业应用、大规模部署 |

### 端口配置
- **前端**：5173 (开发环境)
- **Node.js后端**：3000
- **Java后端**：3001
- **MySQL**：3306

### AI服务配置
需要在对应的配置文件中设置AI服务地址和密钥：
```
AI服务地址：http://localhost:8082/v1/chat-messages
API密钥：app-uAGYYnfpXdB5t1EdYdNP7wgP
```

## 📚 API文档

### 主要接口

#### 获取食物推荐
```http
POST /api/recommendations
Content-Type: application/json

{
  "userId": "user-123",
  "filters": {
    "healthGoals": ["lose-weight", "gain-muscle"],
    "dietPreferences": ["high-protein", "low-fat"],
    "allergies": "无"
  }
}
```

#### 健康检查
```http
GET /api/health
```

#### 获取食物图片
```http
GET /images/{category}/{foodName}.jpeg
```

### 响应格式
```json
{
  "id": 1,
  "name": "三文鱼",
  "calories": 208.0,
  "protein": 20.4,
  "carbs": 0.0,
  "fat": 13.4,
  "image": "/images/海鲜类/三文鱼.jpg",
  "tags": ["海鲜类", "high-protein", "low-sugar"],
  "description": "富含优质蛋白质和Omega-3脂肪酸"
}
```

## ❓ 常见问题

### 部署问题

**Q: 数据库连接失败？**
A: 检查MySQL服务是否启动，用户名密码是否正确，防火墙设置是否允许连接。

**Q: 前端无法访问后端API？**
A: 检查CORS配置，确保后端允许前端域名的跨域请求。

**Q: Java后端启动失败？**
A: 检查Java版本（需要11+），Maven依赖是否正确下载，端口是否被占用。

**Q: Docker容器启动失败？**
A: 
- 确保Docker服务正在运行
- 检查端口是否被占用：`docker-compose down` 然后重新启动
- 查看具体错误：`docker-compose logs [service-name]`
- 清理Docker缓存：`docker system prune -a`

### 网络问题

**Q: 局域网其他设备访问时页面闪烁？**
A: 这是静态资源加载问题，解决方案：
1. 获取本机IP地址：`ipconfig`（Windows）或 `ifconfig`（Linux/macOS）
2. 修改前端API配置：将localhost改为实际IP地址
3. 配置防火墙允许相应端口访问
4. 详细解决方案请参考 [DEPLOYMENT.md](DEPLOYMENT.md)

**Q: 前端可以访问但API调用失败？**
A: 
- 检查后端服务是否正常运行
- 确认API URL配置正确
- 查看浏览器控制台网络错误信息
- 检查CORS配置是否允许跨域访问

### 功能问题

**Q: AI推荐返回空结果？**
A: 检查AI服务配置是否正确，API密钥是否有效，网络连接是否正常。

**Q: 食物图片无法显示？**
A: 确保images目录存在，图片文件完整，静态资源服务配置正确。

**Q: 推荐结果总是相同的几个食物？**
A: 已在最新版本中修复，现在支持智能随机化推荐，每次结果都会不同。

### 性能问题

**Q: 推荐响应较慢？**
A: 可能是AI服务响应慢，可以考虑添加缓存机制或优化数据库查询。

**Q: 内存占用过高？**
A: Java版本可以调整JVM参数，Node.js版本可以使用集群模式。

**Q: Docker容器占用资源过多？**
A: 
- 调整Docker内存限制：在docker-compose.yml中添加 `mem_limit`
- 优化JVM参数：减小 `-Xms` 和 `-Xmx` 值
- 使用alpine版本的基础镜像

### 开发问题

**Q: 热重载不工作？**
A: 
- 确保使用开发模式启动：`npm run dev`
- 检查文件监听权限
- 在Docker中开发时需要配置volume映射

**Q: 依赖安装失败？**
A: 
- 清理npm缓存：`npm cache clean --force`
- 删除node_modules重新安装：`rm -rf node_modules && npm install`
- 检查网络连接和npm源配置

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献
1. Fork本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

### 代码规范
- 使用ESLint进行JavaScript代码检查
- 遵循Google Java Style Guide（Java版本）
- 添加适当的注释和文档
- 确保所有测试通过

### 报告问题
在[Issues页面](../../issues)创建新的issue，请提供：
- 详细的问题描述
- 重现步骤
- 系统环境信息
- 相关的错误日志

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 🙏 致谢

- Vue.js团队提供的优秀前端框架
- Spring Boot团队提供的企业级Java框架
- MySQL团队提供的可靠数据库系统
- 所有为开源社区做出贡献的开发者们

---

如果这个项目对您有帮助，请给我们一个⭐！
