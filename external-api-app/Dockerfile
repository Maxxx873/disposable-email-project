FROM eclipse-temurin:17-jre-alpine as builder
WORKDIR extracted
ADD target/disposable-email-external-api-app-0.0.1-SNAPSHOT.jar disposable-email-external-api.jar
RUN java -Djarmode=layertools -jar disposable-email-external-api.jar extract

FROM eclipse-temurin:17-jre-alpine
WORKDIR usr/src/app
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./
ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher"]