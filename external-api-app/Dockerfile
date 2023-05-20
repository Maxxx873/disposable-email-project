FROM eclipse-temurin:17-jre-alpine as builder
WORKDIR extracted
ENV JAR_FILE=*.jar
ADD target/${JAR_FILE} disposable-email-external-api.jar
RUN java -Djarmode=layertools -jar disposable-email-external-api.jar extract

FROM eclipse-temurin:17-jre-alpine
WORKDIR usr/src/app
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./
ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher"]
