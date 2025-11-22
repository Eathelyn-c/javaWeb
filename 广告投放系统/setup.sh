#!/bin/bash

echo "=========================================="
echo "  广告管理系统 - 配置向导"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

CONFIG_FILE="src/main/resources/config.properties"

# 1. 检查MySQL是否运行
echo "1. 检查MySQL服务..."
if ps aux | grep -v grep | grep mysqld > /dev/null; then
    echo -e "${GREEN}✓${NC} MySQL服务正在运行"
else
    echo -e "${RED}✗${NC} MySQL服务未运行"
    echo "请先启动MySQL服务"
    exit 1
fi

# 2. 获取MySQL密码
echo ""
echo "2. 配置MySQL连接..."
read -sp "请输入MySQL root密码: " MYSQL_PASSWORD
echo ""

# 测试MySQL连接
if mysql -u root -p"$MYSQL_PASSWORD" -e "SELECT 1;" &> /dev/null; then
    echo -e "${GREEN}✓${NC} MySQL连接成功"
else
    echo -e "${RED}✗${NC} MySQL连接失败，请检查密码"
    exit 1
fi

# 3. 初始化数据库
echo ""
echo "3. 初始化数据库..."
mysql -u root -p"$MYSQL_PASSWORD" <<EOF
CREATE DATABASE IF NOT EXISTS ad_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ad_management;
SOURCE database/schema.sql;
SOURCE database/sample_data.sql;
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} 数据库初始化成功"
else
    echo -e "${RED}✗${NC} 数据库初始化失败"
    exit 1
fi

# 4. 生成JWT密钥
echo ""
echo "4. 生成JWT密钥..."
if command -v openssl &> /dev/null; then
    JWT_SECRET=$(openssl rand -base64 32)
    echo -e "${GREEN}✓${NC} JWT密钥已生成"
else
    JWT_SECRET="mySecretKey12345678901234567890123456789012345678901234567890"
    echo -e "${YELLOW}!${NC} 使用默认JWT密钥（仅用于测试）"
fi

# 5. 更新配置文件
echo ""
echo "5. 更新配置文件..."

# 读取原配置文件
cp "$CONFIG_FILE" "${CONFIG_FILE}.backup"

# 更新密码和密钥
sed -i.tmp "s/db.password=.*/db.password=$MYSQL_PASSWORD/" "$CONFIG_FILE"
sed -i.tmp "s|jwt.secret=.*|jwt.secret=$JWT_SECRET|" "$CONFIG_FILE"
rm "${CONFIG_FILE}.tmp" 2>/dev/null

echo -e "${GREEN}✓${NC} 配置文件已更新"
echo "  - 备份保存在: ${CONFIG_FILE}.backup"

# 6. 创建上传目录
echo ""
echo "6. 创建上传目录..."
mkdir -p src/main/webapp/uploads
echo -e "${GREEN}✓${NC} 上传目录已创建"

# 7. 编译项目
echo ""
echo "7. 编译项目..."
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} 项目编译成功"
else
    echo -e "${RED}✗${NC} 项目编译失败"
    exit 1
fi

# 完成
echo ""
echo "=========================================="
echo -e "${GREEN}配置完成！${NC}"
echo "=========================================="
echo ""
echo "现在可以启动应用："
echo ""
echo -e "  ${GREEN}mvn jetty:run${NC}"
echo ""
echo "启动后访问："
echo "  首页: http://localhost:8080/ad-management/frontend/index.html"
echo "  登录: http://localhost:8080/ad-management/frontend/login.html"
echo ""
echo "测试账号："
echo "  用户名: testuser"
echo "  密码: password123"
echo ""
