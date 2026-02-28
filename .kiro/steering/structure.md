# 项目结构

## 根目录布局

```
iras/
├── iras-admin/          # 主应用入口和控制器
├── iras-bi/             # 商业智能模块（仪表板和分析）
├── iras-common/         # 通用工具、常量和基础类
├── iras-framework/      # 核心框架（安全、配置、拦截器）
├── iras-generator/      # 代码生成模块
├── iras-system/         # 系统管理（用户、角色、权限、部门）
├── ui/                  # Vue.js 前端应用
├── pom.xml              # 父级 Maven POM
└── package.json         # 根目录 npm 依赖（echarts、vuedraggable）
```

## 后端模块结构

### iras-admin
主应用入口和 REST 控制器。

```
iras-admin/
├── src/main/java/com/zjrcu/iras/
│   └── IrasApplication.java              # Spring Boot 主类
└── src/main/resources/
    ├── application.yml                    # 主配置文件
    ├── application-druid.yml              # 数据库配置
    ├── logback.xml                        # 日志配置
    ├── mybatis/mybatis-config.xml         # MyBatis 全局配置
    └── i18n/messages.properties           # 国际化
```

### iras-bi
商业智能模块，包含所有仪表板和分析功能。

```
iras-bi/
├── src/main/java/com/zjrcu/iras/bi/
│   ├── conversionquality/                 # 转换质量监控
│   ├── fieldquality/                      # 字段/数据质量监控
│   ├── indicatorquality/                  # 指标质量监控
│   └── globaloverview/                    # 全局总览仪表板
└── src/main/resources/
    ├── mapper/                            # MyBatis XML 映射文件
    │   ├── conversionqualityboard/
    │   ├── dataqualityboard/
    │   ├── indicatorqualityboard/
    │   └── globaloverview/
    └── config/whitelist.yml               # 白名单配置
```

### iras-common
所有模块共享的工具和领域对象。

```
iras-common/
└── src/main/java/com/zjrcu/iras/common/
    ├── annotation/                        # 自定义注解
    ├── constant/                          # 常量和枚举
    ├── core/                              # 核心领域对象
    │   ├── domain/                        # 基础领域类（AjaxResult、BaseEntity）
    │   ├── controller/                    # 基础控制器
    │   └── page/                          # 分页支持
    ├── exception/                         # 自定义异常
    ├── utils/                             # 工具类
    └── config/                            # 通用配置
```

### iras-framework
核心框架功能，包括安全、配置和拦截器。

```
iras-framework/
└── src/main/java/com/zjrcu/iras/framework/
    ├── config/                            # 框架配置
    ├── security/                          # 安全与认证
    ├── interceptor/                       # 请求拦截器
    ├── aspectj/                           # AOP 切面（日志、权限）
    └── web/                               # Web 相关工具
```

### iras-system
系统管理模块，用于用户、角色、部门和权限管理。

```
iras-system/
├── src/main/java/com/zjrcu/iras/system/
│   ├── controller/                        # 系统管理控制器
│   ├── service/                           # 业务逻辑服务
│   ├── mapper/                            # MyBatis mapper 接口
│   └── domain/                            # 领域实体
└── src/main/resources/mapper/system/
    ├── SysUserMapper.xml                  # 用户管理
    ├── SysRoleMapper.xml                  # 角色管理
    ├── SysDeptMapper.xml                  # 部门管理
    ├── SysMenuMapper.xml                  # 菜单/权限管理
    └── ...                                # 其他系统映射文件
```

## 前端结构

### ui/ 目录

```
ui/
├── public/                                # 静态资源
│   ├── index.html                         # HTML 模板
│   └── styles/theme-chalk/                # Element UI 主题
├── src/
│   ├── api/                               # API 服务模块
│   │   ├── login.js                       # 认证 API
│   │   ├── system/                        # 系统管理 API
│   │   └── monitor/                       # 监控 API
│   ├── assets/                            # 静态资源
│   │   ├── icons/svg/                     # SVG 图标
│   │   ├── images/                        # 图片
│   │   └── styles/                        # 全局样式（SCSS）
│   ├── components/                        # 可复用组件
│   │   ├── Pagination/                    # 分页组件
│   │   ├── RightToolbar/                  # 表格工具栏
│   │   ├── DictTag/                       # 字典标签
│   │   ├── Editor/                        # 富文本编辑器
│   │   └── ...                            # 其他组件
│   ├── directive/                         # 自定义 Vue 指令
│   │   ├── permission/                    # 权限指令
│   │   └── dialog/                        # 对话框拖拽指令
│   ├── layout/                            # 布局组件
│   ├── router/                            # Vue Router 配置
│   ├── store/                             # Vuex 状态管理模块
│   │   └── modules/                       # Store 模块（app、user、permission）
│   ├── utils/                             # 工具函数
│   │   ├── request.js                     # Axios 封装
│   │   ├── auth.js                        # 认证工具
│   │   ├── dict/                          # 字典工具
│   │   └── ...                            # 其他工具
│   ├── views/                             # 页面组件
│   │   ├── dashboard/                     # 仪表板页面
│   │   ├── dataquality/                   # 数据质量页面
│   │   │   ├── dataqualityBoard/          # 质量看板视图
│   │   │   ├── creditRisk/                # 信贷风险监控
│   │   │   └── estate/                    # 房地产监控
│   │   ├── system/                        # 系统管理页面
│   │   ├── monitor/                       # 系统监控页面
│   │   └── error/                         # 错误页面（401、404）
│   ├── App.vue                            # 根组件
│   ├── main.js                            # 应用入口
│   ├── permission.js                      # 路由权限控制
│   └── settings.js                        # 应用设置
├── babel.config.js                        # Babel 配置
├── vue.config.js                          # Vue CLI 配置
└── package.json                           # npm 依赖
```

## 关键架构模式

### 后端模式

1. **分层架构**：Controller → Service → Mapper → Database
2. **MyBatis XML Mapper**：SQL 查询位于 `resources/mapper/` 下的 XML 文件中
3. **AjaxResult 响应**：标准化的 API 响应格式，包含 code、msg、data
4. **基础类**：BaseEntity、BaseController 提供通用功能
5. **多模块 Maven**：不同关注点分离到不同模块
6. **包结构**：`com.zjrcu.iras.{模块}.{层级}`

### 前端模式

1. **组件化**：`components/` 中的可复用 Vue 组件
2. **视图-视图模型**：页面在 `views/`，API 调用在 `api/`
3. **集中式状态**：Vuex store 用于全局状态管理
4. **路由守卫**：`permission.js` 中的权限检查
5. **API 代理**：`vue.config.js` 中配置开发环境代理到后端
6. **全局混入**：通用工具挂载到 Vue.prototype

## 文件命名规范

### 后端
- **Java 类**：大驼峰命名（如 `UserController.java`、`AjaxResult.java`）
- **Mapper XML**：与接口名称匹配（如 `SysUserMapper.xml`）
- **包名**：小写（如 `com.zjrcu.iras.system.controller`）

### 前端
- **组件**：大驼峰命名的文件夹和文件（如 `Pagination/index.vue`）
- **视图**：小写加连字符（如 `data-quality/index.vue`）
- **工具/API**：小驼峰命名（如 `request.js`、`login.js`）
- **样式**：短横线命名（如 `element-ui.scss`、`quality-board.scss`）

## 配置文件

### 后端
- `application.yml`：主应用配置
- `application-druid.yml`：数据库连接池设置
- `mybatis-config.xml`：MyBatis 全局设置
- `logback.xml`：日志配置

### 前端
- `vue.config.js`：Webpack 和开发服务器配置
- `settings.js`：应用 UI 设置
- `.env.*`：环境特定变量
- `babel.config.js`：JavaScript 转译设置
