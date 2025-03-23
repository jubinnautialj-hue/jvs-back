#jvs_report
##jvs-data-report-api
feign接口
##jvs-data-report-common
报表服务层 持久层 数据组装
###
| 模块名称 | 描述 |
|------|--------------------|
|cache|缓存工具|
|data|计算工具 按页面设计组装数据|
|dto|业务使用的类|
|entity|数据库对应的实体类|
|enums|枚举|
|handler|数据库 实体类转换处理类|
|job|定时任务|
|mapper|数据库映射|
|mongo|mongo数据库相关服务 主要用于静态报表存储数据|
|po|实体类|
|service| 实体类对应的操作服务|
|utils|工具|
##jvs-data-report-job
定时任务执行
##jvs-data-report-mgr
报表主要接口