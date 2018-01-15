# Symphony Java Sample Bots

[![Build Status](https://travis-ci.org/symphonyoss/symphony-java-sample-bots.svg)](https://travis-ci.org/symphonyoss/symphony-java-sample-bots)
[![Open Issues](https://img.shields.io/github/issues/symphonyoss/symphony-java-sample-bots.svg)](https://github.com/symphonyoss/symphony-java-sample-bots/issues)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/symphonyoss/symphony-java-sample-bots.svg)](http://isitmaintained.com/project/symphonyoss/symphony-java-sample-bots "Average time to resolve an issue")
[![License](https://img.shields.io/github/license/symphonyoss/symphony-java-sample-bots.svg)](https://github.com/symphonyoss/symphony-java-sample-bots/blob/master/LICENSE)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/990/badge)](https://bestpractices.coreinfrastructure.org/projects/990)
[![Validation Status](https://scan.coverity.com/projects/10072/badge.svg)](https://scan.coverity.com/projects/symphonyoss-symphony-java-sample-bots)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bhttps%3A%2F%2Fgithub.com%2Fsymphonyoss%2Fsymphony-java-sample-bots.svg?size=small)](https://app.fossa.io/reports/ae98d965-2431-4e7f-84bd-40d67678014c)

A series of sample Java bots that use the [symphony-java-client](https://github.com/symphonyoss/symphony-java-client/) (SJC) to interact with the Symphony platform. The main goal of this repository is to help developers moving their first steps with SJC, including the initial and minimum Maven project setup.

The code structure is intentionally kept simple, to improve code readability; no advanced use cases will be hosted in
 this project, since SJC already includes a [long collection of examples](https://github.com/symphonyoss/symphony-java-client/tree/develop/examples)

Below are listed the 4 sample bots currently hosted; 2 bots (`HelloWorldBot` and `RssBot`) run and terminate, while 2
 others (`EchoBot` and `StockInfoBot`) run in background continuously and respond to messages under certain conditions.

[Browse the code](src/main/java/org/symphonyoss/samples) to check the differences.

- Hello World Bot - `org.symphonyoss.samples.HelloWorldBot`; sends a hello world message to a given Symphony user (specified via `receiver.email` in `symphony.properties`) in a 1:1 chat, then terminates
- Echo Bot - `org.symphonyoss.samples.EchoBot`; listens and posts back messages on 1:1 and group Symphony chats
- Stock Info Bot - `org.symphonyoss.samples.StockInfoBot`; listens to 1:1 and group Symphony chats, checks messages for cashtags and posts related data extracted from [Yahoo Finance API](http://financequotes-api.com/)
- RSS Bot - `org.symphonyoss.samples.RssBot`; fetches RSS feed data from given url (`rss.url` in `symphony.properties`) and sends some (`rss.limit` in `symphony.properties`) of them to a given Symphony user (`user.email` in `symphony.properties`) in a 1:1 chat.

## Project setup
Follow these instructions to get started with this project and run your first java application using the Symphony Java client.

1. Make sure [Apache Maven 3.x](maven.apache.org) is installed in your workstation; run `which mvn` on your console
to check
2. Clone this repo - `git clone https://github.com/symphonyoss/symphony-java-sample-bots.git ; cd
symphony-java-sample-bots`
3. Create a `symphony.properties` file in the project root - `cp symphony.properties.sample symphony.properties`
4. Follow the instructions below to put the right configuration

## Bot configuration
Open `symphony.properties` and edit the properties documented below.

### Symphony API endpoints
```
sessionauth.url=https://foundation-dev-api.symphony.com/sessionauth
keyauth.url=https://foundation-dev-api.symphony.com/keyauth
pod.url=https://foundation-dev.symphony.com/pod
agent.url=https://foundation-dev.symphony.com/agent
```
The Symphony API endpoints, defaulting to the [Foundation Developer Pod](https://symphonyoss.atlassian.net/wiki/display/FM/Foundation+Open+Developer+Platform) values; make sure that you have access to these endpoints,
using `curl` or similar commands.

### SSL endpoints truststore
```
truststore.file=/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/security/cacerts
truststore.password=changeit
```
The certificate truststore to validate SSL certificates of the Symphony API endpoints; if the server is using
certificates that are not included in the default JVM bundle, you can specify the location of a custom truststore to
use.

Make sure to locate the `cacert` file of your JVM; in OSX, the command is `$(/usr/libexec/java_home)
/jre/lib/security/cacerts`, check [this article](https://stackoverflow.com/a/11937940) for other platforms.

### Symphony service account
```
user.cert.file=./certs/bot.user.p12
user.cert.password=changeit
user.email=bot.user@YOURNAME.com
```
These properties identify the Symphony service account that will impersonate the bot; to authenticate, it needs a P12
 certificate released by the Symphony pod administrator (and its related password).

If you don't have a Symphony pod, you can apply for a [14 day trial of the Foundation Developer Pod](symphony
.foundation/odp).

To validate your p12 certificate, try `openssl pkcs12 -info -in <file-name>.p12`.

### Running behind an HTTP proxy
The bot configuration allows to define a `proxy.url` parameter that will configure SJC accordingly; the code is part
of [Utils.java](src/main/java/org/symphonyoss/Utils.java) and relies on `jersey-apache-connector-2.23.1` and `javax.ws
.rs-api-2.1` libraries, defined in `pom.xml`

### Other bot configurations

```
receiver.email=your.name@YOURNAME.com
```
Specifies the email of the Symphony user that should receive the message; it is only used by `HelloWorldBot` and
`RssBot`.

```
rss.url=https://twitrss.me/twitter_user_to_rss/?user=symphonyoss
rss.limit=3
```
Specifies the source and limit of the RSS feed used by `RssBot`

## Building the project
Simply type `mvn package` and a uberjar will be created in the `./target` folder.

## Running the Bots
Assuming the Maven is executed, you can run each sample bot using the following Java command:
```
export SYMPHONY_CONFIG_FILE=symphony.properties
java -Xmx1024m -classpath target/symphony-java-sample-bots-0.9.1-SNAPSHOT.jar org.symphonyoss.samples.HelloWorldBot
```
You can replace `HelloWorldBot` with the other samples mentioned before.
For OSX/Linux users, a [run-bot.sh](run-bot.sh) is provided.

If you're running on Windows, you should use `set` instead of `export`.

If you're using Java 9 (check with `java -version`), please add `--add-modules java.activation` right after the `java` element in the commandline reported above.

## Integration testing
This project ships with [EchoBotIT](src/test/java/org/symphonyoss/samples/EchoBotIT.java), a simple example of integration testing using the Symphony Java client.

To configure it, you must create a `symphony.properties.it` configuration file (checkout [symphony.properties.it
.sample](symphony.properties.it.sample)), which will be used to instanciate a second client that listens to the
sender's messages.

Make sure that `user.email` in `symphony.properties.it` matches with `sender.email` defined in `symphony.properties`.

To run it, simply type:
```
export SYMPHONY_CONFIG_FILE=symphony.properties.it
mvn clean install -Pintegration-testing
```

## Dependencies
This project uses the following libraries:
- [Symphony Java Client](https://github.com/symphonyoss/symphony-java-client)
- [Rome](https://rometools.github.io/rome/) (a Java framework for RSS and Atom feeds)
- [Quotes API for Yahoo Finance](http://financequotes-api.com/)

## Contribute
Please read our [Contribution guidelines](https://github.com/symphonyoss/symphony-java-sample-bots/blob/develop/.github/CONTRIBUTING.md) and access our [issue tracker on Github](https://github.com/symphonyoss/symphony-java-sample-bots/issues).

## Project team
- Maurizio (maoo) Pillitu - Devops Director at the Symphony Software Foundation ; Project leader, Administrator and main developer of the project
- Frank (ftbb) Tarsillo - MD at IHS MarkIT ; Administrator and support developer of the project; he's also the Project leader of the [Symphony Java Client](github.com/symphonyoss/symphony-java-client)

All Administrators can:
- Access to the project build settings (on [Travis CI](https://travis-ci.org/symphonyoss/symphony-java-sample-bots))
- Access SonarCloud, Coverity, WhiteSource (or any other reporting system) to manage authentication keys connected with CI build
- Deploy artifacts (nightly build snapshots) on Sonatype and (releases on) Maven Central
The OpenShift Online environment, used for Continuous Delivery (against the Symphony Foundation Developer pod), can is managed by the [Foundation Infra team](infra@symphony.foundation)

## Governance model
This project is largely self-governed; to know more, please read on https://symphonyoss.atlassian.net/wiki/display/FM/Project+Governance
