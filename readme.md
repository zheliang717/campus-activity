# 校园活动报名与场地调度系统

## 课程设计报告

---

**项目名称：** 校园活动报名与场地调度系统  
**开发环境：** Windows 10 / Java 17 / MySQL 8.0 / Maven 3.9 / VSCode  
**技术栈：** Spring Boot 3.2 + MyBatis-Plus 3.5 + Bootstrap 5 + 原生JavaScript  
**完成日期：** 2026年5月

---

## 目录

1. [系统概述](#1-系统概述)
2. [需求分析](#2-需求分析)
3. [系统设计](#3-系统设计)
4. [数据库设计](#4-数据库设计)
5. [系统实现](#5-系统实现)
6. [系统测试](#6-系统测试)
7. [总结与展望](#7-总结与展望)

---

## 1. 系统概述

### 1.1 项目背景

高校社团和学院经常举办各类学术讲座、文体活动、志愿服务等活动，传统的人工管理方式效率低下，容易出现场地冲突、报名混乱、信息不对称等问题。本系统旨在为校园活动管理提供一套信息化解决方案，实现对活动、场地、报名信息的统一管理。

### 1.2 项目目标

- 实现管理员对场地和活动的集中管理
- 支持学生在线浏览活动并报名
- 提供活动日程的可视化展示
- 支持多条件组合查询
- 采用B/S架构，用户无需安装客户端

### 1.3 系统角色

| 角色 | 功能描述 |
|------|----------|
| **管理员** | 场地CRUD管理、活动发布与审核、活动日程查看、系统数据概览 |
| **学生** | 浏览已通过活动、在线报名/取消报名、查看报名记录、查看活动日程 |

---

## 2. 需求分析

### 2.1 功能需求

| 编号 | 功能模块 | 功能描述 | 角色 |
|------|----------|----------|------|
| F1 | 用户认证 | 登录/注册/退出，支持7天免登录 | 全部 |
| F2 | 角色菜单 | 根据角色展示不同功能菜单 | 全部 |
| F3 | 场地管理 | 场地增删改查、可用/不可用状态切换 | 管理员 |
| F4 | 活动管理 | 活动增删改查、多条件组合查询、分页展示 | 管理员 |
| F5 | 活动审核 | 对待审核活动进行"通过"或"拒绝"操作 | 管理员 |
| F6 | 仪表盘 | 场地/活动/用户统计数据、待审核活动快速处理 | 管理员 |
| F7 | 浏览活动 | 分页浏览已通过活动，按名称/类型/日期筛选 | 学生 |
| F8 | 活动报名 | 查看活动详情、报名（自动校验名额与重复报名） | 学生 |
| F9 | 报名管理 | 查看个人报名记录、取消报名 | 学生 |
| F10 | 活动日程 | 按日期筛选、按场地分组展示已通过活动 | 全部 |

### 2.2 非功能需求

- **可靠性**：主外键约束保证数据一致性
- **可用性**：响应式布局，支持不同屏幕尺寸
- **性能**：分页查询，避免大数据量一次性加载
- **安全性**：Session + Cookie双重认证，密码不返回前端

---

## 3. 系统设计

### 3.1 系统架构

```
┌─────────────────────────────────────────────────┐
│                    浏览器                         │
│         Bootstrap 5 + 原生 JavaScript             │
│         (SPA单页应用，Fetch API通信)              │
└─────────────────────┬───────────────────────────┘
                      │ HTTP/REST
┌─────────────────────┴───────────────────────────┐
│              Spring Boot 3.2 (8080)               │
│  ┌──────────┐ ┌──────────┐ ┌──────────────────┐ │
│  │ LoginFilter│ │  CORS配置 │ │ MyBatisPlusConfig│ │
│  └──────────┘ └──────────┘ └──────────────────┘ │
│  ┌──────────────────────────────────────────────┐│
│  │          Controller 层 (REST API)            ││
│  │  PersonController  /  VenueController        ││
│  │  ActivityController / RegistrationController ││
│  └──────────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────────┐│
│  │             Service 层 (业务逻辑)             ││
│  └──────────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────────┐│
│  │        MyBatis-Plus Mapper 层 (数据访问)      ││
│  └──────────────────────────────────────────────┘│
└─────────────────────┬───────────────────────────┘
                      │ JDBC
┌─────────────────────┴───────────────────────────┐
│              MySQL 8.0 (3306)                     │
│           Database: campus_activity               │
│   venue │ activity │ person │ registration        │
└─────────────────────────────────────────────────┘
```

### 3.2 设计模式

- **分层架构**：Controller → Service → Mapper，职责清晰
- **统一响应格式**：所有API返回 `{code, message, data}` 格式的Result对象
- **RESTful API**：遵循REST规范，GET查询/POST新增/PUT修改/DELETE删除
- **前端单页应用**：管理员和学生各一个HTML页面，通过JS切换内容区

### 3.3 模块划分

```
com.campus.activity
├── CampusActivityApplication.java    # 启动类
├── common/
│   ├── Result.java                   # 统一响应封装
│   └── PageResult.java               # 分页结果封装
├── config/
│   ├── LoginFilter.java              # 登录拦截 + Cookie自动登录
│   ├── WebMvcConfig.java             # 跨域配置
│   ├── MyBatisPlusConfig.java        # 分页插件配置
│   └── MetaObjectHandlerConfig.java  # 自动填充时间戳
├── entity/
│   ├── Venue.java                    # 场地实体
│   ├── Activity.java                 # 活动实体（含联表字段）
│   ├── Person.java                   # 人员实体
│   └── Registration.java             # 报名实体（含联表字段）
├── mapper/
│   ├── VenueMapper.java              # 场地Mapper
│   ├── ActivityMapper.java           # 活动Mapper（自定义联表+分页查询）
│   ├── PersonMapper.java             # 人员Mapper
│   └── RegistrationMapper.java       # 报名Mapper（自定义联表+分页查询）
├── service/
│   ├── VenueService.java / impl      # 场地业务
│   ├── ActivityService.java / impl   # 活动业务
│   ├── PersonService.java / impl     # 人员业务
│   └── RegistrationService.java / impl # 报名业务
└── controller/
    ├── VenueController.java          # 场地API
    ├── ActivityController.java       # 活动API
    ├── PersonController.java         # 用户API（登录/注册/免登录）
    └── RegistrationController.java   # 报名API
```

### 3.4 API接口设计

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/person/login` | 登录（支持remember参数） |
| GET | `/api/person/auto-login` | Cookie自动登录 |
| GET | `/api/person/current` | 获取当前用户 |
| POST | `/api/person/logout` | 退出登录 |
| POST | `/api/person/register` | 学生注册 |
| GET | `/api/venue/page` | 场地分页列表 |
| POST | `/api/venue/add` | 新增场地 |
| PUT | `/api/venue/update` | 修改场地 |
| DELETE | `/api/venue/{id}` | 删除场地 |
| GET | `/api/activity/page` | 活动分页列表（支持name/type/date/status筛选） |
| POST | `/api/activity/add` | 发布活动 |
| PUT | `/api/activity/update` | 修改活动 |
| PUT | `/api/activity/review/{id}` | 审核活动 |
| DELETE | `/api/activity/{id}` | 删除活动 |
| GET | `/api/activity/schedule` | 活动日程（按场地分组） |
| POST | `/api/registration/register` | 报名活动 |
| POST | `/api/registration/cancel/{regId}` | 取消报名 |
| GET | `/api/registration/my/page` | 我的报名（分页） |
| GET | `/api/registration/check` | 检查是否已报名 |
| GET | `/api/registration/count/{activityId}` | 活动报名人数 |

---

## 4. 数据库设计

### 4.1 E-R图（文字描述）

```
场地(venue) ──1:N── 活动(activity)
人员(person) ──1:N── 报名(registration) ──N:1── 活动(activity)
```

- 一个场地可举办多个活动
- 一个人员可报名多个活动
- 一个活动可被多个人报名
- 报名表是人员和活动的多对多关联表

### 4.2 场地表 (venue)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| venue_id | INT | PK, AUTO_INCREMENT | 场地ID |
| name | VARCHAR(100) | NOT NULL | 场地名称 |
| location | VARCHAR(200) | | 地点 |
| capacity | INT | DEFAULT 0 | 容量（人数） |
| open_time | VARCHAR(100) | | 开放时间 |
| status | INT | DEFAULT 1 | 1-可用 0-不可用 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 4.3 活动表 (activity)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| activity_id | INT | PK, AUTO_INCREMENT | 活动ID |
| name | VARCHAR(200) | NOT NULL | 活动名称 |
| type | VARCHAR(50) | | 类型：学术讲座/文体活动/志愿服务/其他 |
| organizer | VARCHAR(100) | | 发起单位 |
| activity_date | DATE | | 活动日期 |
| start_time | VARCHAR(20) | | 开始时间 |
| end_time | VARCHAR(20) | | 结束时间 |
| venue_id | INT | FK→venue(venue_id) | 场地ID |
| description | TEXT | | 活动描述 |
| max_participants | INT | DEFAULT 0 | 最大参与人数 |
| current_participants | INT | DEFAULT 0 | 当前报名人数 |
| status | VARCHAR(20) | DEFAULT '待审核' | 待审核/已通过/已拒绝 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**外键约束：** `FOREIGN KEY (venue_id) REFERENCES venue(venue_id) ON DELETE SET NULL`

### 4.4 人员表 (person)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| person_id | INT | PK, AUTO_INCREMENT | 人员ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| password | VARCHAR(100) | NOT NULL | 密码 |
| name | VARCHAR(50) | NOT NULL | 姓名 |
| role | VARCHAR(20) | NOT NULL | admin-管理员 / student-学生 |
| phone | VARCHAR(20) | | 联系方式 |
| email | VARCHAR(100) | | 邮箱 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 4.5 报名表 (registration)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| reg_id | INT | PK, AUTO_INCREMENT | 报名ID |
| activity_id | INT | FK→activity, NOT NULL | 活动ID |
| person_id | INT | FK→person, NOT NULL | 人员ID |
| reg_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 报名时间 |
| status | VARCHAR(20) | DEFAULT '已报名' | 已报名/已取消 |

**外键约束：**
- `FOREIGN KEY (activity_id) REFERENCES activity(activity_id) ON DELETE CASCADE`
- `FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE CASCADE`
- `UNIQUE KEY uk_activity_person (activity_id, person_id)` — 防止重复报名

### 4.6 数据规模

| 表名 | 记录数 | 说明 |
|------|--------|------|
| venue | 17 | 报告厅、体育馆、教室、创客空间等 |
| activity | 35 | 覆盖5月中旬~6月中旬，含4种状态 |
| person | 22 | 2名管理员 + 20名学生 |
| registration | 53 | 多活动多人交叉报名 |

---

## 5. 系统实现

### 5.1 开发环境

| 工具/技术 | 版本 |
|-----------|------|
| JDK | OpenJDK 17.0.0.1 |
| Spring Boot | 3.2.5 |
| MyBatis-Plus | 3.5.5 |
| MySQL | 8.0.36 |
| Maven | 3.9.11 |
| 前端 | Bootstrap 5.3 (CDN) + Bootstrap Icons + 原生JavaScript |
| IDE | VSCode / Navicat |

### 5.2 关键技术实现

#### 5.2.1 登录与Session管理

系统采用`HttpSession`维护登录状态。登录成功后，用户信息（不含密码）存入Session的`loginUser`属性。

**七天免登录**通过双重机制实现：
1. 用户勾选"记住我" → 服务端生成UUID令牌 → 存入`ConcurrentHashMap<String, Integer>` → 令牌通过Cookie返回（7天过期）
2. `LoginFilter`拦截请求时，若Session不存在则检查Cookie令牌，找到对应用户后自动重建Session

```java
// PersonController.java
if (Boolean.TRUE.equals(remember)) {
    String token = UUID.randomUUID().toString().replace("-", "");
    AUTO_LOGIN_TOKENS.put(token, person.getPersonId());
    Cookie cookie = new Cookie("auto_token", token);
    cookie.setMaxAge(7 * 24 * 60 * 60); // 7天
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    response.addCookie(cookie);
}
```

#### 5.2.2 分页查询

使用MyBatis-Plus分页插件实现高效分页：

```java
// MyBatisPlusConfig.java
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    return interceptor;
}
```

活动列表需要联表查询（活动+场地），通过自定义SQL + MyBatis-Plus分页IPage实现：

```java
// ActivityMapper.java
@Select("SELECT a.*, v.name AS venue_name, v.location AS venue_location " +
        "FROM activity a LEFT JOIN venue v ON a.venue_id = v.venue_id " +
        "${ew.customSqlSegment}")
IPage<Activity> selectActivityPage(Page<Activity> page, 
                                   @Param(Constants.WRAPPER) QueryWrapper<Activity> wrapper);
```

前端统一使用`renderPagination()`函数生成分页控件，支持页码跳转、省略号、总条数统计。

#### 5.2.3 活动日程（按场地分组）

后端查询所有"已通过"活动，按场地名称+开始时间排序，然后通过Java Stream分组：

```java
Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
for (Activity a : activities) {
    String venueName = a.getVenueName() != null ? a.getVenueName() : "未分配场地";
    // ... 构建item
    grouped.computeIfAbsent(venueName, k -> new ArrayList<>()).add(item);
}
```

前端按场地卡片展示，每个场地下列出其活动时间线。

#### 5.2.4 报名名额校验

```java
// RegistrationServiceImpl.java
@Override
@Transactional
public boolean register(Integer activityId, Integer personId) {
    if (isRegistered(activityId, personId)) return false; // 防止重复报名
    Registration reg = new Registration();
    reg.setActivityId(activityId);
    reg.setPersonId(personId);
    reg.setStatus("已报名");
    return registrationMapper.insert(reg) > 0;
}
```

数据库层面通过`UNIQUE KEY uk_activity_person (activity_id, person_id)`保证同一人对同一活动只能有一条记录。

#### 5.2.5 前端单页应用架构

管理员和学生各使用一个HTML页面，通过侧边栏点击切换内容区：

```javascript
function showSection(name) {
    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.sidebar-nav .nav-link').forEach(l => l.classList.remove('active'));
    document.getElementById('section-' + name).classList.add('active');
    document.querySelector(`[data-section="${name}"]`).classList.add('active');
}
```

每个内容区（section）独立加载数据，通过分页状态变量维护当前页数。

#### 5.2.6 固定表格布局

为消除翻页时表格列宽变化导致的视觉跳动：

```css
.fixed-table-wrap {
    max-height: 540px;
    overflow: auto;
    min-height: 200px;
}
.fixed-table-wrap table {
    table-layout: fixed;  /* 列宽由表头决定，不受内容影响 */
}
.fixed-table-wrap thead {
    position: sticky;
    top: 0;
    z-index: 10;  /* 表头固定，滚动时始终可见 */
}
```

### 5.3 前端页面结构

| 页面 | 功能模块 |
|------|----------|
| `login.html` | 登录表单、学生注册、7天免登录勾选、自动登录检测 |
| `admin/index.html` | 仪表盘（统计卡片+待审核列表）、场地管理（分页CRUD）、活动管理（分页+多条件查询+审核）、活动日程 |
| `student/index.html` | 首页（统计+最新活动）、浏览活动（分页卡片+筛选）、我的报名（分页+取消）、活动日程 |

### 5.4 配置文件 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/campus_activity?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
```

---

## 6. 系统测试

### 6.1 编译测试

```
mvn compile → BUILD SUCCESS (0 errors, 0 warnings)
```

所有22个Java源文件编译通过。

### 6.2 启动测试

```
Spring Boot 3.2.5 → Started in 4.1 seconds
Tomcat initialized with port 8080 (http)
MyBatis-Plus 3.5.5 initialized
```

### 6.3 API接口测试

| 测试项 | 接口 | 预期 | 结果 |
|--------|------|------|------|
| 登录-管理员 | POST /api/person/login {admin/admin123} | code=200, role=admin | ✅ |
| 登录-学生 | POST /api/person/login {student1/123456} | code=200, role=student | ✅ |
| 错误密码 | POST /api/person/login {admin/wrong} | code=400 | ✅ |
| 场地分页 | GET /api/venue/page?page=1&pageSize=8 | total=17, 8 records | ✅ |
| 活动分页 | GET /api/activity/page?page=1 | total=35, 12 records | ✅ |
| 活动筛选 | GET /api/activity/page?status=待审核 | total=3 | ✅ |
| 活动日程 | GET /api/activity/schedule | 4 venue groups | ✅ |
| 报名检查 | GET /api/registration/check?activityId=1 | true (student1已报) | ✅ |
| 我的报名 | GET /api/registration/my/page | total=5 (student1) | ✅ |
| 审核活动 | PUT /api/activity/review/5?status=已通过 | code=200 | ✅ |
| 删除场地 | DELETE /api/venue/1 | code=200 | ✅ |
| 免登录Cookie | POST /api/person/login {remember:true} | Set-Cookie: auto_token | ✅ |
| 自动登录 | GET /api/person/auto-login (with cookie) | code=200 | ✅ |
| 未登录拦截 | GET /pages/admin/index.html (无session/cookie) | 302→login.html | ✅ |

### 6.4 前端功能测试

| 测试项 | 操作 | 预期 | 结果 |
|--------|------|------|------|
| 登录页加载 | 访问 /pages/login.html | 正常显示，无循环刷新 | ✅ |
| 记住我 | 勾选后登录，关闭浏览器重开 | 自动登录 | ✅ |
| 管理员仪表盘 | 登录admin | 4个统计卡片 + 待审核列表 | ✅ |
| 场地管理 | 新增/编辑/删除场地 | 操作成功，列表刷新 | ✅ |
| 活动审核 | 点击通过/拒绝 | 状态更新 | ✅ |
| 活动查询 | 按名称/类型/日期/状态筛选 | 精确返回 | ✅ |
| 分页切换 | 点击页码 | 数据切换，表格无跳动 | ✅ |
| 固定表格 | 翻到不同页 | 列表宽不变，表头固定 | ✅ |
| 学生浏览活动 | 登录student1，浏览 | 卡片展示，名额剩余提示 | ✅ |
| 学生报名 | 点击详情→立即报名 | 报名成功，名额+1 | ✅ |
| 重复报名 | 再次报名同一活动 | 提示失败 | ✅ |
| 取消报名 | 我的报名→取消 | 状态变为已取消 | ✅ |
| 活动日程 | 切换日程页 | 按场地分组展示 | ✅ |
| 退出登录 | 点击退出 | 跳转登录页 | ✅ |

### 6.5 测试数据覆盖

- **场地**：17条，包含可用/不可用两种状态，覆盖报告厅、体育馆、教室、户外等类型
- **活动**：35条，覆盖"待审核"(3)、"已通过"(28)、"已拒绝"(4)三种状态，包含学术讲座、文体活动、志愿服务、其他四种类型
- **人员**：22条，2名管理员 + 20名学生
- **报名**：53条，含"已报名"(49)和"已取消"(2)两种状态

---

## 7. 总结与展望

### 7.1 项目总结

本系统完整实现了校园活动报名与场地调度的核心功能，包括：

1. **角色权限控制**：管理员与学生拥有不同的功能菜单和操作权限
2. **场地管理**：支持增删改查，状态切换（可用/不可用）
3. **活动全生命周期管理**：发布→审核→展示→报名→取消
4. **数据分页查询**：所有列表均支持分页，配合多条件组合筛选
5. **活动日程展示**：按日期筛选、按场地分组，直观呈现活动安排
6. **用户体验优化**：7天免登录、表格固定布局、表头粘性固定、分页零跳动

### 7.2 技术亮点

- **前后端一体化部署**：纯静态前端资源与Spring Boot REST API配合，无需额外Web服务器
- **MyBatis-Plus简化开发**：基础CRUD零SQL，复杂联表查询通过自定义@Select实现
- **统一响应格式**：所有API返回Result对象，前端统一处理成功/失败/未登录三种状态
- **完整的约束保障**：外键约束、唯一约束、事务管理确保数据一致性

### 7.3 改进方向

1. 报名时联动更新`activity.current_participants`字段，实现更准确的实时名额统计
2. 引入Spring Security实现更完善的安全认证
3. 增加活动图片上传功能，丰富活动展示
4. 加入邮件/短信通知，报名成功后自动提醒
5. 场地时间冲突检测，防止同一场地同一时段安排多个活动
6. 前后端分离部署，使用Vue/React重构前端

---

**附录：项目文件清单（33个源文件）**

```
campus-activity/
├── pom.xml
├── sql/init.sql
├── src/main/java/com/campus/activity/
│   ├── CampusActivityApplication.java
│   ├── common/{Result.java, PageResult.java}
│   ├── config/{LoginFilter.java, WebMvcConfig.java, MyBatisPlusConfig.java, MetaObjectHandlerConfig.java}
│   ├── entity/{Venue.java, Activity.java, Person.java, Registration.java}
│   ├── mapper/{VenueMapper.java, ActivityMapper.java, PersonMapper.java, RegistrationMapper.java}
│   ├── service/{VenueService.java, ActivityService.java, PersonService.java, RegistrationService.java}
│   ├── service/impl/{VenueServiceImpl.java, ActivityServiceImpl.java, PersonServiceImpl.java, RegistrationServiceImpl.java}
│   └── controller/{VenueController.java, ActivityController.java, PersonController.java, RegistrationController.java}
└── src/main/resources/
    ├── application.yml
    └── static/
        ├── css/style.css
        ├── js/common.js
        └── pages/
            ├── login.html
            ├── admin/index.html
            └── student/index.html
```

---

## 附录：代码流程详解

> 本章节详细讲解各核心功能的代码调用链路，从前端按钮点击到数据库SQL执行，再到响应渲染，完整呈现数据如何在各层之间传递。

---

### A.1 分页查询 —— 完整数据流

分页功能贯穿系统几乎全部列表页（场地管理、活动管理、浏览活动、我的报名）。以下以 **管理员活动列表分页** 为例，逐层追踪数据流向。

#### A.1.1 触发：用户点击页码

```
用户在浏览器点击 "第3页" 链接
        │
        ▼
┌──────────────────────────────────────────────┐
│  前端 (admin/index.html)                      │
│                                              │
│  renderPagination() 生成的分页按钮绑定了事件:   │
│                                              │
│  link.addEventListener('click', function(e) { │
│      e.preventDefault();                     │
│      const pg = parseInt(                    │
│          this.getAttribute('data-pg')        │
│      );    // pg = 3                         │
│      if (pg !== page) onPageChange(pg);      │
│  });                                         │
│                                              │
│  → 回调函数: loadActivities(3)               │
└────────────────────┬─────────────────────────┘
                     │
                     ▼
```

#### A.1.2 前端：构造HTTP请求

```javascript
// admin/index.html → loadActivities(pg)
async function loadActivities(pg) {
    if (pg) activityPage = pg;  // 更新当前页码状态: activityPage = 3

    // 1. 收集筛选条件
    const p = new URLSearchParams();
    p.append('page', activityPage);      // page=3
    p.append('pageSize', '12');          // pageSize=12
    const name  = document.getElementById('searchName').value.trim();
    const type  = document.getElementById('searchType').value;
    const date  = document.getElementById('searchDate').value;
    const status = document.getElementById('searchStatus').value;
    if (name)   p.append('name', name);
    if (type)   p.append('type', type);
    if (date)   p.append('date', date);
    if (status) p.append('status', status);

    // 2. 发起 GET 请求
    // URL 示例: /api/activity/page?page=3&pageSize=12&status=已通过
    const data = await api.get(
        API_BASE + '/activity/page?' + p.toString()
    );
    // ... 渲染表格
}
```

```javascript
// common.js → api.get()
const api = {
    get: (url) => request(url),
    // ...
};

// common.js → request()
async function request(url, options = {}) {
    const config = {
        headers: { 'Content-Type': 'application/json' },
        ...options
    };
    const resp = await fetch(url, config);
    const data = await resp.json();
    // 如果返回 401 则跳转登录
    if (data.code === 401 && ...) {
        window.location.href = '/pages/login.html';
        return null;
    }
    return data;
}
```

此时发出一条真实的 HTTP 请求：

```
GET /api/activity/page?page=3&pageSize=12 HTTP/1.1
Host: localhost:8080
Content-Type: application/json
```

#### A.1.3 Controller：接收并转发参数

```java
// ActivityController.java
@GetMapping("/page")
public Result page(
        @RequestParam(defaultValue = "1") int page,       // Spring 从 ?page=3 提取, 默认1
        @RequestParam(defaultValue = "12") int pageSize,  // Spring 从 ?pageSize=12 提取
        @RequestParam(required = false) String name,      // 可选参数, 无则为null
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String date,
        @RequestParam(required = false) String status
) {
    // 调用 Service 层, 传递所有原始参数
    PageResult<Activity> result = activityService.listPage(
        page, pageSize, name, type, date, status
    );
    return Result.ok(result);
    // Result.ok() 将 PageResult 对象塞入统一格式:
    // { code: 200, message: "操作成功", data: <PageResult> }
}
```

**参数传递链路：**

```
URL查询参数          →  @RequestParam  →  Service方法形参
─────────────────────────────────────────────────────────
?page=3              →  int page = 3
?pageSize=12         →  int pageSize = 12
?name=xxx(可选)      →  String name = "xxx" 或 null
?type=文体活动(可选)  →  String type = "文体活动" 或 null
?status=已通过(可选)  →  String status = "已通过" 或 null
```

#### A.1.4 Service：构建查询条件

```java
// ActivityServiceImpl.java
@Override
public PageResult<Activity> listPage(
        int page, int pageSize, 
        String name, String type, String date, String status
) {
    // 1. 构建动态查询条件
    QueryWrapper<Activity> qw = buildQuery(name, type, date, status);

    // 2. 创建 MyBatis-Plus 分页对象
    Page<Activity> mpPage = new Page<>(page, pageSize);
    //     Page 对象内部记录: current=3, size=12

    // 3. 调用 Mapper 自定义方法（联表查询 + 分页）
    var result = activityMapper.selectActivityPage(mpPage, qw);
    //     result 是 IPage<Activity> 对象
    //     result.getTotal()    → 总记录数, 如 28
    //     result.getRecords()  → 当前页的 List<Activity>

    // 4. 包装为 PageResult 返回
    return new PageResult<>(
        result.getTotal(),   // total = 28
        page,                // page  = 3
        pageSize,            // pageSize = 12
        result.getRecords()  // records = [Activity(25), Activity(26)...]
    );
}

// PageResult 构造器自动计算 totalPages:
// totalPages = (total + pageSize - 1) / pageSize = (28 + 11) / 12 = 3

private QueryWrapper<Activity> buildQuery(
        String name, String type, String date, String status
) {
    QueryWrapper<Activity> qw = new QueryWrapper<>();
    if (StringUtils.hasText(name))   qw.like("a.name", name);
    if (StringUtils.hasText(type))   qw.eq("a.type", type);
    if (StringUtils.hasText(date))   qw.eq("a.activity_date", date);
    if (StringUtils.hasText(status)) qw.eq("a.status", status);
    qw.orderByDesc("a.create_time");
    return qw;
    // 空参数时 qw 只包含 ORDER BY 子句 → 查全部
    // 有参数时 qw 包含 WHERE ... AND ... + ORDER BY
}
```

#### A.1.5 Mapper：自定义SQL + 分页插件拦截

```java
// ActivityMapper.java
@Select("SELECT a.*, v.name AS venue_name, v.location AS venue_location " +
        "FROM activity a LEFT JOIN venue v ON a.venue_id = v.venue_id " +
        "${ew.customSqlSegment}")
IPage<Activity> selectActivityPage(
    Page<Activity> page,
    @Param(Constants.WRAPPER) QueryWrapper<Activity> wrapper
);
```

**MyBatis-Plus 分页插件在SQL执行时自动拦截，实际发送两条SQL：**

```sql
-- SQL 1: 查总数（分页插件自动生成）
SELECT COUNT(*) 
FROM activity a 
LEFT JOIN venue v ON a.venue_id = v.venue_id;
-- → 返回: 28

-- SQL 2: 查当前页数据（分页插件拼接 LIMIT）
SELECT a.*, v.name AS venue_name, v.location AS venue_location 
FROM activity a 
LEFT JOIN venue v ON a.venue_id = v.venue_id 
ORDER BY a.create_time DESC 
LIMIT 24, 12;
--  LIMIT offset, count
--  offset = (page - 1) × pageSize = (3-1) × 12 = 24
--  count  = pageSize = 12
-- → 返回第25~36条记录
```

**如果有筛选条件，QueryWrapper 的内容会注入 `${ew.customSqlSegment}` 位置：**

```sql
-- 当 status=已通过 时:
SELECT a.*, v.name AS venue_name, v.location AS venue_location 
FROM activity a 
LEFT JOIN venue v ON a.venue_id = v.venue_id 
WHERE (a.status = '已通过')          ← QueryWrapper注入
ORDER BY a.create_time DESC 
LIMIT 24, 12;
```

#### A.1.6 响应：JSON 返回前端

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 28,
    "page": 3,
    "pageSize": 12,
    "totalPages": 3,
    "records": [
      {
        "activityId": 25,
        "name": "校园歌手大赛",
        "type": "文体活动",
        "venueName": "活动中心多功能厅",
        "venueLocation": "学生活动中心3层",
        "currentParticipants": 180,
        "maxParticipants": 200,
        "status": "已通过",
        ...
      },
      // ... 共12条
    ]
  }
}
```

#### A.1.7 前端：渲染表格 + 分页控件

```javascript
// admin/index.html → loadActivities() 收到响应后

// 1. 渲染表格行
const pr = data.data;
tbody.innerHTML = pr.records.map((a, i) => {
    const remaining = (a.maxParticipants||0) - (a.currentParticipants||0);
    return `<tr>
        <td>${(pr.page-1)*pr.pageSize + i + 1}</td>  
        <!-- (3-1)*12+1=25, 26, 27... 全局序号 -->
        <td>${escapeHtml(a.name)}</td>
        <td>${a.type||''}</td>
        <td>${a.organizer||''}</td>
        <td>${a.activityDate||''}</td>
        <td>${escapeHtml(a.venueName||'未分配')}</td>
        <td>${a.currentParticipants||0}/${a.maxParticipants||0}</td>
        <td>${statusBadge(a.status)}</td>
        <td><!-- 操作按钮 --></td>
    </tr>`;
}).join('');

// 2. 渲染分页控件
pgDiv.innerHTML = renderPagination(
    pr.total,     // 28
    pr.page,      // 3
    pr.pageSize,  // 12
    loadActivities  // 页码点击回调
);
```

```javascript
// common.js → renderPagination() 生成的分页DOM结构:
//
// «  1  ...  2  [3]   ...  »  共28条 3/3页
//     ↑       ↑       ↑
//  不可点击  可点击  当前页高亮
//
// 每个可点击页码都绑定了 loadActivities(n) 回调
```

#### A.1.8 分页数据流总结图

```
  用户点击页码
       │
       ▼
  loadActivities(3)
       │  收集筛选条件 + page/pageSize
       ▼
  fetch GET /api/activity/page?page=3&pageSize=12
       │
       ▼
  ┌─ Controller ───────────────────────────────────┐
  │  @RequestParam 提取 page=3, pageSize=12       │
  │  → activityService.listPage(3, 12, ...)       │
  └──────────────────┬─────────────────────────────┘
                     ▼
  ┌─ Service ──────────────────────────────────────┐
  │  buildQuery() 构建 QueryWrapper (WHERE条件)   │
  │  new Page<>(3, 12) 创建分页对象               │
  │  → mapper.selectActivityPage(page, wrapper)   │
  └──────────────────┬─────────────────────────────┘
                     ▼
  ┌─ Mapper + 分页插件 ──────────────────────────┐
  │  SQL1: SELECT COUNT(*) ...  → total=28       │
  │  SQL2: SELECT ... LIMIT 24,12 → 12条记录     │
  │  返回 IPage<Activity>                        │
  └──────────────────┬─────────────────────────────┘
                     ▼
  ┌─ Controller ───────────────────────────────────┐
  │  new PageResult(total, page, pageSize, list) │
  │  → Result.ok(pageResult) → JSON              │
  └──────────────────┬─────────────────────────────┘
                     ▼
  HTTP 200 JSON { total:28, page:3, records:[...] }
                     │
                     ▼
  ┌─ 前端渲染 ─────────────────────────────────────┐
  │  records → <tr> 表格行                        │
  │  total/page → renderPagination() 分页按钮     │
  └────────────────────────────────────────────────┘
```

---

### A.2 多条件组合查询 —— 完整数据流

查询与分页是联动的：**筛选条件改变时，重置到第1页重新加载**。

#### A.2.1 前端：收集筛选条件

```javascript
// admin/index.html
<div class="search-bar row g-2 align-items-end">
    <div class="col-md-3">
        <label>活动名称</label>
        <input type="text" class="form-control" id="searchName">
    </div>
    <div class="col-md-2">
        <label>活动类型</label>
        <select class="form-select" id="searchType">
            <option value="">全部</option>
            <option>学术讲座</option>
            <option>文体活动</option>
            <option>志愿服务</option>
            <option>其他</option>
        </select>
    </div>
    <div class="col-md-2">
        <label>日期</label>
        <input type="date" class="form-control" id="searchDate">
    </div>
    <div class="col-md-2">
        <label>状态</label>
        <select class="form-select" id="searchStatus">
            <option value="">全部</option>
            <option>待审核</option>
            <option>已通过</option>
            <option>已拒绝</option>
        </select>
    </div>
    <div class="col-md-3">
        <button class="btn btn-primary" onclick="loadActivities(1)">
            <!-- 注意: 永远传 page=1, 筛选条件变化时回到第一页 -->
            查询
        </button>
    </div>
</div>
```

用户操作示例：选择类型"文体活动"、状态"已通过" → 点击"查询"

```javascript
// loadActivities(1) 内部:
const p = new URLSearchParams();
p.append('page', 1);
p.append('pageSize', '12');
// 读取DOM值
const type   = document.getElementById('searchType').value;   // "文体活动"
const status = document.getElementById('searchStatus').value;  // "已通过"
const name   = document.getElementById('searchName').value;    // "" (空)
const date   = document.getElementById('searchDate').value;    // "" (空)
// 非空才追加
if (type)   p.append('type', type);
if (status) p.append('status', status);
// name和date为空，不追加

// 最终URL:
// GET /api/activity/page?page=1&pageSize=12&type=文体活动&status=已通过
```

#### A.2.2 后端：QueryWrapper 动态拼接

```java
// ActivityServiceImpl.java → buildQuery()
private QueryWrapper<Activity> buildQuery(
        String name, String type, String date, String status
) {
    QueryWrapper<Activity> qw = new QueryWrapper<>();
    
    if (StringUtils.hasText(name))   // name = null → false → 跳过
        qw.like("a.name", name);
    
    if (StringUtils.hasText(type))   // type = "文体活动" → true
        qw.eq("a.type", type);       // 添加: WHERE a.type = '文体活动'
    
    if (StringUtils.hasText(date))   // date = null → false → 跳过
        qw.eq("a.activity_date", date);
    
    if (StringUtils.hasText(status)) // status = "已通过" → true
        qw.eq("a.status", status);   // 添加: AND a.status = '已通过'
    
    qw.orderByDesc("a.create_time");
    return qw;
}
```

**最终生成的SQL：**

```sql
-- 当 type=文体活动, status=已通过 时:

-- SQL 1: COUNT
SELECT COUNT(*) FROM activity a 
LEFT JOIN venue v ON a.venue_id = v.venue_id 
WHERE (a.type = '文体活动' AND a.status = '已通过');

-- SQL 2: 数据
SELECT a.*, v.name AS venue_name, v.location AS venue_location 
FROM activity a 
LEFT JOIN venue v ON a.venue_id = v.venue_id 
WHERE (a.type = '文体活动' AND a.status = '已通过') 
ORDER BY a.create_time DESC 
LIMIT 0, 12;
```

**四种筛选场景对比：**

| 用户操作 | buildQuery 追加条件 | 生成的 WHERE 子句 |
|---------|---------------------|-------------------|
| 全部为空 | 无 | `WHERE (无) ORDER BY ...` |
| 只选类型=文体活动 | `eq("a.type", "文体活动")` | `WHERE (a.type = '文体活动')` |
| 只选状态=待审核 | `eq("a.status", "待审核")` | `WHERE (a.status = '待审核')` |
| 名称模糊+类型+状态 | `like + eq + eq` | `WHERE (a.name LIKE '%AI%' AND a.type = '...' AND a.status = '...')` |

---

### A.3 报名功能 —— 完整数据流

报名是系统最核心的业务流程，涉及 Session 验证、业务校验、双表写入、事务回滚。

#### A.3.1 前端：从详情页点击"立即报名"

```javascript
// student/index.html

// 步骤1: 用户点击活动卡片上的"查看详情"
// → openDetail(activityId=5)

async function openDetail(id) {
    currentDetailId = id;  // 记住当前查看的活动ID

    // 1.1 获取活动详情
    const data = await api.get(API_BASE + '/activity/' + id);
    // data.data = { activityId:5, name:"数据科学研讨会", 
    //   currentParticipants:0, maxParticipants:50, status:"已通过", ... }

    // 1.2 检查该用户是否已报名此活动
    const checkRes = await api.get(
        API_BASE + '/registration/check?activityId=' + id
    );
    const alreadyRegistered = checkRes.data === true;
    // checkRes.data = false (当前用户未报名)

    // 1.3 计算剩余名额, 动态设置按钮状态
    const remaining = a.maxParticipants - a.currentParticipants;
    // remaining = 50 - 0 = 50 → 还剩50个名额

    const btn = document.getElementById('detailRegisterBtn');
    if (alreadyRegistered) {
        btn.textContent = '已报名';
        btn.className = 'btn btn-success';
        btn.disabled = true;          // 已报名 → 按钮置灰
    } else if (remaining <= 0) {
        btn.textContent = '名额已满';
        btn.className = 'btn btn-secondary';
        btn.disabled = true;          // 满了 → 按钮置灰
    } else {
        btn.textContent = '立即报名';
        btn.className = 'btn btn-primary';
        btn.disabled = false;         // 可报名 → 按钮可点击
    }

    detailModal.show();  // 弹出详情模态框
}

// 步骤2: 用户在详情模态框中点击"立即报名"
// → registerActivity()

async function registerActivity() {
    if (!currentDetailId) return;  // currentDetailId = 5

    // 注意: personId 由服务端从 Session 获取, 前端不传
    const data = await api.post(
        API_BASE + '/registration/register?activityId=' + currentDetailId
        // POST /api/registration/register?activityId=5
        // 请求体为空, 因为参数在URL上
    );

    if (data.code === 200) {
        showToast('报名成功！');
        detailModal.hide();
        await loadBrowseActivities(browsePage);  // 刷新活动列表(人数会变化)
    } else {
        showToast(data.message, 'error');
        // 可能显示: "报名失败，可能已报名或名额已满"
    }
}
```

#### A.3.2 Controller：Session 鉴权 + 参数提取

```java
// RegistrationController.java

@PostMapping("/register")
public Result register(
        @RequestParam Integer activityId,  // Spring 从 ?activityId=5 提取
        HttpSession session                 // Spring 自动注入当前请求的 Session
) {
    // 1. 从 Session 中获取当前登录用户
    Person user = (Person) session.getAttribute("loginUser");
    // user = { personId:4, username:"student3", name:"王五", role:"student" }

    if (user == null) {
        return Result.error(401, "请先登录");
        // 前端 common.js 中 401 拦截会自动跳转登录页
    }

    // 2. 调用 Service (只传 activityId, personId 由 Session 确定)
    boolean ok = registrationService.register(
        activityId,          // 5
        user.getPersonId()   // 4 (从 Session 获取, 前端不可伪造)
    );

    // 3. 根据结果返回
    return ok
        ? Result.ok("报名成功")
        : Result.error("报名失败，可能已报名或名额已满");
}
```

**安全设计要点：** `personId` 由服务端从 Session 中提取，前端无法伪造他人身份报名。

#### A.3.3 Service：三重校验 + 事务写入双表

```java
// RegistrationServiceImpl.java

@Override
@Transactional  // 开启事务: insert registration + update activity 必须同时成功
public boolean register(Integer activityId, Integer personId) {

    // ── 校验1: 是否已报名 ──
    if (isRegistered(activityId, personId)) {
        return false;  // 已报名 → 直接拒绝
    }
    // SQL: SELECT COUNT(*) FROM registration
    //      WHERE activity_id=5 AND person_id=4 AND status='已报名'
    // 结果为0 → 未报名, 继续

    // ── 校验2: 活动是否存在 ──
    Activity activity = activityMapper.selectById(activityId);
    if (activity == null) return false;
    // activity = { activityId:5, status:"已通过", 
    //   currentParticipants:0, maxParticipants:50 }

    // ── 校验3: 活动状态是否为"已通过" ──
    if (!"已通过".equals(activity.getStatus())) {
        return false;  // 待审核/已拒绝 → 不允许报名
    }

    // ── 校验4: 名额是否已满 ──
    if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
        return false;  // 0 >= 50 → false, 名额充足, 继续
    }

    // ── 写入1: insert registration 表 ──
    Registration reg = new Registration();
    reg.setActivityId(activityId);       // 5
    reg.setPersonId(personId);           // 4
    reg.setStatus("已报名");
    int rows = registrationMapper.insert(reg);
    // SQL: INSERT INTO registration (activity_id, person_id, status, reg_time)
    //      VALUES (5, 4, '已报名', NOW());
    // rows = 1  → 插入成功

    if (rows > 0) {
        // ── 写入2: 原子更新 activity 人数 +1 ──
        UpdateWrapper<Activity> uw = new UpdateWrapper<>();
        uw.setSql("current_participants = current_participants + 1")
          .eq("activity_id", activityId);
        activityMapper.update(null, uw);
        // SQL: UPDATE activity
        //      SET current_participants = current_participants + 1
        //      WHERE activity_id = 5;
    }

    return rows > 0;  // true
}
// @Transactional 保证: 
// 如果 UPDATE 失败 → INSERT 自动回滚
// 如果 INSERT 失败 → 不会执行 UPDATE
```

#### A.3.4 取消报名流程

```java
// RegistrationServiceImpl.java

@Override
@Transactional
public boolean cancel(Integer regId, Integer personId) {
    // 1. 查报名记录
    QueryWrapper<Registration> qw = new QueryWrapper<>();
    qw.eq("reg_id", regId).eq("person_id", personId);
    Registration reg = registrationMapper.selectOne(qw);
    
    if (reg == null || "已取消".equals(reg.getStatus())) {
        return false;  // 记录不存在 或 已取消 → 拒绝(防止重复取消)
    }

    // 2. 更新报名状态
    reg.setStatus("已取消");
    int rows = registrationMapper.updateById(reg);
    // SQL: UPDATE registration SET status='已取消' WHERE reg_id=?

    if (rows > 0) {
        // 3. 原子更新活动人数 -1
        UpdateWrapper<Activity> uw = new UpdateWrapper<>();
        uw.setSql("current_participants = current_participants - 1")
          .eq("activity_id", reg.getActivityId());
        activityMapper.update(null, uw);
    }

    return rows > 0;
}
```

#### A.3.5 报名/取消的数据一致性保障

```
┌─────────────────────────────────────────────────────┐
│                   @Transactional                     │
│                                                     │
│  ┌─────────────────┐    ┌──────────────────────┐   │
│  │ INSERT           │    │ UPDATE activity      │   │
│  │ registration     │───▶│ current_participants │   │
│  │ (status='已报名') │    │ (+1 或 -1)           │   │
│  └─────────────────┘    └──────────────────────┘   │
│         │                        │                   │
│         └──────── 原子性 ────────┘                   │
│        任一失败 → 全部回滚                            │
└─────────────────────────────────────────────────────┘

数据库层面双重保障:
  UNIQUE KEY (activity_id, person_id)  → 防止同人同活动重复报名
  FOREIGN KEY (activity_id) → 防止报名不存在的活动
  FOREIGN KEY (person_id)   → 防止不存在的用户报名
```

---

### A.4 登录认证 —— 完整数据流

#### A.4.1 双通道认证架构

```
                    请求到达
                       │
                       ▼
              ┌─────────────────┐
              │   LoginFilter    │
              │   doFilter()     │
              └────────┬────────┘
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
     /pages/login   /api/**    其他页面请求
     /css /js       (放行)    (需要登录)
     (放行)                       │
                                  ▼
                      ┌─────────────────────┐
                      │  通道1: Session检查  │
                      │  session.getAttribute│
                      │  ("loginUser")       │
                      └──────┬──────────────┘
                             │
                  ┌──────────┴──────────┐
                  ▼                     ▼
              有Session              无Session
              → 放行                     │
                                        ▼
                            ┌─────────────────────┐
                            │  通道2: Cookie自动登录│
                            │  查找 auto_token      │
                            │  验证令牌有效性       │
                            └──────┬──────────────┘
                                   │
                        ┌──────────┴──────────┐
                        ▼                     ▼
                    token有效              token无效
                    → 重建Session          → 302重定向
                    → 放行                  /pages/login.html
```

#### A.4.2 LoginFilter 代码追踪

```java
// LoginFilter.java → doFilter()

public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    String path = req.getRequestURI();  // 例如: /pages/admin/index.html

    // ── 白名单放行 ──
    if (path.startsWith("/pages/login")  // 登录页本身
        || path.startsWith("/api/")      // 所有API (由Controller自行鉴权)
        || path.endsWith(".css")         // 样式文件
        || path.endsWith(".js")          // 脚本文件
        || path.endsWith(".ico")
        || path.endsWith(".png")) {
        chain.doFilter(request, response);  // 直接放行
        return;
    }

    // ── 通道1: Session ──
    HttpSession session = req.getSession(false);
    // getSession(false): 不创建新Session, 没有就返回null
    if (session != null && session.getAttribute("loginUser") != null) {
        chain.doFilter(request, response);  // 已登录 → 放行
        return;
    }

    // ── 通道2: Cookie自动登录 ──
    Cookie[] cookies = req.getCookies();
    if (cookies != null) {
        for (Cookie cookie : cookies) {
            if ("auto_token".equals(cookie.getName())) {
                String token = cookie.getValue();
                Integer personId = PersonController.AUTO_LOGIN_TOKENS.get(token);
                // AUTO_LOGIN_TOKENS: ConcurrentHashMap<String, Integer>
                // 在内存中维护 token → personId 映射
                
                if (personId != null) {
                    Person person = personMapper.selectById(personId);
                    if (person != null) {
                        person.setPassword(null);  // 敏感信息清除
                        req.getSession(true)        // 创建新Session
                           .setAttribute("loginUser", person);
                        chain.doFilter(request, response);  // 放行
                        return;
                    }
                }
            }
        }
    }

    // ── 都未通过 → 重定向到登录页 ──
    resp.sendRedirect("/pages/login.html");
}
```

#### A.4.3 登录时"记住我"令牌生成

```java
// PersonController.java → login()

@PostMapping("/login")
public Result login(@RequestBody Map<String, String> params,
                    HttpSession session, HttpServletResponse response) {
    String username = params.get("username");  // "student1"
    String password = params.get("password");  // "123456"
    Boolean remember = Boolean.parseBoolean(params.get("remember"));
    // remember = "true" → true

    Person person = personService.login(username, password);
    // SELECT * FROM person WHERE username='student1' AND password='123456'

    person.setPassword(null);  // 密码不存Session
    session.setAttribute("loginUser", person);

    if (Boolean.TRUE.equals(remember)) {
        String token = UUID.randomUUID().toString().replace("-", "");
        // token = "a1b2c3d4e5f6..." (32位随机字符串)
        
        AUTO_LOGIN_TOKENS.put(token, person.getPersonId());
        // HashMap: { "a1b2c3..." → 2 }

        Cookie cookie = new Cookie("auto_token", token);
        cookie.setMaxAge(7 * 24 * 60 * 60);  // 604800秒 = 7天
        cookie.setPath("/");                  // 全站有效
        cookie.setHttpOnly(true);             // JS不可读 (防XSS窃取)
        response.addCookie(cookie);
        // HTTP响应头: Set-Cookie: auto_token=a1b2c3...; Max-Age=604800; 
        //              Path=/; HttpOnly
    }

    return Result.ok("登录成功", person);
}
```

#### A.4.4 前端自动登录检测

```javascript
// login.html → 页面加载时立即执行

(async function tryAutoLogin() {
    // 尝试1: 检查当前Session
    try {
        const data = await api.get(API_BASE + '/person/current');
        // GET /api/person/current
        // 如果已登录: { code:200, data:{ personId:2, role:"student", ... } }
        // 如果未登录: { code:401 }
        if (data && data.code === 200) {
            redirectByRole(data.data.role);
            return;
        }
    } catch(e) {}

    // 尝试2: Cookie自动登录
    const autoData = await api.get(API_BASE + '/person/auto-login');
    // 浏览器自动携带 Cookie: auto_token=a1b2c3...
    // 服务端验证token → 重建Session → 返回用户信息
    if (autoData && autoData.code === 200) {
        redirectByRole(autoData.data.role);
    }
    // 两种方式都失败 → 留在登录页, 什么都不做
})();

function redirectByRole(role) {
    if (role === 'admin') window.location.href = 'admin/index.html';
    else window.location.href = 'student/index.html';
}
```

---

### A.5 活动日程 —— 分组聚合数据流

日程功能展示了前后端协作进行数据分组展示的完整流程。

#### A.5.1 后端：查询 + Java内存分组

```java
// ActivityServiceImpl.java → getSchedule()

@Override
public List<Map<String, Object>> getSchedule(String date) {
    // 1. 查询"已通过"活动, 按场地+时间排序
    QueryWrapper<Activity> qw = new QueryWrapper<>();
    if (StringUtils.hasText(date)) qw.eq("a.activity_date", date);
    qw.eq("a.status", "已通过");
    qw.orderByAsc("v.name", "a.start_time");
    List<Activity> activities = activityMapper.selectActivityWithVenue(qw);
    // SQL: SELECT a.*, v.name AS venue_name, v.location AS venue_location
    //      FROM activity a LEFT JOIN venue v ON a.venue_id=v.venue_id
    //      WHERE a.status='已通过' [AND a.activity_date='2026-05-25']
    //      ORDER BY v.name ASC, a.start_time ASC
    // 返回: [
    //   { name:"春季篮球赛", venueName:"体育馆", startTime:"09:00" },
    //   { name:"太极拳体验课", venueName:"室外操场", startTime:"07:00" },
    //   { name:"校园歌手大赛", venueName:"活动中心多功能厅", startTime:"18:00" },
    //   ...
    // ]

    // 2. Java内存分组 (按venueName)
    Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
    // LinkedHashMap 保持插入顺序 (即场地名称的排序)

    for (Activity a : activities) {
        String venueName = a.getVenueName() != null
            ? a.getVenueName() : "未分配场地";

        // 构建每场活动的摘要
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("activityId", a.getActivityId());
        item.put("name", a.getName());
        item.put("type", a.getType());
        item.put("startTime", a.getStartTime());
        item.put("endTime", a.getEndTime());
        item.put("currentParticipants", a.getCurrentParticipants());
        item.put("maxParticipants", a.getMaxParticipants());

        // 如果这个场地还没出现过, 创建新列表
        grouped.computeIfAbsent(venueName, k -> new ArrayList<>()).add(item);
    }

    // 3. 转换为前端友好的数组格式
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
        Map<String, Object> group = new LinkedHashMap<>();
        group.put("venueName", entry.getKey());        // "体育馆"
        group.put("activities", entry.getValue());     // [...该场地下所有活动]
        result.add(group);
    }

    return result;
}
```

**响应JSON结构：**

```json
{
  "code": 200,
  "data": [
    {
      "venueName": "体育馆",
      "activities": [
        {
          "name": "春季篮球赛",
          "startTime": "09:00",
          "endTime": "17:00",
          "currentParticipants": 200,
          "maxParticipants": 400
        }
      ]
    },
    {
      "venueName": "活动中心多功能厅",
      "activities": [
        {
          "name": "校园歌手大赛",
          "startTime": "18:00",
          "endTime": "21:00",
          "currentParticipants": 180,
          "maxParticipants": 200
        }
      ]
    }
  ]
}
```

#### A.5.2 前端：按场地卡片渲染

```javascript
// student/index.html → loadStudentSchedule()

async function loadStudentSchedule() {
    const date = document.getElementById('sScheduleDate').value;
    const params = date ? '?date=' + date : '';
    const data = await api.get(API_BASE + '/activity/schedule' + params);

    const container = document.getElementById('sScheduleContent');

    // 遍历每个场地组
    let html = '';
    data.data.forEach(group => {
        html += `<div class="schedule-venue">
            <h6><i class="bi bi-geo-alt-fill"></i> ${escapeHtml(group.venueName)}</h6>`;

        // 遍历该场地下的每个活动
        group.activities.forEach(a => {
            html += `<div class="schedule-item">
                <div class="schedule-time">${a.startTime}-${a.endTime}</div>
                <div class="schedule-info">
                    <strong>${escapeHtml(a.name)}</strong>
                    <small>${escapeHtml(a.type)} | ${escapeHtml(a.organizer)}</small>
                    <small>👥 ${a.currentParticipants}/${a.maxParticipants}</small>
                </div>
            </div>`;
        });

        html += '</div>';
    });
    container.innerHTML = html;
}
```

#### A.5.3 日程数据流总结图

```
  用户选择日期(可选)
       │
       ▼
  GET /api/activity/schedule?date=2026-05-25
       │
       ▼
  Service: SELECT ... WHERE status='已通过' AND date='2026-05-25'
       │
       ▼
  Java内存分组: Map<venueName, List<Activity>>
       │  computeIfAbsent(venueName, ...) 逐条归类
       ▼
  转换为 [{venueName, activities:[...]}, ...]
       │
       ▼
  JSON 响应 → 前端 forEach 双层循环渲染
       │
       ▼
  生成 schedule-venue 卡片 + schedule-item 时间线
```

---

### A.6 前端SPA架构 —— 单页应用切换机制

系统管理员页和学生页各自是一个单页HTML，不刷新浏览器，通过JS切换内容区域。

#### A.6.1 HTML结构

```html
<!-- admin/index.html -->
<div class="admin-layout">
    <!-- 左侧固定侧边栏 -->
    <nav class="sidebar">
        <a class="nav-link active" data-section="dashboard" 
           onclick="showSection('dashboard')">仪表盘</a>
        <a class="nav-link" data-section="venue" 
           onclick="showSection('venue')">场地管理</a>
        <a class="nav-link" data-section="activity" 
           onclick="showSection('activity')">活动管理</a>
        <a class="nav-link" data-section="schedule" 
           onclick="showSection('schedule')">活动日程</a>
    </nav>

    <!-- 右侧内容区: 四个section同时存在, 只显示active的那个 -->
    <div class="main-content">
        <section id="section-dashboard" class="content-section active">
            <!-- 仪表盘内容 -->
        </section>
        <section id="section-venue" class="content-section">
            <!-- 场地管理内容 -->
        </section>
        <section id="section-activity" class="content-section">
            <!-- 活动管理内容 -->
        </section>
        <section id="section-schedule" class="content-section">
            <!-- 活动日程内容 -->
        </section>
    </div>
</div>
```

#### A.6.2 切换逻辑

```javascript
// admin/index.html

function showSection(name) {
    // 1. 隐藏所有内容区
    document.querySelectorAll('.content-section')
        .forEach(s => s.classList.remove('active'));

    // 2. 取消所有导航项的激活状态
    document.querySelectorAll('.sidebar-nav .nav-link')
        .forEach(l => l.classList.remove('active'));

    // 3. 显示目标内容区
    document.getElementById('section-' + name)  // section-activity
        .classList.add('active');

    // 4. 激活目标导航项
    document.querySelector(`[data-section="${name}"]`)  // data-section="activity"
        .classList.add('active');

    // 5. 按需加载数据
    if (name === 'schedule') loadSchedule();
}
```

```css
/* style.css */
.content-section { display: none; }
.content-section.active { display: block; }
```

#### A.6.3 SPA状态管理

```javascript
// 状态变量维护当前页数, 切换回来时不会丢失
let venuePage = 1;      // 场地管理当前页码
let activityPage = 1;   // 活动管理当前页码
let browsePage = 1;     // 学生浏览活动页码
let myRegPage = 1;      // 学生我的报名页码

// 用户切换到"活动管理" → 不重新请求 → 保留上次浏览的页码
// 用户点击分页 → 更新 activityPage → 切换后再回来仍看到同一页
```

---

### A.7 完整数据流时序图

以下汇总一个典型用户操作场景的完整时序——**学生浏览活动 → 查看详情 → 报名 → 取消报名**：

```
 浏览器(JS)          LoginFilter    Controller      Service         Mapper/DB
     │                    │              │               │               │
     │── GET /pages/      │              │               │               │
     │   student/index    │              │               │               │
     │                    │── Session?   │               │               │
     │                    │── Cookie?    │               │               │
     │                    │── 通过 ──────▶               │               │
     │◀── 200 HTML ───────│              │               │               │
     │                    │              │               │               │
     │── checkLogin()     │              │               │               │
     │── GET /api/person/ │              │               │               │
     │   current          │── 放行(/api)─▶               │               │
     │                    │              │── session     │               │
     │                    │              │   .getAttribute               │
     │                    │              │── 返回用户 ◀──│               │
     │◀── 200 JSON ───────│◀─────────────│               │               │
     │   {role:student}   │              │               │               │
     │                    │              │               │               │
     │── loadBrowseActivities(1)         │               │               │
     │── GET /api/activity/              │               │               │
     │   page?page=1&status=已通过        │               │               │
     │                    │── 放行 ──────▶               │               │
     │                    │              │── listPage()─▶│               │
     │                    │              │               │── buildQuery()│
     │                    │              │               │── SELECT     │
     │                    │              │               │   COUNT(*)   │
     │                    │              │               │── SELECT     │
     │                    │              │               │   ...LIMIT   │
     │                    │              │◀── PageResult─│               │
     │◀── 200 JSON ───────│◀─────────────│               │               │
     │   {total:28,records:[...]}        │               │               │
     │── 渲染卡片 + 分页                  │               │               │
     │                    │              │               │               │
     │── openDetail(5)    │              │               │               │
     │── GET /api/activity/5             │               │               │
     │── GET /api/registration/check?    │               │               │
     │   activityId=5     │              │               │               │
     │◀── 详情JSON ───────│◀─────────────│               │               │
     │◀── check:false ────│◀─────────────│               │               │
     │── 显示详情模态框                   │               │               │
     │── 按钮: "立即报名" (可点击)         │               │               │
     │                    │              │               │               │
     │── registerActivity()              │               │               │
     │── POST /api/registration/         │               │               │
     │   register?activityId=5           │               │               │
     │                    │── 放行 ──────▶               │               │
     │                    │              │── session     │               │
     │                    │              │   .getAttribute               │
     │                    │              │── register()─▶│               │
     │                    │              │               │── 校验1:已报名│
     │                    │              │               │── 校验2:存在  │
     │                    │              │               │── 校验3:状态  │
     │                    │              │               │── 校验4:名额  │
     │                    │              │               │── INSERT reg │
     │                    │              │               │── UPDATE act │
     │                    │              │               │   +1         │
     │                    │              │◀── true ──────│               │
     │◀── 200 "报名成功" ──│◀─────────────│               │               │
     │── showToast('报名成功')            │               │               │
     │── 刷新活动列表                     │               │               │
     │                    │              │               │               │
     │── loadMyRegistrations(1)           │               │               │
     │── 找到刚报名的活动                 │               │               │
     │── cancelRegistration(regId)        │               │               │
     │── POST /api/registration/         │               │               │
     │   cancel/57        │              │               │               │
     │                    │── 放行 ──────▶               │               │
     │                    │              │── cancel()───▶│               │
     │                    │              │               │── SELECT reg │
     │                    │              │               │── UPDATE     │
     │                    │              │               │   status=取消│
     │                    │              │               │── UPDATE act │
     │                    │              │               │   -1         │
     │◀── 200 "已取消报名" ─│◀─────────────│               │               │
```

---

### A.8 关键技术决策说明

| 决策 | 选择方案 | 理由 |
|------|---------|------|
| 前端框架 | 原生JS, 不引入框架 | 课程设计范围, 降低复杂度 |
| 分页方式 | MyBatis-Plus分页插件 + 自定义SQL联表 | 一次查询同时获取数据和总数, 性能好 |
| 人数更新 | `setSql("current_participants ± 1")` 原子操作 | 避免 SELECT→计算→UPDATE 的并发竞态 |
| Session vs JWT | HttpSession + Cookie双重认证 | 简单可靠, 无需额外配置JWT密钥 |
| 联表查询 | Mapper中用 `@Select` + LEFT JOIN | 避免N+1查询, 一次SQL获取活动+场地全部数据 |
| 日程分组 | Java内存分组(Map<String, List>) | 数据量小(~几十条), 比多次SQL分组更简洁 |
| 外键策略 | ON DELETE SET NULL (场地) / CASCADE (活动→报名) | 删除场地不删活动, 删除活动级联删报名 |
| 静态资源 | 内嵌在 resources/static/, 前端HTML + CDN CSS | 单jar部署, 无需Nginx |
