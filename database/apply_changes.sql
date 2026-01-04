-- ============================================
-- 数据库修改脚本
-- 数据库: ad_management @ 10.100.164.6:3306
-- 执行前请备份数据库！
-- ============================================

USE ad_management;

-- 1. 修改 user_behaviors 表 - 添加 ad_id 字段
ALTER TABLE user_behaviors 
ADD COLUMN ad_id INT DEFAULT NULL AFTER tag;

-- 2. 添加索引以提高查询性能
ALTER TABLE user_behaviors 
ADD INDEX idx_ad_id (ad_id);

-- 3. 删除唯一约束，允许多次记录
ALTER TABLE user_behaviors 
DROP INDEX uk_user_tag;

-- 4. 验证修改结果
DESC user_behaviors;
SHOW INDEX FROM user_behaviors;

-- 5. 查看表数据统计
SELECT 
    COUNT(*) as total_behaviors,
    COUNT(DISTINCT ad_id) as unique_ads,
    COUNT(CASE WHEN score = 0 THEN 1 END) as total_views,
    COUNT(CASE WHEN score > 0 THEN 1 END) as total_clicks
FROM user_behaviors;
