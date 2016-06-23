# Symphony Simple Bot

To test it, run
```
export FOUNDATION_CERTS=~/Desktop/certs
mvn clean package
java \
-Dkeystore.password=changeit \
-Dtruststore.password=changeit \
-Dsessionauth.url=https://foundation-api.symphony.com/sessionauth \
-Dkeyauth.url=https://foundation-api.symphony.com/keyauth \
-Dsymphony.agent.pod.url=https://agent.symphony.foundation:8444/pod \
-Dsymphony.agent.agent.url=https://agent.symphony.foundation:8444/agent \
-Dcerts.dir=$FOUNDATION_CERTS/ \
-Dtruststore.file=$FOUNDATION_CERTS/server.truststore \
-Dbot.user=bot.user1 \
-jar target/symphony-simple-bot-1.0-SNAPSHOT-jar-with-dependencies.jar
```
