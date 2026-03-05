# 基于微服务的旅游攻略系统的设计与实现

## 🧑‍💻作者

**作者：** 刘俊涛

**联系邮箱：** 13503089560@163.com

---

## 📋项目简介

本项目是一个基于微服务架构的旅游攻略系统，旨在为用户提供全面的旅游信息查询、游记分享、互动交流等功能。系统采用前后端分离架构，后端使用Spring Boot + Spring Cloud微服务框架，前端使用Vue.js框架。

---

## 🏗️技术架构

### 后端技术栈
- **框架：** Spring Boot 3.x
- **微服务：** Spring Cloud Alibaba
- **服务注册与发现：** Nacos
- **服务调用：** OpenFeign
- **网关：** Express-Gateway
- **配置中心：** Nacos Config
- **数据库：** PostgreSQL 8.0
- **ORM框架：** MyBatis-Plus
- **缓存：** Redis
- **消息队列：** RabbitMQ
- **认证授权：** JWT

### 前端技术栈
- **框架：** Next.js
- **UI组件库：** ShadcnUI
- **HTTP客户端：** Axios

---

## 🗄️数据库设计

系统共包含12张数据表，按服务划分如下：

| 服务 | 数据表 |
|------|--------|
| 用户服务 | t_user, t_user_profile |
| 内容服务 | t_destination, t_attraction, t_play_item, t_ticket |
| 游记服务 | t_travelogue, t_comment |
| 互动服务 | t_collect, t_like |
| 公共服务 | t_banner, t_statistics |

详细数据库设计请参考：[database-design.md](../database-design.md)

---

## 📦项目结构

```
parent/
├── user-service/          # 用户服务
├── content-service/       # 内容服务
├── travelogue-service/    # 游记服务
├── interaction-service/   # 互动服务
├── common-service/        # 公共服务
├── gateway/               # 网关服务
└── common/                # 公共模块
```

---

## 🔐认证授权

系统采用JWT（JSON Web Token）进行用户认证与授权：

- 用户登录成功后，服务端签发JWT令牌
- 客户端在后续请求中携带JWT令牌
- 网关服务统一验证令牌有效性
- 各服务通过令牌获取用户身份信息

详细设计请参考：[jwt-payload-design.md](../jwt-payload-design.md)

---

## 🚀快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+

### 启动步骤
1. 克隆项目到本地
2. 导入数据库脚本
3. 配置Nacos注册中心
4. 启动各微服务
5. 启动前端项目

---

## 📄License

Copyright © 2026 刘俊涛. All rights reserved.


## 🚀服务划分

### 👤用户服务 (user-service)
**服务职责：** 负责用户身份认证、授权及个人资料管理

**功能模块：**
- ✅ 用户注册、登录
- ✅ JWT令牌签发与验证
- ✅ 个人资料管理
- ✅ 用户信息查询与更新

**数据库表：**
- `t_user` - 用户表
- `t_user_profile` - 用户个人资料表

---

### 📖内容服务 (content-service)
**服务职责：** 负责目的地、景点、游玩项目及票务信息管理

**功能模块：**
- ✅ 目的地管理（增删改查、推荐）
- ✅ 景点管理（增删改查、搜索）
- ✅ 游玩项目管理
- ✅ 票种信息管理
- ✅ 开放规则管理

**数据库表：**
- `t_destination` - 目的地表
- `t_attraction` - 景点表
- `t_play_item` - 游玩项目表
- `t_ticket` - 票种表

---

### 📕游记服务 (travelogue-service)
**服务职责：** 负责游记内容的发布、展示及评论管理

**功能模块：**
- ✅ 游记发布与编辑
- ✅ 游记列表查询与搜索
- ✅ 游记详情展示
- ✅ 评论管理（发表、回复、删除）

**数据库表：**
- `t_travelogue` - 游记表
- `t_comment` - 评论表

---

### ❤️互动服务 (interaction-service)
**服务职责：** 负责用户与内容的互动功能

**功能模块：**
- ✅ 收藏功能
- ✅ 点赞功能
- ✅ 互动记录查询

**数据库表：**
- `t_collect` - 收藏表
- `t_like` - 点赞表

---

### 🎨公共服务 (common-service)
**服务职责：** 负责系统公共功能及数据统计

**功能模块：**
- ✅ 轮播图管理
- ✅ 系统统计数据

**数据库表：**
- `t_banner` - 轮播图表
- `t_statistics` - 系统统计表