# Demo 3: ElasticSearch sink in distributed mode

Goal: write data from a Kafka Topic with multiple partitions to ElasticSearch using Kafka Connect Sink.

For this demo, you need to run the Kafka Cluster and ElasticSearch:

```
docker-compose -f ../../docker-compose.yml up -d kafka-cluster elasticsearch
```
