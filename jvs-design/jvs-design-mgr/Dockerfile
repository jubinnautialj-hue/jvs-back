FROM registry.cn-hangzhou.aliyuncs.com/glg/sky-agent:8.8.0
MAINTAINER jvs <www.bctools.com>

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' > /etc/timezone
ADD ./target/jvs-design-mgr.jar /app/app.jar

ADD fonts /usr/share/fonts/zh_CN
RUN chmod 775 /usr/share/fonts/zh_CN

ENV JAVA_OPTS=""
ENV nacosAddr="cloud-nacos:8848"
ENV namespace="jvs"
ENV nacosPass="nacos"
ENV nacosName="nacos"

ENTRYPOINT ["sh","-c","java -Dspring.cloud.nacos.config.password=$nacosPass -Dspring.cloud.nacos.config.username=$nacosName -Dspring.cloud.nacos.discovery.server-addr=$nacosAddr -Dspring.cloud.nacos.discovery.namespace=$namespace $JAVA_OPTS -jar /app/app.jar"]
