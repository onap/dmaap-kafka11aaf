FROM alpine:3.7

ENV LANG C.UTF-8

RUN echo "http://dl-cdn.alpinelinux.org/alpine/edge/testing" >> /etc/apk/repositories
RUN { \
		echo '#!/bin/sh'; \
		echo 'set -e'; \
		echo; \
		echo 'dirname "$(dirname "$(readlink -f "$(which javac || which java)")")"'; \
	} > /usr/local/bin/docker-java-home \
	&& chmod +x /usr/local/bin/docker-java-home
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk/jre
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin

ENV JAVA_VERSION 8u212
ENV JAVA_ALPINE_VERSION 8.212.04-r0

RUN set -x \
	&& apk add --no-cache \
		openjdk8-jre="$JAVA_ALPINE_VERSION" \
	&& [ "$JAVA_HOME" = "$(docker-java-home)" ]

ARG kafka_version=1.1.1
ARG scala_version=2.12

RUN apk add --no-cache unzip wget docker curl jq coreutils libstdc++ ca-certificates bash java-cacerts

ENV KAFKA_VERSION=$kafka_version SCALA_VERSION=$scala_version

COPY download-kafka.sh \
     kafka_server_jaas.conf \
     org.onap.dmaap.mr.trust.jks \
     org.onap.dmaap.mr.p12 \
     org.onap.dmaap.mr.keyfile \
     cadi.properties \
     kafka11aaf-jar-with-dependencies.jar \
     dmaapMMAgent.jar \
     kafka-run-class.sh \
     /tmp/

COPY mmagent.config \
     consumer.properties \
     producer.properties \
     /opt/etc/

RUN chmod a+x /tmp/download-kafka.sh && sync && /tmp/download-kafka.sh && tar xfz /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz -C /opt && rm /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz && ln -s /opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} /opt/kafka

VOLUME ["/kafka"]

ENV KAFKA_HOME /opt/kafka
ENV PATH ${PATH}:${KAFKA_HOME}/bin

COPY start-kafka.sh \
     broker-list.sh \
     create-topics.sh \
     start-kafkaOrMirrorMaker.sh \
     start-mirrormaker.sh \
     /usr/bin/

RUN mkdir /opt/logs
RUN touch /opt/logs/mmagent.log
# The scripts need to have executable permission
RUN chmod a+x /usr/bin/start-kafka.sh && \
    chmod a+x /usr/bin/broker-list.sh && \
    chmod a+x /usr/bin/start-kafkaOrMirrorMaker.sh && \
    chmod a+x /usr/bin/start-mirrormaker.sh && \
    chmod a+x /usr/bin/create-topics.sh
# Use "exec" form so that it runs as PID 1 (useful for graceful shutdown)
CMD ["start-kafkaOrMirrorMaker.sh"]

RUN addgroup -S -g 1000  mrkafka  \
    && adduser -S  -u 1000 mrkafka  mrkafka \
    && chown -R mrkafka:mrkafka  /opt/kafka/ /opt/logs/ /opt/etc/ /kafka/  /usr/bin/ /tmp/

USER mrkafka
