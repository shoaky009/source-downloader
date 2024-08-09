FROM azul/zulu-openjdk-alpine:21-jre as builder

COPY core/build/libs/source-downloader-core-*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM azul/zulu-openjdk-alpine:21-jre
VOLUME /tmp

ENV JAVA_OPTS="-Xmx512m -Xms64m"
ENV SPRINGBOOT_OPTS="--spring.profiles.active=production"
ENV TZ=Asia/Shanghai
ENV SOURCE_DOWNLOADER_DATA_LOCATION=/app/data/
ENV SOURCE_DOWNLOADER_PLUGIN_LOCATION=/app/plugins/

RUN mkdir -p /app/data /app/plugins /app/lib
#RUN apk update && apk add tcpdump bind-tools curl

WORKDIR /app

COPY --from=builder dependencies/BOOT-INF /app
COPY --from=builder snapshot-dependencies/BOOT-INF /app
COPY --from=builder source-downloader-plugins/BOOT-INF/lib /app/lib
COPY --from=builder application/BOOT-INF/classes /app
COPY --from=builder application/META-INF /app/META-INF

ENTRYPOINT java -cp ".:/app/lib/*:/app/plugins/*:/app/plugins/lib/*" $JAVA_OPTS io.github.shoaky.sourcedownloader.SourceDownloaderApplication $SPRINGBOOT_OPTS