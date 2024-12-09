FROM registry.cn-hangzhou.aliyuncs.com/glg/sky-agent:8.8.0

MAINTAINER jvs <www.bctools.com>

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' > /etc/timezone
ADD ./target/jvs-auth-mgr.jar /app/app.jar

ADD fonts /usr/share/fonts/zh_CN
RUN chmod 775 /usr/share/fonts/zh_CN

ENV LANG C.UTF-8
ENV skyname="jvs-auth"
ENV JAVA_OPTS=""
ENV skyip="localhost:11800"
ENV authentication=""
#镜像默认地址为 -javaagent:/skywalking-agent/skywalking-agent.jar
ENV skywalkingPath=""
ENV nacosAddr="cloud-nacos:8848"
ENV namespace=""
ENTRYPOINT ["sh","-c","java $skywalkingPath  -Dskywalking.agent.service_name=$skyname -Dskywalking.agent.authentication=$authentication -Dskywalking.collector.backend_service=$skyip -Dspring.cloud.nacos.config.password=$nacosPass -Dspring.cloud.nacos.config.username=$nacosName -Dspring.cloud.nacos.discovery.server-addr=$nacosAddr -Dspring.cloud.nacos.discovery.namespace=$namespace $JAVA_OPTS -jar /app/app.jar"]




