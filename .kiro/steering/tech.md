# 技术栈

## 后端

### 核心框架
- **Spring Boot**: 3.5.4
- **Java**: 17
- **Maven**: 多模块项目结构

### 数据库与持久化
- **MyBatis**: 3.0.4，使用 XML mapper 文件
- **MySQL**: 8.2.0
- **Druid**: 1.2.23（数据库连接池）
- **PageHelper**: 2.1.1（分页插件）
- **Hive JDBC**: 3.1.3（数据仓库连接）

### 安全与认证
- **JWT**: 0.9.1（基于令牌的认证）
- **Spring Security**: 若依框架内置
- **JSEncrypt**: 敏感数据的 RSA 加密

### API 文档
- **Springdoc OpenAPI**: 2.8.9（替代 Swagger）
- 访问地址：`/swagger-ui.html` 和 `/v3/api-docs`

### 工具库
- **FastJSON**: 2.0.57（JSON 处理）
- **Apache POI**: 4.1.2（Excel 操作）
- **Apache Velocity**: 2.3（代码生成模板）
- **Kaptcha**: 2.3.3（验证码生成）
- **OSHI**: 6.8.2（系统监控）

## 前端

### 核心框架
- **Vue.js**: 2.6.12
- **Vue Router**: 3.4.9
- **Vuex**: 3.6.0（状态管理）

### UI 组件
- **Element UI**: 2.15.14（主要组件库）
- **ECharts**: 5.4.0（数据可视化）
- **Vue TreeSelect**: 0.4.0
- **Quill**: 2.0.2（富文本编辑器）
- **Vue Cropper**: 0.5.5（图片裁剪）

### 构建工具
- **Vue CLI**: 4.4.6
- **Webpack**: 通过 Vue CLI
- **Babel**: ES6+ 转译
- **Sass**: 1.32.13（CSS 预处理）

### 工具库
- **Axios**: 0.28.1（HTTP 客户端）
- **js-cookie**: 3.0.1（Cookie 管理）
- **Sortable.js**: 1.10.2（拖拽功能）
- **Screenfull**: 5.0.2（全屏 API）
- **NProgress**: 0.2.0（进度条）

## 常用命令

### 后端（Maven）

```bash
# 构建整个项目
mvn clean install

# 运行特定模块（在模块目录下）
mvn spring-boot:run

# 运行主应用（在 iras-admin 目录下）
mvn spring-boot:run

# 打包生产环境
mvn clean package -DskipTests

# 运行测试
mvn test
```

### 前端（npm/yarn）

```bash
# 安装依赖
npm install

# 开发服务器（运行在 80 端口，代理到 localhost:8080）
npm run dev

# 生产环境构建
npm run build:prod

# 预发布环境构建
npm run build:stage

# 预览生产构建
npm run preview
```

### 应用启动

1. 启动后端：运行 `IrasApplication.java` 或使用 `ry.bat`（Windows）/ `ry.sh`（Linux）
2. 启动前端：在 `ui/` 目录下运行 `npm run dev`
3. 访问应用：`http://localhost:80`
4. API 文档：`http://localhost:8080/swagger-ui.html`

## 配置文件

### 后端配置
- 主配置：`iras-admin/src/main/resources/application.yml`
- 数据库配置：`application-druid.yml`
- MyBatis 配置：`mybatis/mybatis-config.xml`

### 前端配置
- 构建配置：`ui/vue.config.js`
- 应用设置：`ui/src/settings.js`
- 环境变量：`.env.development`、`.env.production`、`.env.staging`
- API 代理：在 `vue.config.js` 的 devServer 中配置

## 开发工具

- **热重载**：后端（Spring DevTools）和前端（Vue CLI）均已启用
- **代码生成**：内置生成器模块用于 CRUD 脚手架
- **API 测试**：Springdoc UI 提供交互式 API 测试
- **Redis**：可选的缓存层（localhost:6379）
