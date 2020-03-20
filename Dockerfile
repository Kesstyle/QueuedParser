# Pushed as kesstyle/keshtml:consulled

FROM openjdk:8

MAINTAINER "Siarhei Kavalchuk" "kess@tut.by"

EXPOSE 8080/tcp
EXPOSE 8090/tcp
EXPOSE 8443/tcp
EXPOSE 8787/tcp

ENV JAR_NAME queuedParser-1.0.0.jar

ENV APP_PROPS application.properties
ENV TARGET /parser
ENV JAVA_OPTS="-server -verbose:gc -Xms128m -Xmx256m -XX:MetaspaceSize=256m -XX:+UseConcMarkSweepGC"

RUN cd /
RUN mkdir parser

COPY $JAR_NAME $TARGET
COPY $APP_PROPS $TARGET

RUN addgroup --system kes_group && adduser --system kes && adduser kes kes_group
RUN chown kes:kes_group $TARGET
RUN chown kes:kes_group $TARGET/$JAR_NAME
RUN chown kes:kes_group $TARGET/$APP_PROPS
RUN apt-get update && apt-get upgrade && apt-get install sudo && apt-get install nano

USER kes

CMD java -jar $TARGET/$JAR_NAME --spring.config.location=$TARGET/$APP_PROPS
