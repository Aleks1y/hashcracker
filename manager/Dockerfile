FROM openjdk:17-jdk-slim

COPY /build/libs/manager.jar app.jar

ENTRYPOINT ["java", \
"-jar", "/app.jar"]