## Disposable Email Telegram Bot

___
### Technologies used:
Java 17, Spring Boot 3, Spring Data JPA, OpenAPI Generator, Hibernate, PostgreSQL, H2, Liquibase, Telegram API.

___
### Features:
- Automatic client code generation using the WebClient

Telegram bot:
- Using the Disposable Email Project External API
- Registering customers to manage multiple email accounts

```bash
docker compose -f docker-compose.yml up
```

___
### Liquibase:
To disable Liquibase, in `application.yaml` set following properties to:
```
spring
    liquibase
    enabled: false
    
       jpa
        hibernate:
          ddl-auto: update
```
To use Liquibase, in `application.yaml` set following properties to:
```
spring
    liquibase
    enabled: true
    
       jpa
        hibernate:
          ddl-auto: none
```

Create `src/main/resources/liquibase.properties` file with own `DATABASE_USERNAME` and `DATABASE_PASSWORD` for using PostgreSQL:

```
driver=org.postgresql.Driver
referenceUrl=hibernate:spring:com.disposableemail.telegram.dao.entity?dialect=org.hibernate.dialect.PostgreSQLDialect
url=jdbc:postgresql://localhost:5432/disposableemailbot
username=DATABASE_USERNAME
password=DATABASE_PASSWORD
```

To generate a ChangeLog from an existing database:
```
mvn liquibase:generateChangeLog
```

To generate a diff ChangeLog:
```
mvn clean install liquibase:diff -DskipTests=true
```
___
### Pushing to dockerhub:
```bash
docker login
```
```bash
sudo docker build -t maxxx873/dsp-eml-prj-bot:latest -f Dockerfile .
```
```bash
docker push maxxx873/dsp-eml-prj-bot:latest
```
