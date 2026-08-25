USE book_manager;

CREATE TABLE IF NOT EXISTS `user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `role` VARCHAR(10) NOT NULL DEFAULT 'user' COMMENT 'admin/user',
    `phone` VARCHAR(20) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `status` INT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `category` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `parent_id` INT DEFAULT NULL COMMENT '父级分类ID',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `book` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `isbn` VARCHAR(20) NOT NULL UNIQUE,
    `name` VARCHAR(200) NOT NULL,
    `author` VARCHAR(100) NOT NULL,
    `publisher` VARCHAR(100) DEFAULT NULL,
    `category_id` INT DEFAULT NULL,
    `description` TEXT DEFAULT NULL,
    `cover` VARCHAR(255) DEFAULT NULL,
    `stock` INT NOT NULL DEFAULT 1,
    `total` INT NOT NULL DEFAULT 1,
    `borrow_count` INT NOT NULL DEFAULT 0,
    `status` INT NOT NULL DEFAULT 1 COMMENT '0-下架 1-上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `borrow_record` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `book_id` INT NOT NULL,
    `borrow_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `due_time` DATETIME NOT NULL,
    `return_time` DATETIME DEFAULT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'borrowing' COMMENT 'borrowing/returned/overdue',
    `fine_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '逾期罚款金额',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`book_id`) REFERENCES `book`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `notification` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `type` VARCHAR(20) NOT NULL COMMENT 'borrow/return/overdue_warn',
    `title` VARCHAR(200) NOT NULL,
    `content` VARCHAR(500) DEFAULT NULL,
    `related_id` INT DEFAULT NULL,
    `is_read` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `operator_id` INT NOT NULL,
    `operator_name` VARCHAR(50) NOT NULL,
    `action` VARCHAR(20) NOT NULL COMMENT 'CREATE/UPDATE/DELETE',
    `target_type` VARCHAR(30) NOT NULL COMMENT 'BOOK/USER/CATEGORY/BORROW',
    `target_id` INT DEFAULT NULL,
    `detail` VARCHAR(500) DEFAULT NULL,
    `ip` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `fine_rule` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `fine_per_day` DECIMAL(10,2) NOT NULL DEFAULT 0.50 COMMENT '每天罚款金额',
    `max_fine` DECIMAL(10,2) NOT NULL DEFAULT 100.00 COMMENT '最高罚款上限',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 默认用户
INSERT IGNORE INTO `user` (`username`, `password`, `name`, `role`, `status`) VALUES
('admin', 'admin123', '系统管理员', 'admin', 1),
('zhangsan', '123456', '张三', 'user', 1);

-- 图书分类
INSERT IGNORE INTO `category` (`id`, `name`, `description`, `sort_order`, `status`) VALUES
(1, '计算机科学', '计算机、编程、算法相关书籍', 1, 1),
(2, '文学小说', '文学、小说类书籍', 2, 1),
(3, '历史哲学', '历史、哲学类书籍', 3, 1),
(4, '自然科学', '物理、化学、生物等自然科学', 4, 1),
(5, '经济管理', '经济、管理、商业类书籍', 5, 1);

-- 二级分类示例
INSERT IGNORE INTO `category` (`id`, `name`, `description`, `parent_id`, `sort_order`, `status`) VALUES
(6, '前端开发', 'HTML/CSS/JavaScript/TypeScript', 1, 1, 1),
(7, '人工智能', '机器学习、深度学习、NLP', 1, 2, 1),
(8, '中国文学', '中国现当代文学', 2, 1, 1),
(9, '外国文学', '外国文学名著', 2, 2, 1);

-- 示例图书
INSERT IGNORE INTO `book` (`isbn`, `name`, `author`, `publisher`, `category_id`, `stock`, `total`, `description`) VALUES
('9787111213826', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', 1, 5, 5, 'Java编程经典入门教材，详解面向对象编程思想'),
('9787111407010', '算法导论', 'Thomas H.Cormen', '机械工业出版社', 1, 3, 3, '计算机算法领域的圣经之作'),
('9787111544937', '深入理解计算机系统', 'Randal E.Bryant', '机械工业出版社', 1, 4, 4, '从程序员视角深入理解计算机系统原理'),
('9787111575290', '数据库系统概念', 'Abraham Silberschatz', '机械工业出版社', 1, 2, 2, '数据库系统领域的经典教材'),
('9787111599715', '计算机网络：自顶向下方法', 'James F.Kurose', '机械工业出版社', 1, 3, 3, '计算机网络入门经典，自顶向下讲解网络协议'),
('9787115516992', 'Spring实战', 'Craig Walls', '人民邮电出版社', 1, 6, 6, 'Spring框架实战指南，覆盖Spring Boot与Spring Cloud'),
('9787536692930', '三体', '刘慈欣', '重庆出版社', 2, 8, 8, '刘慈欣科幻巨著，荣获雨果奖最佳长篇小说'),
('9787506365437', '活着', '余华', '作家出版社', 2, 5, 5, '余华代表作，讲述人在极端环境下的生存意志');

-- 默认罚款规则
INSERT IGNORE INTO `fine_rule` (`fine_per_day`, `max_fine`, `status`) VALUES (0.50, 100.00, 1);
