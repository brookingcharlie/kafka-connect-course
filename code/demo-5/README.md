# Demo 5: PostgreSQL sink in distributed mode

Goal: write data from a Kafka Topic to a PostgreSQL database using Kafka Connect Sink.

For this demo, you need to run the Kafka Cluster and PostgreSQL:

```
docker-compose -f ../../docker-compose.yml up -d kafka-cluster postgres
```

We configure our connector in [connector.json](connector.json) with the following:

* `connector.class` using Confluent's JDBC Sink Connector
* `tasks.max = 1` since we're not showing parallelism this time
* `topics = demo-3-twitter` to read from our Twitter-sourced topic from [demo 3](../demo-3)
* `connection.{url,user,password}` pointing to our PostgreSQL instance within Docker Compose
* `insert.mode = upsert`
* `pk.mode = kafka` so the primary key is offset + partition
* `pk.fields` to include topic, partition, and offset
* `fields.whitelist` to only include `id`, `created_at`, `lang`, `is_retweet` (other are nested JSON fields, so not possible with this connector)
* `auto.create = true` so tables are created for us by connector
* `auto.evolve = true` so columns are automatically added to the table's schema

We create the connect using the Kafka Connect REST API:

```
$ curl -H "Content-Type: application/json" -d @connector.json http://localhost:8083/connectors | jq
{
  "name": "sink-postgres-twitter-distributed",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
    "tasks.max": "1",
    "topics": "demo-3-twitter",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "true",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "true",
    "connection.url": "jdbc:postgresql://postgres:5432/postgres",
    "connection.user": "postgres",
    "connection.password": "postgres",
    "insert.mode": "upsert",
    "pk.mode": "kafka",
    "pk.fields": "__connect_topic,__connect_partition,__connect_offset",
    "fields.whitelist": "id,created_at,lang,is_retweet",
    "auto.create": "true",
    "auto.evolve": "true",
    "name": "sink-postgres-twitter-distributed"
  },
  "tasks": [],
  "type": "sink"
}
```

From its status, we can it's started successfully:

```
$ curl localhost:8083/connectors/sink-postgres-twitter-distributed/status | jq
{
  "name": "sink-postgres-twitter-distributed",
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
  "type": "sink"
}
```

Let's connect to PostgreSQL to see what's in the table:

```
$ docker-compose -f ../../docker-compose.yml exec postgres psql -U postgres
psql (13beta1 (Debian 13~beta1-1.pgdg100+1))
Type "help" for help.

postgres=# \dt
             List of relations
 Schema |      Name      | Type  |  Owner
--------+----------------+-------+----------
 public | demo-3-twitter | table | postgres
(1 row)

postgres=# select * from "demo-3-twitter";
 __connect_topic | __connect_partition | __connect_offset |         id          |          created_at          | lang | is_retweet
-----------------+---------------------+------------------+---------------------+------------------------------+------+------------
 demo-3-twitter  |                   0 |                0 | 1271599177882128384 | 2020-06-13T00:24:03.000+0000 | en   | t
 demo-3-twitter  |                   0 |                1 | 1271599217199517696 | 2020-06-13T00:24:13.000+0000 | en   | f
 demo-3-twitter  |                   0 |                2 | 1271599248824393728 | 2020-06-13T00:24:20.000+0000 | en   | f
 demo-3-twitter  |                   2 |                0 | 1271599243631960064 | 2020-06-13T00:24:19.000+0000 | en   | t
 demo-3-twitter  |                   2 |                1 | 1271599265530417153 | 2020-06-13T00:24:24.000+0000 | en   | t
 demo-3-twitter  |                   1 |                0 | 1271599180876853252 | 2020-06-13T00:24:04.000+0000 | en   | t
(6 rows)

postgres=# \q
```

To clean up, we can delete our connector:

```
$ curl -X DELETE http://localhost:8083/connectors/sink-postgres-twitter-distributed
```
