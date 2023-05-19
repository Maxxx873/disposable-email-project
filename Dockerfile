FROM eclipse-temurin:17-jdk-alpine as builder
COPY . extracted
WORKDIR extracted
RUN chmod +x mvnw && ./mvnw clean package -DskipTests
ENV JAR_FILE=disposable-email-external-api-app-0.0.1-SNAPSHOT.jar
ADD ./external-api-app/target/disposable-email-external-api-app-0.0.1-SNAPSHOT.jar disposable-email-external-api.jar
RUN java -Djarmode=layertools -jar disposable-email-external-api-app-0.0.1-SNAPSHOT.jar extract

FROM eclipse-temurin:17-jre-alpine
WORKDIR usr/src/app
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./
ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher"]