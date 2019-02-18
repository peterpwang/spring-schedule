FROM centos:centos7
MAINTAINER peter.p.wang@outlook.com
RUN yum -y install java-1.8.0-openjdk-headless
COPY spring-schedule-docker/registry.jar /opt/spring-schedule/lib/
COPY spring-schedule-docker/auth.jar /opt/spring-schedule/lib/
COPY spring-schedule-docker/user.jar /opt/spring-schedule/lib/
COPY spring-schedule-docker/schedule.jar /opt/spring-schedule/lib/
COPY spring-schedule-docker/gateway.jar /opt/spring-schedule/lib/

