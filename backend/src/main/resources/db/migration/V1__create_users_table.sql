-- 创建用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名，用于登录',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希(使用PBKDF2算法)',
    salt VARCHAR(100) NOT NULL COMMENT '密码盐值',
    last_login_time DATETIME NULL COMMENT '最后登录时间',
    password_expiry_time DATETIME NULL COMMENT '密码过期时间',
    failed_attempts INT DEFAULT 0 COMMENT '登录失败次数',
    locked_until DATETIME NULL COMMENT '账户锁定到期时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账户创建时间',
    CONSTRAINT UK_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建索引
CREATE INDEX IDX_users_username ON users(username);

-- 添加管理员账户用于演示(系统会自动设置正确的密码哈希)
-- 此处仅添加一个空记录，实际密码由应用创建管理员账户时设置
-- INSERT INTO users (username, password_hash, salt, created_at, password_expiry_time) 
-- VALUES ('admin', '[应用程序生成]', '[应用程序生成]', NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)); 