-- 数据库初始化脚本：用于创建计划书中定义的 6 张核心业务表。
CREATE DATABASE IF NOT EXISTS fish_shop_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE fish_shop_db;

-- 重建表前先关闭外键检查，避免删除顺序受外键依赖影响。
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS user_address;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS `user`;
SET FOREIGN_KEY_CHECKS = 1;

-- 用户表：保存普通用户和管理员账号，手机号用于登录，当前项目密码按明文保存。
CREATE TABLE `user` (
    id INT(11) NOT NULL AUTO_INCREMENT,
    avatar VARCHAR(255) DEFAULT NULL,
    nickname VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role TINYINT(1) NOT NULL DEFAULT 0,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分类表：由后台维护，商品通过 category_id 关联到分类。
CREATE TABLE category (
    id INT(11) NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    sort_order INT(11) NOT NULL DEFAULT 0,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 商品表：保存商品主图、价格、库存、销量等在线销售核心信息。
CREATE TABLE product (
    id INT(11) NOT NULL AUTO_INCREMENT,
    category_id INT(11) NOT NULL,
    name VARCHAR(200) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT(11) NOT NULL,
    specification VARCHAR(100) DEFAULT NULL,
    image_url MEDIUMTEXT DEFAULT NULL,
    description TEXT,
    origin VARCHAR(100) DEFAULT NULL,
    shelf_life VARCHAR(100) DEFAULT NULL,
    storage_method VARCHAR(100) DEFAULT NULL,
    delivery_info VARCHAR(200) DEFAULT NULL,
    sales INT(11) NOT NULL DEFAULT 0,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_name (name),
    KEY idx_sales (sales),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 常用收货地址表：用户下单时可以直接选择常用地址。
CREATE TABLE user_address (
    id INT(11) NOT NULL AUTO_INCREMENT,
    user_id INT(11) NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    address_detail VARCHAR(255) NOT NULL,
    is_default TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_address_user_id (user_id),
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES `user` (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 购物车表：记录用户加入购物车的商品和数量，用户与商品组合保持唯一。
CREATE TABLE cart (
    id INT(11) NOT NULL AUTO_INCREMENT,
    user_id INT(11) NOT NULL,
    product_id INT(11) NOT NULL,
    quantity INT(11) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_product (user_id, product_id),
    KEY idx_cart_user_id (user_id),
    KEY idx_cart_product_id (product_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES `user` (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表：保存一次下单的收货信息、总金额和状态流转。
CREATE TABLE orders (
    id INT(11) NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL,
    user_id INT(11) NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    address_detail VARCHAR(255) NOT NULL,
    tracking_no VARCHAR(100) DEFAULT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_orders_user_id (user_id),
    KEY idx_orders_status (status),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES `user` (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单商品表：保存下单时商品名称、价格、数量等快照，避免商品被删改后影响历史订单。
CREATE TABLE order_item (
    id INT(11) NOT NULL AUTO_INCREMENT,
    order_id INT(11) NOT NULL,
    product_id INT(11) DEFAULT NULL,
    product_name VARCHAR(200) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT(11) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_item_order_id (order_id),
    KEY idx_item_product_id (product_id),
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES product (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化计划书中的默认水产品分类。
INSERT INTO category (name, sort_order, status) VALUES
('鱼类', 1, 1),
('虾类', 2, 1),
('贝类', 3, 1),
('蟹类', 4, 1),
('其他', 5, 1);

-- 初始化示例商品：用于前端首页、商品列表和详情页联调；正式数据可在后台商品管理中维护。
INSERT INTO product (category_id, name, price, stock, specification, image_url, description,
                     origin, shelf_life, storage_method, delivery_info, sales, status) VALUES
(1, '鲜活鲈鱼', 39.90, 28, '约500g/条', 'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?auto=format&fit=crop&w=900&q=80', '肉质细嫩，适合清蒸、红烧，门店当日到货。', '广东湛江', '冷藏 2 天', '0-4℃ 冷藏', '同城冷链配送', 36, 1),
(1, '深海带鱼段', 32.80, 42, '500g/份', 'https://images.unsplash.com/photo-1604909052743-94e838986d24?auto=format&fit=crop&w=900&q=80', '精选带鱼切段，适合香煎和家常红烧。', '浙江舟山', '冷冻 30 天', '-18℃ 冷冻', '冷链配送', 25, 1),
(2, '基围虾', 58.00, 35, '500g/份', 'https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?auto=format&fit=crop&w=900&q=80', '虾肉紧实鲜甜，适合白灼、油焖和烧烤。', '福建漳州', '冷藏 1 天', '0-4℃ 冷藏', '同城冷链配送', 52, 1),
(2, '冷冻虾仁', 46.50, 0, '300g/袋', 'https://images.unsplash.com/photo-1559737558-2f5a35f4523b?auto=format&fit=crop&w=900&q=80', '已去壳虾仁，适合炒菜、煮粥和做馅。', '山东青岛', '冷冻 60 天', '-18℃ 冷冻', '冷链配送', 19, 1),
(3, '鲜活花蛤', 18.80, 60, '500g/份', 'https://images.unsplash.com/photo-1606851091851-e8c8c0fca5ba?auto=format&fit=crop&w=900&q=80', '适合辣炒、煮汤，建议充分吐沙后烹饪。', '辽宁大连', '冷藏 1 天', '0-4℃ 冷藏', '同城冷链配送', 41, 1),
(4, '大闸蟹', 88.00, 16, '约150g/只', 'https://images.unsplash.com/photo-1559737558-2f5a35f4523b?auto=format&fit=crop&w=900&q=80', '蟹黄饱满，适合清蒸，搭配姜醋风味更佳。', '江苏阳澄湖', '冷藏 2 天', '4-8℃ 暂养冷藏', '同城冷链配送', 30, 1),
(5, '海带结', 9.90, 80, '400g/份', 'https://images.unsplash.com/photo-1544943910-4c1dc44aab44?auto=format&fit=crop&w=900&q=80', '适合凉拌、炖汤和火锅配菜。', '福建霞浦', '冷藏 7 天', '0-4℃ 冷藏', '普通冷链配送', 13, 1),
(5, '鱼丸组合', 26.80, 50, '500g/袋', 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=900&q=80', '多口味鱼丸组合，适合火锅、煮汤和关东煮。', '广东汕头', '冷冻 60 天', '-18℃ 冷冻', '冷链配送', 22, 1);
