# Symphony RSS Bot

[![Build Status](https://travis-ci.org/symphonyoss/symphony-rss-bot.svg)](https://travis-ci.org/symphonyoss/symphony-rss-bot)
[![Validation Status](https://scan.coverity.com/projects/9269/badge.svg?flat=1)](https://scan.coverity.com/projects/symphonyoss-symphony-rss-bot)

This bot fetches feed items from given url (`-Drss.url`) and sends an X amount (`-Drss.limit`) of them to a given Symphony user (`-Dreceiver.user.email`); check the other parameters needed in the following example

The java command runs and exit, there is no daemon running and waiting for incoming messages to the bot user

## Example
```
export CERTS=~/certs

git clone https://github.com/symphonyoss/symphony-rss-bot.git
cd symphony-rss-bot
mvn clean package

java \
-Dkeystore.password=changeit \
-Dtruststore.password=changeit \
-Dsessionauth.url=https://foundation-api.symphony.com/sessionauth \
-Dkeyauth.url=https://foundation-api.symphony.com/keyauth \
-Dpod.url=https://foundation-api.symphony.com/pod \
-Dagent.url=https://foundation-api.symphony.com/agent \
-Drss.url=http://bit.ly/28T2riT \
-Drss.limit=10 \
-Dcerts.dir=$FCERTS/ \
-Dtruststore.file=$CERTS/server.truststore \
-Dbot.user.name=bot.user1 \
-Dreceiver.user.email=user@symphony.foundation \
-Dbot.user.email=botuser1@symphony.foundation \
-jar target/symphony-rss-bot-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Roadmap
- Exception handling
- Busy wait logic and command-based bot interaction (check symphony-java-client listeners)
