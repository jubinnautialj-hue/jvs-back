FROM openjdk:8-jre-alpine
MAINTAINER jvs <www.bctools.com>

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' > /etc/timezone
ADD ./target/jvs-xxl-job-admin.jar /app/app.jar

ENV JAVA_OPTS=""
ENV nacosAddr="cloud-nacos:8848"
ENV namespace="jvs"
ENV nacosPass="nacos"
ENV nacosName="nacos"

ENTRYPOINT ["sh","-c","java -Dspring.cloud.nacos.config.password=$nacosPass -Dspring.cloud.nacos.config.username=$nacosName -Dspring.cloud.nacos.discovery.server-addr=$nacosAddr -Dspring.cloud.nacos.discovery.namespace=$namespace $JAVA_OPTS -jar /app/app.jar"]
