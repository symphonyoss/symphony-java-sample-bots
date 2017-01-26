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

# DNS debugging
# RUN echo "104.16.104.33	foundation-dev.symphony.com" >> /etc/hosts
# RUN echo "54.152.95.26	foundation-dev-api.symphony.com" >> /etc/hosts

# Certs are now managed via volumes
# CMD curl -s https://raw.githubusercontent.com/symphonyoss/contrib-toolbox/master/scripts/download-files.sh | bash

CMD /bot/bin/RunBot org.symphonyoss.simplebot.EchoBot
