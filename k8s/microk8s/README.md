# Using Helm 3 with Microk8s

> **NOTE**: When using a `microk8s` cluster, the following add-ons must be enabled:
> ```bash
> microk8s enable dns
> ```
> ```bash
> systemctl status iscsid
>```
> If the service status is shown as Inactive , then you may have to enable and start iscsid service using the following command
> ```bash
> sudo systemctl enable --now iscsid
> ```
> ```bash
> microk8s enable openebs
> ```


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
    --set global.storageClass=openebs-hostpath \
    --set persistence.size=1Gi \
    --set persistence.commitLogsize=500Mi
```
To install the chart with the release name rabbitmq:
1.
```bash
$ helm install rabbitmq bitnami/rabbitmq \
    --set global.storageClass=openebs-hostpath,ingress.enabled=true,ingress.hostname=dashboard.dev.rmq.cryptopantry.tech,auth.username=guest,auth.password=guest,ingress.ingressClassName=nginx
```
2.
```bash
$ helm install rabbitmq bitnami/rabbitmq \
    --set persistence.enabled=false,ingress.enabled=false,auth.username=guest,auth.password=guest
```

To install the chart with the release name mongodb:
1.
```bash
$ helm install mongodb bitnami/mongodb \
    --set service.nameOverride=mongodb \
    --set auth.enabled=false \
    --set global.storageClass=openebs-hostpath \
    --set persistence.size=1Gi \
    --set architecture=replicaset \
    --set replicaCount=1
```
2.
```bash
$ helm install mongodb bitnami/mongodb \
    --set service.nameOverride=mongodb \
    --set auth.enabled=false \
    --set global.storageClass=openebs-hostpath \
    --set persistence.size=1Gi
```
To install the chart with the release name elasticsearch:
1.
```bash
$ helm install elasticsearch bitnami/elasticsearch \
    --set global.storageClass=openebs-hostpath \
    --set security.elasticPassword=secret \
    --set persistence.size=1Gi \
    --set master.persistence.size=1Gi \
    --set data.persistence.size=1Gi \
    --set master.replicaCount=1 \
    --set master.autoscaling.minReplicas=1 \
    --set master.autoscaling.maxReplicas=2 \
    --set data.replicaCount=1 \
    --set ingest.enabled=false \
    --set coordinating.replicaCount=0
```
2.
```bash
$ helm install elasticsearch elastic/elasticsearch -f values.yaml \
   --set replicas=1
```

To install the chart with the release name keycloak:
```bash
$ helm install keycloak bitnami/keycloak \
    --set nameOverride=service \
    --set global.storageClass=openebs-hostpath \
    --set service.type=NodePort \
    --set service.ports.http=9080
```