FROM azul/zulu-openjdk-alpine:19
VOLUME /tmp

ENV JAVA_OPTS="-Xmx512m -Xms64m"
ENV SPRINGBOOT_OPTS=""
ENV TZ=Asia/Shanghai
ENV SOURCE_DOWNLOADER_DATA_LOCATION=/app/data/

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar
RUN mkdir /app/data
WORKDIR /app

ENTRYPOINT java $JAVA_OPTS -jar app.jar $SPRINGBOOT_OPTS