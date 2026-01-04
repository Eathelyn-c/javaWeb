# 数据库修改文档

## 数据库连接信息
- **主机**: 10.100.164.6:3306
- **数据库名**: ad_management
- **用户名**: root
- **密码**: 111728

## 数据库结构修改

### 1. user_behaviors 表修改

#### 1.1 添加 ad_id 字段
为了支持外部API统计功能，需要在 user_behaviors 表中添加 ad_id 字段来关联广告。

```sql
-- 添加 ad_id 列
ALTER TABLE user_behaviors 
ADD COLUMN ad_id INT DEFAULT NULL AFTER tag;

-- 添加索引以提高查询性能
ALTER TABLE user_behaviors 
ADD INDEX idx_ad_id (ad_id);
```

**修改原因**: 
- 外部平台调用 API 获取广告时，需要记录每个广告的浏览量
- 外部平台报告点击数据时，需要通过 ad_id 关联到具体广告
- 统计数据需要按广告ID聚合

#### 1.2 删除唯一约束
删除 uk_user_tag 唯一约束，允许同一用户对同一标签产生多次行为记录。

```sql
-- 删除唯一约束
ALTER TABLE user_behaviors 
DROP INDEX uk_user_tag;
```

**修改原因**:
- 外部平台每次调用 API 都需要记录一条浏览记录
- 外部平台可能多次报告同一广告的点击
- 需要累积统计所有的浏览和点击行为

#### 1.3 修改后的表结构
```sql
CREATE TABLE user_behaviors (
    behavior_id INT AUTO_INCREMENT PRIMARY KEY,
    anonymous_user_id VARCHAR(255) NOT NULL,
    tag VARCHAR(100) NOT NULL,
    ad_id INT DEFAULT NULL,                    -- 新增字段
    score INT DEFAULT 0,
    platform VARCHAR(50) DEFAULT 'web',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_tag (anonymous_user_id, tag),
    INDEX idx_ad_id (ad_id)                   -- 新增索引
);
```

### 2. 统计逻辑变更

#### 2.1 统计数据来源
- **旧逻辑**: 从 ad_statistics 表读取预先聚合的统计数据
- **新逻辑**: 从 user_behaviors 表实时聚合统计数据

#### 2.2 统计查询SQL
```sql
-- 获取广告的浏览量和点击量
SELECT 
    COUNT(CASE WHEN score = 0 THEN 1 END) as views,
    COUNT(CASE WHEN score > 0 THEN 1 END) as clicks
FROM user_behaviors
WHERE ad_id = ?;
```

**score 字段含义**:
- `score = 0`: 外部平台获取广告（记录为浏览）
- `score > 0`: 外部平台报告点击（记录为点击）

### 3. 外部API工作流程

#### 3.1 获取广告 (浏览量+1)
外部平台调用 API 获取广告列表：
```json
POST /api/external/api/getAds
{
  "anonymousUserId": "test_user_123",
  "tag": "makeup",
  "platform": "news",
  "limit": 10
}
```

**数据库操作**:
- 返回的每个广告都会插入一条记录到 user_behaviors 表
- score = 0（表示浏览）
- ad_id = 广告ID

#### 3.2 报告点击 (点击量+N)
外部平台报告用户点击广告：
```json
POST /api/external/api/getAds
{
  "adId": 9,
  "clicks": 5,
  "anonymousUserId": "test_user_123",
  "platform": "news"
}
```

**数据库操作**:
- 插入 N 条记录到 user_behaviors 表（N = clicks）
- 每条记录: score = 1, ad_id = 广告ID

### 4. 测试账户

数据库中的测试用户账户：
```sql
-- 用户名: testuser
-- 密码: password123
-- BCrypt hash: $2a$10$dWw8B6tZPXLZ1WQZJX1N0.h9EYt5fH5n8kZKF/QxL/D5yXgm6wG7K

-- 用户名: testuser2  
-- 密码: password123
-- BCrypt hash: $2a$10$dWw8B6tZPXLZ1WQZJX1N0.h9EYt5fH5n8kZKF/QxL/D5yXgm6wG7K
```

### 5. 完整修改脚本

```sql
-- ============================================
-- 数据库修改脚本
-- 执行前请备份数据库！
-- ============================================

USE ad_management;

-- 1. 修改 user_behaviors 表
ALTER TABLE user_behaviors 
ADD COLUMN ad_id INT DEFAULT NULL AFTER tag;

ALTER TABLE user_behaviors 
ADD INDEX idx_ad_id (ad_id);

ALTER TABLE user_behaviors 
DROP INDEX uk_user_tag;

-- 2. 验证修改
DESC user_behaviors;
SHOW INDEX FROM user_behaviors;

-- 3. 清理旧的测试数据（可选）
-- TRUNCATE TABLE user_behaviors;

-- 4. 验证数据
SELECT COUNT(*) FROM user_behaviors;
SELECT COUNT(*) FROM advertisements;
SELECT COUNT(*) FROM users;
```

## 部署步骤

1. **备份数据库**
   ```bash
   mysqldump -h 10.100.164.6 -u root -p111728 ad_management > backup_$(date +%Y%m%d_%H%M%S).sql
   ```

2. **执行修改脚本**
   ```bash
   mysql -h 10.100.164.6 -u root -p111728 ad_management < database/DATABASE_CHANGES.sql
   ```

3. **更新配置文件**
   - 已修改 `src/main/resources/config.properties`
   - 数据库地址: 10.100.164.6:3306
   - 密码: 111728

4. **重新编译部署**
   ```bash
   mvn clean compile war:war
   cp target/ad-management.war /path/to/tomcat/webapps/
   ```

## 注意事项

1. **数据迁移**: 如果本地数据库(localhost)有重要数据，需要先迁移到远程数据库
2. **网络连接**: 确保应用服务器能访问 10.100.164.6:3306
3. **防火墙**: 确保 MySQL 端口 3306 已开放
4. **权限**: 确保 root 用户有远程访问权限
5. **时区**: 已配置 serverTimezone=Asia/Shanghai

## 功能影响

修改后的功能变化：
- ✅ 外部平台API可以正常获取广告并记录浏览量
- ✅ 外部平台可以报告点击数据
- ✅ Dashboard 显示的统计数据来自 user_behaviors 表
- ✅ 支持多次记录同一用户对同一广告的行为
- ✅ 统计数据按 ad_id 聚合
