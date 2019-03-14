#########
#  ============LICENSE_START====================================================
#  org.onap.dmaap
#  ===========================================================================
#  Copyright © 2017 AT&T Intellectual Property. All rights reserved.
#  Modifications Copyright (C) 2018 Nokia. All rights reserved.
#  ===========================================================================
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#  ============LICENSE_END====================================================
#
FROM openjdk:8-alpine

MAINTAINER DMAAP Team

ARG kafka_version=0.11.0.1
ARG scala_version=2.12

VOLUME ["/kafka"]

ENV KAFKA_VERSION $kafka_version
ENV SCALA_VERSION $scala_version
ENV KAFKA_HOME /opt/kafka
ENV PATH ${PATH}:${KAFKA_HOME}/bin

COPY broker-list.sh \
    create-topics.sh \
    start-kafka.sh \
    start-kafkaOrMirrorMaker.sh \
    start-mirrormaker.sh \
    /usr/bin/ 

COPY mmagent.config \
    consumer.properties \
    producer.properties \
    /opt/etc/

COPY cadi.properties \
    download-kafka.sh \
    kafka_server_jaas.conf \
    kafka-run-class.sh \
    keyfilenew \
    org.onap.dmaap.mr.p12 \
    truststoreONAPall.jks \
    kafka11aaf-jar-with-dependencies.jar \
    dmaapMMAgent.jar \
    /tmp/

RUN apk add --update bash unzip wget curl docker jq coreutils && \
    chmod a+x /usr/bin/start-kafka.sh && \
    chmod a+x /usr/bin/broker-list.sh && \
    chmod a+x /usr/bin/start-kafkaOrMirrorMaker.sh && \
    chmod a+x /usr/bin/start-mirrormaker.sh && \
    chmod a+x /usr/bin/create-topics.sh && \
    chmod a+x /tmp/download-kafka.sh && \
    sync && \
    /tmp/download-kafka.sh && \
    tar xfz /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz -C /opt && \
    rm /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz && \
    ln -s /opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} /opt/kafka
    

WORKDIR /usr/bin
CMD ["start-kafkaOrMirrorMaker.sh"]