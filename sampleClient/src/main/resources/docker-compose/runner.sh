#!/bin/bash

function start {
  docker compose -f scram-docker-compose.yml up -d

  until [ "$(docker inspect -f {{.State.Running}} broker)" == "true" ]; do
      sleep 1;
  done;

  echo -e "\n Creating kafka users"
  docker exec broker kafka-configs --zookeeper zookeeper:2181 --alter --add-config 'SCRAM-SHA-256=[password=broker],SCRAM-SHA-512=[password=broker]' --entity-type users --entity-name broker
  docker exec broker kafka-configs --zookeeper zookeeper:2181 --alter --add-config 'SCRAM-SHA-256=[password=client-secret],SCRAM-SHA-512=[password=client-secret]' --entity-type users --entity-name client

  echo -e "\n Creating test topic"
  docker exec broker kafka-topics --create --bootstrap-server broker:9092 --replication-factor 1 --partitions 1 --topic test-topic.1 --command-config config.properties

  echo -e "\n Listing existing topics"
  docker exec broker kafka-topics --list --bootstrap-server localhost:9092 --command-config config.properties

  echo -e "\n Adding broker to /etc/hosts"
  echo '127.0.0.1 broker' | sudo tee -a /etc/hosts
}


function stop {

  docker compose -f scram-docker-compose.yml down

  sudo sed -i.bak '/broker/d' /etc/hosts
}

function publisher {
    docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic test-topic.1 --producer.config config.properties
}

showHelp() {
cat << EOF
Usage: ./runner.sh [start|stop]

start

stop

EOF
}

while true
do
case "$1" in
start)
    start
    ;;
pub)
    publisher
    ;;
stop)
    stop
    ;;
*)
    showHelp
    shift
    break;;
esac
shift
done