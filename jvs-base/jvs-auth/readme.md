## 其它所有三方系统登录的入口

支持多种方式登陆,根据系统配置逻辑判断是否有哪些登陆方式,帐号密码,手机号密码,手机短信,钉钉扫码, 微信扫码, 企业微信扫码,ldap登陆方式.

![图片alt](img.png''图片title'')


> 不同的登录方式实现了`cn.bctools.auth.login.LoginHandler`，微信扫码、企微、钉钉、ldap等等
其它方式登录方式默认会走这个接口实现，通过获取配置信息获取对应的三方认证信息`otherLoginUserInfoComponent`

* DdInsideLoginHandler 钉钉应用内部登录
* DdScanLoginHandler 钉钉扫码登录
* InsideLoginHandler 内部登录
* PhoneLoginHandler 手机号登录
* RegisterLoginHandler 注册自动登录
* WxEnterpriseHandler 企微应用内部登录
* WxAppLoginHandler 小程序登录
* WxEnterpriseWebHandler 企微登录
* WxLoginHandler 微信开放平台登录
* WxOfficialAccountsLoginHandler 公众号扫码关注登录
* otherLoginUserInfoComponent 其它登录方式，justAuth 登录方式，可直接在界面上进行添加配置

### `OtherAuthenticationProvider`

* 根据 clientId 获取 client信息
* 根据用户名获取用户信息，并判断是否锁定
* 获取帐号密码判断是否是正确
* 其它登录方式通过调用`loadUserByOtherAuth`获取用户信息，如果默认没有三方用户，是否自动注册 ，通过`OtherLoginConfig`配置生效

### 根据不同类型获取用户并组装用户的详细信息，包含权限信息。和租户信息`UserDetailsServiceImpl`

- loadUserByUsername  根据用户名获取相关信息

- loadUserByOtherAuth  根据不同登录类型获取对应的用户租户权限信息
