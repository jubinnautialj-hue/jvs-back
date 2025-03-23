# Dockerfile说明

前端docker
~~~
FROM nginx:1.20
VOLUME /tmp
ENV LANG en_US.UTF-8
COPY dist  /usr/share/nginx/html/
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
#这个地方一定要把之前的默认给替换掉，不然使用这个默认nginx的镜像会无法替换配置
#部署时,可以进行替换.将文件统一在外部管理
COPY my.conf  /etc/nginx/conf.d/default.conf
COPY nginx.conf  /etc/nginx/nginx.conf
EXPOSE 80
EXPOSE 443
~~~

后端docker

后端dockerfile集成 Skywalking
~~~
#基础镜像,支持Skywalking 可以自建,也可以支持阿里云链路服务 OpenTelemetry 服务
FROM registry.cn-hangzhou.aliyuncs.com/glg/sky-agent:8.8.0

ADD fonts /usr/share/fonts/zh_CN
RUN chmod 775 /usr/share/fonts/zh_CN

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' > /etc/timezone
ADD ./target/jvs-auth-mgr.jar /app/app.jar
ENV skyname="jvs-auth-mgr"
ENV JAVA_OPTS=""
#sky地址
ENV skyip="localhost:11800"
#凭证
ENV authentication=""
#镜像默认地址为 -javaagent:/skywalking-agent/skywalking-agent.jar
ENV skywalkingPath=""
ENV nacosAddr="cloud-nacos:8848"
ENV namespace=""
ENTRYPOINT ["sh","-c","java $skywalkingPath  -Dskywalking.agent.service_name=$skyname -Dskywalking.agent.authentication=$authentication -Dskywalking.collector.backend_service=$skyip -Dspring.cloud.nacos.discovery.server-addr=$nacosAddr -Dspring.cloud.nacos.discovery.namespace=$namespace  $JAVA_OPTS -jar /app/app.jar"]
~~~
