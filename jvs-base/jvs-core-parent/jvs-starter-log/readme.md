### aop 日志拦截

> 用于记录在 controller 中方法名和执行时间\ip等等

#### 目录结构
```
├── jvs-starter-log            (基础公共包)
│   ├── annotation
│   │   ├── Log.java       (Controller 日志注解)
│   │   ├── LogCallBack.java   (Aop回调函数)
│   │   └── LogIgnore.java      
│   ├── aspect
│   │   └── SysLogAspect.java  (日志环绕方法)
│   ├── config
│   │   └── LogMqConfig.java   (日志mq保存)
│   ├── po
│   │   └── LogPo.java   ( 日志对象)
│   └── utils
│       └── IpUtils.java (ip获取工具)
```
