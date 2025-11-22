# 快速启动指南

## 第一步：初始化数据库

```bash
# 1. 登录MySQL（输入你的MySQL root密码）
mysql -u root -p

# 2. 在MySQL命令行中执行以下命令：
CREATE DATABASE IF NOT EXISTS ad_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ad_management;
SOURCE database/schema.sql;
SOURCE database/sample_data.sql;
EXIT;
```

## 第二步：配置数据库密码

编辑 `src/main/resources/config.properties` 文件：

```properties
# 修改以下配置：
db.password=你的MySQL密码

# JWT密钥（可以使用以下命令生成一个随机密钥）：
# openssl rand -base64 32
jwt.secret=你生成的密钥或任意64位字符串
```

### 生成JWT密钥的方法：

**方法1：使用openssl（推荐）**
```bash
openssl rand -base64 32
```

**方法2：使用在线工具**
- 访问: https://www.grc.com/passwords.htm
- 复制63个随机字符

**方法3：简单密钥（仅用于测试）**
```
jwt.secret=mySecretKey12345678901234567890123456789012345678901234567890
```

## 第三步：启动应用

```bash
# 使用Jetty快速启动（推荐）
mvn jetty:run

# 或者使用我们准备的启动脚本
./start.sh
```

## 第四步：访问应用

启动成功后，在浏览器访问：

- **首页**: http://localhost:8080/ad-management/frontend/index.html
- **登录**: http://localhost:8080/ad-management/frontend/login.html
- **注册**: http://localhost:8080/ad-management/frontend/register.html

### 测试账号

```
用户名: testuser
密码: password123
```

## 常见问题

### 1. 数据库连接失败
- 确认MySQL服务已启动：`brew services list`
- 如果未启动：`brew services start mysql`
- 检查config.properties中的密码是否正确

### 2. 端口8080被占用
修改pom.xml中Jetty插件的端口：
```xml
<httpConnector>
    <port>8081</port>  <!-- 改为其他端口 -->
</httpConnector>
```

### 3. JWT token错误
- 确保jwt.secret已正确配置且长度足够（至少32个字符）
- 清除浏览器localStorage：打开开发者工具 > Console > 输入 `localStorage.clear()`

### 4. 文件上传失败
- 确认upload目录存在且有写权限：`mkdir -p src/main/webapp/uploads`
- 检查文件大小是否超过10MB限制

## API测试

### 使用curl测试

```bash
# 1. 注册新用户
curl -X POST http://localhost:8080/ad-management/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123",
    "email": "newuser@example.com",
    "companyName": "测试公司"
  }'

# 2. 登录获取token
TOKEN=$(curl -X POST http://localhost:8080/ad-management/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }' | jq -r '.data.token')

echo "Token: $TOKEN"

# 3. 获取我的广告
curl http://localhost:8080/ad-management/api/ads/my \
  -H "Authorization: Bearer $TOKEN"

# 4. 获取所有分类
curl http://localhost:8080/ad-management/api/categories
```

## 停止应用

在运行`mvn jetty:run`的终端中按 `Ctrl + C`

## 下一步

- 查看 [README.md](README.md) 了解完整功能
- 查看 [DEPLOYMENT.md](DEPLOYMENT.md) 了解生产环境部署
- 在浏览器开发者工具中查看网络请求和响应

---

**遇到问题？** 检查终端输出的错误信息，通常会有明确的提示。
