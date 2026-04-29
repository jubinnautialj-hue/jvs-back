# JVS 项目架构设计与核心技术原理分析

## 一、项目概述

### 1.1 项目简介
JVS 是一个基于 Spring Cloud Alibaba 实现的企业级微服务快速开发平台，采用多租户架构设计，支持灰度发布、统一认证授权等企业级特性。

### 1.2 技术栈版本
| 技术组件 | 版本号 |
|---------|--------|
| Java | 1.8 |
| Spring Boot | 2.7.12 |
| Spring Cloud | 2021.0.8 |
| Spring Cloud Alibaba | 2021.0.5.0 |
| MyBatis Plus | 3.5.3.1 |
| Hutool | 5.8.24 |

### 1.3 项目模块结构
```
jvs-back/
├── jvs-base/                    # 基础服务层
│   ├── jvs-gateway/             # 网关服务（使用 WebFlux）
│   ├── jvs-auth/                # 认证服务
│   ├── jvs-auth-mgr/            # 认证管理服务
│   ├── jvs-core-parent/         # 核心 starter 父模块
│   │   ├── jvs-starter-web/     # Web 层 starter
│   │   ├── jvs-starter-common/  # 公共基础 starter
│   │   ├── jvs-starter-database/# 数据库 starter
│   │   ├── jvs-starter-redis/   # Redis starter
│   │   ├── jvs-starter-gray/    # 灰度发布 starter
│   │   ├── jvs-starter-oauth2/  # OAuth2 认证 starter
│   │   ├── jvs-starter-log/     # 操作日志 starter
│   │   └── ...                   # 其他 starter
│   └── jvs-message-push/        # 消息推送
├── jvs-design/                   # 低代码设计模块
├── jvs-bi/                       # 商业智能模块
└── jvs-apply-document/           # 文档应用模块
```

---

## 二、WebFlux 技术选型分析

### 2.1 WebFlux 使用场景

#### 网关层使用 WebFlux
项目在 **网关层（jvs-gateway）** 使用了 WebFlux，而业务服务层使用传统的 Spring MVC。

**关键代码位置**：
- [jvs-gateway/pom.xml](jvs-base/jvs-gateway/pom.xml:39-52) 引入 `spring-cloud-starter-gateway`（内置 WebFlux）
- [GatewayErrorConfig.java](jvs-base/jvs-gateway/src/main/java/cn/bctools/gateway/config/GatewayErrorConfig.java:45) 实现 `ErrorWebExceptionHandler`
- [AuthorizationManager.java](jvs-base/jvs-gateway/src/main/java/cn/bctools/gateway/config/AuthorizationManager.java:35) 实现 `ReactiveAuthorizationManager`

#### 业务层使用 Spring MVC + Undertow
**关键代码位置**：
- [jvs-starter-web/pom.xml](jvs-base/jvs-core-parent/jvs-starter-web/pom.xml:36-53) 使用 `spring-boot-starter-web` 并排除 Tomcat，引入 Undertow

### 2.2 为什么这样设计

#### 网关层选择 WebFlux 的原因

| 场景需求 | WebFlux 优势 |
|---------|-------------|
| **高并发入口** | 网关作为系统唯一入口，需要处理大量并发请求，WebFlux 的 Reactor 模型能更好地利用 CPU |
| **IO 密集型操作** | 网关需要频繁调用认证服务、Redis、配置中心等，非阻塞 IO 能显著提升吞吐量 |
| **Spring Gateway 集成** | Spring Cloud Gateway 本身就是基于 WebFlux 构建的，天然集成 |

#### 业务层选择 Spring MVC 的原因

| 场景需求 | Spring MVC 优势 |
|---------|----------------|
| **业务逻辑复杂度** | 传统同步编程模型更易理解和调试，适合复杂业务逻辑 |
| **开发团队熟悉度** | 多数 Java 开发者更熟悉 Spring MVC 的编程模型 |
| **事务管理** | Spring MVC 配合 `@Transactional` 的事务管理更直观 |
| **生态成熟度** | Spring MVC 生态更加成熟，问题定位和解决方案更丰富 |

### 2.3 设计启示

**分层技术选型策略**：
- **入口层（网关）**：选择高并发、非阻塞方案（WebFlux）
- **业务层**：选择易开发、易维护方案（Spring MVC）

这种"**网关用 Reactive，业务用 Imperative**"的分层策略是微服务架构的最佳实践之一，值得在其他项目中参考。

---

## 三、核心技术组件封装分析

### 3.1 公共基础组件 - jvs-starter-common

#### 统一响应对象 R<T>
**文件位置**：[R.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/utils/R.java)

```java
@Builder
@Accessors(chain = true)
public class R<T> implements Serializable {
    private int code = 0;           // 0 表示成功
    private String msg = "success"; // 消息
    private T data;                 // 业务数据
    private LocalDateTime timestamp; // 时间戳
    
    // 静态工厂方法
    public static <T> R<T> ok(T data) { ... }
    public static <T> R<T> failed(String msg) { ... }
}
```

**设计意图**：
1. **统一 API 响应格式**：所有服务返回统一结构，便于前端统一处理
2. **建造者模式**：使用 `@Builder` 链式调用，代码更优雅
3. **时间戳追踪**：返回时间戳便于问题定位

#### 租户上下文持有者
**文件位置**：[TenantContextHolder.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/utils/TenantContextHolder.java)

```java
@UtilityClass
public class TenantContextHolder {
    public void setTenantId(String tenantId) {
        SystemThreadLocal.set(SysConstant.TENANTID, tenantId);
    }
    
    public String getTenantId() {
        return SystemThreadLocal.get(SysConstant.TENANTID);
    }
}
```

**设计亮点**：
- 使用 `TransmittableThreadLocal`（见 `SystemThreadLocal`）确保线程池场景下上下文传递
- `@UtilityClass` 标记为工具类，防止实例化

#### 业务异常类
**文件位置**：[BusinessException.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/exception/BusinessException.java)

```java
@Data
@Accessors(chain = true)
public class BusinessException extends RuntimeException {
    private int code = -1;
    private String message;
}
```

**设计意图**：
- 区分业务异常与系统异常
- 支持自定义错误码，便于国际化和前端错误处理

#### Spring 上下文工具
**文件位置**：[SpringContextUtil.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/utils/SpringContextUtil.java)

**核心功能**：
| 功能 | 用途 |
|-----|------|
| `getBean(Class<T>)` | 从 Spring 容器获取 Bean |
| `getApplicationContextName()` | 获取当前服务名 |
| `getEnv()` | 获取环境信息 |
| `getVersion()` | 获取版本号 |
| `msg()` | 国际化消息获取 |

---

### 3.2 Web 层组件 - jvs-starter-web

#### 统一异常处理器
**文件位置**：[UnifyExceptionHandler.java](jvs-base/jvs-core-parent/jvs-starter-web/src/main/java/cn/bctools/web/config/UnifyExceptionHandler.java)

```java
@ControllerAdvice
@Configuration
public class UnifyExceptionHandler {
    
    @ExceptionHandler(value = Exception.class)
    public R exceptionHandler(HttpServletRequest request, Exception e) {
        // 处理系统异常
        log.error("系统严重错误", e);
        return R.failed(msg);
    }
    
    @ExceptionHandler(value = RuntimeException.class)
    public R runtimeException(HttpServletRequest request, RuntimeException e) {
        // 处理业务异常
        if (e instanceof BusinessException) {
            return R.failed(e.getMessage()).setCode(ex.getCode());
        }
        return exceptionHandler(request, e);
    }
}
```

**设计要点**：
1. **双层异常保护**：网关层 + 业务层都有异常处理
2. **异常分级处理**：
   - `Exception`：最严重的系统异常，记录完整堆栈
   - `RuntimeException`：运行时异常，区分业务异常
3. **国际化支持**：错误消息支持多语言

#### Web 容器选型：Undertow 替代 Tomcat
**关键配置**：
```xml
<!-- jvs-starter-web/pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <artifactId>spring-boot-starter-tomcat</artifactId>
            <groupId>org.springframework.boot</groupId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-undertow</artifactId>
</dependency>
```

**Undertow 优势**：
- **轻量级**：比 Tomcat 更小、更快启动
- **高性能**：在高并发场景下性能更优
- **灵活性**：提供更底层的 API，便于定制

---

### 3.3 数据库组件 - jvs-starter-database

#### 多租户处理器
**文件位置**：[JvsTenantHandler.java](jvs-base/jvs-core-parent/jvs-starter-database/src/main/java/cn/bctools/database/interceptor/tenant/JvsTenantHandler.java)

```java
public class JvsTenantHandler implements TenantLineHandler {
    
    private static final String TENANT_ID = "tenant_id";
    
    @Override
    public Expression getTenantId() {
        String tenantId = TenantContextHolder.getTenantId();
        return new StringValue(tenantId);
    }
    
    @Override
    public boolean ignoreTable(String tableName) {
        // 1. 检查当前线程是否有租户ID
        // 2. 检查表是否有 tenant_id 字段
        // 3. 没有则跳过租户拦截
        return false;
    }
}
```

**设计亮点**：
1. **自动 SQL 注入**：MyBatis Plus 拦截器自动在 SQL 中添加 `tenant_id = ?` 条件
2. **智能跳过**：
   - 无租户ID时跳过（如定时任务、系统初始化）
   - 表无 `tenant_id` 字段时跳过
3. **透明化设计**：业务代码无需关心租户逻辑

#### 基础实体类
**文件位置**：[BasePo.java](jvs-base/jvs-core-parent/jvs-starter-database/src/main/java/cn/bctools/database/entity/po/BasePo.java)

```java
public class BasePo extends BasalPo {
    @TableField(fill = FieldFill.INSERT)
    private String deptId;  // 部门ID
    
    @TableField(fill = FieldFill.INSERT)
    private String jobId;   // 岗位ID
}
```

**设计意图**：
- **自动填充**：使用 `FieldFill.INSERT` 在插入时自动填充
- **统一字段**：所有业务表继承此类，保证数据结构一致性

---

### 3.4 Redis 组件 - jvs-starter-redis

#### 缓存键生成器
**文件位置**：[JvsCacheKeyGenerator.java](jvs-base/jvs-core-parent/jvs-starter-redis/src/main/java/cn/bctools/redis/config/JvsCacheKeyGenerator.java)

```java
public class JvsCacheKeyGenerator implements KeyGenerator {
    
    @Override
    public Object generate(Object target, Method method, Object... params) {
        // 根据方法名、参数生成缓存 key
        // 支持 @CacheTenantSkip 注解跳过多租户
    }
}
```

**设计亮点**：
- 支持多租户缓存隔离
- 可通过 `@CacheTenantSkip` 注解跳过特定方法的租户隔离

---

### 3.5 灰度发布组件 - jvs-starter-gray

#### 版本负载均衡器
**文件位置**：[VersionLoadBalancer.java](jvs-base/jvs-core-parent/jvs-starter-gray/src/main/java/cn/bctools/gray/rule/VersionLoadBalancer.java)

**核心路由规则**：
1. **版本优先**：根据请求头 `version` 匹配相同版本的服务
2. **权重路由**：同版本内按权重随机选择
3. **降级策略**：未找到匹配版本时，尝试网关同版本或默认版本

**关键代码逻辑**：
```java
Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, Request request) {
    // 1. 获取请求头中的版本号
    String reqVersion = headers.getFirst(SysConstant.VERSION);
    
    // 2. 筛选相同版本的实例
    List<ServiceInstance> matchedInstances = instances.stream()
        .filter(instance -> {
            String targetVersion = instance.getMetadata().get(SysConstant.VERSION);
            return targetVersion.contains(reqVersion);
        }).collect(Collectors.toList());
    
    // 3. 按权重选择
    if (CollUtil.isNotEmpty(matchedInstances)) {
        return NacosBalancer.getHostByRandomWeight3(matchedInstances);
    }
    
    // 4. 降级逻辑...
}
```

**设计价值**：
| 功能 | 业务价值 |
|-----|---------|
| 金丝雀发布 | 小流量验证新版本，风险可控 |
| 多版本共存 | 支持 A/B 测试、多租户定制化 |
| 无缝回滚 | 出现问题可快速切回旧版本 |

---

### 3.6 操作日志组件 - jvs-starter-log

#### AOP 日志切面
**文件位置**：[SysLogAspect.java](jvs-base/jvs-core-parent/jvs-starter-log/src/main/java/cn/bctools/log/aspect/SysLogAspect.java)

**核心设计**：
```java
@Aspect
@Order(1)
public class SysLogAspect {
    
    @Around("@annotation(logannotation)")
    public Object around(ProceedingJoinPoint point, Log logannotation) {
        // 1. 记录开始时间
        // 2. 执行目标方法
        // 3. 计算执行时间
        // 4. 异步发送日志到 MQ
    }
}
```

**设计亮点**：
1. **注解驱动**：`@Log` 注解标记需要记录的方法
2. **与 Swagger 集成**：自动读取 `@ApiOperation` 注解值作为功能名
3. **性能监控**：执行时间超过 1 秒自动警告
4. **异步处理**：使用线程池 + RabbitMQ 异步发送，不影响主流程

**日志记录内容**：
- 用户信息（ID、姓名、客户端）
- 请求信息（URL、参数、IP）
- 执行信息（耗时、成功/失败、异常）
- 环境信息（服务名、版本、租户ID）

---

### 3.7 网关组件 - jvs-gateway

#### 全局认证过滤器
**文件位置**：[AuthGlobalFilter.java](jvs-base/jvs-gateway/src/main/java/cn/bctools/gateway/filter/AuthGlobalFilter.java)

**核心职责**：
1. **域名解析租户**：从请求域名解析租户ID，写入请求头
2. **版本路由**：处理请求头 `version`，用于灰度发布
3. **路径重写**：将 `/mgr/xxx` 重写为 `/xxx`
4. **密码解密**：登录请求的密码解密处理

**关键代码**：
```java
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    // 1. 从域名获取租户ID（带缓存）
    String tenantId = cache.get(host, () -> configService.fromHost(host));
    
    // 2. 写入请求头，供下游服务使用
    ServerHttpRequest request = exchange.getRequest().mutate()
        .header(SysConstant.TENANTID, tenantId)
        .header(SysConstant.VERSION, version)
        .build();
    
    // 3. 路径重写逻辑...
}
```

#### 网关异常处理
**文件位置**：[GatewayErrorConfig.java](jvs-base/jvs-gateway/src/main/java/cn/bctools/gateway/config/GatewayErrorConfig.java)

**设计亮点**：
1. **统一错误码映射**：从数据库查询错误码对应的国际化消息
2. **钉钉告警**：系统错误自动发送钉钉通知
3. **错误去重**：使用 `TimedCache` 防止相同错误频繁告警

---

## 四、设计模式应用分析

### 4.1 建造者模式
**应用场景**：[R.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/utils/R.java)

```java
@Builder
@Accessors(chain = true)
public class R<T> { ... }

// 使用方式
R.ok(data).setMsg("操作成功");
R.failed("用户不存在").setCode(1001);
```

**设计价值**：
- 链式调用，代码更优雅
- 静态工厂方法提供语义化的 API

### 4.2 模板方法模式
**应用场景**：统一异常处理、过滤器链

```java
// UnifyExceptionHandler - 模板化异常处理
@ExceptionHandler(value = Exception.class)
public R exceptionHandler(...) {
    // 1. 记录日志
    // 2. 处理特殊异常
    // 3. 返回统一响应
}
```

### 4.3 策略模式
**应用场景**：[VersionLoadBalancer.java](jvs-base/jvs-core-parent/jvs-starter-gray/src/main/java/cn/bctools/gray/rule/VersionLoadBalancer.java)

**路由策略优先级**：
1. 版本匹配优先
2. 权重路由次之
3. 轮询兜底

### 4.4 责任链模式
**应用场景**：网关过滤器链

```java
// 网关过滤器执行顺序
AuthGlobalFilter (-1)          // 认证、租户解析
→ GrayReactiveLoadBalancerClientFilter // 灰度路由
→ XssFilter                     // XSS 防护
→ ...
```

### 4.5 观察者模式（AOP）
**应用场景**：[SysLogAspect.java](jvs-base/jvs-core-parent/jvs-starter-log/src/main/java/cn/bctools/log/aspect/SysLogAspect.java)

- `@Log` 注解标记切点
- AOP 切面在方法执行前后织入日志逻辑
- 业务代码与日志逻辑解耦

---

## 五、架构设计意图总结

### 5.1 多租户架构设计

#### 实现层次
| 层次 | 组件 | 职责 |
|-----|------|------|
| 网关层 | AuthGlobalFilter | 域名解析租户ID |
| 上下文层 | TenantContextHolder | 线程级租户存储 |
| 数据库层 | JvsTenantHandler | SQL 自动注入条件 |

#### 数据流
```
请求 → 网关解析域名 → 写入请求头 → 下游服务读取 → SQL自动过滤
```

**设计价值**：
- **数据隔离**：同一套代码服务多个租户，数据完全隔离
- **透明化**：业务代码无需感知租户逻辑
- **可扩展**：支持按租户定制化（结合灰度发布）

### 5.2 灰度发布设计

#### 实现层次
| 层次 | 组件 | 职责 |
|-----|------|------|
| 网关层 | GrayReactiveLoadBalancerClientFilter | 网关路由选择 |
| 负载均衡层 | VersionLoadBalancer | 服务实例选择 |
| 服务注册层 | Nacos | 版本元数据存储 |

#### 路由流程
```
请求头 version
    ↓
VersionLoadBalancer
    ↓
筛选相同版本实例 → 按权重选择
    ↓
无匹配 → 降级到网关同版本 → 降级到默认版本
```

### 5.3 统一响应与异常处理

#### 双层防护机制
```
┌─────────────────────────────────────────────────────┐
│                    网关层                              │
│  GatewayErrorConfig (ErrorWebExceptionHandler)      │
│  - 统一错误码映射                                      │
│  - 钉钉告警通知                                        │
└───────────────────────┬─────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│                   业务服务层                           │
│  UnifyExceptionHandler (@ControllerAdvice)          │
│  - 参数校验异常处理                                    │
│  - 业务异常处理                                        │
│  - 空指针等系统异常                                    │
└─────────────────────────────────────────────────────┘
```

### 5.4 服务治理体系

#### 已实现的治理能力
| 能力 | 组件 | 说明 |
|-----|------|------|
| 服务注册发现 | Nacos | 服务注册与发现 |
| 配置中心 | Nacos | 动态配置管理 |
| 负载均衡 | VersionLoadBalancer | 版本路由 + 权重 |
| 服务熔断 | Sentinel（可选） | 限流降级 |
| 链路追踪 | SkyWalking（可选） | 分布式追踪 |
| 日志收集 | ELK（可选） | 日志聚合 |
| 监控告警 | Prometheus + 钉钉 | 监控与告警 |

---

## 六、可复用的设计模式与最佳实践

### 6.1 Starter 封装模式

#### 模块划分原则
```
jvs-starter-xxx/
├── src/main/java/cn/bctools/xxx/
│   ├── annotation/    # 自定义注解
│   ├── config/        # 自动配置类
│   ├── utils/         # 工具类
│   └── ...
└── src/main/resources/META-INF/
    └── spring.factories  # 自动配置声明
```

#### 关键代码示例
**spring.factories**：
```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  cn.bctools.web.config.JacksonConfiguration,\
  cn.bctools.web.config.UnifyExceptionHandler
```

**条件注解使用**：
```java
@ConditionalOnMissingBean(UnifyExceptionHandler.class)
public class UnifyExceptionHandler { ... }
```

**设计价值**：
- 自动配置，开箱即用
- 允许业务覆盖默认实现
- 版本统一管理

### 6.2 上下文传递模式

#### 问题场景
- 线程池执行时，ThreadLocal 数据丢失
- 微服务间调用时，上下文需要传递

#### 解决方案
1. **TransmittableThreadLocal**：解决线程池上下文传递
2. **Feign 拦截器**：服务间调用时传递请求头

### 6.3 异步解耦模式

#### 操作日志异步化
```java
// 使用线程池 + MQ 异步处理
executor.execute(() -> 
    rabbitTemplate.convertAndSend(exchange, routing, logPo)
);
```

**设计原则**：
- 核心业务流程内不做耗时操作
- 通过消息队列解耦非核心逻辑

---

## 七、总结与启示

### 7.1 核心设计理念

| 理念 | 体现 |
|-----|------|
| **分层架构** | 网关 → 业务 → 数据 三层清晰分离 |
| **约定优于配置** | Starter 自动配置，合理默认值 |
| **开闭原则** | 通过条件注解允许扩展，禁止修改核心 |
| **单一职责** | 每个 Starter 专注一个领域能力 |
| **面向接口** | 多数据源、多缓存等通过接口抽象 |

### 7.2 可迁移到其他项目的经验

1. **网关使用 WebFlux，业务使用 MVC**：根据场景选择技术栈
2. **多租户分层实现**：网关解析 + 上下文传递 + SQL 拦截
3. **灰度发布版本路由**：请求头版本 + Nacos 元数据 + 负载均衡策略
4. **双层异常防护**：网关层 + 业务层统一异常处理
5. **Starter 组件化**：将通用能力封装为可复用 Starter
6. **异步解耦**：日志、通知等非核心逻辑异步处理

### 7.3 技术选型建议

| 场景 | 推荐技术 | 理由 |
|-----|---------|------|
| 网关入口 | Spring Gateway + WebFlux | 高并发、非阻塞 |
| 业务服务 | Spring MVC + Undertow | 易开发、高性能 |
| 多租户 | MyBatis Plus 拦截器 | 透明化、低侵入 |
| 灰度发布 | 自定义 LoadBalancer | 灵活可控 |
| 操作日志 | AOP + MQ | 解耦、高性能 |

---

## 八、参考代码位置

| 功能 | 文件路径 |
|-----|---------|
| 统一响应对象 | [jvs-starter-common/src/main/java/cn/bctools/common/utils/R.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/utils/R.java) |
| 租户上下文 | [jvs-starter-common/src/main/java/cn/bctools/common/utils/TenantContextHolder.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/utils/TenantContextHolder.java) |
| 业务异常 | [jvs-starter-common/src/main/java/cn/bctools/common/exception/BusinessException.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/exception/BusinessException.java) |
| Spring 工具 | [jvs-starter-common/src/main/java/cn/bctools/common/utils/SpringContextUtil.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/utils/SpringContextUtil.java) |
| 统一异常处理 | [jvs-starter-web/src/main/java/cn/bctools/web/config/UnifyExceptionHandler.java](jvs-base/jvs-core-parent/jvs-starter-web/src/main/java/cn/bctools/web/config/UnifyExceptionHandler.java) |
| 多租户处理器 | [jvs-starter-database/src/main/java/cn/bctools/database/interceptor/tenant/JvsTenantHandler.java](jvs-base/jvs-core-parent/jvs-starter-database/src/main/java/cn/bctools/database/interceptor/tenant/JvsTenantHandler.java) |
| 版本负载均衡 | [jvs-starter-gray/src/main/java/cn/bctools/gray/rule/VersionLoadBalancer.java](jvs-base/jvs-core-parent/jvs-starter-gray/src/main/java/cn/bctools/gray/rule/VersionLoadBalancer.java) |
| 日志 AOP 切面 | [jvs-starter-log/src/main/java/cn/bctools/log/aspect/SysLogAspect.java](jvs-base/jvs-core-parent/jvs-starter-log/src/main/java/cn/bctools/log/aspect/SysLogAspect.java) |
| 网关认证过滤器 | [jvs-gateway/src/main/java/cn/bctools/gateway/filter/AuthGlobalFilter.java](jvs-base/jvs-gateway/src/main/java/cn/bctools/gateway/filter/AuthGlobalFilter.java) |
| 网关异常处理 | [jvs-gateway/src/main/java/cn/bctools/gateway/config/GatewayErrorConfig.java](jvs-base/jvs-gateway/src/main/java/cn/bctools/gateway/config/GatewayErrorConfig.java) |
