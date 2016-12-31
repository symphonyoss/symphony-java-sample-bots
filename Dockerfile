# This Dockerfile creates a sample Docker image, using the EchoBot class as entry point; it expects that:
# - a certs folder is present at the root project folder (you can use download-files.sh script in contrib-toolbox project to populate it)
# - a ZIP file created by the Maven appassembler plugin in the target folder; the name is expected to be "${project_path}-appassembler.zip"

FROM openjdk:8-jdk

# Project-specific variables
ENV project_name symphony-java-sample-bots
ENV project_version 0.9.0-SNAPSHOT
ENV project_path ${project_name}-${project_version}

# Runtime variables, to connect to ODP
# ENV FOUNDATION_API_URL https://foundation-dev-api.symphony.com
# ENV FOUNDATION_POD_URL https://foundation-dev.symphony.com
ENV SESSIONAUTH_URL https://foundation-dev-api.symphony.com/sessionauth
ENV KEYAUTH_URL https://foundation-dev-api.symphony.com/keyauth
ENV POD_URL https://foundation-dev.symphony.com/pod
ENV AGENT_URL https://foundation-dev-api.symphony.com/agent

ADD /target/${project_path}-appassembler.zip /opt/assembler.zip

# TODO - this should be a run, so certs don't stay in the image
ADD /certs /opt/${project_path}/certs

RUN unzip /opt/assembler.zip -d /opt

WORKDIR /opt/${project_path}

ENTRYPOINT ["./bin/RunBot org.symphonyoss.simplebot.EchoBot"]
