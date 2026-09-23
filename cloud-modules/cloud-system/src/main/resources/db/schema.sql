-- cloud-system 服务的参考建表语句（DDL）。
-- 请手动执行（或通过你的迁移工具执行）。它不会在启动时自动执行，
-- 因此 Spring AOT / 构建阶段无需真实数据库连接。

CREATE DATABASE IF NOT EXISTS `cloud_system`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `cloud_system`;

CREATE TABLE IF NOT EXISTS `sys_user`
(
    `user_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(64)  NOT NULL COMMENT '登录名',
    `nick_name`   VARCHAR(64)           DEFAULT NULL COMMENT '显示名称',
    `email`       VARCHAR(128)          DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(32)           DEFAULT NULL COMMENT '手机号',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    `create_time` DATETIME              DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户';

INSERT INTO `sys_user` (`username`, `nick_name`, `email`, `phone`, `status`)
VALUES ('admin', 'Administrator', 'admin@example.com', '10000000000', 1)
ON DUPLICATE KEY UPDATE `nick_name` = VALUES(`nick_name`);
