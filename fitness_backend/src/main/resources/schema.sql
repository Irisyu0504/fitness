-- ============================================
-- 智能健身管理平台 数据库建表脚本
-- 数据库: fitnessmanagedb
-- ID策略: MyBatis-Plus ASSIGN_ID (雪花算法, BIGINT)
-- ============================================

CREATE DATABASE IF NOT EXISTS fitnessmanagedb
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE fitnessmanagedb;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id`          BIGINT       NOT NULL COMMENT '主键(雪花ID)',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `gender`      VARCHAR(10)  DEFAULT NULL COMMENT '性别',
    `height`      DECIMAL(5,1) DEFAULT NULL COMMENT '身高(cm)',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'user' COMMENT '角色: user/admin',
    `status`          TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `vip_expire_time` DATETIME     DEFAULT NULL COMMENT '会员到期时间',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 体测记录表
CREATE TABLE IF NOT EXISTS `body_records` (
    `id`            BIGINT        NOT NULL COMMENT '主键(雪花ID)',
    `user_id`       BIGINT        NOT NULL COMMENT '用户ID',
    `weight`        DECIMAL(5,1)  DEFAULT NULL COMMENT '体重(kg)',
    `body_fat_rate` DECIMAL(4,1)  DEFAULT NULL COMMENT '体脂率(%)',
    `waistline`     DECIMAL(5,1)  DEFAULT NULL COMMENT '腰围(cm)',
    `bmi`           DECIMAL(4,1)  DEFAULT NULL COMMENT 'BMI指数',
    `record_date`   DATE          NOT NULL COMMENT '记录日期',
    `remark`        VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体测记录表';

-- 3. 饮食记录表
CREATE TABLE IF NOT EXISTS `diet_records` (
    `id`          BIGINT        NOT NULL COMMENT '主键(雪花ID)',
    `user_id`     BIGINT        NOT NULL COMMENT '用户ID',
    `food_name`   VARCHAR(100)  NOT NULL COMMENT '食物名称',
    `meal_type`   VARCHAR(20)   DEFAULT NULL COMMENT '餐次: 早餐/午餐/晚餐/加餐',
    `calories`    DECIMAL(8,1)  NOT NULL COMMENT '卡路里(kcal)',
    `protein`     DECIMAL(6,1)  DEFAULT NULL COMMENT '蛋白质(g)',
    `carbs`       DECIMAL(6,1)  DEFAULT NULL COMMENT '碳水化合物(g)',
    `fat`         DECIMAL(6,1)  DEFAULT NULL COMMENT '脂肪(g)',
    `meal_time`   DATETIME      DEFAULT NULL COMMENT '用餐时间',
    `remark`      VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_meal_time` (`meal_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录表';

-- 4. 运动库表
CREATE TABLE IF NOT EXISTS `exercises` (
    `id`                  BIGINT        NOT NULL COMMENT '主键(雪花ID)',
    `exercise_name`       VARCHAR(100)  NOT NULL COMMENT '运动名称',
    `muscle_group`        VARCHAR(50)   DEFAULT NULL COMMENT '目标肌群',
    `difficulty`          VARCHAR(20)   DEFAULT NULL COMMENT '难度: 初级/中级/高级',
    `calories_per_minute` DECIMAL(5,1)  DEFAULT NULL COMMENT '每分钟消耗卡路里(kcal)',
    `description`         VARCHAR(500)  DEFAULT NULL COMMENT '运动描述',
    `image_url`           VARCHAR(255)  DEFAULT NULL COMMENT '图片URL',
    `status`              TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_muscle_group` (`muscle_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动库表';

-- 5. 健身目标表
CREATE TABLE IF NOT EXISTS `fitness_goals` (
    `id`             BIGINT        NOT NULL COMMENT '主键(雪花ID)',
    `user_id`        BIGINT        NOT NULL COMMENT '用户ID',
    `goal_type`      VARCHAR(20)   DEFAULT NULL COMMENT '目标类型: 减脂/增肌/塑形/保持',
    `current_weight` DECIMAL(5,1)  DEFAULT NULL COMMENT '当前体重(kg)',
    `target_weight`  DECIMAL(5,1)  DEFAULT NULL COMMENT '目标体重(kg)',
    `start_date`     DATE          DEFAULT NULL COMMENT '开始日期',
    `target_date`    DATE          DEFAULT NULL COMMENT '目标日期',
    `weekly_target`  DECIMAL(4,1)  DEFAULT NULL COMMENT '每周目标(kg)',
    `status`         VARCHAR(20)   NOT NULL DEFAULT '进行中' COMMENT '状态: 进行中/已完成/已放弃',
    `remark`         VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健身目标表';

-- 6. 训练计划表
CREATE TABLE IF NOT EXISTS `workout_plans` (
    `id`          BIGINT        NOT NULL COMMENT '主键(雪花ID)',
    `user_id`     BIGINT        NOT NULL COMMENT '用户ID',
    `goal_id`     BIGINT        DEFAULT NULL COMMENT '关联目标ID',
    `plan_name`   VARCHAR(100)  NOT NULL COMMENT '计划名称',
    `plan_goal`   VARCHAR(255)  DEFAULT NULL COMMENT '计划目标',
    `start_date`  DATE          DEFAULT NULL COMMENT '开始日期',
    `end_date`    DATE          DEFAULT NULL COMMENT '结束日期',
    `frequency`   VARCHAR(50)   DEFAULT NULL COMMENT '频率: 如 每周3次',
    `description` VARCHAR(500)  DEFAULT NULL COMMENT '计划描述',
    `status`      VARCHAR(20)   NOT NULL DEFAULT '进行中' COMMENT '状态: 进行中/已完成/已放弃',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_goal_id` (`goal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练计划表';

-- 7. 训练记录表
CREATE TABLE IF NOT EXISTS `workout_records` (
    `id`              BIGINT        NOT NULL COMMENT '主键(雪花ID)',
    `user_id`         BIGINT        NOT NULL COMMENT '用户ID',
    `plan_id`         BIGINT        DEFAULT NULL COMMENT '训练计划ID',
    `exercise_id`     BIGINT        NOT NULL COMMENT '运动ID',
    `duration`        INT           DEFAULT NULL COMMENT '运动时长(分钟)',
    `sets_count`      INT           DEFAULT NULL COMMENT '组数',
    `reps`            INT           DEFAULT NULL COMMENT '每组次数',
    `calories_burned` DECIMAL(8,1)  DEFAULT NULL COMMENT '消耗卡路里(kcal)',
    `record_date`     DATE          NOT NULL COMMENT '记录日期',
    `remark`          VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_plan_id` (`plan_id`),
    KEY `idx_exercise_id` (`exercise_id`),
    KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练记录表';
