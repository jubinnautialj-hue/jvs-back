# 使用aws-java-sdk-s3上传文件

提供了不同文件上传,
base64上传
分片上传
根据文件信息获取相关文件链接
jvs-public公共桶获取的链接将不提供网关前缀
如:
`/jvs-public/xxx/xxx.png`
如果是有业务关联需要获取是全链接
如:
`http://www.xxx.xxx/aaa.png?linke`


整合 AWS S3协议 OSS功能 支持 七牛、阿里、Minio等一切支持S3协议的云厂商
配置文件使用`OssProperties`文件进行配置, 建议使用application.yml公共配置文件,所有项目都使用同一样配置文件.

在接口`cn.bctools.oss.service.DataInterface`中实现了`jvs-auth-api`接口,将上传的文件数据通过feign上传到了基础数据库jvs中保存起来存储到`sys_file`表中,方便计算文件大小和不同终端使用同一个文件.避免文件重复
