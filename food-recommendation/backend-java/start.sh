#!/bin/bash

# Linux/macOS启动脚本
# Food Recommendation Backend (Java)

echo "=========================================="
echo "   Food Recommendation Backend (Java)"
echo "   Linux/macOS启动脚本"
echo "=========================================="
echo ""

# 检查Java环境
echo "正在检查Java环境..."
if ! command -v java &> /dev/null; then
    echo "✗ 错误：未检测到Java环境，请先安装Java 11或以上版本"
    echo "  Ubuntu/Debian: sudo apt install openjdk-11-jdk"
    echo "  CentOS/RHEL: sudo yum install java-11-openjdk-devel"
    echo "  macOS: brew install openjdk@11"
    exit 1
fi

java_version=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
echo "✓ Java环境检查通过: $java_version"

# 检查Maven环境
echo "正在检查Maven环境..."
if ! command -v mvn &> /dev/null; then
    echo "✗ 错误：未检测到Maven环境，请先安装Maven"
    echo "  Ubuntu/Debian: sudo apt install maven"
    echo "  CentOS/RHEL: sudo yum install maven"
    echo "  macOS: brew install maven"
    exit 1
fi

maven_version=$(mvn -version 2>&1 | head -n 1 | awk '{print $3}')
echo "✓ Maven环境检查通过: $maven_version"

# 检查MySQL连接
echo "正在检查MySQL连接..."
if command -v mysql &> /dev/null; then
    if mysql -u root -p10086123 -e "USE food_recommendation; SELECT 1;" &> /dev/null; then
        echo "✓ MySQL连接检查通过"
    else
        echo "⚠ 警告：MySQL连接失败，请检查数据库配置"
        echo "  1. 确保MySQL服务正在运行"
        echo "  2. 确保数据库'food_recommendation'已创建"
        echo "  3. 确保用户名密码正确（当前配置：root/10086123）"
    fi
else
    echo "⚠ 警告：未检测到MySQL客户端，无法验证数据库连接"
fi

echo ""
echo "正在启动应用..."
echo "=========================================="

# 设置JVM参数优化
export JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# 检查是否已编译
if [ ! -f "target/food-recommendation-0.0.1-SNAPSHOT.jar" ]; then
    echo "未找到编译后的JAR文件，正在编译..."
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "✗ 编译失败，请检查错误信息"
        exit 1
    fi
fi

# 启动应用
echo "启动Java应用..."
echo "访问地址: http://localhost:3001"
echo "API文档: http://localhost:3001/api/health"
echo ""
echo "按 Ctrl+C 停止应用"
echo "=========================================="

java $JAVA_OPTS -jar target/food-recommendation-0.0.1-SNAPSHOT.jar

# 如果程序异常退出
if [ $? -ne 0 ]; then
    echo ""
    echo "应用异常退出，请检查错误信息"
    read -p "按回车键退出..."
fi
