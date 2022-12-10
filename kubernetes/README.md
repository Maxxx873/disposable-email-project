# Run REST application with Kubernetes

#### Deployment:

After setting up `kubectl` and connecting to a running kubernetes cluster, run the following commands in the 
correct order:

```bash
kubectl apply -f cassandra-deployment.yaml
```
```bash
kubectl apply -f elasticsearch-deployment.yaml
```
```bash
kubectl apply -f rabbitmq-deployment.yaml
```
```bash
kubectl apply -f mongodb-deployment.yaml
```
```bash
kubectl apply -f keycloak-deployment.yaml
```
```bash
kubectl apply -f rest-deployment.yaml
```


