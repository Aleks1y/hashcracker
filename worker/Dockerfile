FROM openjdk:17-jdk-slim

COPY /build/libs/worker.jar app.jar

ENTRYPOINT ["java", \
"-jar", "/app.jar"]