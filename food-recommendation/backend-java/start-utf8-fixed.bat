@echo off
rem 设置CMD编码为UTF-8
chcp 65001 > nul

echo ==========================================
echo    Food Recommendation Backend (Java)
echo    UTF-8编码启动脚本 - 中文乱码修复版
echo ==========================================
echo.

echo 正在检查Java环境...
java -version > nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未检测到Java环境，请先安装Java 8或以上版本
    pause
    exit /b 1
)

echo 正在检查Maven环境...
mvn -version > nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未检测到Maven环境，请先安装Maven 3.6+
    pause
    exit /b 1
)

echo.
echo 正在启动应用...
echo 请确保已经：
echo 1. 启动MySQL数据库服务
echo 2. 创建food_recommendation数据库
echo 3. 导入数据库结构和数据
echo 4. 配置正确的数据库连接信息
echo.

rem 设置Java环境变量 - 解决中文乱码的关键
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -Duser.language=zh -Duser.country=CN
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -Xmx1024m

echo 应用启动中，请稍候...
echo 如需停止服务，请按 Ctrl+C
echo.

rem 启动Spring Boot应用 - 添加更多编码参数
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -Duser.language=zh -Duser.country=CN -Djava.awt.headless=true"
