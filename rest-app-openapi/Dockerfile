FROM openjdk:17-oracle
WORKDIR usr/src/app
COPY target/rest-app-openapi-0.0.1-SNAPSHOT.jar rest-app-openapi.jar
ENTRYPOINT ["java","-jar","rest-app-openapi.jar"]