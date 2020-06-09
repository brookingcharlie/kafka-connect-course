# Demo 3: Twitter source in distributed mode

## Why the connector config includes a "topics" property

Got a 500 error posting JSON config to the Kafka Connect REST API property:
"Must configure one of topics or topics.regex".
So I added "topics" alongside "topic" and it worked.
