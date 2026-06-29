# 服务网关

在微服务架构中，通常一个系统会被拆分为多个微服务，面对这么多微服务客户端应该如何去调用呢？如果没有其他更优方法，我们只能记录每个微服务对应的地址，分别去调用，但是这样会有很多的问题和潜在因素。

客户端多次请求不同的微服务，会增加客户端代码和配置的复杂性，维护成本比价高。

认证复杂，每个微服务可能存在不同的认证方式，客户端去调用，要去适配不同的认证，

存在跨域的请求，调用链有一定的相对复杂性（防火墙 / 浏览器不友好的协议）。

难以重构，随着项目的迭代，可能需要重新划分微服务

为了解决上面的问题，微服务引入了 网关 的概念，网关为微服务架构的系统提供简单、有效且统一的API路由管理，作为系统的统一入口，提供内部服务的路由中转，给客户端提供统一的服务，可以实现一些和业务没有耦合的公用逻辑，主要功能包含认证、鉴权、路由转发、安全策略、防刷、流量控制、监控日志等。

本网关实现以下功能

* swagger文档聚合
* xssFilter 防攻击由
* 请求时间日志文件记录
* 业务错误code码信息转换
* 请求路由动态加载和转发
* /mgr/开头的返回数据进行业务加密处理
* 版本路由,根据业务版本号转发相关请求
* 用户token资源权限校验



### 目录结构
```
├── jvs-gateway            (网关服务,包含权限校验,是否过滤xss,响应加密)
│   ├── Dockerfile
│   ├── pom.xml
│   ├── readme.md    
│   └── src
│   │   ├── GatewayApplication.java         (启动类)
│   │   ├── config
│   │   │   ├── AuthorizationManager.java   (权限校验)
│   │   │   ├── AuthorizationServerConfig.java  (SecurityWebFilterChain 配置)
│   │   │   ├── GatewayConfigProperties.java  (配置是否加解密)
│   │   │   ├── GatewayErrorConfig.java        (统一异常处理)
│   │   │   ├── GatewayRouteConfig.java      (动态路由配置)
│   │   │   ├── JvsReactiveAuthenticationManager.java     (获取token)
│   │   │   ├── JvsRoutePredicateHandlerMapping.java     (重写RoutePredicateHandlerMapping,添加缓存)
│   │   │   └── JvsServerBearerTokenAuthenticationConverter.java    (token转换)
│   │   ├── controller
│   │   │   └── ResourceController.java   (网关基础资源controller)
│   │   ├── dto
│   │   │   └── CheckToken.java  (用于网关校验的token)
│   │   ├── filter
│   │   │   ├── AuthGlobalFilter.java   (用于判断是否需要权限)
│   │   │   ├── GlobalRequestBodyDecodeFilter.java   (返回数据进行加密)
│   │   │   ├── GrayReactiveLoadBalancerClientFilter.java  (版本路由处理)
│   │   │   ├── RequestTimeFilter.java  (请求时间记录)
│   │   │   └── XssFilter.java  (xss攻击拦截)
│   │   ├── route
│   │   │   └── GatewayRouteDefinitionWriter.java
│   │   ├── swagger
│   │   │   ├── GatewaySwaggerAutoConfiguration.java  (聚合swagger文档)
│   │   │   └── SwaggerProperties.java
│   │   └── utils
│   │       ├── DataUtil.java
│   │       ├── IpUtils.java
│   │       ├── JsonUtil.java
│   │       ├── ServerHttpRequestUtils.java
│   │       ├── UrlUtils.java
│   │       └── XssUtil.java
```
