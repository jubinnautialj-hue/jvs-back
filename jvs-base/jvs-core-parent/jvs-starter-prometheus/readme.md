# 服务监控

Prometheus 
需要自行搭建

sky 接入方式

准备

在Dockerfile基础form使用`FROM registry.cn-hangzhou.aliyuncs.com/glg/sky-agent:8.8.0`

使用Skywalking的时候，并没有修改程序中任何一行 Java 代码，这里便使用到了 Java Agent 技术，我们接下来展开对Java Agent 技术的学习。

1、阿里云接入方式
阿里云产品`可观测链路 OpenTelemetry 版`  配置地址 https://tracing-analysis.console.aliyun.com/

2、自行搭建sky
使用docker-compose-sky.yml 启动服务
