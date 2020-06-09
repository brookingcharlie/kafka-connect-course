# Demo 1: File source in standalone mode

Ran a standalone Connect worker on a new `landoop/fast-data-dev` container with
our working directory containing config files mounted as a volume.

```
$ docker run --rm -it -v "$PWD":/tutorial --net=host landoop/fast-data-dev:2.3 bash
root@fast-data-dev tutorial $ kafka-topics --zookeeper 127.0.0.1:2181 --create --topic demo-1-standalone --partitions 3 --replication-factor 1
root@fast-data-dev tutorial $ cd /tutorial/
root@fast-data-dev tutorial $ connect-standalone worker.properties file-stream-demo-standalone.properties
```
