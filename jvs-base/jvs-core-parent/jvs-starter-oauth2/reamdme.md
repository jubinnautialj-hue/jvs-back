# 权限拦截

所以在此操作时,建议部署服务不要将业务服务端口开放,只开发网关端口即可

为加快请求验证速度,2.1.8 将默认放开全部请求,如果不通过网关请求操作,将自动放开直接获取业务数据. 后续考虑是否需要在业务中移除`security`包
所有的权限资源校验都在网关进行校验,避免浪费业务服务资源

通过拦截器获取请求的请求头,在redis中直接序列化java对象`CustomUser`进行获取当前用户

可能存在的问题
直接请求的时候api没有多租户形式,在调试阶段,需要将租户id携带上


### 目录结构


```
├── jvs-starter-oauth2            (根据token换取用户对象)
│   ├── annotation
│   │   └── EnableJvsMgrResourceServer.java   (用户信息转换声明)
│   ├── config
│   │   ├── ClientFeignConfig.java
│   │   ├── JvsAdapter.java          (用户信息转换)
│   │   ├── JvsOAuth2AuthorizationServiceImpl.java (token获取用户信息)
│   │   └── ThreadPoolProperties.java
│   ├── dto
│   │   ├── CustomUser.java         (用户对象)
│   │   └── OtherAuthenticationToken.java      
│   ├── prop
│   │   └── JvsOAuth2Property.java
│   └── utils
│       ├── AuthorityManagementUtils.java  (用户\角色\岗位获取工具)
│       └── UserCurrentUtils.java  (当前用户获取工具)
```
