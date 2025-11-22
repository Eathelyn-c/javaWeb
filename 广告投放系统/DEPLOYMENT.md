# 快速部署指南

## 当前状态
✅ 项目已成功编译
✅ WAR文件已生成: `target/ad-management.war`

## 下一步操作

### 方式一：使用Tomcat（推荐）

#### 1. 配置数据库
```bash
# 登录MySQL
mysql -u root -p

# 执行SQL脚本
source database/schema.sql
source database/sample_data.sql

# 退出MySQL
exit
```

#### 2. 修改数据库配置
编辑 `src/main/resources/config.properties`，修改以下内容：
```properties
db.password=你的MySQL密码
jwt.secret=你的JWT密钥（至少256位）
```

修改后需要重新打包：
```bash
mvn package -DskipTests
```

#### 3. 部署到Tomcat
```bash
# 复制WAR文件到Tomcat（替换为你的Tomcat路径）
cp target/ad-management.war /path/to/tomcat/webapps/

# 启动Tomcat
/path/to/tomcat/bin/startup.sh    # Mac/Linux
# 或
C:\path\to\tomcat\bin\startup.bat  # Windows
```

#### 4. 访问应用
打开浏览器访问: http://localhost:8080/ad-management/

测试账号：
- 用户名: `testuser`
- 密码: `password123`

---

### 方式二：使用内嵌服务器（测试用）

如果没有Tomcat，可以使用Maven插件直接运行：

#### 1. 添加Jetty插件到pom.xml
在 `<build><plugins>` 中添加：
```xml
<plugin>
    <groupId>org.eclipse.jetty</groupId>
    <artifactId>jetty-maven-plugin</artifactId>
    <version>11.0.15</version>
    <configuration>
        <httpConnector>
            <port>8080</port>
        </httpConnector>
        <webApp>
            <contextPath>/ad-management</contextPath>
        </webApp>
    </configuration>
</plugin>
```

#### 2. 启动服务器
```bash
mvn jetty:run
```

访问: http://localhost:8080/ad-management/

---

## 常见问题

### 数据库连接失败
1. 确认MySQL服务是否启动
2. 检查`config.properties`中的数据库密码
3. 确认数据库`ad_management`已创建

### Tomcat启动失败
1. 检查8080端口是否被占用
2. 查看Tomcat日志: `logs/catalina.out`
3. 确认JDK版本（需要JDK 11+）

### 前端无法访问
1. 清除浏览器缓存
2. 检查浏览器控制台的错误信息
3. 确认API端点配置正确

---

## API测试

可以使用curl测试API：

```bash
# 注册用户
curl -X POST http://localhost:8080/ad-management/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"123456"}'

# 登录
curl -X POST http://localhost:8080/ad-management/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 获取分类
curl http://localhost:8080/ad-management/api/categories
```

---

## 项目文件说明

- `target/ad-management.war` - 可部署的WAR文件
- `database/schema.sql` - 数据库结构
- `database/sample_data.sql` - 测试数据
- `README.md` - 完整文档
- `src/main/resources/config.properties` - 配置文件

---

需要帮助？查看 README.md 获取详细文档。
