# 局域网部署配置指南

## 问题：局域网访问时页面闪烁

当其他设备通过局域网访问时，可能出现食物卡片闪烁的问题。这通常是由以下原因造成的：

### 常见原因和解决方案

#### 1. 静态资源访问问题

**原因**：图片等静态资源通过localhost访问，局域网设备无法加载

**解决方案**：
- 修改后端配置，使用局域网IP而非localhost
- 配置正确的CORS策略

#### 2. 前端配置修改

在 `food-recommendation/frontend/src/services/recommendationService.js` 中：

```javascript
// 修改前（本机访问）
const API_BASE_URL = 'http://localhost:3000';

// 修改后（局域网访问）
const API_BASE_URL = 'http://YOUR_LOCAL_IP:3000';  // 例如：http://192.168.1.100:3000
```

#### 3. 后端CORS配置

##### Node.js版本
在 `food-recommendation/backend/server.js` 中：

```javascript
// 修改CORS配置
app.use(cors({
  origin: ['http://localhost:5173', 'http://YOUR_LOCAL_IP:5173', 'http://192.168.1.*'],
  credentials: true
}));

// 修改静态资源服务
app.use('/images', express.static(path.join(__dirname, '../../images'), {
  setHeaders: (res, path) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
  }
}));
```

##### Java版本
在 `food-recommendation/backend-java/src/main/java/com/aishipin/config/WebConfig.java` 中：

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOriginPatterns("*")  // 允许所有域名
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
}
```

#### 4. 获取本机IP地址

**Windows:**
```cmd
ipconfig
# 查找 "IPv4 地址" 或 "IP Address"
```

**macOS/Linux:**
```bash
ifconfig | grep inet
# 或
ip addr show
```

#### 5. 防火墙配置

确保防火墙允许对应端口的访问：

**Windows防火墙:**
1. 打开控制面板 → 系统和安全 → Windows Defender 防火墙
2. 点击"允许应用或功能通过Windows Defender防火墙"
3. 添加端口3000（Node.js）或3001（Java）和5173（前端）

**Linux防火墙:**
```bash
# Ubuntu/Debian
sudo ufw allow 3000
sudo ufw allow 3001
sudo ufw allow 5173

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=3000/tcp
sudo firewall-cmd --permanent --add-port=3001/tcp
sudo firewall-cmd --permanent --add-port=5173/tcp
sudo firewall-cmd --reload
```

#### 6. 完整的局域网部署步骤

1. **获取本机IP地址**
   ```bash
   ipconfig  # Windows
   ifconfig  # macOS/Linux
   ```

2. **修改前端配置**
   ```javascript
   // 在recommendationService.js中
   const API_BASE_URL = 'http://192.168.1.100:3000';  // 替换为实际IP
   ```

3. **启动后端服务**
   ```bash
   # Node.js版本
   cd food-recommendation/backend
   npm start
   
   # 或Java版本
   cd food-recommendation/backend-java
   ./start.sh  # Linux/macOS
   # 或
   .\start.ps1  # Windows
   ```

4. **启动前端服务**
   ```bash
   cd food-recommendation/frontend
   npm run dev -- --host 0.0.0.0
   ```

5. **局域网访问地址**
   - 前端：http://YOUR_LOCAL_IP:5173
   - 后端API：http://YOUR_LOCAL_IP:3000 或 http://YOUR_LOCAL_IP:3001

#### 7. 生产环境建议

对于生产环境部署，建议：

1. **使用反向代理**（Nginx）
2. **配置SSL证书**（HTTPS）
3. **使用域名**而非IP地址
4. **配置CDN**加速静态资源

示例Nginx配置：
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理
    location /api {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
    # 静态资源
    location /images {
        root /path/to/project;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

这样配置后，局域网访问应该不会再出现闪烁问题。
