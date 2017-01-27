# This Dockerfile creates a sample Docker image, using the EchoBot class as entry point
# To use it:
# - checkout this project
# - run "mvn package"
# - cd target/symphony-java-sample-bots-${version}/
# - docker build ...

# This is ignored apparently, as it's overruled by Openshift BuildConfig
# FROM ubuntu:14.04
FROM null:null

# Runtime variables, to connect to ODP
ENV SESSIONAUTH_URL https://foundation-dev-api.symphony.com/sessionauth
ENV KEYAUTH_URL https://foundation-dev-api.symphony.com/keyauth
ENV POD_URL https://foundation-dev.symphony.com/pod
ENV AGENT_URL https://foundation-dev-api.symphony.com/agent

# Mounts the target/symphony-java-sample-bots-${version}/ folder
ADD . /bot

# Runs the bot, following the appassembler folder layout
CMD /bot/bin/RunBot org.symphonyoss.simplebot.EchoBot
