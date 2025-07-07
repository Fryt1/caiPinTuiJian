# PowerShell启动脚本 - 中文乱码修复版
# Food Recommendation Backend (Java)

# 设置控制台编码为UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "==========================================" -ForegroundColor Green
Write-Host "   Food Recommendation Backend (Java)" -ForegroundColor Green
Write-Host "   PowerShell启动脚本 - 中文乱码修复版" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""

# 检查Java环境
Write-Host "正在检查Java环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java not found"
    }
    Write-Host "✓ Java环境检查通过" -ForegroundColor Green
} catch {
    Write-Host "✗ 错误：未检测到Java环境，请先安装Java 11或以上版本" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

# 检查Maven环境
Write-Host "正在检查Maven环境..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Maven not found"
    }
    Write-Host "✓ Maven环境检查通过" -ForegroundColor Green
} catch {
    Write-Host "✗ 错误：未检测到Maven环境，请先安装Maven 3.6+" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

Write-Host ""
Write-Host "正在启动应用..." -ForegroundColor Cyan
Write-Host "请确保已经：" -ForegroundColor Yellow
Write-Host "1. 启动MySQL数据库服务" -ForegroundColor Yellow
Write-Host "2. 创建food_recommendation数据库" -ForegroundColor Yellow
Write-Host "3. 导入数据库结构和数据" -ForegroundColor Yellow
Write-Host "4. 配置正确的数据库连接信息" -ForegroundColor Yellow
Write-Host ""

# 设置环境变量 - 解决中文乱码的关键
Write-Host "设置UTF-8编码环境..." -ForegroundColor Yellow
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -Duser.language=zh -Duser.country=CN"
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -Xmx1024m"

Write-Host "应用启动中，请稍候..." -ForegroundColor Cyan
Write-Host "如需停止服务，请按 Ctrl+C" -ForegroundColor Red
Write-Host ""

# 启动Spring Boot应用 - 添加更多编码参数
mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -Duser.language=zh -Duser.country=CN -Djava.awt.headless=true"
