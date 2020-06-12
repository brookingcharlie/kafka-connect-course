# Demo 2: File source in distributed mode

Goal: stream a file into Kafka using a distributed cluster.

We don't have worker config in this case. We'll be using the distributed cluster already running in our `landoop/fast-data-dev` container.

Check out the connector config, `connector.properties`, to see how it's configured.

First, we create the output Kafka topic:

```
docker run --rm -it --net=host landoop/fast-data-dev:2.5.0 \
kafka-topics --zookeeper 127.0.0.1:2181 --create --topic demo-2-distributed --partitions 3 --replication-factor 1
```

We need to create the input file, `/demo-file.txt`, on the already-running Connect server since this is the environment where our worker will run:

```
$ docker-compose -f ../../docker-compose.yml exec kafka-cluster bash
root@fast-data-dev / $ touch /demo-file.txt
root@fast-data-dev / $ echo 'hi' >> /demo-file.txt
root@fast-data-dev / $ echo 'hello' >> /demo-file.txt
root@fast-data-dev / $ echo 'goodbye' >> /demo-file.txt
```

To create our connector, we post to the Connect REST API:

```
$ curl -H "Content-Type: application/json" -d @connector.json http://localhost:8083/connectors | jq
{
  "name": "file-stream-demo-distributed",
  "config": {
    "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
    "tasks.max": "1",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "true",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "true",
    "file": "/demo-file.txt",
    "topic": "demo-2-distributed",
    "name": "file-stream-demo-distributed"
  },
  "tasks": [
    {
      "connector": "file-stream-demo-distributed",
      "task": 0
    }
  ],
  "type": "source"
}
```

We can also check its status:

```
$ curl http://localhost:8083/connectors/file-stream-demo-distributed/status | jq
{
  "name": "file-stream-demo-distributed",
  "connector": {
    "state": "RUNNING",
    "worker_id": "127.0.0.1:8083"
  },
  "tasks": [
    {
      "id": 0,
      "state": "RUNNING",
      "worker_id": "127.0.0.1:8083"
    }
  ],
  "type": "source"
}
```

We can see the output record using a console consumer:

```
$ docker run --rm -it --net=host landoop/fast-data-dev:2.5.0 \
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic demo-2-distributed --from-beginning
{"schema":{"type":"string","optional":false},"payload":"hi"}
{"schema":{"type":"string","optional":false},"payload":"hello"}
{"schema":{"type":"string","optional":false},"payload":"goodbye"}
^CProcessed a total of 3 messages
```

To clean up, we can delete our connector:

```
$ curl -X DELETE http://localhost:8083/connectors/file-stream-demo-distributed
```
