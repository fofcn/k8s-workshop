# Overview
1. Helm
2. Rollout and Rollback
3. Metric Server
4. AutoScaling
5. Blue-Green 
6. Canary
7. Logging & Monitoring


# 1. Helm
## 1.1. 创建一个Chart

```shell
# create a new chart
 helm create my-chart

 # structure of chart
 tree my-chart

```

# 2. Rollout and Rollback

# 3. Metric Server
```shell
# download metric-server yaml
curl https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# rename file name
mv components.yaml metric-server.yaml

# edit yaml and ignore tls
vim/nano metric-server.yaml
135         - --cert-dir=/tmp
136         - --secure-port=4443
137         - --kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname
138         - --kubelet-use-node-status-port
139         - --metric-resolution=15s
140         - --kubelet-insecure-tls

# create metric server in k8s cluster
kubectl apply -f metric-server.yaml

# validate if metric server deployed successful
kubectl get pods -n kube-system | grep metrics
metrics-server-85cbcbdd74-rqcft      1/1     Running            10 (131m ago)    4d18h
```

# 4. AutoScaling
## 4.1 create a horizontal pod autoscaler
```shell
apiVersion: autoscaling/v1
kind: HorizontalPodAutoscaler
metadata:
  name: nginx-deployment-autoscaler
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: nginx-deployment
  minReplicas: 1
  maxReplicas: 10
  targetCPUUtilizationPercentage: 10

```

## 4.2 Resource limitation of containers
```shell
    spec:
      containers:
      - name: your image name
        image: image&tag
        ports:
        - containerPort: 80
        resources:
          requests:
            cpu: 20m
            memroy: 128Mi
            hugepages-2Mi: 128Mi
          limits:
            cpu: 50m
            memory: 258Mi
            hugepages-2Mi: 256Mi

```

## 4.3 Run JMeter script and watch 
```shell
kubectl get hpa
kubectl get pods
```

# 5. Blue-Green

# 6. Canary

# 7. Logging & Monitoring

## 7.1 Logging

## 7.2 Monitoring - Prometheus
```shell
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
```

# 8. Reference Links
1. [Introduction to Helm 3 the Package Manager for Kubernetes](https://razorops.com/blog/introduction-to-helm-3-the-package-manager-for-kubernetes/)
2. [Metric Server README.md](https://github.com/kubernetes-sigs/metrics-server)
3. [Metrics Server安装以及报错解决](https://blog.csdn.net/liuyanwuyu/article/details/119793631)
4. [Canary Deployment for Queue Workers](https://abhishekvrshny.medium.com/canary-deployment-for-queue-workers-f06612ef858)
5. [Kubernetes Autoscaling: How to use the Kubernetes Autoscaler](https://www.clickittech.com/devops/kubernetes-autoscaling/)
6. [What Is Blue/Green Deployment and Automating Blue/Green in Kubernetes](https://codefresh.io/learn/software-deployment/what-is-blue-green-deployment/)
7. [7. [](Intro to deployment strategies: blue-green, canary, and more)](https://dev.to/mostlyjason/intro-to-deployment-strategies-blue-green-canary-and-more-3a3)