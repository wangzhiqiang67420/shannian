-- 闪念笔记数据库表结构
-- 适用数据库: MySQL 5.7+
-- 使用前请先创建数据库: CREATE DATABASE demo DEFAULT CHARSET utf8mb4;

-- 笔记主表
CREATE TABLE IF NOT EXISTS notes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '笔记ID',
    phone       VARCHAR(20)   NOT NULL COMMENT '用户手机号',
    content     TEXT          NOT NULL COMMENT '笔记内容',
    created_at  DATETIME      NOT NULL COMMENT '创建时间',
    updated_at  DATETIME      NOT NULL COMMENT '最后修改时间',
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记表';

-- 修改历史表
CREATE TABLE IF NOT EXISTS note_histories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '历史ID',
    note_id     BIGINT        NOT NULL COMMENT '关联笔记ID',
    content     TEXT          NOT NULL COMMENT '修改前的内容快照',
    created_at  DATETIME      NOT NULL COMMENT '快照时间',
    INDEX idx_note_id (note_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记修改历史表';
