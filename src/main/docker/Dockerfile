FROM confluentinc/cp-kafka:6.2.0

ENV COMPONENT=kafka \
    KAFKA_USER=mrkafka \
    KAFKA_GROUP=onap

COPY org.onap.dmaap.mr.trust.jks \
     org.onap.dmaap.mr.p12 \
     org.onap.dmaap.mr.keyfile \
     /etc/${COMPONENT}/secrets/cert/

USER root

RUN userdel -r appuser && groupadd $KAFKA_GROUP && useradd $KAFKA_USER -u 1000 -G 1000,$KAFKA_GROUP

WORKDIR /home/$KAFKA_USER

COPY include/etc/confluent/docker/* /etc/confluent/docker/
RUN chmod -R +x /etc/confluent/docker \
&& mkdir -p /etc/${COMPONENT}/data /etc/${COMPONENT}/secrets \
&& chown -R $KAFKA_USER:$KAFKA_GROUP /var/lib/${COMPONENT} /etc/${COMPONENT} /etc/confluent/docker /var/log/${COMPONENT} /var/lib/${COMPONENT} /var/log/confluent

COPY kafka11aaf.jar /usr/share/java/${COMPONENT}/

USER $KAFKA_USER

EXPOSE 9092 9093

CMD ["/etc/confluent/docker/run"]