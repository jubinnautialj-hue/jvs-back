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

### 3.4 MongoDB 组件 - jvs-starter-mongodb

#### 核心设计理念
MongoDB 组件采用 **注解驱动的条件构建** 模式，通过自定义注解实现声明式查询，大幅简化 MongoDB 查询代码。

#### 基础服务接口
**文件位置**：[BaseMongoService.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/service/BaseMongoService.java)

```java
public interface BaseMongoService<T> {
    // 基础 CRUD
    T getById(String id);
    boolean insert(T t);
    boolean insertBatch(List<T> list);
    boolean deleteById(String id);
    boolean updateById(String id, Object objectUpdate);
    
    // 条件查询 - 支持注解驱动
    List<T> select(Object objectParam);
    T selectOne(Object objectParam);
    Page<T> page(Object objectParam, Page<T> page);
    long count(Object objectParam);
    
    // 聚合查询
    <R> AggregationResults<R> aggregate(Aggregation aggregation, Class<R> outputType);
}
```

**文件位置**：[BaseMongoServiceImpl.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/service/impl/BaseMongoServiceImpl.java)

#### 条件注解体系

项目设计了一套完整的 MongoDB 查询条件注解体系，支持通过 POJO 类的字段注解自动构建查询条件。

##### 条件注解处理器接口
**文件位置**：[ConditionsAnnotationHandler.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/ConditionsAnnotationHandler.java)

```java
@FunctionalInterface
public interface ConditionsAnnotationHandler {
    Criteria handler(Criteria criteria, Object value);
}
```

##### 元注解

| 元注解 | 作用 | 文件位置 |
|-------|------|---------|
| `@ConditionsAnnotation` | 标记为条件注解 | [ConditionsAnnotation.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/annotation/ConditionsAnnotation.java) |
| `@OperatorAnnotation` | 标记为运算符注解（AND/OR/NOR） | [OperatorAnnotation.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/annotation/OperatorAnnotation.java) |

##### 具体条件注解

| 注解 | 对应 MongoDB 操作 | 示例 |
|-----|------------------|------|
| `@Eq` | `$eq` 等于 | `name = "张三"` |
| `@Ne` | `$ne` 不等于 | `status != 0` |
| `@Gt` | `$gt` 大于 | `age > 18` |
| `@Gte` | `$gte` 大于等于 | `age >= 18` |
| `@Lt` | `$lt` 小于 | `age < 18` |
| `@Lte` | `$lte` 小于等于 | `age <= 18` |
| `@In` | `$in` 包含 | `status in [1, 2, 3]` |
| `@Regex` | 正则匹配 | `name like "%张%"` |
| `@No` | 不处理该字段 | - |

##### 运算符注解

| 注解 | 对应 MongoDB 操作 | 说明 |
|-----|------------------|------|
| `@AndOperator` | `$and` | 逻辑与 |
| `@OrOperator` | `$or` | 逻辑或 |
| `@NorOperator` | `$nor` | 逻辑或非 |

**文件位置**：[@Eq.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/annotation/Eq.java)

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ConditionsAnnotation
public @interface Eq {
    String value() default "";  // 字段名，为空则使用字段名
}
```

##### 使用示例

```java
// 查询条件 POJO
public class UserQuery {
    @Eq
    private String name;        // name = ?
    
    @Gt
    private Integer minAge;     // age > ?
    
    @Lte
    private Integer maxAge;     // age <= ?
    
    @Regex
    private String keyword;     // keyword 正则匹配
    
    @In
    private List<String> statuses; // status in ?
}

// 实际使用
UserQuery query = new UserQuery();
query.setName("张三");
query.setMinAge(18);
List<User> users = userMongoService.select(query);
```

#### 类型转换器

| 转换器 | 作用 | 文件位置 |
|-------|------|---------|
| `BigDecimalToDecimal128Converter` | BigDecimal → Decimal128 | [BigDecimalToDecimal128Converter.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/BigDecimalToDecimal128Converter.java) |
| `Decimal128ToBigDecimalConverter` | Decimal128 → BigDecimal | [Decimal128ToBigDecimalConverter.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/Decimal128ToBigDecimalConverter.java) |

#### 设计价值

| 设计点 | 价值 |
|-------|------|
| **注解驱动查询** | 类似 MyBatis Plus 的 QueryWrapper，但更声明式 |
| **POJO 即查询条件** | 无需手动构建 Criteria，通过注解自动生成 |
| **类型安全转换** | 支持 BigDecimal 等特殊类型与 MongoDB 类型互转 |
| **可扩展性** | 可自定义 ConditionsAnnotationHandler 扩展新注解 |

---

### 3.5 Redis 组件 - jvs-starter-redis

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

#### 自定义注解
**文件位置**：[CacheTenantSkip.java](jvs-base/jvs-core-parent/jvs-starter-redis/src/main/java/cn/bctools/redis/annotation/CacheTenantSkip.java)

```java
public @interface CacheTenantSkip {
    // 标记此方法的缓存不进行租户隔离
}
```

**使用场景**：
- 公共数据缓存（如系统配置、字典数据）
- 跨租户共享的数据

---

### 3.6 消息通知组件体系

项目封装了完整的消息通知组件，支持多种消息渠道：RabbitMQ、Kafka、短信、邮件、钉钉。

#### 3.6.1 RabbitMQ 组件 - jvs-starter-rabbit

**文件位置**：[MyRabbitConfig.java](jvs-base/jvs-core-parent/jvs-starter-rabbit/src/main/java/cn/bctools/rabbit/config/MyRabbitConfig.java)

**设计用途**：
- 操作日志异步存储
- 消息推送异步处理
- 业务解耦（订单通知、审批流程等）

#### 3.6.2 短信组件 - jvs-starter-sms

**文件位置**：[SmsSendUtils.java](jvs-base/jvs-core-parent/jvs-starter-sms/src/main/java/cn/bctools/sms/utils/SmsSendUtils.java)

```java
public class SmsSendUtils {
    public static final String ALIYUN_SMS_SUCCESS_CODE = "OK";
    
    public static Object aliImpl(String templateCode, List<String> phones, Map<String, String> variables) {
        // 1. 从 Spring 容器获取阿里云短信配置
        // 2. 构建短信请求
        // 3. 调用阿里云短信 API
        // 4. 处理响应，抛出业务异常
    }
}
```

**配置类**：
- [AliSmsConfig.java](jvs-base/jvs-core-parent/jvs-starter-sms/src/main/java/cn/bctools/sms/config/AliSmsConfig.java)
- [Template.java](jvs-base/jvs-core-parent/jvs-starter-sms/src/main/java/cn/bctools/sms/config/Template.java)

**设计亮点**：
- **配置中心化**：通过 `SpringContextUtil.getBean()` 获取配置
- **异常标准化**：将阿里云错误码转换为业务异常
- **模板变量替换**：支持 `${变量}` 格式的模板内容

#### 3.6.3 邮件组件 - jvs-starter-email

**文件位置**：[EmailUtils.java](jvs-base/jvs-core-parent/jvs-starter-email/src/main/java/cn/bctools/email/EmailUtils.java)

```java
public class EmailUtils {
    public boolean sendEmailMessage(MailAccount emailconfig, String title, String content, 
                                     List<String> emailList, List<DataSource> files) {
        // 1. 配置 SMTP 认证
        // 2. 支持 SSL/TLS 加密
        // 3. 支持 HTML 内容
        // 4. 支持附件
    }
}
```

**技术选型**：
- 使用 Hutool 的 `Mail` 工具类简化邮件发送
- 支持 `StartTLS` 和 `SSL` 双重加密模式

#### 3.6.4 钉钉组件 - jvs-starter-dingding

**文件位置**：[DingSendUtils.java](jvs-base/jvs-core-parent/jvs-starter-dingding/src/main/java/cn/bctools/dingding/DingSendUtils.java)

```java
@Component
@AllArgsConstructor
public class DingSendUtils {
    DingDingConfig dingDingConfig;
    
    // 发送文本消息（支持 @指定人）
    public DingRes sendMessage(String content, List<String> mobiles) { ... }
    
    // 发送 Link 消息
    public boolean sendLinkMessage(String title, String text, String picUrl, String messageUrl) { ... }
    
    // 签名生成（加签方式）
    public String getUrl(String url, String secret) {
        // HmacSHA256 签名算法
        // 时间戳 + 签名拼接 URL
    }
}
```

**支持的消息类型**：
| 消息类型 | 类 | 文件位置 |
|---------|-----|---------|
| 文本消息 | `TextMessage` | [TestMessage.java](jvs-base/jvs-core-parent/jvs-starter-dingding/src/main/java/cn/bctools/dingding/TestMessage.java) |
| Link 消息 | `LinkMessage` | [LinkMessage.java](jvs-base/jvs-core-parent/jvs-starter-dingding/src/main/java/cn/bctools/dingding/LinkMessage.java) |
| Markdown 消息 | `MarkdownMessage` | [MarkdownMessage.java](jvs-base/jvs-core-parent/jvs-starter-dingding/src/main/java/cn/bctools/dingding/MarkdownMessage.java) |

**设计亮点**：
- **加签支持**：支持钉钉 Webhook 的加签安全模式
- **消息类型抽象**：不同消息类型有独立的 POJO 类
- **响应封装**：`DingRes` 统一封装钉钉响应

#### 3.6.5 消息通知组件设计总结

| 组件 | 技术选型 | 主要用途 |
|-----|---------|---------|
| **RabbitMQ** | Spring AMQP | 业务解耦、异步处理、日志存储 |
| **Kafka** | Spring Kafka | 高吞吐量场景、日志聚合 |
| **短信** | 阿里云短信 SDK | 验证码、通知类短信 |
| **邮件** | Hutool Mail | 正式通知、报表发送 |
| **钉钉** | HTTP + 签名 | 告警通知、群机器人消息 |

**统一设计模式**：
1. **工具类封装**：`XxxUtils` 静态方法封装
2. **配置中心化**：通过 `@ConfigurationProperties` 或 Nacos 配置
3. **异常标准化**：统一抛出 `BusinessException`
4. **自动配置**：通过 `spring.factories` 自动装配

---

### 3.7 灰度发布组件 - jvs-starter-gray

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

#### 自定义注解
**文件位置**：[@Log.java](jvs-base/jvs-core-parent/jvs-starter-log/src/main/java/cn/bctools/log/annotation/Log.java)

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    String operationType() default "--";  // 操作类型
    String value() default "";             // 描述
    boolean back() default true;           // 是否记录返回结果
    Class<? extends LogCallBack> callBackClass() default LogCallBack.class; // 回调类
}
```

**设计亮点**：
1. **注解驱动**：`@Log` 注解标记需要记录的方法
2. **与 Swagger 集成**：自动读取 `@ApiOperation` 注解值作为功能名
3. **性能监控**：执行时间超过 1 秒自动警告
4. **异步处理**：使用线程池 + RabbitMQ 异步发送，不影响主流程
5. **回调机制**：支持自定义 `LogCallBack` 实现特殊业务处理

---

### 3.8 逻辑引擎/函数组件 - jvs-starter-function + jvs-design

#### 3.8.1 核心架构

JVS 的逻辑引擎是低代码平台的核心，支持 **表达式计算**、**自定义函数**、**参数解析** 三大能力。

**模块划分**：
| 模块 | 职责 |
|-----|------|
| `jvs-starter-function` | 核心表达式引擎、函数框架 |
| `jvs-design/jvs-function-*` | 具体函数实现（数据源、钉钉、第三方集成） |

#### 3.8.2 核心接口定义

**文件位置**：[IJvsFunction.java](jvs-base/jvs-core-parent/jvs-starter-function/src/main/java/cn/bctools/function/handler/IJvsFunction.java)

```java
public interface IJvsFunction<T extends ElementVo> extends IJvsExpressionElement<T> {
    String NAME = "函数列表";
    
    /**
     * 函数计算
     * @param functionName 函数名
     * @param data 参数
     * @return 计算结果
     */
    Object calculate(String functionName, @NotNull Object... data);
}
```

**文件位置**：[IJvsParam.java](jvs-base/jvs-core-parent/jvs-starter-function/src/main/java/cn/bctools/function/handler/IJvsParam.java)

```java
public interface IJvsParam<T extends ElementVo> extends IJvsExpressionElement<T> {
    String NAME = "参数列表";
    
    /**
     * 获取参数值
     * @param paramName 参数名
     * @param data 上下文数据
     * @return 参数值
     */
    Object get(String paramName, Map<String, Object> data);
}
```

#### 3.8.3 表达式处理器

**文件位置**：[ExpressionHandler.java](jvs-base/jvs-core-parent/jvs-starter-function/src/main/java/cn/bctools/function/handler/ExpressionHandler.java)

```java
@Component
public class ExpressionHandler {
    
    // 参数映射 <useCase, List<IJvsParam>>
    private Map<String, List<IJvsParam<? extends ElementVo>>> paramMap;
    
    // 函数映射 <useCase, List<IJvsFunction>>
    private Map<String, List<IJvsFunction<? extends ElementVo>>> functionMap;
    
    /**
     * 计算表达式
     * @param expression 表达式字符串，如 "SYS.USER.id + 10"
     * @param data 上下文数据
     * @param useCase 使用场景
     * @return 计算结果
     */
    public Object calculate(String expression, Map<String, Object> data, String useCase) {
        return this.calculate(ExpressionUtils.parsePostfixExpression(expression), data, useCase);
    }
    
    /**
     * 获取参数值
     * 1. 按前缀逐一匹配自定义参数
     * 2. 从环境参数中获取值
     */
    private Object getParamValue(String useCase, String paramName, Map<String, Object> data) {
        IJvsParam<? extends ElementVo> param = this.getJvsElement(useCase, paramName, this.params);
        JvsExpression annotation = this.getAnnotation(param);
        return param.get(paramName.substring(annotation.prefix().length()), data);
    }
    
    /**
     * 计算函数值
     */
    private Object getFunctionValue(String useCase, String functionName, Object... data) {
        IJvsFunction<? extends ElementVo> function = this.getJvsElement(useCase, functionName, this.functions);
        JvsExpression annotation = this.getAnnotation(function);
        return function.calculate(functionName.substring(annotation.prefix().length()), data);
    }
}
```

#### 3.8.4 函数标记注解

**文件位置**：[@JvsExpression.java](jvs-base/jvs-core-parent/jvs-starter-function/src/main/java/cn/bctools/function/handler/JvsExpression.java)

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface JvsExpression {
    
    /** 前缀，如 "SYS", "variable", "DATA" */
    String prefix() default "";
    
    /** 使用场景，如 "rule.default", "form.default" */
    String[] useCase() default {};
    
    /** 分组名称 */
    String groupName();
    
    /** 排序 */
    int order() default 0;
}
```

#### 3.8.5 使用示例

```java
// 1. 定义自定义参数
@JvsExpression(prefix = "SYS", groupName = "系统参数", useCase = {"rule.default"})
public class SysParamImpl implements IJvsParam<ElementVo> {
    @Override
    public Object get(String paramName, Map<String, Object> data) {
        // SYS.USER.id -> 获取当前用户ID
        // SYS.TIME -> 获取当前时间
        // ...
    }
}

// 2. 定义自定义函数
@JvsExpression(prefix = "FUNC", groupName = "自定义函数", useCase = {"rule.default"})
public class MyFunctionImpl implements IJvsFunction<ElementVo> {
    @Override
    public Object calculate(String functionName, Object... data) {
        // FUNC.add(1, 2) -> 3
        // FUNC.formatDate(date, "yyyy-MM-dd") -> 格式化日期
        // ...
    }
}

// 3. 使用表达式
Map<String, Object> context = new HashMap<>();
context.put("price", 100);
Object result = expressionHandler.calculate(
    "SYS.USER.level == 'VIP' ? price * 0.8 : price",
    context,
    "rule.default"
);
```

#### 3.8.6 jvs-function 插件体系

**目录**：[jvs-design/jvs-function/](jvs-design/jvs-function/)

| 插件模块 | 功能 |
|---------|------|
| `jvs-function-base` | 基础函数（加密解密、布尔运算、Excel 解析） |
| `jvs-function-data` | 数据源函数（MySQL、PostgreSQL、Oracle、MongoDB、Redis、ES、RabbitMQ） |
| `jvs-function-dingding-plug` | 钉钉集成（待办任务、消息发送、考勤） |
| `jvs-function-api-gentleman-plug` | 君子签电子签章（合同签署、实名认证） |
| `jvs-function-api-tencent-ess-plug` | 腾讯电子签 |
| `jvs-function-api-open-platform` | 第三方平台（阿里云市场、企查查、天眼查） |

**数据源函数设计**：
以 MySQL 为例，支持：
- 执行 SQL 查询/更新
- 存储过程调用
- 分页查询
- 多数据源支持

#### 3.8.7 逻辑引擎设计价值

| 设计点 | 价值 |
|-------|------|
| **表达式解析** | 支持中缀表达式转后缀，类似 JSP EL 表达式 |
| **前缀匹配** | 通过前缀区分不同参数/函数来源（SYS、variable、DATA） |
| **场景隔离** | 同一函数可限制在特定场景使用（useCase） |
| **插件化扩展** | 通过 `@JvsExpression` 注解自动注册，无需修改核心代码 |
| **低代码支撑** | 可视化拖拽配置时，后端通过表达式引擎执行逻辑 |

---

### 3.9 网关组件 - jvs-gateway

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

## 四、自定义注解体系设计

### 4.1 注解分类总览

JVS 项目设计了丰富的自定义注解体系，覆盖 **功能标记**、**配置声明**、**行为控制** 三大类。

| 分类 | 注解 | 用途 |
|-----|------|------|
| **功能标记** | `@Log` | 标记方法需要记录操作日志 |
| | `@JvsExpression` | 标记为逻辑引擎的参数/函数 |
| | `@ConditionsAnnotation` | 标记为 MongoDB 查询条件注解 |
| **配置声明** | `@CacheTenantSkip` | 声明缓存不进行租户隔离 |
| | `@JvsDataTable` | 声明数据表信息 |
| **行为控制** | `@LogIgnore` | 标记参数不记录日志 |
| | `@LogCallBack` | 日志回调接口标记 |

### 4.2 功能标记类注解

#### @Log - 操作日志标记
**文件位置**：[@Log.java](jvs-base/jvs-core-parent/jvs-starter-log/src/main/java/cn/bctools/log/annotation/Log.java)

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    String operationType() default "--";  // 操作类型
    String value() default "";             // 功能描述
    boolean back() default true;           // 是否记录返回值
    Class<? extends LogCallBack> callBackClass() default LogCallBack.class;
}
```

**设计模式**：**观察者模式 + 注解驱动**
- AOP 切面 `SysLogAspect` 监听带 `@Log` 注解的方法
- 方法执行前后自动记录日志
- 通过 `callBackClass` 支持自定义回调逻辑

#### @JvsExpression - 逻辑引擎组件标记
**文件位置**：[@JvsExpression.java](jvs-base/jvs-core-parent/jvs-starter-function/src/main/java/cn/bctools/function/handler/JvsExpression.java)

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface JvsExpression {
    String prefix() default "";           // 前缀，如 "SYS", "DATA"
    String[] useCase() default {};        // 使用场景
    String groupName();                    // 分组名称
    int order() default 0;                 // 排序
}
```

**设计模式**：**策略模式 + 自动注册**
- 实现类自动注册到 `ExpressionHandler`
- 通过 `prefix` 区分不同策略
- 通过 `useCase` 限制使用场景

#### @ConditionsAnnotation - MongoDB 条件元注解
**文件位置**：[@ConditionsAnnotation.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/annotation/ConditionsAnnotation.java)

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConditionsAnnotation {
}
```

**设计模式**：**元注解模式**
- 作为元注解标记其他注解（如 `@Eq`、`@Gt`）
- 便于 `ConditionsAnnotationHandlerRegister` 统一发现和处理

### 4.3 配置声明类注解

#### @CacheTenantSkip - 缓存租户跳过
**文件位置**：[@CacheTenantSkip.java](jvs-base/jvs-core-parent/jvs-starter-redis/src/main/java/cn/bctools/redis/annotation/CacheTenantSkip.java)

```java
public @interface CacheTenantSkip {
}
```

**设计意图**：
- 默认情况下，Redis 缓存 key 包含租户 ID，实现租户隔离
- 此注解声明该方法的缓存**不进行租户隔离**，所有租户共享
- 适用于公共配置、字典数据等场景

#### @JvsDataTable - 数据表声明
**文件位置**：[@JvsDataTable.java](jvs-base/jvs-core-parent/jvs-starter-database/src/main/java/cn/bctools/database/annotation/JvsDataTable.java)

```java
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JvsDataTable {
    String value();           // 表名
    String desc();            // 中文描述
}
```

**设计用途**：
- 用于数据权限控制
- 描述表的业务含义
- 支持自动化文档生成

### 4.4 行为控制类注解

#### @LogIgnore - 日志忽略标记
**文件位置**：[@LogIgnore.java](jvs-base/jvs-core-parent/jvs-starter-log/src/main/java/cn/bctools/log/annotation/LogIgnore.java)

**设计场景**：
- 敏感参数（如密码）不记录日志
- 大对象参数（如文件流）不记录日志
- 无需记录的参数

**AOP 处理逻辑**（SysLogAspect.java）：
```java
// 检查参数是否有 @LogIgnore 注解
if (Arrays.stream(method.getParameterAnnotations()[i])
    .noneMatch(e -> e instanceof LogIgnore)) {
    objects[i] = point.getArgs()[i];  // 记录
}
```

#### @LogCallBack - 日志回调接口
**文件位置**：[@LogCallBack.java](jvs-base/jvs-core-parent/jvs-starter-log/src/main/java/cn/bctools/log/annotation/LogCallBack.java)

**设计模式**：**回调模式**
- 特殊业务需要在日志记录后执行自定义逻辑
- 如：审批操作后发送通知、重要操作二次审计

**使用示例**：
```java
public class OrderLogCallback implements LogCallBack {
    @Override
    public void callBack(String userName, String functionName, String params) {
        // 订单创建日志后，发送短信通知管理员
    }
}

@Log(callBackClass = OrderLogCallback.class)
public R createOrder(OrderDto dto) { ... }
```

### 4.5 MongoDB 条件注解详解

#### 条件注解体系结构

```
@ConditionsAnnotation (元注解)
├── @Eq          // 等于
├── @Ne          // 不等于
├── @Gt          // 大于
├── @Gte         // 大于等于
├── @Lt          // 小于
├── @Lte         // 小于等于
├── @In          // 包含
├── @Regex       // 正则匹配
└── @No          // 不处理
```

**文件位置**：[@Eq.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/annotation/Eq.java)

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ConditionsAnnotation  // 元注解标记
public @interface Eq {
    String value() default "";  // 可选：指定 MongoDB 字段名
}
```

#### 运算符注解

```
@OperatorAnnotation (元注解)
├── @AndOperator   // $and
├── @OrOperator    // $or
└── @NorOperator   // $nor
```

#### 设计模式：**注解驱动的 DSL**

类似 JPA 的 `@Query` 或 MyBatis Plus 的 `@TableField`，但更面向对象：

```java
public class UserQuery {
    @Eq
    private String name;           // 生成：name = ?
    
    @Gt("age")
    private Integer minAge;       // 生成：age > ?
    
    @In
    private List<String> status;  // 生成：status in ?
}
```

**优势**：
1. **类型安全**：编译期检查，避免字符串拼写错误
2. **IDE 支持**：自动补全、重构支持
3. **可扩展**：可自定义 `ConditionsAnnotationHandler` 添加新注解

### 4.6 注解设计总结

#### 设计原则

| 原则 | 体现 |
|-----|------|
| **单一职责** | 每个注解只做一件事（@Log 只负责日志） |
| **声明式** | 通过注解声明意图，而非编码实现 |
| **可组合** | 元注解模式支持组合（@ConditionsAnnotation） |
| **约定优于配置** | 合理的默认值（@Log.back() 默认为 true） |
| **开闭原则** | 通过注解扩展功能，无需修改核心代码 |

#### 注解与 AOP 配合模式

```
注解定义 (@Log)
    ↓
AOP 切面扫描 (SysLogAspect)
    ↓
环绕通知织入逻辑
    ↓
执行业务方法
    ↓
记录日志 / 执行回调
```

这种模式在项目中被广泛应用：
- `@Log` + `SysLogAspect` → 操作日志
- `@JvsExpression` + `ExpressionHandler` → 逻辑引擎
- `@ConditionsAnnotation` + `ConditionsAnnotationHandler` → MongoDB 查询

## 五、设计模式应用分析

### 5.1 建造者模式
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

### 5.2 模板方法模式
**应用场景**：统一异常处理、过滤器链、BaseMongoServiceImpl

```java
// UnifyExceptionHandler - 模板化异常处理
@ExceptionHandler(value = Exception.class)
public R exceptionHandler(...) {
    // 1. 记录日志
    // 2. 处理特殊异常
    // 3. 返回统一响应
}

// BaseMongoServiceImpl - 模板化 CRUD
public abstract class BaseMongoServiceImpl<T> implements BaseMongoService<T> {
    // 定义模板方法，子类只需提供集合名和类型
    public BaseMongoServiceImpl(String table, Class<T> tableClass) {
        this.table = table;
        this.tableClass = tableClass;
    }
}
```

### 5.3 策略模式
**应用场景**：版本负载均衡、逻辑引擎函数、MongoDB 条件处理

#### 版本路由策略
**文件位置**：[VersionLoadBalancer.java](jvs-base/jvs-core-parent/jvs-starter-gray/src/main/java/cn/bctools/gray/rule/VersionLoadBalancer.java)

**路由策略优先级**：
1. 版本匹配优先
2. 权重路由次之
3. 轮询兜底

#### 逻辑引擎策略
```java
// 通过 @JvsExpression 注册不同策略
@JvsExpression(prefix = "SYS", groupName = "系统参数")
public class SysParamImpl implements IJvsParam<ElementVo> { ... }

@JvsExpression(prefix = "variable", groupName = "变量")
public class VariableParamImpl implements IJvsParam<ElementVo> { ... }

// ExpressionHandler 根据前缀选择策略
private Object getParamValue(String useCase, String paramName, Map<String, Object> data) {
    // paramName = "SYS.USER.id" -> 选择 SysParamImpl
    // paramName = "variable.price" -> 选择 VariableParamImpl
}
```

### 5.4 责任链模式
**应用场景**：网关过滤器链、表达式解析链

#### 网关过滤器链
```java
// 网关过滤器执行顺序（Ordered）
AuthGlobalFilter (-1)                 // 认证、租户解析（最高优先级）
→ GrayReactiveLoadBalancerClientFilter  // 灰度路由
→ XssFilter                              // XSS 防护
→ RequestTimeFilter                      // 请求耗时统计
→ GlobalRequestBodyDecodeFilter          // 请求体解码
```

**设计价值**：
- 每个过滤器职责单一
- 可通过 `@Order` 调整执行顺序
- 可动态添加/移除过滤器

### 5.5 观察者模式（AOP）
**应用场景**：操作日志、事务管理

#### 操作日志 AOP
**文件位置**：[SysLogAspect.java](jvs-base/jvs-core-parent/jvs-starter-log/src/main/java/cn/bctools/log/aspect/SysLogAspect.java)

```java
@Aspect
@Order(1)
public class SysLogAspect {
    
    @Around("@annotation(logannotation)")
    public Object around(ProceedingJoinPoint point, Log logannotation) {
        // 前置通知：记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 执行目标方法
            Object result = point.proceed();
            
            // 后置返回通知：记录成功日志
            logSuccess(result, startTime);
            return result;
        } catch (Throwable e) {
            // 后置异常通知：记录失败日志
            logError(e, startTime);
            throw e;
        }
    }
}
```

**设计价值**：
- `@Log` 注解标记切点（被观察者）
- AOP 切面在方法执行前后织入逻辑（观察者）
- 业务代码与日志逻辑完全解耦

### 5.6 插件模式（SPI）
**应用场景**：逻辑引擎函数扩展、数据源插件

#### 逻辑引擎插件化
```
核心框架 (jvs-starter-function)
    ↓
    ExpressionHandler (插件管理器)
    ↓
    发现 @JvsExpression 注解的实现类
    ↓
自动注册到函数/参数注册表
```

**插件开发步骤**：
1. 实现 `IJvsFunction` 或 `IJvsParam` 接口
2. 添加 `@JvsExpression` 注解
3. 放置到 classpath 下，自动扫描注册

**设计价值**：
- **开闭原则**：扩展功能无需修改核心代码
- **热插拔**：添加/移除插件只需修改依赖
- **可测试**：插件可独立测试

### 5.7 元注解模式
**应用场景**：MongoDB 条件注解

```java
// 元注解
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConditionsAnnotation {}

// 具体注解（使用元注解标记）
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ConditionsAnnotation  // 元注解
public @interface Eq {
    String value() default "";
}

// 处理器统一识别
public class DefaultConditionsAnnotationHandlerRegister {
    // 扫描所有带 @ConditionsAnnotation 的注解
    // 注册对应的 Handler
}
```

**设计价值**：
- 统一管理一类注解
- 便于扩展新的条件注解
- 符合 DRY 原则

---

## 六、架构设计意图总结

### 6.1 多租户架构设计

#### 实现层次
| 层次 | 组件 | 职责 |
|-----|------|------|
| 网关层 | AuthGlobalFilter | 域名解析租户ID |
| 上下文层 | TenantContextHolder | 线程级租户存储 |
| 数据库层 | JvsTenantHandler | SQL 自动注入条件 |
| 缓存层 | JvsCacheKeyGenerator | Redis Key 租户隔离 |

#### 数据流
```
请求 → 网关解析域名 → 写入请求头 → 下游服务读取 → SQL自动过滤
                                    ↓
                              Redis Key 自动拼接租户ID
```

**设计价值**：
- **数据隔离**：同一套代码服务多个租户，数据完全隔离
- **透明化**：业务代码无需感知租户逻辑
- **可扩展**：支持按租户定制化（结合灰度发布）
- **灵活控制**：通过 `@CacheTenantSkip` 选择性跳过

### 6.2 灰度发布设计

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

### 6.3 统一响应与异常处理

#### 双层防护机制
```
┌─────────────────────────────────────────────────────┐
│                    网关层                              │
│  GatewayErrorConfig (ErrorWebExceptionHandler)      │
│  - 统一错误码映射                                      │
│  - 钉钉告警通知                                        │
│  - 错误去重                                            │
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

### 6.4 低代码逻辑引擎设计

#### 核心架构
```
┌─────────────────────────────────────────────────────────┐
│                    可视化设计器（前端）                    │
│  拖拽配置 → 生成表达式字符串 → 提交后端执行               │
└─────────────────────────┬───────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   后端表达式引擎                          │
│  ExpressionHandler                                       │
│  ├── 参数解析 (IJvsParam)                                │
│  │   ├── SYS.* (系统参数：当前用户、时间等)              │
│  │   ├── variable.* (表单变量)                          │
│  │   └── DATA.* (数据源查询)                            │
│  └── 函数计算 (IJvsFunction)                            │
│      ├── 基础函数 (加密、解密、格式转换)                 │
│      ├── 数据源函数 (SQL、Mongo、Redis)                 │
│      └── 第三方集成 (钉钉、电子签、企查查)               │
└─────────────────────────────────────────────────────────┘
```

#### 插件化扩展
```
核心框架 (jvs-starter-function)
    ↓
    插件接口 (IJvsFunction / IJvsParam)
    ↓
    ┌─────────────────┬─────────────────┬─────────────────┐
    │ jvs-function-   │ jvs-function-   │ jvs-function-   │
    │ base            │ data            │ dingding-plug   │
    │ (基础函数)       │ (数据源)        │ (钉钉集成)       │
    └─────────────────┴─────────────────┴─────────────────┘
                    ↓
            自动扫描 @JvsExpression
                    ↓
            注册到 ExpressionHandler
```

### 6.5 消息通知体系设计

#### 统一封装模式
```
                    业务代码
                       ↓
              调用 XxxUtils 静态方法
                       ↓
    ┌──────────┬──────────┬──────────┬──────────┐
    │ 短信      │ 邮件      │ 钉钉      │ RabbitMQ │
    │ SmsUtils  │ EmailUtils│ DingUtils │ -        │
    └──────────┴──────────┴──────────┴──────────┘
                       ↓
              统一抛出 BusinessException
                       ↓
              统一异常处理返回 R<T>
```

#### 设计要点
| 要点 | 说明 |
|-----|------|
| **工具类封装** | 屏蔽第三方 SDK 复杂度 |
| **配置中心化** | 从 Spring 容器或 Nacos 获取配置 |
| **异常标准化** | 统一转换为 `BusinessException` |
| **自动装配** | 通过 `spring.factories` 自动注入 |

### 6.6 服务治理体系

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
| 操作审计 | jvs-starter-log | 操作日志记录 |
| 消息通知 | 多渠道封装 | 短信/邮件/钉钉 |

---

## 七、可复用的设计模式与最佳实践

### 7.1 Starter 封装模式

#### 模块划分原则
```
jvs-starter-xxx/
├── src/main/java/cn/bctools/xxx/
│   ├── annotation/    # 自定义注解
│   ├── config/        # 自动配置类
│   ├── handler/       # 处理器/拦截器
│   ├── utils/         # 工具类
│   ├── service/       # 服务接口与实现
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

### 7.2 上下文传递模式

#### 问题场景
- 线程池执行时，ThreadLocal 数据丢失
- 微服务间调用时，上下文需要传递

#### 解决方案
1. **TransmittableThreadLocal**：解决线程池上下文传递
   - 文件位置：[SystemThreadLocal.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/utils/SystemThreadLocal.java)（推测）
   
2. **Feign 拦截器**：服务间调用时传递请求头
   - 文件位置：[jvs-starter-feign/](jvs-base/jvs-core-parent/jvs-starter-feign/)

### 7.3 异步解耦模式

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
- 使用线程池 + MQ 双层异步

### 7.4 注解驱动模式

#### 核心要素
| 要素 | 说明 |
|-----|------|
| **注解定义** | `@Log`、`@JvsExpression`、`@ConditionsAnnotation` |
| **元注解** | `@ConditionsAnnotation` 标记其他注解 |
| **AOP 切面** | `SysLogAspect` 监听注解方法 |
| **处理器** | `ConditionsAnnotationHandler` 处理注解逻辑 |
| **注册表** | `ExpressionHandler` 管理所有实现 |

#### 开发步骤
```
1. 定义注解 @Xxx
2. 定义处理器接口 XxxHandler
3. 实现处理器或 AOP 切面
4. 通过 spring.factories 或 @Component 自动注册
5. 业务代码使用 @Xxx 注解
```

### 7.5 插件化扩展模式

#### 核心接口
```java
// 1. 定义扩展接口
public interface IJvsFunction<T extends ElementVo> {
    Object calculate(String functionName, Object... data);
}

// 2. 定义注解标记
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface JvsExpression {
    String prefix();
    String groupName();
}

// 3. 实现插件
@JvsExpression(prefix = "MY_FUNC", groupName = "我的函数")
public class MyFunction implements IJvsFunction<ElementVo> {
    // 实现...
}

// 4. 自动注册
@Component
public class ExpressionHandler {
    // 构造函数注入所有 IJvsFunction 实现
    public ExpressionHandler(List<IJvsFunction<?>> functions) {
        // 注册到内部 Map
    }
}
```

---

## 八、总结与启示

### 8.1 核心设计理念

| 理念 | 体现 |
|-----|------|
| **分层架构** | 网关 → 业务 → 数据 三层清晰分离 |
| **约定优于配置** | Starter 自动配置，合理默认值 |
| **开闭原则** | 通过条件注解、插件模式允许扩展 |
| **单一职责** | 每个 Starter、每个注解专注一个领域 |
| **面向接口** | 多数据源、多缓存、函数引擎通过接口抽象 |
| **注解驱动** | 大量使用自定义注解简化配置 |
| **插件化扩展** | 逻辑引擎、数据源支持热插拔扩展 |

### 8.2 可迁移到其他项目的经验

#### 架构层面
1. **网关使用 WebFlux，业务使用 MVC**：根据场景选择技术栈
2. **多租户分层实现**：网关解析 + 上下文传递 + SQL 拦截 + 缓存隔离
3. **灰度发布版本路由**：请求头版本 + Nacos 元数据 + 负载均衡策略
4. **双层异常防护**：网关层 + 业务层统一异常处理
5. **Starter 组件化**：将通用能力封装为可复用 Starter
6. **异步解耦**：日志、通知等非核心逻辑异步处理

#### 设计模式层面
1. **注解驱动开发**：通过 `@Log` 模式简化横切关注点
2. **策略模式 + 自动注册**：类似 `@JvsExpression` 的插件化设计
3. **元注解模式**：类似 `@ConditionsAnnotation` 的注解分类管理
4. **模板方法模式**：`BaseMongoServiceImpl` 等抽象基类设计
5. **责任链模式**：网关过滤器、表达式解析链

#### 技术选型建议

| 场景 | 推荐技术 | 理由 |
|-----|---------|------|
| 网关入口 | Spring Gateway + WebFlux | 高并发、非阻塞 |
| 业务服务 | Spring MVC + Undertow | 易开发、高性能 |
| 关系型数据库 | MyBatis Plus | 生态成熟、多租户支持 |
| NoSQL 数据库 | Spring Data MongoDB | 与 Spring 生态集成好 |
| 多租户 | MyBatis Plus 拦截器 | 透明化、低侵入 |
| 灰度发布 | 自定义 LoadBalancer | 灵活可控 |
| 操作日志 | AOP + MQ | 解耦、高性能 |
| 消息通知 | 多渠道封装 | 短信/邮件/钉钉统一接口 |
| 低代码引擎 | 表达式解析 + 插件化 | 可视化配置、易扩展 |

---

## 九、参考代码位置

### 9.1 核心组件

| 功能 | 文件路径 |
|-----|---------|
| 统一响应对象 | [jvs-starter-common/src/main/java/cn/bctools/common/utils/R.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/utils/R.java) |
| 租户上下文 | [jvs-starter-common/src/main/java/cn/bctools/common/utils/TenantContextHolder.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/utils/TenantContextHolder.java) |
| 业务异常 | [jvs-starter-common/src/main/java/cn/bctools/common/exception/BusinessException.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/exception/BusinessException.java) |
| Spring 工具 | [jvs-starter-common/src/main/java/cn/bctools/common/utils/SpringContextUtil.java](jvs-base/jvs-core-parent/jvs-starter-common/src/main/java/cn/bctools/common/utils/SpringContextUtil.java) |
| 统一异常处理 | [jvs-starter-web/src/main/java/cn/bctools/web/config/UnifyExceptionHandler.java](jvs-base/jvs-core-parent/jvs-starter-web/src/main/java/cn/bctools/web/config/UnifyExceptionHandler.java) |
| 多租户处理器 | [jvs-starter-database/src/main/java/cn/bctools/database/interceptor/tenant/JvsTenantHandler.java](jvs-base/jvs-core-parent/jvs-starter-database/src/main/java/cn/bctools/database/interceptor/tenant/JvsTenantHandler.java) |

### 9.2 MongoDB 组件

| 功能 | 文件路径 |
|-----|---------|
| MongoDB 基础服务接口 | [jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/service/BaseMongoService.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/service/BaseMongoService.java) |
| MongoDB 基础服务实现 | [jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/service/impl/BaseMongoServiceImpl.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/service/impl/BaseMongoServiceImpl.java) |
| 条件元注解 | [jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/annotation/ConditionsAnnotation.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/annotation/ConditionsAnnotation.java) |
| 等于条件注解 | [jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/annotation/Eq.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/annotation/Eq.java) |
| 条件注解处理器 | [jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/ConditionsAnnotationHandler.java](jvs-base/jvs-core-parent/jvs-starter-mongodb/src/main/java/cn/bctools/mongodb/core/ConditionsAnnotationHandler.java) |

### 9.3 消息通知组件

| 功能 | 文件路径 |
|-----|---------|
| RabbitMQ 配置 | [jvs-starter-rabbit/src/main/java/cn/bctools/rabbit/config/MyRabbitConfig.java](jvs-base/jvs-core-parent/jvs-starter-rabbit/src/main/java/cn/bctools/rabbit/config/MyRabbitConfig.java) |
| 短信发送工具 | [jvs-starter-sms/src/main/java/cn/bctools/sms/utils/SmsSendUtils.java](jvs-base/jvs-core-parent/jvs-starter-sms/src/main/java/cn/bctools/sms/utils/SmsSendUtils.java) |
| 短信配置 | [jvs-starter-sms/src/main/java/cn/bctools/sms/config/AliSmsConfig.java](jvs-base/jvs-core-parent/jvs-starter-sms/src/main/java/cn/bctools/sms/config/AliSmsConfig.java) |
| 邮件发送工具 | [jvs-starter-email/src/main/java/cn/bctools/email/EmailUtils.java](jvs-base/jvs-core-parent/jvs-starter-email/src/main/java/cn/bctools/email/EmailUtils.java) |
| 钉钉发送工具 | [jvs-starter-dingding/src/main/java/cn/bctools/dingding/DingSendUtils.java](jvs-base/jvs-core-parent/jvs-starter-dingding/src/main/java/cn/bctools/dingding/DingSendUtils.java) |
| 钉钉配置 | [jvs-starter-dingding/src/main/java/cn/bctools/dingding/DingDingConfig.java](jvs-base/jvs-core-parent/jvs-starter-dingding/src/main/java/cn/bctools/dingding/DingDingConfig.java) |

### 9.4 逻辑引擎组件

| 功能 | 文件路径 |
|-----|---------|
| 函数接口 | [jvs-starter-function/src/main/java/cn/bctools/function/handler/IJvsFunction.java](jvs-base/jvs-core-parent/jvs-starter-function/src/main/java/cn/bctools/function/handler/IJvsFunction.java) |
| 参数接口 | [jvs-starter-function/src/main/java/cn/bctools/function/handler/IJvsParam.java](jvs-base/jvs-core-parent/jvs-starter-function/src/main/java/cn/bctools/function/handler/IJvsParam.java) |
| 表达式处理器 | [jvs-starter-function/src/main/java/cn/bctools/function/handler/ExpressionHandler.java](jvs-base/jvs-core-parent/jvs-starter-function/src/main/java/cn/bctools/function/handler/ExpressionHandler.java) |
| 函数标记注解 | [jvs-starter-function/src/main/java/cn/bctools/function/handler/JvsExpression.java](jvs-base/jvs-core-parent/jvs-starter-function/src/main/java/cn/bctools/function/handler/JvsExpression.java) |

### 9.5 自定义注解

| 功能 | 文件路径 |
|-----|---------|
| 操作日志注解 | [jvs-starter-log/src/main/java/cn/bctools/log/annotation/Log.java](jvs-base/jvs-core-parent/jvs-starter-log/src/main/java/cn/bctools/log/annotation/Log.java) |
| 日志忽略注解 | [jvs-starter-log/src/main/java/cn/bctools/log/annotation/LogIgnore.java](jvs-base/jvs-core-parent/jvs-starter-log/src/main/java/cn/bctools/log/annotation/LogIgnore.java) |
| 缓存租户跳过注解 | [jvs-starter-redis/src/main/java/cn/bctools/redis/annotation/CacheTenantSkip.java](jvs-base/jvs-core-parent/jvs-starter-redis/src/main/java/cn/bctools/redis/annotation/CacheTenantSkip.java) |
| 数据表注解 | [jvs-starter-database/src/main/java/cn/bctools/database/annotation/JvsDataTable.java](jvs-base/jvs-core-parent/jvs-starter-database/src/main/java/cn/bctools/database/annotation/JvsDataTable.java) |

### 9.6 网关与灰度组件

| 功能 | 文件路径 |
|-----|---------|
| 版本负载均衡 | [jvs-starter-gray/src/main/java/cn/bctools/gray/rule/VersionLoadBalancer.java](jvs-base/jvs-core-parent/jvs-starter-gray/src/main/java/cn/bctools/gray/rule/VersionLoadBalancer.java) |
| 日志 AOP 切面 | [jvs-starter-log/src/main/java/cn/bctools/log/aspect/SysLogAspect.java](jvs-base/jvs-core-parent/jvs-starter-log/src/main/java/cn/bctools/log/aspect/SysLogAspect.java) |
| 网关认证过滤器 | [jvs-gateway/src/main/java/cn/bctools/gateway/filter/AuthGlobalFilter.java](jvs-base/jvs-gateway/src/main/java/cn/bctools/gateway/filter/AuthGlobalFilter.java) |
| 网关异常处理 | [jvs-gateway/src/main/java/cn/bctools/gateway/config/GatewayErrorConfig.java](jvs-base/jvs-gateway/src/main/java/cn/bctools/gateway/config/GatewayErrorConfig.java) |
