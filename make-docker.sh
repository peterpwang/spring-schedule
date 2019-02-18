#!/bin/bash

# make executable jars
cd Registry/ && mvn clean install spring-boot:repackage
cd .. && cp Registry/target/workerschedule-0.0.1-SNAPSHOT.jar spring-schedule-docker/registry.jar
cd AuthService/ && mvn clean install spring-boot:repackage
cd .. && cp AuthService/target/workerschedule-0.0.1-SNAPSHOT.jar spring-schedule-docker/auth.jar
cd UserService/ && mvn clean install spring-boot:repackage
cd .. && cp UserService/target/workerschedule-0.0.1-SNAPSHOT.jar spring-schedule-docker/user.jar
cd ScheduleService/ && mvn clean install spring-boot:repackage
cd .. && cp ScheduleService/target/workerschedule-0.0.1-SNAPSHOT.jar spring-schedule-docker/schedule.jar
cd Gateway/ && mvn clean install spring-boot:repackage
cd .. && cp Gateway/target/workerschedule-0.0.1-SNAPSHOT.jar spring-schedule-docker/gateway.jar

# make docker file
docker build --tag=centos:spring-schedule --rm=true .
