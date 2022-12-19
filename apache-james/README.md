# Apache James

#### Running Demo [Apache James](https://james.apache.org/) in memory:
```bash
docker run --network host -d -p "25:25" -p "143:143" -p "127.0.0.1:8000:8000" -v $(pwd)/apache-james/conf/webadmin.properties:/root/conf/webadmin.properties --name james-demo apache/james:demo-3.7.0
```

#### Running **Distributed Apache James server**:

Generating a keystore:
```bash
keytool -genkey -alias james -keyalg RSA -keystore conf/keystore
```
Creating network on docker for the **Apache James** environment:
```bash
docker network create --driver bridge james
```
Running third party dependencies:
```bash
docker run -d --network james -p 9042:9042 --name=cassandra cassandra:3.11.10
docker run -d --network james -p 9200:9200 --name=elasticsearch --env 'discovery.type=single-node' blacktop/elasticsearch:7.10.2
docker run -d --network james -p 5672:5672 -p 15672:15672 --name=rabbitmq rabbitmq:3.9.18-management
docker run -d --network james --env 'REMOTE_MANAGEMENT_DISABLE=1' --env 'SCALITY_ACCESS_KEY_ID=accessKey1' --env 'SCALITY_SECRET_ACCESS_KEY=secretKey1' --name=s3 zenko/cloudserver:8.2.6
```
Running **Distributed Apache James**:
```bash
docker run --network james -p 25:25 -p 143:143 -p 8000:8000 -v $(pwd)/apache-james/conf/:/root/conf/ --name james-distributed apache/james:distributed-latest
```
Running **Distributed Apache James** with docker-compose:
```bash
docker compose -f apache-james/docker-compose.yml up
```

#### Pushing to dockerhub:
```bash
docker login
```
```bash
docker build -t maxxx873/dsp-eml-prj-james:latest -f Dockerfile .
```
```bash
docker push maxxx873/dsp-eml-prj-james:latest
```

Running **Mongodb**:
```bash
docker run --name mongodb -d -p 27017:27017 mongo
```

### Web administration for James

Get the list of domains:
```bash
curl -XGET http://localhost:8000/domains
```

Create a domain:
```bash
curl -XPUT http://localhost:8000/domains/example.com
```

### Thunderbird mail configuration locally:

#### Incoming Server:
- IMAP server name: 127.0.0.1
- IMAP port: 993
- IMAP connection security: SSL/TLS
- IMAP authentication: Normal password

#### Outgoing Server:
- SMTP server name: localhost
- SMTP port: 465
- SMTP connection security: SSL/TLS
- SMTP authentication: Normal password