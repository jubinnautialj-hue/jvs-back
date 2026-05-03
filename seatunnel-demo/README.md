# SeaTunnel Demo

这是一个基于 Spring Boot + SeaTunnel + Vue 3 的数据集成平台演示项目。

## 项目结构

```
seatunnel-demo/
├── backend/                    # 后端 Spring Boot 项目
│   ├── pom.xml
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/seatunnel/demo/
│   │       │       ├── SeatunnelDemoApplication.java          # 启动类
│   │       │       ├── config/                                # 配置类
│   │       │       │   ├── CorsConfig.java
│   │       │       │   ├── SeaTunnelApiConfig.java
│   │       │       │   └── SeaTunnelServerConfig.java
│   │       │       ├── controller/                            # 控制器
│   │       │       │   ├── DemoController.java
│   │       │       │   └── SeaTunnelController.java
│   │       │       ├── dto/                                   # 数据传输对象
│   │       │       │   ├── enums/
│   │       │       │   │   └── JobStatusEnum.java
│   │       │       │   ├── Result.java
│   │       │       │   ├── SeaTunnelJobInfoDto.java
│   │       │       │   ├── SeaTunnelStopJobDto.java
│   │       │       │   ├── SeaTunnelSubmitJobDto.java
│   │       │       │   └── SeaTunnelSubmitJobResponseDto.java
│   │       │       └── service/                               # 服务层
│   │       │           ├── MockSeaTunnelApiService.java      # 模拟 SeaTunnel 服务
│   │       │           └── SeaTunnelApiService.java          # 服务接口
│   │       └── resources/
│   │           └── application.yml
│   └── ...
├── frontend/                   # 前端 Vue 3 项目
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── api/
│       │   └── index.js
│       ├── router/
│       │   └── index.js
│       └── views/
│           ├── Overview.vue          # 概览页面
│           ├── Jobs.vue              # 任务管理页面
│           ├── CreateJob.vue         # 创建任务页面
│           └── Connectors.vue        # 连接器页面
└── README.md
```

## 功能特性

### 后端功能

1. **SeaTunnel 任务管理**
   - 提交任务
   - 查询任务状态
   - 停止任务
   - 列出所有任务

2. **演示数据接口**
   - 获取系统概览统计
   - 获取支持的连接器列表
   - 获取任务模板列表
   - 快速启动示例任务

### 前端功能

1. **概览页面**
   - 系统统计卡片（总任务数、运行中、已完成）
   - SeaTunnel 简介和核心特性
   - 快速开始引导
   - 架构说明图解

2. **任务管理页面**
   - 任务列表展示
   - 任务搜索功能
   - 任务状态刷新（运行中任务自动刷新）
   - 任务详情查看
   - 停止任务操作

3. **创建任务页面**
   - 四步引导式创建任务
   - 预设模板选择
   - 自定义任务配置
   - 数据源配置（MySQL、PostgreSQL、Oracle）
   - 目标配置（Doris、ClickHouse、Hive）
   - 配置预览和提交

4. **连接器页面**
   - 支持的连接器展示
   - 按类型筛选（Source/Sink）
   - 连接器详情查看
   - 配置示例展示
   - 快速创建任务入口

## 技术栈

### 后端
- **框架**: Spring Boot 2.7.18
- **语言**: Java 8
- **工具库**: Hutool 5.8.26, Fastjson2 2.0.45
- **端口**: 7790

### 前端
- **框架**: Vue 3
- **构建工具**: Vite 5
- **UI 组件库**: Element Plus
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios
- **端口**: 3080

## 快速启动

### 前置要求
- JDK 8+
- Maven 3.6+
- Node.js 16+
- npm 或 yarn

### 启动后端

```bash
cd seatunnel-demo/backend

# 编译项目
mvn clean package -DskipTests

# 运行项目
mvn spring-boot:run
```

后端服务启动后，访问 http://localhost:7790

### 启动前端

```bash
cd seatunnel-demo/frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev
```

前端服务启动后，访问 http://localhost:3080

## API 接口

### SeaTunnel 任务管理

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/seatunnel/job/submit` | POST | 提交任务 |
| `/api/seatunnel/job/{jobId}` | GET | 获取任务详情 |
| `/api/seatunnel/job/stop` | POST | 停止任务 |
| `/api/seatunnel/jobs` | GET | 获取任务列表 |

### 演示接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/demo/overview` | GET | 获取系统概览 |
| `/api/demo/connectors` | GET | 获取连接器列表 |
| `/api/demo/templates` | GET | 获取任务模板 |
| `/api/demo/quick-start/{templateName}` | POST | 快速启动任务 |

## SeaTunnel 配置说明

### 任务配置结构

SeaTunnel 任务配置包含以下核心部分：

```json
{
  "env": {
    "job.mode": "batch",
    "parallelism": 5
  },
  "source": [
    {
      "plugin_name": "MySQL",
      "url": "jdbc:mysql://localhost:3306/test",
      "table": "users"
    }
  ],
  "transform": [
    {
      "plugin_name": "FieldMapper",
      "field_mapper": {
        "original_field": "target_field"
      }
    }
  ],
  "sink": [
    {
      "plugin_name": "Doris",
      "table": "users_ods"
    }
  ]
}
```

### 运行模式

- **batch**: 批量模式，适用于一次性数据同步
- **STREAMING**: 流模式，适用于 CDC 实时同步

## 与原项目对比

本 demo 基于原项目 `jvs-back` 中 SeaTunnel 的封装实现，主要参考了以下核心类：

| 原项目类 | Demo 对应实现 | 说明 |
|----------|---------------|------|
| `SeaTunnelApiConfig` | `SeaTunnelApiConfig` | API 端点配置 |
| `CommonConfig` | `SeaTunnelServerConfig` | 服务地址配置 |
| `SeaTunnelApiService` | `SeaTunnelApiService` | API 服务接口 |
| `SeaTunnelApiServiceImpl` | `MockSeaTunnelApiService` | API 服务实现（模拟版） |
| `SeaTunnelSubmitJobDto` | `SeaTunnelSubmitJobDto` | 任务提交 DTO |
| `SeaTunnelJobInfoReturnDto` | `SeaTunnelJobInfoDto` | 任务信息 DTO |
| `JobInfoStatusEnums` | `JobStatusEnum` | 任务状态枚举 |

## 注意事项

1. **模拟服务**: 本 demo 使用模拟的 SeaTunnel 服务，不依赖真实的 SeaTunnel 集群。在实际生产环境中，需要配置真实的 SeaTunnel 服务地址。

2. **数据持久化**: 任务数据存储在内存中，服务重启后数据会丢失。如需持久化，请集成数据库。

3. **实际 SeaTunnel 集成**: 如需连接真实的 SeaTunnel 服务，请修改 `application.yml` 中的配置：
   ```yaml
   seatunnel:
     server:
       url: http://your-seatunnel-server:8080
       enabled: true
   ```

## 扩展建议

1. **集成真实 SeaTunnel**: 实现真实的 `SeaTunnelApiService`，通过 HTTP 调用真实的 SeaTunnel REST API。

2. **添加数据库**: 集成 MySQL/PostgreSQL 等数据库，持久化任务配置和状态。

3. **添加认证**: 集成 Spring Security，实现用户认证和授权。

4. **增强监控**: 添加任务执行日志、Metrics 指标采集等功能。

5. **更多连接器**: 支持更多 SeaTunnel 连接器类型，如 Kafka、Redis、MongoDB 等。

## 许可证

MIT License
