# Demo 2: File source in distributed mode

Created the output Kafka topic:

```
kafka-topics --zookeeper 127.0.0.1:2181 --create --topic demo-2-distributed --partitions 3 --replication-factor 1
```

Created file connector using the Kafka Connect UI by pasting-in the
properties config file in this directory.

