# This Dockerfile creates a sample Docker image, using the EchoBot class as entry point; it expects that:
# - a certs folder is present at the root project folder; you can use download-files.sh script in contrib-toolbox project to populate it
# - a folder created by the Maven appassembler plugin as the current folder; invoke "mvn package" and check the target folder

FROM openjdk:8-jdk

# Runtime variables, to connect to ODP
ENV SESSIONAUTH_URL https://foundation-dev-api.symphony.com/sessionauth
ENV KEYAUTH_URL https://foundation-dev-api.symphony.com/keyauth
ENV POD_URL https://foundation-dev.symphony.com/pod
ENV AGENT_URL https://foundation-dev-api.symphony.com/agent

ADD . /bot

ADD ./certs /certs

CMD /bot/bin/RunBot org.symphonyoss.simplebot.EchoBot
