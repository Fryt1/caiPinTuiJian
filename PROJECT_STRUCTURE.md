# 项目结构说明

```
aiShiPin/
├── README.md                           # 主要项目文档
├── DEPLOYMENT.md                       # 局域网部署配置指南
├── docker-compose.yml                  # Docker编排配置
├── 
├── food-recommendation/                # 主应用目录
│   ├── frontend/                       # Vue.js前端
│   │   ├── src/                        # 源代码
│   │   │   ├── components/             # Vue组件
│   │   │   ├── services/               # API服务
│   │   │   └── ...
│   │   ├── public/                     # 静态资源
│   │   ├── package.json                # 前端依赖配置
│   │   ├── vite.config.js              # Vite构建配置
│   │   ├── Dockerfile                  # 前端容器化配置
│   │   └── nginx.conf                  # Nginx配置
│   │
│   ├── backend/                        # Node.js后端
│   │   ├── server.js                   # 主服务器文件
│   │   ├── database.sql                # 数据库初始化脚本
│   │   ├── package.json                # 后端依赖配置
│   │   ├── .env.example                # 环境变量示例
│   │   └── Dockerfile                  # 后端容器化配置
│   │
│   └── backend-java/                   # Java Spring Boot后端
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/aishipin/  # Java源代码
│       │   │   │   ├── controller/     # 控制器
│       │   │   │   ├── service/        # 业务逻辑
│       │   │   │   ├── repository/     # 数据访问层
│       │   │   │   ├── entity/         # 实体类
│       │   │   │   ├── dto/            # 数据传输对象
│       │   │   │   └── config/         # 配置类
│       │   │   └── resources/
│       │   │       └── application.properties
│       │   └── test/                   # 测试代码
│       ├── pom.xml                     # Maven配置
│       ├── start.ps1                   # Windows启动脚本
│       ├── start.sh                    # Linux/macOS启动脚本
│       └── Dockerfile                  # Java后端容器化配置
│
└── images/                             # 食物图片资源
    ├── 豆类及制品/                     # 豆类食物图片
    ├── 谷类及制品/                     # 谷类食物图片
    ├── 海鲜类/                         # 海鲜类食物图片
    ├── 蔬菜类/                         # 蔬菜类食物图片
    └── 水果类/                         # 水果类食物图片
```

## 文件说明

### 📁 根目录文件
- **README.md**: 完整的项目文档，包含部署指南和使用说明
- **DEPLOYMENT.md**: 专门解决局域网访问问题的配置指南
- **docker-compose.yml**: Docker容器编排，一键部署所有服务

### 🎨 前端 (frontend/)
- **Vue.js 3** + **Vite** 构建的现代前端应用
- 响应式设计，支持PC和移动端
- 包含完整的Docker配置用于生产环境部署

### 🟢 Node.js后端 (backend/)
- 轻量级，适合快速开发和小型项目
- 包含完整的API接口和数据库交互
- 支持环境变量配置

### ☕ Java后端 (backend-java/)
- 企业级Spring Boot应用
- 完整的MVC架构
- 包含启动脚本和Docker配置
- 适合大规模生产环境

### 🖼️ 图片资源 (images/)
- 按食物类别组织的图片资源
- 支持JPEG和PNG格式
- 通过静态文件服务提供访问

## 部署选择指南

### 🐳 Docker部署（推荐）
适合所有用户，特别是：
- 想要快速体验的用户
- 生产环境部署
- 需要隔离环境的场景

### 📦 手动部署
适合：
- 开发环境
- 需要自定义配置的场景
- 学习项目结构的开发者

### 后端选择
- **Node.js版本**: 简单、快速、适合个人项目
- **Java版本**: 企业级、高性能、适合生产环境

## 关键配置文件

### 数据库配置
- Node.js: `backend/server.js` 中的 `dbConfig`
- Java: `backend-java/src/main/resources/application.properties`

### API配置
- 前端API地址: `frontend/src/services/recommendationService.js`
- AI服务配置: 后端配置文件中的AI相关设置

### 网络配置
- CORS设置: 后端配置文件
- 静态资源路径: 各后端的静态文件服务配置

这个项目结构支持多种部署方式，可以根据实际需求选择最适合的部署方案。
