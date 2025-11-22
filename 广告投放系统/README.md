# 广告管理系统 (Advertisement Management System)

一个功能完整的广告管理系统，采用 Servlet + MVC + MySQL 架构，前后端分离设计。

## 项目特性

### 核心功能
- ✅ 用户注册与登录（JWT认证）
- ✅ 广告CRUD操作（创建、读取、更新、删除）
- ✅ 广告分类管理（7大类别：食品、美妆、数码、运动、服饰、图书、其他）
- ✅ 多种广告形式（文字、图片、视频、文字+图片）
- ✅ 广告状态管理（活跃、暂停、删除）
- ✅ 实时统计（浏览量、点击量跟踪）
- ✅ 文件上传（图片、视频）
- ✅ API发布接口（新闻、视频、购物平台）

### 技术架构
- **后端**: Java Servlet + MVC架构
- **前端**: HTML5 + CSS3 + Vanilla JavaScript
- **数据库**: MySQL 8.0+
- **认证**: JWT (JSON Web Token)
- **构建工具**: Maven
- **服务器**: Tomcat 9.0+

## 项目结构

```
广告投放系统/
├── src/
│   ├── main/
│   │   ├── java/com/admanagement/
│   │   │   ├── model/          # 实体类
│   │   │   │   ├── User.java
│   │   │   │   ├── Advertisement.java
│   │   │   │   ├── Category.java
│   │   │   │   └── AdStatistics.java
│   │   │   ├── dao/            # 数据访问层
│   │   │   │   ├── UserDAO.java
│   │   │   │   ├── AdvertisementDAO.java
│   │   │   │   ├── CategoryDAO.java
│   │   │   │   └── StatisticsDAO.java
│   │   │   ├── service/        # 业务逻辑层
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── AdService.java
│   │   │   │   └── APIPublishService.java
│   │   │   ├── servlet/        # 控制器层
│   │   │   │   ├── AuthServlet.java
│   │   │   │   ├── AdServlet.java
│   │   │   │   ├── CategoryServlet.java
│   │   │   │   └── PublishAPIServlet.java
│   │   │   ├── filter/         # 过滤器
│   │   │   │   └── AuthFilter.java
│   │   │   └── util/           # 工具类
│   │   │       ├── DatabaseUtil.java
│   │   │       ├── JWTUtil.java
│   │   │       ├── ResponseUtil.java
│   │   │       └── FileUploadUtil.java
│   │   ├── resources/
│   │   │   └── config.properties
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml
│   │       └── frontend/
│   │           ├── index.html
│   │           ├── login.html
│   │           ├── register.html
│   │           ├── dashboard.html
│   │           ├── create-ad.html
│   │           ├── css/
│   │           │   └── style.css
│   │           └── js/
│   │               ├── config.js
│   │               ├── login.js
│   │               ├── register.js
│   │               ├── index.js
│   │               ├── dashboard.js
│   │               └── create-ad.js
├── database/
│   └── schema.sql
├── pom.xml
└── README.md
```

## 安装步骤

### 1. 环境要求
- JDK 11 或更高版本
- Maven 3.6+
- MySQL 8.0+
- Apache Tomcat 9.0+

### 2. 克隆项目
```bash
cd /path/to/广告投放系统
```

### 3. 配置数据库

#### 3.1 创建数据库
```bash
mysql -u root -p
```

#### 3.2 执行SQL脚本
```sql
source database/schema.sql
```

或者直接在MySQL客户端中运行 `database/schema.sql` 文件。

#### 3.3 配置数据库连接
编辑 `src/main/resources/config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/ad_management?useSSL=false&serverTimezone=UTC
db.username=root
db.password=your_password
```

**重要**: 请修改 `db.password` 为您的MySQL密码。

### 4. 配置JWT密钥
在 `config.properties` 中设置JWT密钥（至少256位）:

```properties
jwt.secret=your_secret_key_change_this_in_production_min_256_bits_required
```

**重要**: 生产环境中请使用强密钥。

### 5. 构建项目
```bash
mvn clean package
```

这将在 `target/` 目录下生成 `ad-management.war` 文件。

### 6. 部署到Tomcat

#### 方法1: 手动部署
1. 将 `target/ad-management.war` 复制到 Tomcat 的 `webapps/` 目录
2. 启动 Tomcat:
   ```bash
   # Linux/Mac
   $CATALINA_HOME/bin/startup.sh
   
   # Windows
   %CATALINA_HOME%\bin\startup.bat
   ```

#### 方法2: 使用Maven插件
在 `pom.xml` 中配置 Tomcat Maven 插件后:
```bash
mvn tomcat7:deploy
```

### 7. 访问应用
打开浏览器访问: `http://localhost:8080/ad-management/`

## API文档

### 认证接口

#### 注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "advertiser1",
  "email": "advertiser@example.com",
  "password": "password123",
  "fullName": "张三",
  "companyName": "ABC公司",
  "phone": "13800138000"
}
```

#### 登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "advertiser1",
  "password": "password123"
}
```

### 广告接口

所有广告接口（除查看公开广告外）都需要在请求头中包含JWT token:
```
Authorization: Bearer <your_token>
```

#### 创建广告
```
POST /api/ads
Content-Type: multipart/form-data 或 application/json

{
  "title": "新产品发布",
  "description": "描述信息",
  "category": "digital",
  "adType": "text_image",
  "textContent": "广告文字内容",
  "targetUrl": "https://example.com"
}
```

#### 获取我的广告
```
GET /api/ads
Authorization: Bearer <token>
```

#### 获取活跃广告（公开）
```
GET /api/ads/active
```

#### 更新广告
```
PUT /api/ads/{adId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "更新的标题",
  ...
}
```

#### 删除广告
```
DELETE /api/ads/{adId}
Authorization: Bearer <token>
```

#### 暂停/激活广告
```
PUT /api/ads/{adId}/toggle
Authorization: Bearer <token>
```

#### 获取广告统计
```
GET /api/ads/stats/{adId}
Authorization: Bearer <token>
```

#### 记录浏览
```
GET /api/ads/view/{adId}
```

#### 记录点击
```
POST /api/ads/click
Content-Type: application/json

{
  "adId": 1
}
```

### 分类接口

#### 获取所有分类
```
GET /api/categories
```

### API发布接口

#### 发布到单个平台
```
POST /api/publish/{platform}
Authorization: Bearer <token>
Content-Type: application/json

{
  "adId": 1
}
```

平台可选值: `news`, `video`, `shopping`

#### 发布到多个平台
```
POST /api/publish/multiple
Authorization: Bearer <token>
Content-Type: application/json

{
  "adId": 1,
  "platforms": ["news", "video", "shopping"]
}
```

## 数据库设计

### 主要表结构

#### users - 用户表
- user_id: 用户ID（主键）
- username: 用户名（唯一）
- email: 邮箱（唯一）
- password_hash: 密码哈希
- company_name: 公司名称
- created_at: 创建时间

#### advertisements - 广告表
- ad_id: 广告ID（主键）
- user_id: 用户ID（外键）
- category_id: 分类ID（外键）
- title: 标题
- description: 描述
- ad_type: 广告类型（text, image, video, text_image）
- status: 状态（active, paused, deleted）
- created_at: 创建时间

#### ad_statistics - 统计表
- stat_id: 统计ID（主键）
- ad_id: 广告ID（外键）
- view_count: 浏览量
- click_count: 点击量
- last_viewed: 最后浏览时间
- last_clicked: 最后点击时间

#### categories - 分类表
- category_id: 分类ID（主键）
- category_name: 分类名称（food, makeup, digital, sport, clothes, book, others）

## 安全特性

1. **JWT认证**: 所有需要认证的API都使用JWT token验证
2. **密码加密**: 使用BCrypt哈希算法存储密码
3. **SQL注入防护**: 使用PreparedStatement防止SQL注入
4. **CORS配置**: 支持跨域请求
5. **文件上传验证**: 限制文件类型和大小

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 验证 `config.properties` 中的数据库配置
- 确认MySQL用户权限

### 2. JWT token过期
- 默认token有效期为24小时
- 可在 `config.properties` 中修改 `jwt.expiration`

### 3. 文件上传失败
- 检查 `uploads` 目录是否存在且有写权限
- 验证文件大小是否超过限制（默认10MB）

### 4. Tomcat部署问题
- 确保Tomcat版本为9.0或更高
- 检查端口8080是否被占用
- 查看Tomcat日志文件 `logs/catalina.out`

## 开发者信息

### 技术栈
- Java 11
- Servlet 4.0
- MySQL 8.0
- JWT
- BCrypt
- Gson
- Apache Commons FileUpload

### 依赖管理
项目使用Maven管理依赖，所有依赖配置在 `pom.xml` 中。

### 代码规范
- 遵循MVC设计模式
- RESTful API设计
- 统一的JSON响应格式
- 详细的代码注释

## 许可证

本项目仅供学习和参考使用。

## 联系方式

如有问题或建议，欢迎提出issue或联系开发团队。
