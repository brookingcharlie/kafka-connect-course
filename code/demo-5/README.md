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
    "fields.whitelist": "id,created_at,text,lang,is_retweet",
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
$ docker run --rm -it --net=host postgres:13 psql -h localhost -U postgres
Password for user postgres:
psql (13beta1 (Debian 13~beta1-1.pgdg100+1))
Type "help" for help.

postgres=# \d
             List of relations
 Schema |      Name      | Type  |  Owner
--------+----------------+-------+----------
 public | demo-3-twitter | table | postgres
(1 row)

postgres=# \d "demo-3-twitter"
                 Table "public.demo-3-twitter"
       Column        |  Type   | Collation | Nullable | Default
---------------------+---------+-----------+----------+---------
 __connect_topic     | text    |           | not null |
 __connect_partition | integer |           | not null |
 __connect_offset    | bigint  |           | not null |
 id                  | bigint  |           | not null |
 created_at          | text    |           |          |
 text                | text    |           |          |
 lang                | text    |           |          |
 is_retweet          | boolean |           | not null |
Indexes:
    "demo-3-twitter_pkey" PRIMARY KEY, btree (__connect_topic, __connect_partition, __connect_offset)

postgres=# select __connect_topic, __connect_partition, __connect_offset, left(text, 60) from "demo-3-twitter" ;
 __connect_topic | __connect_partition | __connect_offset |                             left
-----------------+---------------------+------------------+--------------------------------------------------------------
 demo-3-twitter  |                   0 |                0 | @PDChina That's an incident in Cianjur, west java, indonesia
 demo-3-twitter  |                   0 |                1 | RT @NikkiKnight104: @JuliansRum Their mind control programmi
 demo-3-twitter  |                   0 |                2 | Physical Aesthetics is not enough. You need Mental Aesthetic
 demo-3-twitter  |                   0 |                3 | The Different Types of Programming Languages – Learn the Bas
 demo-3-twitter  |                   0 |                4 | RT @ADatainsight: The beginning of wisdom is to realized.eve
 demo-3-twitter  |                   0 |                5 | @MoiTrades @badamczewski01 It has some other upsides as well
 demo-3-twitter  |                   0 |                6 | RT @SolidaldubT: Pls support our OHT for today June 15, 2020
 demo-3-twitter  |                   0 |                7 | RT @ADatainsight: The beginning of wisdom is to realized.eve
 demo-3-twitter  |                   0 |                8 | RT @QPrayerTeam: Lord - We pray for the battlefield of the m
 demo-3-twitter  |                   0 |                9 | RT @WWG1WGAUSA: I see liberals are shaving their heads......
 demo-3-twitter  |                   2 |                0 | Concept Ideation - Level Design - Frameworks - Blueprint pro
 demo-3-twitter  |                   2 |                1 | RT @Hakin9: Cheat sheets comes in handy in those times when
 demo-3-twitter  |                   2 |                2 | RT @Hakin9: Cheat sheets comes in handy in those times when
 demo-3-twitter  |                   2 |                3 | The beginning of wisdom is to realized.every problem is a da
 demo-3-twitter  |                   2 |                4 | RT @cobrainfo1: Our highest purpose is to be free (from all
 demo-3-twitter  |                   2 |                5 | RT @ADatainsight: The beginning of wisdom is to realized.eve
 demo-3-twitter  |                   2 |                6 | RT @ADatainsight: The beginning of wisdom is to realized.eve
 demo-3-twitter  |                   2 |                7 | RT @shutupaida: we are experiencing a SERIES of lynchings an
 demo-3-twitter  |                   2 |                8 | RT @ADatainsight: The beginning of wisdom is to realized.eve
 demo-3-twitter  |                   2 |                9 | RT @VaughnHillyard: The look entering from the top of Seattl
 demo-3-twitter  |                   2 |               10 | Bumped for the “if it bleeds it leads” crowd speculating w/
 demo-3-twitter  |                   2 |               11 | RT @ADatainsight: The beginning of wisdom is to realized.eve
 demo-3-twitter  |                   1 |                0 | RT @chuckchucknock: Chuck Norris solved the Travelling Sales
 demo-3-twitter  |                   1 |                1 | RT @Hakin9: Cheat sheets comes in handy in those times when
 demo-3-twitter  |                   1 |                2 | RT @OneThyFox: i think u should follow @AnswonSeabwa because
 demo-3-twitter  |                   1 |                3 | #programming #programmer #100DaysOfCode #Java #Python #javas
 demo-3-twitter  |                   1 |                4 | RT @ADatainsight: The beginning of wisdom is to realized.eve
 demo-3-twitter  |                   1 |                5 | RT @ADatainsight: The beginning of wisdom is to realized.eve
 demo-3-twitter  |                   1 |                6 | RT @TonyHWindsor: Funding cutbacks starting to show up in pr
 demo-3-twitter  |                   1 |                7 | RT @VaughnHillyard: The look entering from the top of Seattl
 demo-3-twitter  |                   1 |                8 | RT @ADatainsight: The beginning of wisdom is to realized.eve
 demo-3-twitter  |                   1 |                9 | RT @ADatainsight: The beginning of wisdom is to realized.eve
 demo-3-twitter  |                   1 |               10 | RT @JacobHayes_: The most satisfying thing with game develop
(33 rows)

postgres=# \q
```

To clean up, we can delete our connector:

```
$ curl -X DELETE http://localhost:8083/connectors/sink-postgres-twitter-distributed
```
