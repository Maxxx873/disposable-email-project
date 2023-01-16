# # Using Helm 3 with Openshift

#### Install chart

To install chart for REST application with 'rest' name:
```bash
helm install rest charts/rest-chart
```
To upgrade chart for REST application with 'rest' name:
```bash
helm upgrade --install rest charts/rest-chart
```
To install chart for james server with 'james' name:
```bash
helm install james charts/james-chart
```
To upgrade chart for james server with 'james' name:
```bash
helm upgrade --install james charts/james-chart
```

Add repo:
```bash
$ helm repo add bitnami https://charts.bitnami.com/bitnami
```
```bash
$ helm repo add elastic https://helm.elastic.co
```


To install the chart with the release name cassandra:
```bash
$ helm install cassandra bitnami/cassandra \
    --set dbUser.user=cassandra \
    --set dbUser.password=cassandra \
    --set persistence.size=1Gi \
    --set persistence.commitLogsize=500Mi \
    --set podSecurityContext.enabled=false \
    --set containerSecurityContext.enabled=false \
    --set containerSecurityContext.runAsNonRoot=false \
    --set jvm.maxHeapSize=2048m \
    --set jvm.newHeapSize=2048m \
    --set resources.limits.memory=4Gi \
    --set resources.requests.memory=4Gi
```
To install the chart with the release name rabbitmq:
```bash
$ helm install rabbitmq bitnami/rabbitmq \
    --set persistence.enabled=false,ingress.enabled=false,auth.username=guest,auth.password=guest \
    --set podSecurityContext.enabled=false \
    --set containerSecurityContext.enabled=false \
    --set containerSecurityContext.runAsNonRoot=false
```

To install the chart with the release name mongodb:
```bash
$ helm install mongodb bitnami/mongodb \
    --set service.nameOverride=mongodb \
    --set podSecurityContext.enabled=false \
    --set containerSecurityContext.enabled=false \
    --set auth.enabled=false \
    --set persistence.size=1Gi
```
To install the chart with the release name elasticsearch:
```bash
$ helm install elasticsearch elastic/elasticsearch \
    --set fullnameOverride=elasticsearch \
    --set replicas=1 \
    --set securityContext.runAsUser=null \
    --set podSecurityContext.fsGroup=null \
    --set podSecurityContext.runAsUser=null \
    --set sysctlInitContainer.enabled=false \
    --set volumeClaimTemplate.resources.requests.storage=1Gi
```
2. elastuc quickstart
```bash
helm install elasticsearch elastic/elasticsearch \
   --set fullnameOverride=elasticsearch \
   --set replicas=1 \
   --set secret.password=secret \
   --set securityContext.runAsUser=null \
   --set podSecurityContext.fsGroup=null \
   --set podSecurityContext.runAsUser=null \
   --set sysctlInitContainer.enabled=false \
   --set volumeClaimTemplate.resources.requests.storage=1Gi
```
3.
```bash
$ helm install elasticsearch bitnami/elasticsearch \
    --set security.elasticPassword=secret \
    --set master.podSecurityContext.enabled=false \
    --set master.podSecurityContext.fsGroup=null \
    --set master.containerSecurityContext.enabled=false \
    --set data.podSecurityContext.enabled=false \
    --set data.podSecurityContext.fsGroup=null \
    --set data.containerSecurityContext.enabled=false \
    --set sysctlImage.enabled=false \
    --set master.persistence.enabled=false \
    --set master.replicaCount=1 \
    --set master.autoscaling.minReplicas=1 \
    --set master.autoscaling.maxReplicas=2 \
    --set data.replicaCount=1 \
    --set data.persistence.enabled=false \
    --set data.resources.limits.memory=2Gi \
    --set data.resources.requests.memory=2Gi \
    --set ingest.enabled=false \
    --set coordinating.replicaCount=0
```

```bash
helm install -f values.yaml keycloak charts/keycloak-chart
```
To install the chart with the release name keycloak:
```bash
$ helm install -f values.yaml keycloak bitnami/keycloak \
    --set podSecurityContext.enabled=false \
    --set containerSecurityContext.enabled=false \
    --set containerSecurityContext.runAsNonRoot=false \
    --set postgresql.podSecurityContext.enabled=false \
    --set postgresql.containerSecurityContext.enabled=false \
    --set postgresql.containerSecurityContext.runAsNonRoot=false \
    --set volumeClaimTemplate.resources.requests.storage=1Gi \
    --set auth.adminPassword=admin \
    --set auth.adminUser=admin \
    --set nameOverride=service \
    --set service.type=NodePort \
    --set service.ports.http=9080
```