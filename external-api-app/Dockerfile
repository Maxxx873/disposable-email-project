FROM eclipse-temurin:17-jre-alpine as builder
RUN mkdir /app

COPY disposable-email-external-api.jar /app/disposable-email-external-api.jar

WORKDIR /app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "disposable-email-external-api.jar.jar"]
