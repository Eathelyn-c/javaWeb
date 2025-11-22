#!/bin/bash

# 广告管理系统 - 快速启动脚本

echo "======================================"
echo "   广告管理系统 - 快速启动"
echo "======================================"
echo ""

# 1. 检查环境
echo "1. 检查环境..."
if ! command -v java &> /dev/null; then
    echo "❌ Java未安装，请先安装JDK 11或更高版本"
    exit 1
fi
echo "✅ Java版本: $(java -version 2>&1 | head -n 1)"

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven未安装，请先安装Maven"
    exit 1
fi
echo "✅ Maven版本: $(mvn -version | head -n 1)"

if ! command -v mysql &> /dev/null; then
    echo "⚠️  MySQL命令行工具未找到，请确保MySQL已安装并运行"
fi

echo ""

# 2. 配置数据库
echo "2. 配置数据库..."
echo "请输入MySQL root密码（或按Ctrl+C跳过）："
read -s MYSQL_PASSWORD

if [ ! -z "$MYSQL_PASSWORD" ]; then
    echo "正在创建数据库..."
    mysql -u root -p"$MYSQL_PASSWORD" < database/schema.sql 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "✅ 数据库创建成功"
        
        echo "是否插入测试数据？(y/n)"
        read -r INSERT_SAMPLE
        if [ "$INSERT_SAMPLE" = "y" ]; then
            mysql -u root -p"$MYSQL_PASSWORD" < database/sample_data.sql 2>/dev/null
            echo "✅ 测试数据插入成功"
            echo "   测试账号: testuser / password123"
        fi
    else
        echo "⚠️  数据库创建失败，请手动执行 database/schema.sql"
    fi
    
    # 更新配置文件
    sed -i.bak "s/db.password=.*/db.password=$MYSQL_PASSWORD/" src/main/resources/config.properties
    echo "✅ 配置文件已更新"
fi

echo ""

# 3. 编译项目
echo "3. 编译项目..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi
echo "✅ 编译成功"

echo ""

# 4. 部署说明
echo "4. 部署到Tomcat..."
echo ""
echo "WAR文件已生成: target/ad-management.war"
echo ""
echo "请执行以下步骤："
echo "  1. 将 target/ad-management.war 复制到 Tomcat 的 webapps 目录"
echo "  2. 启动 Tomcat:"
echo "     - Linux/Mac: \$CATALINA_HOME/bin/startup.sh"
echo "     - Windows: %CATALINA_HOME%\\bin\\startup.bat"
echo "  3. 访问: http://localhost:8080/ad-management/"
echo ""
echo "或者使用Maven Tomcat插件（需要配置pom.xml）:"
echo "  mvn tomcat7:deploy"
echo ""

# 5. 检查Tomcat
if [ -d "$CATALINA_HOME" ]; then
    echo "检测到Tomcat: $CATALINA_HOME"
    echo "是否自动部署到Tomcat？(y/n)"
    read -r AUTO_DEPLOY
    if [ "$AUTO_DEPLOY" = "y" ]; then
        cp target/ad-management.war "$CATALINA_HOME/webapps/"
        echo "✅ WAR文件已复制到Tomcat"
        echo ""
        echo "请手动启动Tomcat或执行："
        echo "  $CATALINA_HOME/bin/startup.sh"
    fi
fi

echo ""
echo "======================================"
echo "   启动准备完成！"
echo "======================================"
