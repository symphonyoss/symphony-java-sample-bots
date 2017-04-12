# Symphony Java Sample Bots

[![Symphony Software Foundation - Long Term Maintenance](https://cdn.rawgit.com/symphonyoss/contrib-toolbox/master/images/ssf-badge-long-term-maintenance.svg)](https://symphonyoss.atlassian.net/wiki/display/FM/Long+Term+Maintenance)
[![Build Status](https://travis-ci.org/symphonyoss/symphony-java-sample-bots.svg)](https://travis-ci.org/symphonyoss/symphony-java-sample-bots)
[![Dependencies](https://www.versioneye.com/user/projects/57cada12939fc60037ebd03c/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/57cada12939fc60037ebd03c)
[![Validation Status](https://scan.coverity.com/projects/10072/badge.svg)](https://scan.coverity.com/projects/symphonyoss-symphony-java-sample-bots)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bhttps%3A%2F%2Fgithub.com%2Fsymphonyoss%2Fsymphony-java-sample-bots.svg?size=small)](https://app.fossa.io/reports/ae98d965-2431-4e7f-84bd-40d67678014c)

A series of sample Java bots that use the [symphony-java-client](https://github.com/symphonyoss/symphony-java-client/) to interact with the Symphony platform.

## Hello World Bot
This bot says hello world to a given Symphony user (specified via `-Dreceiver.user.email`), then terminates.

## Echo Bot
This bot initiates a chat with a given Symphony user (specified via `-sender.user.email`), and echoes back every message in that chat.  Terminates automatically after 5 minutes.

## Stock Info Bot
This bot initiates a chat with a given Symphony user (specified via `-Dreceiver.user.email`), looks for cashtags in any messages in that chat, then responds with a message containing information on those stocks (obtained from the free [Yahoo Finance API](http://financequotes-api.com/), which is 20 minutes delayed).  Terminates automatically after 5 minutes.

## RSS Bot
This bot fetches feed items from given url (`-Drss.url`) and sends an X amount (`-Drss.limit`) of them to a given Symphony user (`-Dreceiver.user.email`); check the other parameters needed in the following example.

The java command runs and exit, there is no daemon running and waiting for incoming messages; for more complex bots, checkout the [symphony-java-client-examples](https://github.com/symphonyoss/symphony-java-client/tree/develop/symphony-client-examples)

## Running the Bots
- Obtain the coordinates (URLs) of your Symphony pod and agent from your Symphony administrator.  If you wish to use the Foundation's Open Developer Platform (ODP) instead, see [this link](https://symphonyoss.atlassian.net/wiki/display/FM/Foundation+Open+Developer+Platform)
- Obtain a service account certificate from your Symphony administrator (you should have already received this from the Foundation if using the ODP).
- Generate an "empty" Java truststore ([this StackOverflow post](http://stackoverflow.com/questions/6340918/trust-store-vs-key-store-creating-with-keytool) may help with this step).
  - Note that Java doesn't support empty truststores - they have to contain at least one public certificate.  This could be a dummy certificate, one from your own organisation, or one from a certificate authority you trust, for example.
  - You can also copy the default Java truststore (`$JAVA_HOME/jre/lib/security/cacerts`, password `changeit`) and use that as the base truststore for these bots.
- Checkout and build the project:
```
git clone https://github.com/symphonyoss/symphony-java-sample-bots.git
cd symphony-java-sample-bots
mvn clean package
```
- Copy [`env.sh.sample`](https://github.com/symphonyoss/symphony-java-sample-bots/blob/master/env.sh.sample) to `env.sh`
- Make `env.sh` executable
```
chmod u+x env.sh
```
- Review and edit the [relevant configuration settings in `env.sh`](https://github.com/symphonyoss/symphony-java-sample-bots/blob/master/env.sh.sample) to match the information and certificates obtained above.
  - `RECEIVER_USER_EMAIL` should be set to your email address as registered in the pod you're using (all of the sample bots initiate a conversation with the user identified by this email address - you won't see anything if this is not your registered email address)
- Run `run-bot.sh`, providing the fully qualified classname of the bot you wish to run. e.g.
```
./run-bot.sh org.symphonyoss.simplebot.HelloWorldBot
```
Type `./run-bot.sh` to check how to run the other bot samples.

## Integration testing
This project ships with [EchoBotIT](src/test/java/org/symphonyoss/simplebot/EchoBotIT.java), a simple example of integration testing using the Symphony Java client.

The test runs through the following steps:
1. instanciates the [EchoBot](src/main/java/org/symphonyoss/simplebot/EchoBot.java) using one `SymphonyClient` instance (the bot user)
2. sends a message from another `SymphonyClient` (the sender user), to the bot user (email)
3. registers a `ChatListener` on the sender user `SymphonyClient` and waits 10 seconds for the message to come back (as effect of the echo)
4. asserts that the message have been received back and is the same message previously sent

### Prerequisites
To run this test, you must:
1. Create `./certs` folder containing `server.truststore`, a `bot.p12` and a `sender.p12` file
2. Edit `env.sh` and adjust Symphony endpoints and certificate file locations

### Run IT
Just run `./run-it.sh` from the project root; the script will invoke Maven, specifically the Maven Failsafe Plugin

The available sample bots are:
- Hello World Bot: `org.symphonyoss.simplebot.HelloWorldBot`
- Echo Bot: `org.symphonyoss.simplebot.EchoBot`
- Stock Info Bot: `org.symphonyoss.simplebot.StockInfoBot`
- RSS Bot: `org.symphonyoss.simplebot.RssBot`

## Dependencies
This project uses the following libraries:
- [Symphony Java Client](https://github.com/symphonyoss/symphony-java-client)
- [Rome](https://rometools.github.io/rome/) (a Java framework for RSS and Atom feeds)
- [Quotes API for Yahoo Finance](http://financequotes-api.com/)

## Roadmap
- [ ] Separate out main() function and parameter handling from individual bot classes, and allow bot impl to be specified via command line arg - HIGH PRIORITY
- [ ] Exception handling
- [ ] Busy wait logic and command-based bot interaction (check symphony-java-client listeners)
