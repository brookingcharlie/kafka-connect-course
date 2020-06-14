# Demo 1: File source in standalone mode

Goal: use Kafka Connect in standalone mode to read a file into Kafka.

For this demo, you need to run the Kafka Cluster:

```
docker-compose -f ../../docker-compose.yml up -d kafka-cluster
```

In this directory, we have config files - `worker.properties` and `connector.properties` - for
running a standalone connector. See the comments in those files explaining each setting.

We create an empty text file, `demo-file.txt`, that the connector will read from.

```
touch demo-file.txt
```

And create the Kafka topic the connector will write to:

```
docker run --rm -it --net=host landoop/fast-data-dev:2.5.0 \
kafka-topics --zookeeper 127.0.0.1:2181 --create --topic demo-1-standalone --partitions 3 --replication-factor 1
```

To start our standalone worker, we run `connect-standalone` on a `landoop/fast-data-dev` container.
We mount our working directory as a volume so the connector can read our config files and the input file.

```
docker run --rm -it -v "$PWD":/mnt --net=host landoop/fast-data-dev:2.5.0 bash -c \
"cd /mnt && connect-standalone worker.properties connector.properties"
```

When we save lines of text to the input file,

```
echo 'hi' >> demo-file.txt
echo 'hello' >> demo-file.txt
echo 'goodbye' >> demo-file.txt
```

we can see them appear in the topic using a console consumer:

```
$ docker run --rm -it --net=host landoop/fast-data-dev:2.5.0 \
> kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic demo-1-standalone --from-beginning
"hi"
"hello"
"goodbye"
^CProcessed a total of 3 messages
```
