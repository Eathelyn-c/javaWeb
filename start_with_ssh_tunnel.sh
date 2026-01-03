#!/bin/bash

echo "======================================"
echo "  广告管理系统 - SSH隧道启动脚本"
echo "======================================"
echo ""
echo "此脚本将："
echo "1. 创建SSH隧道到远程MySQL服务器"
echo "2. 修改应用配置使用本地隧道"
echo "3. 编译和部署应用"
echo "4. 启动Tomcat"
echo ""
echo "请输入SSH连接信息："
read -p "SSH用户名 (例如: root): " SSH_USER
read -p "SSH主机 (默认: 10.100.164.6): " SSH_HOST
SSH_HOST=${SSH_HOST:-10.100.164.6}

echo ""
echo "正在创建SSH隧道..."
echo "命令: ssh -N -L 3307:localhost:3306 $SSH_USER@$SSH_HOST"
echo ""
echo "请在另一个终端窗口执行上述命令，然后按回车继续..."
read -p "SSH隧道已建立？(y/n): " TUNNEL_READY

if [ "$TUNNEL_READY" != "y" ]; then
    echo "取消部署"
    exit 1
fi

echo ""
echo "修改配置文件使用SSH隧道..."
cd /Users/eathelyn/Downloads/javaWeb-new-branch-AD_newest

# 备份原配置
cp src/main/resources/config.properties src/main/resources/config.properties.ssh_backup

# 修改为使用localhost:3307
sed -i '' 's|jdbc:mysql://10.100.164.6:3306|jdbc:mysql://localhost:3307|g' src/main/resources/config.properties

echo "✓ 配置已更新为使用 localhost:3307"
echo ""

echo "编译项目..."
mvn clean package -DskipTests

echo ""
echo "部署到Tomcat..."
cp target/ad-management.war /Users/eathelyn/Downloads/apache-tomcat-10.1.33/webapps/

echo ""
echo "启动Tomcat..."
export CATALINA_OPTS="-Djava.net.preferIPv4Stack=true"
/Users/eathelyn/Downloads/apache-tomcat-10.1.33/bin/startup.sh

echo ""
echo "等待应用部署..."
sleep 8

echo ""
echo "======================================"
echo "  部署完成！"
echo "======================================"
echo "应用URL: http://localhost:8080/ad-management/"
echo ""
echo "注意: 保持SSH隧道窗口运行，不要关闭"
echo "停止应用: /Users/eathelyn/Downloads/apache-tomcat-10.1.33/bin/shutdown.sh"
echo "恢复配置: mv src/main/resources/config.properties.ssh_backup src/main/resources/config.properties"
