# Symphony Java Sample Bots

[![Dependencies](https://www.versioneye.com/user/projects/57cada12939fc60037ebd03c/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/57cada12939fc60037ebd03c)
[![Build Status](https://travis-ci.org/symphonyoss/symphony-java-sample-bots.svg)](https://travis-ci.org/symphonyoss/symphony-java-sample-bots)
[![Validation Status](https://scan.coverity.com/projects/10072/badge.svg)](https://scan.coverity.com/projects/symphonyoss-symphony-java-sample-bots)

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

## Running the HelloWorldBot
- Checkout and build the project
```
git clone https://github.com/symphonyoss/symphony-java-sample-bots.git
cd symphony-java-sample-bots
mvn clean package
```
- `cp env.sh.sample env.sh`
- Edit the [`env.sh` variables](env.sh) locally
- Run the bash script
```
./run-bot.sh org.symphonyoss.simplebot.HelloWorldBot
```
Type `./run-bot.sh` to check how to run the other bot samples.

## Running integration testing
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

### Run
Just run `./run-it.sh` from the project root; the script will invoke Maven, specifically the Maven Failsafe Plugin

## Libraries
- [Symphony Java Client](https://github.com/symphonyoss/symphony-java-client)
- [Rome](https://rometools.github.io/rome/) (a Java framework for RSS and Atom feeds)
- [Quotes API for Yahoo Finance](http://financequotes-api.com/)

## Roadmap
- [ ] Separate out main() function and parameter handling from individual bot classes, and allow bot impl to be specified via command line arg - HIGH PRIORITY
- [ ] Exception handling
- [ ] Busy wait logic and command-based bot interaction (check symphony-java-client listeners)
