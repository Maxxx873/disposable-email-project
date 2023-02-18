FROM eclipse-temurin:17-jre-alpine as builder
WORKDIR extracted
ADD target/rest-app-openapi-0.0.1-SNAPSHOT.jar dsp-eml-prj-rest-app.jar
RUN java -Djarmode=layertools -jar dsp-eml-prj-rest-app.jar extract

FROM eclipse-temurin:17-jre-alpine
WORKDIR usr/src/app
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./
ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher"]