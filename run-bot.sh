#!/bin/sh

. ./env.sh

if [ -z "$1" ]; then
  echo "Please run one of the following commands:"
  echo "./run-bot.sh org.symphonyoss.simplebot.HelloWorldBot"
  echo "./run-bot.sh org.symphonyoss.simplebot.EchoBot"
  echo "./run-bot.sh org.symphonyoss.simplebot.StockInfoBot"
  echo "./run-bot.sh org.symphonyoss.simplebot.RssBot"
  exit -1
fi

mvn package
cd target/symphony-java-sample-bots-0.9.0-SNAPSHOT
./bin/RunBot $1
