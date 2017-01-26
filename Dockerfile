# This Dockerfile creates a sample Docker image, using the EchoBot class as entry point
# To use it:
# - checkout this project
# - run "mvn package"
# - cd target/symphony-java-sample-bots-${version}/
# - docker build ...

# FROM openjdk:8-jdk
FROM ubuntu:14.04

# Runtime variables, to connect to ODP
ENV SESSIONAUTH_URL https://foundation-dev-api.symphony.com/sessionauth
ENV KEYAUTH_URL https://foundation-dev-api.symphony.com/keyauth
ENV POD_URL https://foundation-dev.symphony.com/pod
ENV AGENT_URL https://foundation-dev-api.symphony.com/agent

# DNS debugging
# ENV SESSIONAUTH_URL https://54.152.95.26/sessionauth
# ENV KEYAUTH_URL https://54.152.95.26/keyauth
# ENV POD_URL https://54.152.95.26/pod
# ENV AGENT_URL https://54.152.95.26/agent

# Mounts the target/symphony-java-sample-bots-${version}/ folder
ADD . /bot

# Install the python script required for "add-apt-repository"
RUN apt-get update && apt-get install -y software-properties-common

# Sets language to UTF8 : this works in pretty much all cases
ENV LANG en_US.UTF-8
RUN locale-gen $LANG

# Setup the openjdk 8 repo
RUN add-apt-repository ppa:openjdk-r/ppa

# Install java8
RUN apt-get update && apt-get install -y openjdk-8-jdk

# Setup JAVA_HOME, this is useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

# DNS debugging
CMD echo "104.16.104.33	foundation-dev.symphony.com" >> /etc/hosts
CMD echo "54.152.95.26	foundation-dev-api.symphony.com" >> /etc/hosts

# Certs are now managed via volumes
# CMD curl -s https://raw.githubusercontent.com/symphonyoss/contrib-toolbox/master/scripts/download-files.sh | bash

CMD cat /etc/hosts

CMD /bot/bin/RunBot org.symphonyoss.simplebot.EchoBot
