FROM gradle:7.4.2-jdk18

USER root

RUN mkdir -p /usr/src/cs50xiran_bot
COPY . /usr/src/cs50xiran_bot

WORKDIR /usr/src/cs50xiran_bot
RUN gradle bootJar
CMD ["java", "-jar", "build/libs/cs50.jar"]