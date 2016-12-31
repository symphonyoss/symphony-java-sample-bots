FROM openjdk:8-jdk

ENV project_name symphony-java-sample-bots
ENV project_version 0.9.0-SNAPSHOT
ENV project_path ${project_name}-${project_version}

WORKDIR /opt

ADD /target/${project_path}-appassembler.zip assembler.zip

RUN unzip assembler.zip

ENTRYPOINT ["${project_path}/bin/RunBot","org.symphonyoss.simplebot.EchoBot"]
