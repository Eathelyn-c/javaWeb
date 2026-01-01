-- Sample data for testing
-- This script adds test users and sample advertisements

USE ad_management;

-- Insert a test user (password: "password123")
-- Password hash generated with BCrypt for "password123"
-- You can generate a new one by running: BCrypt.hashpw("password123", BCrypt.gensalt())
INSERT INTO users (username, email, password_hash, full_name, company_name, phone) VALUES
('testuser', 'test@example.com', '$2a$10$7AOoGnl2pUfK39/2wUieCOL56b.ycGYGaoy6C7n17LXkzKnRjdHqS', 
 '测试用户', '测试公司', '13800138000')
ON DUPLICATE KEY UPDATE password_hash = '$2a$10$7AOoGnl2pUfK39/2wUieCOL56b.ycGYGaoy6C7n17LXkzKnRjdHqS';

-- Get the user ID
SET @user_id = LAST_INSERT_ID();

-- Insert sample advertisements
INSERT INTO advertisements (user_id, category_id, title, description, ad_type, text_content, status) VALUES
(@user_id, 1, '美味披萨限时优惠', '本周特惠，所有披萨8折优惠！', 'text', '现在订购任意披萨，享受8折优惠！限时3天！', 'active'),
(@user_id, 2, '新款口红上市', '全新色号，持久不脱色', 'text', '革命性配方，24小时持妆不掉色。现已上市！', 'active'),
(@user_id, 3, '最新智能手机', '5G旗舰手机，性能强劲', 'text', '搭载最新处理器，支持全网5G，拍照更清晰！', 'active'),
(@user_id, 4, '运动健身装备', '专业运动装备，提升您的训练效果', 'text', '采用透气面料，舒适不闷热，让运动更自在！', 'active'),
(@user_id, 5, '春季新款服装', '时尚设计，舒适面料', 'text', '春季新品上市，多种款式任您选择！', 'active');

-- Insert initial statistics for each ad
INSERT INTO ad_statistics (ad_id, view_count, click_count)
SELECT ad_id, 0, 0 FROM advertisements WHERE user_id = @user_id;

-- Show created data
SELECT 
    a.ad_id,
    a.title,
    c.category_name,
    a.ad_type,
    a.status,
    s.view_count,
    s.click_count
FROM advertisements a
JOIN categories c ON a.category_id = c.category_id
LEFT JOIN ad_statistics s ON a.ad_id = s.ad_id
WHERE a.user_id = @user_id;

-- Display user info
SELECT 
    user_id,
    username,
    email,
    company_name,
    created_at
FROM users
WHERE user_id = @user_id;

SELECT '示例数据插入完成！' as message;
SELECT '测试账号 - 用户名: testuser, 密码: password123' as login_info;
