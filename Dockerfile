FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
ENV ES_URL=""
ENV KAFKA_SERVER=""
ADD ${JAR_FILE} app.jar
EXPOSE 8091
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--kafka.server=${KAFKA_SERVER}","--elasticsearch.url=${ES_URL}"]