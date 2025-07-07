### JVS低代码适用场景

企业需要IT支撑的事项内容众多
业务需求部门经常调整需求
业务部门对IT系统要求上线的周期短
内部IT人员支持力量不够
业务系统需要持续演进版本迭代

### JVS低代码平台特点

* 可视化设计：通过拖放组件、模块和页面元素，用户可以轻松地创建自定义应用程序界面。
* 预制模板：提供丰富的预制模板，帮助用户快速搭建各种类型的应用程序。
* 集成API:支持与现有系统和服务的集成，方便用户将应用程序与其他系统进行数据交互。
* 自动化测试：内置自动化测试功能，确保应用程序的质量和稳定性。
* 可扩展性：支持自定义开发和扩展，满足不同行业和业务需求。
* 移动端兼容: 一键发布到PC和手机端

### 项目结构

~~~
├── jvs-design-api
│   ├── pom.xml
│   ├── src
│   │   └── main
├── jvs-design-common
│   ├── pom.xml
│   ├── src
│   │   ├── chart   （对接图表）
│   │   ├── constant  基础常量
│   │   ├── crud      列表设计
│   │   ├── data      数据增删改查
│   │   │   ├── aspect   模型自定义名称转换
│   │   │   ├── component   模型数据操作抽象类
│   │   │   ├── dto              关联标识
│   │   │   ├── entity        事件，字段， 日志，设置，主键生成对象
│   │   │   ├── fields
│   │   │   │   ├── dto     设计解析对象，包含表单组件，自定义添加组件时需要添加此添加
│   │   │   │   ├── enums
│   │   │   │   │   ├── DataEventType.java   数据事件类型
│   │   │   │   │   ├── DataFieldType.java   数据字段类型
│   │   │   │   │   ├── DataQueryType.java    查询类型
│   │   │   │   │   ├── DesignType.java      设计套件类型
│   │   │   │   │   ├── FormComponentType.java  表单组件类型
│   │   │   │   │   ├── FormTypeEnum.java     表单类型， 普通表单和流程表单
│   │   │   │   │   └── LinkTypeDto.java    特殊字段数据结构转换对象
│   │   │   │   └── impl
│   │   │   │   │   └── DataFieldHandler.java    字段处理公共类，字段处理时调用的公共方法
│   │   │   │   │   └── DesignField.java     字段类型公共注解
│   │   │   │   │   └── IDataFieldHandler.java 字段处理接口
│   │   │   ├── mapper
│   │   │   ├── service
│   │   │   │   └── impl
│   │   │   └── util
│   │   ├── expression    公式场景常量
│   │   ├── external     （未启用）
│   │   ├── h5              外面页面接入
│   │   ├── jvslog        应用日志
│   │   ├── menu          菜单
│   │   ├── notice        数据变化通知
│   │   ├── project       应用管理
│   │   ├── report        对接报表
│   │   ├── rule          逻辑引擎dao service
│   │   │   │   ├── api
│   │   │   │   │   └── dto
│   │   │   │   ├── component  对接xxl-job-admin 保存逻辑日志
│   │   │   │   ├── entity   设计对象，日志对象
│   │   │   │   ├── mapper
│   │   │   │   ├── selectoption  用于存储配置的对象信息
│   │   │   │   └── service
│   │   │   │       └── impl
│   │   ├── screen     （未启用）
│   │   ├── template    （未启用模板）
│   │   ├── util             数据管理组装工具类
│   │   └── workflow    工作流
│   │   │   ├── constant    常量
│   │   │   ├── dto           接口入参/出参
│   │   │   ├── entity       实体类
│   │   │   │   ├── dto     实体类属性映射对象
│   │   │   │   ├── enums   实体类属性枚举
│   │   │   │   └── handler  自定义字段类型转换
│   │   │   ├── enums     工作流服务相关枚举
│   │   │   ├── mapper   
│   │   │   ├── model      工作流设计数据结构
│   │   │   │   ├── condition  工作流设计条件相关属性
│   │   │   │   ├── enums      工作流设计相关枚举
│   │   │   │   └── properties  工作流设计节点配置
│   │   │   ├── service    工作流服务
│   │   │   │   └── impl
│   │   │   ├── support    工作流任务流转处理
│   │   │   │   ├── condition  条件支持的各种条件判断实现
│   │   │   │   │   └── impl
│   │   │   │   ├── config  配置
│   │   │   │   ├── context 上下文
│   │   │   │   ├── empty    审批人为空处理策略
│   │   │   │   │   └── impl
│   │   │   │   ├── enums  流转相关枚举
│   │   │   │   ├── function  工作流所有节点支持的基础功能
│   │   │   │   │   ├── dto
│   │   │   │   │   └── impl
│   │   │   │   ├── node  各节点类型的处理
│   │   │   │   ├── process  对工作流节点执行结果进行处理
│   │   │   │   │   └── impl
│   │   │   │   ├── runtime 工作流流转运行时服务
│   │   │   │   │   └── impl  
│   │   │   │   ├── timelimit  审批超时处理
│   │   │   │   │   └── impl
│   │   │   │   └── valid  工作流设计校验
│   │   │   │   └── AbstractCompositeFlowHandler.java  工作流流转处理抽象类
│   │   │   │   └── CompositeFlowHandler.java  工作流流转处理统一入口
│   │   │   └── utils 工作流工具
├── jvs-design-job
│   ├── Dockerfile
│   ├── pom.xml
│   ├── readme.md
├── jvs-design-mgr
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src
│   │   ├── JvsDesignApplication.java
│   │   ├── chart
│   │   ├── component
│   │   ├── crud
│   │   ├── data
│   │   ├── external
│   │   ├── filter
│   │   ├── notice
│   │   ├── platform
│   │   ├── project
│   │   ├── report
│   │   ├── rule
│   │   ├── screen
│   │   ├── tenant
│   │   ├── use
│   │   └── workflow
├── jvs-function
│   ├── jvs-function-base            (基础节点)
│   ├── jvs-function-data            (数据节点)
│   ├── jvs-function-dingding-plug            (钉钉节点)
│   ├── jvs-function-discriminate            (脱敏节点)
│   ├── jvs-function-encryption            (加密节点)
│   ├── jvs-function-service            (服务节点)
│   ├── jvs-function-system            (系统节点)   （如获取用户节点，通过token转换为用户信息）
│   ├── jvs-function-tools             (逻辑工具类)
│   ├── pom.xml
├── jvs-function-annotation
│   ├── annotations
│   │   └── Rule.java              (逻辑声明注解) 用于标记节点， 方法，参数 等的规范，其它服务依赖此包可以进行节点注册，声明是否支持逻辑编排
├── config
│   └── RuleSpringContextUtil.java   初始化项目时扫描是否包含@Rule注解判断是否是扩展服务
└── entity
│   └── enums
│   │   ├── ClassType.java    数据类型枚举
│   │   ├── InputType.java    控件类型与前端显示一致
│   │   ├── RuleGroup.java    逻辑节点的分组
│   │   ├── TestShowEnum.java   测试执行的预览结果类型
│   │   └── type    特殊文件对象
│   ├── annotations
│   └── Rule.java
├── config
│   └── RuleSpringContextUtil.java
├── jvs-function-common
│   ├── pom.xml
│   │   ├── annotations      （注解声明）
│   │   │   ├── Inspect.java  (逻辑自定义校验注解)
│   │   │   ├── ParameterValue.java   (逻辑参数注解)
│   │   │   ├── SelectOption.java   ( 逻辑选择值注解)
│   │   │   └── SelectOptionField.java   (逻辑选择字段注解)
│   │   ├── common
│   │   │   ├── DefaultValueParameter.java  (默认值接口)
│   │   │   ├── LinkFieldSelected.java    (关联选择值)
│   │   │   ├── OptionsDto.java  (动态选择值)
│   │   │   ├── ParameterOption.java   (参数选择值)
│   │   │   ├── ParameterSelected.java   (属性参数选择项)
│   │   │   └── RuleElementVo.java    (逻辑字段属性对象)
│   │   ├── config
│   │   │   └── SystemInit.java   (逻辑核心初始化模块,或方法值)
│   │   ├── cons  (自定义扩展显示,目前未使用)
│   │   │   ├── CEEnum.java
│   │   │   └── SHEnum.java
│   │   ├── constant   (逻辑常量)
│   │   │   └── RuleConstant.java
│   │   ├── dto
│   │   │   ├── BindBaseBodyDto.java   
│   │   │   ├── BindBaseDto.java
│   │   │   ├── BindConditionsDto.java       
│   │   │   ├── BindDto.java             变量绑定，未启用
│   │   │   ├── LinkTypeEnum.java    关联枚举类型
│   │   │   ├── OptionsType.java     选项值的类型
│   │   │   ├── RuleFunctionDto.java    节点的属性对象
│   │   │   └── RuleFunctionDtoParameter.java     (这个方法的入参解释)
│   │   ├── entity
│   │   │   └── enums
│   │   ├── function
│   │   │   ├── BaseCustomFunctionInterface.java   (所有节点的抽象类,执行,校验,结构解析)
│   │   ├── mapper
│   │   ├── po
│   │   │   └── RuleOptionPo.java     自定义选择项的设置数据对象
│   │   └── utils
│   │       ├── RuleElementUtils.java
│   │       └── TaskLogUtil.java      (逻辑工具包执行)
├── jvs-function-run
│   ├── pom.xml
│   │   ├── ergodic  循环容器节点
│   │   ├── error  消息提示节点
│   │   └── utils
│   │   │   ├── dto    用于存储运行中的节点对象
│   │   │   └── html   画布的解析对象，与界面显示的组件一致
│   │   └── ExternalService.java    用于执行扩展的接口
└── pom.xml



~~~
