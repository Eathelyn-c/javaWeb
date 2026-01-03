#!/bin/bash
# Tomcat启动脚本 - 带MySQL连接调试

export CATALINA_OPTS="-Djava.net.preferIPv4Stack=true -Dcom.mysql.cj.disableAbandonedConnectionCleanup=true"

echo "停止Tomcat..."
/Users/eathelyn/Downloads/apache-tomcat-10.1.33/bin/shutdown.sh

sleep 3

echo "编译项目..."
cd /Users/eathelyn/Downloads/javaWeb-new-branch-AD_newest
mvn clean package -DskipTests

echo "部署WAR文件..."
cp target/ad-management.war /Users/eathelyn/Downloads/apache-tomcat-10.1.33/webapps/

echo "启动Tomcat..."
/Users/eathelyn/Downloads/apache-tomcat-10.1.33/bin/startup.sh

echo "Tomcat已启动，等待应用部署..."
sleep 8

echo "测试应用..."
curl -s http://localhost:8080/ad-management/ | head -5

echo ""
echo "应用URL: http://localhost:8080/ad-management/"
echo "查看日志: tail -f /Users/eathelyn/Downloads/apache-tomcat-10.1.33/logs/catalina.out"
