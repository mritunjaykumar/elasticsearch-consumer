FROM openjdk:8-jdk-alpine

ARG APP_DIR=/usr/src/apps/

COPY ./target/elasticsearch-consumer-0.0.1-SNAPSHOT.jar ${APP_DIR}/kafka-es-consumer.jar
WORKDIR ${APP_DIR}
EXPOSE 8080

ENTRYPOINT ["java","-jar","./kafka-es-consumer.jar"]