# Demo 4: ElasticSearch sink in distributed mode

Goal: write data from a Kafka Topic with multiple partitions to ElasticSearch using Kafka Connect Sink.

For this demo, you need to run the Kafka Cluster and ElasticSearch:

```
docker-compose -f ../../docker-compose.yml up -d kafka-cluster elasticsearch
```

If you'd like to see the ElasticSearch index in a Web UI, you can also run Kibana:

```
docker-compose -f ../../docker-compose.yml up -d kibana
```

We configure our connector in [connector.json](connector.json) with the following:

* `connector.class` using Confluent's ElasticSearch Sink Connector
* `tasks.max = 2` to show parallelism reading from multiple partitions.
* `topics = demo-3-twitter` to read from our Twitter-sourced topic from [demo 3](../demo-3)
* `connection.url` pointing to our ElasticSearch instance within Docker Compose
* `type.name` to specify the target type in ElasticSearch ("kafka-connect")

We create the connect using the Kafka Connect REST API:

```
$ curl -H "Content-Type: application/json" -d @connector.json http://localhost:8083/connectors | jq
{
  "name": "sink-elastic-twitter-distributed",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "tasks.max": "2",
    "topics": "demo-3-twitter",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "true",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "true",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "kafka-connect",
    "key.ignore": "true",
    "name": "sink-elastic-twitter-distributed"
  },
  "tasks": [],
  "type": "sink"
}
```

From its status, we can it's running two tasks:

```
$ curl http://localhost:8083/connectors/sink-elastic-twitter-distributed/status | jq
{
  "name": "sink-elastic-twitter-distributed",
  "connector": {
    "state": "RUNNING",
    "worker_id": "127.0.0.1:8083"
  },
  "tasks": [
    {
      "id": 0,
      "state": "RUNNING",
      "worker_id": "127.0.0.1:8083"
    },
    {
      "id": 1,
      "state": "RUNNING",
      "worker_id": "127.0.0.1:8083"
    }
  ],
  "type": "sink"
}
```

We can that the connector has created an index in ElasticSearch:

```
$ curl localhost:9200/_cat/indices
green  open .apm-custom-link         h_KIm3G9SZG48UAXyF4FHw 1 0  0 0   208b   208b
green  open .kibana_task_manager_1   AIa0j8e6Q0uycK3VOJW3og 1 0  5 2 43.3kb 43.3kb
green  open .apm-agent-configuration XMlplnk0QpWETeeRUzcoHg 1 0  0 0   208b   208b
yellow open demo-3-twitter           r1Rcpr5qTaWgIL6Y8nqekg 1 1  6 0 32.7kb 32.7kb
green  open .kibana_1                7FyENkzFRyO2H95fL_VrQg 1 0 38 0 83.8kb 83.8kb
```

Searching for documents in the `demo-3-twitter` index shows tweets:

```
$ curl localhost:9200/demo-3-twitter/_search | jq
{
  "took": 1,
  "timed_out": false,
  "_shards": {"total": 1, "successful": 1, "skipped": 0, "failed": 0},
  "hits": {
    "total": {"value": 6, "relation": "eq"},
    "max_score": 1,
    "hits": [
      {
        "_index": "demo-3-twitter",
        "_type": "kafka-connect",
        "_id": "demo-3-twitter+0+0",
        "_score": 1,
        "_source": {
          "id": 1271599177882128400,
          "created_at": "2020-06-13T00:24:03.000+0000",
          "user": {
            "id": 2321141074,
            "name": "rachel 🔮",
            "screen_name": "MlCHAELCAMPAYNO",
            "location": "BLM",
            "verified": false,
            "friends_count": 496,
            "followers_count": 1368,
            "statuses_count": 61666
          },
          "text": "RT @JoeyGuidi: CAMDEN NJ DEFUNDED THEIR POLICE DEPT IN 2012 AND REINVESTED IN EDUCATION/WORKPLACE PROGRAMMING AND THE CRIME RATE HAS DROPPE…",
          "lang": "en",
          "is_retweet": true,
          "entities": {
            "hashtags": [],
            "media": [],
            "urls": [],
            "user_mentions": [
              {
                "id": 445210016,
                "name": "loaf of milk",
                "screen_name": "JoeyGuidi"
              }
            ]
          }
        }
      },
...
```

To clean up, we can delete our connector:

```
$ curl -X DELETE http://localhost:8083/connectors/sink-elastic-twitter-distributed
```
