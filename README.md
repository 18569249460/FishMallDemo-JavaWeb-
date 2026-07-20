# 鲜渔小店运行说明

这是一个基于 SSM + Vue 3 的水产品商城项目，包含用户端商品浏览、购物车、下单、个人订单，以及管理端商品、分类、用户、订单维护。

## 1. 本地运行环境

- JDK 8
- Maven 3
- MySQL 8
- Tomcat 8.5 或 9
- Node.js 18 或更高版本
- IDEA

## 2. 数据库准备

1. 启动本机 MySQL。
2. 创建数据库并导入数据：

```sql
fish_shop_db.sql
```

3. 当前数据库连接配置在：

```text
src/main/resources/db.properties
```

默认配置为：

```properties
jdbc.url=jdbc:mysql://localhost:3306/fish_shop_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=123456
```

如果你的 MySQL 密码不是 `123456`，需要先修改 `db.properties`。

## 3. 后端启动

后端通过 IDEA 配置 Tomcat 启动。

推荐配置：

- Tomcat：本机 Tomcat 8.5 或 9
- Artifact：`fishMall:war exploded`
- Application context：`/fishMall`
- HTTP Port：`8080`

启动成功后，后端基础地址是：

```text
http://localhost:8080/fishMall
```

头像和商品图片上传到 Web 应用内的不同图片目录：

```text
src/main/webapp/uploads/images/touxiang
src/main/webapp/uploads/images/shangpin
```

运行时返回的图片访问地址形如：

```text
http://localhost:8080/fishMall/uploads/images/touxiang/avatar_xxx.jpg
http://localhost:8080/fishMall/uploads/images/shangpin/product_xxx.jpg
```

前端接口代理会把 `/api` 请求转发到：

```text
http://localhost:8080/fishMall/api
```

## 4. 前端启动

电脑重启后，前端不会自动运行，需要重新打开 PowerShell：

```powershell
cd ‘本地路径’\fishMall\frontend
npm run dev
```

启动成功后访问：

```text
http://localhost:5173
```

日常测试请打开 `http://localhost:5173`，不要直接打开 `http://localhost:8080/fishMall`。

## 5. 推荐启动顺序

每次电脑重启后按这个顺序操作：

1. 启动 MySQL。
2. 在 IDEA 中启动 Tomcat 后端。
3. 在 `frontend` 目录执行 `npm run dev`。
4. 浏览器访问 `http://localhost:5173`。

如果页面提示“商品加载失败”或“无法连接后端服务”，优先检查 Tomcat 是否启动成功。

## 6. 测试账号

执行 `docs/测试数据.sql` 后，可使用以下账号测试：

| 角色 | 手机号 | 密码 |
| :--- | :--- | :--- |
| 普通用户 | `18812345678` | `123456` |
| 管理员 | `19900000000` | `123456` |

管理员登录后可以进入：

```text
http://localhost:5173/admin
```

## 7. 常用命令

后端打包验证：

```powershell
cd D:\Acode\fishMall
mvn -q -DskipTests package
```

前端构建验证：

```powershell
cd D:\Acode\fishMall\frontend
npm run build
```

## 8. 常见问题

### 1. 前端页面能打开，但商品加载失败

通常是后端 Tomcat 没启动，或者 Tomcat 的 Application context 不是 `/fishMall`。

检查：

- Tomcat 是否正在运行。
- IDEA 部署路径是否是 `/fishMall`。
- `frontend/vite.config.js` 中代理地址是否仍是 `http://localhost:8080/fishMall`。

### 2. 后端启动失败，提示数据库连接错误

检查：

- MySQL 是否启动。
- `fish_shop_db` 数据库是否存在。
- `db.properties` 中用户名和密码是否正确。

### 3. 登录后无法进入管理后台

检查：

- 是否使用管理员账号 `19900000000`。
- 是否已执行 `docs/测试数据.sql`。
- 数据库中管理员用户的 `role` 是否为 `1`。

### 4. 前端端口被占用

如果 `5173` 被占用，可以临时换端口：

```powershell
cd D:\Acode\fishMall\frontend
npm run dev -- --port 5174
```

浏览器访问：

```text
http://localhost:5174
```
