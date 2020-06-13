# Demo 3: Twitter source in distributed mode

Goal: gather data from Twitter in Kafka Connect distributed mode.

This exercise relies on having a Twitter developer account and setting-up an application.

Copy `connector-TEMPLATE.json` to create `connector.json` and replace these lines:

```
"twitter.consumerkey": "***YOUR_TWITTER_OAUTH_CONSUMER_KEY***",
"twitter.consumersecret": "***YOUR_TWITTER_OAUTH_CONSUMER_SECRET***",
"twitter.token": "***YOUR_TWITTER_OAUTH_ACCESS_TOKEN***",
"twitter.secret": "***YOUR_TWITTER_OAUTH_ACCESS_TOKEN_SECRET***",
```

First we create a Kafka topic that the connector will write to:

```
docker run --rm -it --net=host landoop/fast-data-dev:2.5.0 \
kafka-topics --zookeeper 127.0.0.1:2181 --create --topic demo-3-twitter --partitions 3 --replication-factor 1
```

We create the connector by posting `connector.json` to the Connect REST API.
Note that this config includes both `topic` and `topics` properties;
this is a workaround since I got a 500 error, "Must configure one of topics or topics.regex",
when I tried with just the `topic` property.

```
$ curl -H "Content-Type: application/json" -d @connector.json http://localhost:8083/connectors | jq
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  1592  100   789  100   803   1403   1428 --:--:-- --:--:-- --:--:--  2832
{
  "name": "source-twitter-distributed",
  "config": {
    "connector.class": "com.eneco.trading.kafka.connect.twitter.TwitterSourceConnector",
    "tasks.max": "1",
    "topic": "demo-3-twitter",
    "topics": "demo-3-twitter",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "true",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "true",
    "twitter.consumerkey": "...",
    "twitter.consumersecret": "...",
    "twitter.token": "...",
    "twitter.secret": "...",
    "track.terms": "programming,java,kafka,scala",
    "language": "en",
    "name": "source-twitter-distributed"
  },
  "tasks": [],
  "type": "unknown"
}
```

Since we've probably produced enough records by now, let's pause the connector:

```
$ curl -X PUT localhost:8083/connectors/source-twitter-distributed/pause
```

We can see tweets being consumed on the console:

```
$ docker run --rm -it --net=host landoop/fast-data-dev:2.5.0 \
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic demo-3-twitter --from-beginning
{"schema":{"type":"struct","fields":[{"type":"int64","optional":false,"field":"id"},{"type":"string","optional":true,"field":"created_at"},{"type":"struct","fields":[{"type":"int64","optional":false,"field":"id"},{"type":"string","optional":true,"field":"name"},{"type":"string","optional":true,"field":"screen_name"},{"type":"string","optional":true,"field":"location"},{"type":"boolean","optional":false,"field":"verified"},{"type":"int32","optional":false,"field":"friends_count"},{"type":"int32","optional":false,"field":"followers_count"},{"type":"int32","optional":false,"field":"statuses_count"}],"optional":false,"name":"com.eneco.trading.kafka.connect.twitter.User","field":"user"},{"type":"string","optional":true,"field":"text"},{"type":"string","optional":true,"field":"lang"},{"type":"boolean","optional":false,"field":"is_retweet"},{"type":"struct","fields":[{"type":"array","items":{"type":"struct","fields":[{"type":"string","optional":true,"field":"text"}],"optional":false,"name":"com.eneco.trading.kafka.connect.twitter.Hashtag"},"optional":true,"field":"hashtags"},{"type":"array","items":{"type":"struct","fields":[{"type":"string","optional":true,"field":"display_url"},{"type":"string","optional":true,"field":"expanded_url"},{"type":"int64","optional":false,"field":"id"},{"type":"string","optional":true,"field":"type"},{"type":"string","optional":true,"field":"url"}],"optional":false,"name":"com.eneco.trading.kafka.connect.twitter.Medium"},"optional":true,"field":"media"},{"type":"array","items":{"type":"struct","fields":[{"type":"string","optional":true,"field":"display_url"},{"type":"string","optional":true,"field":"expanded_url"},{"type":"string","optional":true,"field":"url"}],"optional":false,"name":"com.eneco.trading.kafka.connect.twitter.Url"},"optional":true,"field":"urls"},{"type":"array","items":{"type":"struct","fields":[{"type":"int64","optional":false,"field":"id"},{"type":"string","optional":true,"field":"name"},{"type":"string","optional":true,"field":"screen_name"}],"optional":false,"name":"com.eneco.trading.kafka.connect.twitter.UserMention"},"optional":true,"field":"user_mentions"}],"optional":false,"name":"com.eneco.trading.kafka.connect.twitter.Entities","field":"entities"}],"optional":false,"name":"com.eneco.trading.kafka.connect.twitter.Tweet"},"payload":{"id":1271599177882128384,"created_at":"2020-06-13T00:24:03.000+0000","user":{"id":2321141074,"name":"rachel \uD83D\uDD2E","screen_name":"MlCHAELCAMPAYNO","location":"BLM","verified":false,"friends_count":496,"followers_count":1368,"statuses_count":61666},"text":"RT @JoeyGuidi: CAMDEN NJ DEFUNDED THEIR POLICE DEPT IN 2012 AND REINVESTED IN EDUCATION/WORKPLACE PROGRAMMING AND THE CRIME RATE HAS DROPPE…","lang":"en","is_retweet":true,"entities":{"hashtags":[],"media":[],"urls":[],"user_mentions":[{"id":445210016,"name":"loaf of milk","screen_name":"JoeyGuidi"}]}}}
...
```

To clean up, we can delete the connector:

```
$ curl -X DELETE localhost:8083/connectors/source-twitter-distributed
```
