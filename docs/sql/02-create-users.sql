-- 微信小程序登录用户表
-- 执行前请先确认数据库已创建，并与 application.yml 中的数据源一致。

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    openid        VARCHAR(64)  NOT NULL COMMENT '微信小程序openid',
    unionid       VARCHAR(64)  NULL COMMENT '微信unionid',
    session_key   VARCHAR(128) NULL COMMENT '微信会话密钥',
    created_at    DATETIME     NOT NULL COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_openid (openid),
    KEY idx_unionid (unionid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
