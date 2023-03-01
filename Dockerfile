FROM azul/zulu-openjdk-alpine:19 as builder
ARG JAR_FILE=source-downloader-core/build/libs/source-downloader-core-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM azul/zulu-openjdk-alpine:19
VOLUME /tmp

ENV JAVA_OPTS="-Xmx512m -Xms64m"
ENV SPRINGBOOT_OPTS=""
ENV TZ=Asia/Shanghai
ENV SOURCE_DOWNLOADER_DATA_LOCATION=/app/data/
ENV SOURCE_DOWNLOADER_PLUGIN_LOCATION=/app/plugins/

RUN mkdir -p /app/data /app/plugins /app/lib
RUN touch /app/data/config.yaml

WORKDIR /app

COPY --from=builder dependencies/BOOT-INF /app
COPY --from=builder snapshot-dependencies /app
COPY --from=builder application/BOOT-INF/lib /app/lib
COPY --from=builder application/BOOT-INF/classes /app

ENTRYPOINT java -cp ".:/app/lib/*:/app/plugins/*" $JAVA_OPTS xyz.shoaky.sourcedownloader.SourceDownloaderApplication $SPRINGBOOT_OPTS