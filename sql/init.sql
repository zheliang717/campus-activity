-- ============================================
-- 校园活动报名与场地调度系统 - 数据库初始化
-- 使用 Navicat 执行本脚本即可创建数据库和初始数据
-- ============================================

-- 创建数据库（如不存在则创建）
CREATE DATABASE IF NOT EXISTS campus_activity
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_activity;

-- ============================================
-- 1. 场地表 (venue)
-- ============================================
DROP TABLE IF EXISTS registration;
DROP TABLE IF EXISTS activity;
DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS venue;

CREATE TABLE venue (
    venue_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '场地ID',
    name       VARCHAR(100) NOT NULL COMMENT '场地名称',
    location   VARCHAR(200) COMMENT '地点',
    capacity   INT          DEFAULT 0 COMMENT '容量(人数)',
    open_time  VARCHAR(100) COMMENT '开放时间(如: 08:00-22:00)',
    status     INT          DEFAULT 1 COMMENT '使用状态: 1-可用 0-不可用',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地表';

-- ============================================
-- 2. 活动表 (activity)
-- ============================================
CREATE TABLE activity (
    activity_id         INT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
    name                VARCHAR(200) NOT NULL COMMENT '活动名称',
    type                VARCHAR(50)  COMMENT '活动类型(学术讲座/文体活动/志愿服务/其他)',
    organizer           VARCHAR(100) COMMENT '发起单位(社团/学院)',
    activity_date       DATE         COMMENT '活动日期',
    start_time          VARCHAR(20)  COMMENT '开始时间(HH:mm)',
    end_time            VARCHAR(20)  COMMENT '结束时间(HH:mm)',
    venue_id            INT          COMMENT '场地ID',
    description         TEXT         COMMENT '活动描述',
    max_participants    INT          DEFAULT 0 COMMENT '最大参与人数',
    current_participants INT         DEFAULT 0 COMMENT '当前报名人数',
    status              VARCHAR(20)  DEFAULT '待审核' COMMENT '状态: 待审核/已通过/已拒绝',
    create_time         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (venue_id) REFERENCES venue(venue_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- ============================================
-- 3. 人员表 (person)
-- ============================================
CREATE TABLE person (
    person_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '人员ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名(登录用)',
    password    VARCHAR(100) NOT NULL COMMENT '密码',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    role        VARCHAR(20)  NOT NULL COMMENT '角色: admin-管理员 student-学生',
    phone       VARCHAR(20)  COMMENT '联系方式(电话)',
    email       VARCHAR(100) COMMENT '邮箱',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员表';

-- ============================================
-- 4. 报名表 (registration)
-- ============================================
CREATE TABLE registration (
    reg_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '报名ID',
    activity_id INT NOT NULL COMMENT '活动ID',
    person_id   INT NOT NULL COMMENT '人员ID',
    reg_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    status      VARCHAR(20) DEFAULT '已报名' COMMENT '状态: 已报名/已取消',
    FOREIGN KEY (activity_id) REFERENCES activity(activity_id) ON DELETE CASCADE,
    FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE CASCADE,
    UNIQUE KEY uk_activity_person (activity_id, person_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名表';

-- ============================================
-- 初始数据
-- ============================================

-- 管理员账号: admin / admin123
-- 学生账号:   student1 / 123456
INSERT INTO person (username, password, name, role, phone, email) VALUES
('admin',    'admin123', '系统管理员', 'admin',   '13800000001', 'admin@campus.edu'),
('student1', '123456',   '张三',       'student', '13800000002', 'zhangsan@campus.edu'),
('student2', '123456',   '李四',       'student', '13800000003', 'lisi@campus.edu'),
('student3', '123456',   '王五',       'student', '13800000004', 'wangwu@campus.edu');

-- 场地数据
INSERT INTO venue (name, location, capacity, open_time, status) VALUES
('报告厅A',   '综合楼1层101室', 300, '08:00-22:00', 1),
('体育馆',     '体育中心',       500, '06:00-21:00', 1),
('活动中心多功能厅', '学生活动中心3层', 200, '08:00-22:00', 1),
('小礼堂',     '行政楼B1层',     150, '08:00-20:00', 1),
('室外操场',   '南操场',        1000, '06:00-19:00', 1),
('教室A201',  '教学楼A栋201',    60,  '08:00-21:00', 1),
('教室A301',  '教学楼A栋301',    80,  '08:00-21:00', 0);

-- 活动数据
INSERT INTO activity (name, type, organizer, activity_date, start_time, end_time, venue_id, description, max_participants, current_participants, status) VALUES
('人工智能前沿讲座',      '学术讲座', '计算机学院',   '2026-05-15', '14:00', '16:00', 1, '邀请知名教授讲解AI最新进展', 250, 120, '已通过'),
('春季篮球赛',           '文体活动', '学生会',       '2026-05-20', '09:00', '17:00', 2, '各学院篮球队友谊赛', 400, 200, '已通过'),
('志愿者支教招募说明会',  '志愿服务', '青年志愿者协会', '2026-05-18', '10:00', '11:30', 4, '暑期支教项目说明会', 100, 45,  '已通过'),
('校园歌手大赛',         '文体活动', '文艺部',       '2026-05-25', '18:00', '21:00', 3, '一年一度的校园歌手大赛决赛', 200, 180, '已通过'),
('数据科学研讨会',        '学术讲座', '数学学院',     '2026-05-22', '14:00', '17:00', 6, '数据科学方法与前沿', 50, 0,   '待审核'),
('太极拳体验课',         '文体活动', '体育部',       '2026-05-28', '07:00', '08:30', 5, '太极拳入门体验课', 50, 0,    '待审核'),
('编程马拉松',           '其他',    'ACM社团',      '2026-05-30', '08:00', '20:00', 1, '48小时极限编程挑战', 150, 0,  '已拒绝');

-- 报名数据
INSERT INTO registration (activity_id, person_id, reg_time, status) VALUES
(1, 2, '2026-05-10 09:00:00', '已报名'),
(1, 3, '2026-05-10 09:30:00', '已报名'),
(2, 2, '2026-05-10 10:00:00', '已报名'),
(3, 3, '2026-05-10 11:00:00', '已报名'),
(4, 2, '2026-05-09 14:00:00', '已报名'),
(4, 3, '2026-05-09 15:00:00', '已报名'),
(4, 4, '2026-05-09 16:00:00', '已报名');
