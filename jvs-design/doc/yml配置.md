

```yaml

server:
 port: 31004
spring:
    #用于设计存储
  datasource:
    url: jdbc:mysql://jvs-mysql:3306/design?&maxLifetime=1790000&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true
    username: root
    password: root
#用于数据存储，低代码数据不同模式存储的数据在不同的库里
  data:
    mongodb:
      database: jvs-data
      username: "jvs"
      password: jvs
      host: jvs-mongo
      port: 27017
      authentication-database: admin
      dev:
        database: jvs-data-dev
        username: "jvs"
        password: jvs
        host: jvs-mongo
        port: 27017
        authentication-database: admin
      beta:
        database: jvs-data-beta
        username: "jvs"
        password: jvs
        host: jvs-mongo
        port: 27017
        authentication-database: admin

  cloud:
    # spring-cloud-stream消息配置
    function:
      # flowApprovalTimeout-工作流审批超时消息
      definition: flowApprovalTimeout
    stream:
      bindings:
        flowApprovalTimeout-out-0:
          destination: jvs-flowApprovalTimeout
        flowApprovalTimeout-in-0:
          destination: jvs-flowApprovalTimeout
          group: workflow
      rabbit:
        bindings:
          flowApprovalTimeout-out-0:
            producer:
              delayed-exchange: true
          flowApprovalTimeout-in-0:
            consumer:
              delayed-exchange: true

jvs:
  #添加mybatis 二级缓存前缀
  mybatis_cache_prefix: design
# 是否打开应用日志
app.log: true
#配置定时任务最小执行时间，默认为 0，即可以为一秒一次
xxl.job.cron.time: 0

```
