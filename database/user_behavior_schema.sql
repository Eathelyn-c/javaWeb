-- 用户行为表
CREATE TABLE IF NOT EXISTS user_behaviors (
    behavior_id INT AUTO_INCREMENT PRIMARY KEY,
    anonymous_user_id VARCHAR(255) NOT NULL COMMENT '匿名用户ID',
    tag VARCHAR(50) NOT NULL COMMENT '广告标签: food/makeup/digital/sport/clothes/book/others',
    score INT NOT NULL COMMENT '交互评分: 1/2/3',
    platform VARCHAR(50) NOT NULL COMMENT '投放平台: video/text/image/text+image',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX idx_anonymous_user_id (anonymous_user_id),
    INDEX idx_tag (tag),
    INDEX idx_platform (platform),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为记录表';

-- 1. 添加唯一索引
ALTER TABLE user_behaviors ADD UNIQUE INDEX uk_user_tag (anonymous_user_id, tag);

-- 2. 添加更新时间字段
ALTER TABLE user_behaviors ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;