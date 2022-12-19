# Run REST application with Kubernetes

#### Deployment:

After setting up `kubectl` and connecting to a running kubernetes cluster, run the following commands in the 
correct order:

```bash
kubectl apply -f deployment/cassandra-deployment.yaml
```
```bash
kubectl apply -f deployment/elasticsearch-deployment.yaml
```
```bash
kubectl apply -f deployment/rabbitmq-deployment.yaml
```
```bash
kubectl apply -f deployment/mongo-deployment.yaml
```
```bash
kubectl apply -f deployment/keycloak-deployment.yaml
```
```bash
kubectl apply -f deployment/rest-deployment.yaml
```
```bash
kubectl apply -f deployment/james-deployment.yaml

```
To restart:
```bash
kubectl rollout restart deployment cassandra-deployment
```
```bash
kubectl rollout restart deployment elasticsearch-deployment
```
```bash
kubectl rollout restart deployment rabbitmq-deployment
```
```bash
kubectl rollout restart deployment mongo-deployment
```
```bash
kubectl rollout restart deployment keycloak-deployment
```
```bash
kubectl rollout restart deployment rest-deployment
```
```bash
kubectl rollout restart deployment james-deployment
```






