#FROM java:openjdk-8-alpine
FROM openjdk:8
WORKDIR /usr/src/app
COPY ./target/*.jar ./app.jar
ENTRYPOINT ["java","-jar","./app.jar","--port=8080"]
