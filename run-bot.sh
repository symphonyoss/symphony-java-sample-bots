#!/bin/sh

if [ -z "$1" ]; then
  echo "Please run one of the following commands:"
  echo "./run-bot.sh org.symphonyoss.samples.HelloWorldBot"
  echo "./run-bot.sh org.symphonyoss.samples.EchoBot"
  echo "./run-bot.sh org.symphonyoss.samples.StockInfoBot"
  echo "./run-bot.sh org.symphonyoss.samples.RssBot"
  exit -1
fi

export SYMPHONY_CONFIG_FILE=symphony.properties

java -Xmx1024m \
-classpath target/symphony-java-sample-bots-0.9.1-SNAPSHOT.jar "$1"
