FROM openjdk:17-oracle
WORKDIR usr/src/app
COPY target/rest-app-openapi-0.0.1-SNAPSHOT.jar dsp-eml-prj-rest-app.jar
ENTRYPOINT ["java","-jar","dsp-eml-prj-rest-app.jar"]