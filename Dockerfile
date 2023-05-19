FROM eclipse-temurin:17-jdk-alpine as builder
COPY . extracted
WORKDIR extracted
RUN chmod +x mvnw && ./mvnw clean package -DskipTests
ENV JAR_FILE=*.jar
ADD ./external-api-app/target/${JAR_FILE} disposable-email-external-api.jar
RUN java -Djarmode=layertools -jar ${JAR_FILE} extract

FROM eclipse-temurin:17-jre-alpine
WORKDIR usr/src/app
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./
ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher"]