FROM openjdk:8-jre-alpine3.9

ARG kafka_version=1.1.1
ARG scala_version=2.12


RUN apk add --update unzip wget curl jq coreutils bash

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

RUN mkdir /opt/logs && \
    touch /opt/logs/mmagent.log

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
