# Overview
1. 私有仓库部署与配置
2. containerd配置
3. docker配置
4. 代码克隆
5. 构建镜像并推送
6. 部署应用（Ingress）
7. Postman测试

# 私有仓库部署与配置
## 1. 部署

```shell
kubectl apply -f nexus-storage.yml
kubectl apply -f nexus-pv.yml
kubectl apply -f nexus.yml
```
* 注意： nexus-pv.yml需要将最后一行的k8s-master修改为自己node的hostname，可以使用命令查看：
```shell
kubectl get nodes
NAME         STATUS   ROLES           AGE    VERSION
k8s-master   Ready    control-plane   2d2h   v1.28.2
```
* 同时需要修改nexus-pv.yml中本地路径
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695290296177.png)

## 2. 登录Nexus
访问：http://your_host-only_ip:30001.

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695290128943.png)

登录时默认密码在容器的硬盘中，可以使用
```shell
kubectl get pods 
NAME                               READY   STATUS    RESTARTS        AGE
curl                               1/1     Running   7 (3h57m ago)   42h
nexus3-7f56dd49cf-mtzbs            1/1     Running   0               49m
nginx-deployment-67594bcd6-pqdcm   1/1     Running   1 (3h57m ago)   19h
sales-order-77fdd59584-5qr46       1/1     Running   0               24m
```
命令查看nexus Pod信息，然后使用
```shell
kubectl exec -it po/{pod_name} bash 
```
进入容器后使用cat命令查看指定文件。

## 3. 配置Nexus
新建三个仓库，分别为：
* docker-hosted 用于保存私有镜像
* docker-proxy 用于共有镜像代理
* docker-group 用于统一访问公私有镜像
如下截图:
截图1： 仓库列表
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695290624574.png)

截图2： docker-hosted配置
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695290689423.png)

截图3： docker-proxy配置
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695290735829.png)

截图4： docker-group配置
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695290818003.png)
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695290860167.png)

# Containerd配置
编辑Containerd配置文件,增加私有仓库配置
```shell
vim /etc/containerd/config.toml
[plugins."io.containerd.grpc.v1.cri".registry]
      config_path = ""

      [plugins."io.containerd.grpc.v1.cri".registry.auths]

      [plugins."io.containerd.grpc.v1.cri".registry.configs]
      [plugins."io.containerd.grpc.v1.cri".registry.configs."192.168.56.105:30002"]
      [plugins."io.containerd.grpc.v1.cri".registry.configs."192.168.56.105:30002".tls]
        insecure_skip_verify = true

      [plugins."io.containerd.grpc.v1.cri".registry.headers]

      [plugins."io.containerd.grpc.v1.cri".registry.mirrors]
      [plugins."io.containerd.grpc.v1.cri".registry.mirrors."192.168.56.105:30002"]
        endpoint = ["http://192.168.56.105:30002"]

```
编辑完成后，需要重启Containerd服务
```shell
systemctl restart containerd
```

如果遇到问题一般是语法问题，可以可以使用命令查看错误原因并进行修复:
```shell
journalctl -xeu containerd
```

# docker配置
## docker desktop配置
如果使用docker desktop作为镜像打包工具，那么需要配置docker daemon.json，增加自己的registry,如截图所示：
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695291706563.png)

如果使用Linux Docker作为打包工具，则也需要上面的配置，命令如下：
```shell
# 安装docker.io
apt install -y docker.io

# 增加私有仓库
vim /etc/docker/daemon.json
{
  "insecure-registries" : ["192.168.56.105:30002", "192.168.56.105:30003"]
}

# 重启dokcer服务
systemctl restart docker
```
# 代码克隆
克隆demo代码
```shell
git clone https://github.com/fofcn/k8s-workshop.git
```

# 构建镜像并推送
```shell
cd sales-order
docker build . -t sales-order
docker tag sales-order:latest 192.168.56.105:30003/sales-order:latest
docker push 192.168.56.105:30003/sales-order:latest
```


# 部署应用
## 编写sales-order yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sales-order
spec:
  selector:
    matchLabels:
      app: sales-order
  replicas: 1
  minReadySeconds: 10
  template:
    metadata:
      labels:
        app: sales-order
    spec:
      containers:
        - name: sales-order
          image: 192.168.56.105:30002/sales-order:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8080

---
apiVersion: v1
kind: Service
metadata:
  name: sales-order
  labels:
    app: sales-order
spec:
  ports:
    - name: service0
      port: 8080
      protocol: TCP
      targetPort: 8080
      nodePort: 8080
  selector:
    app: sales-order
  type: NodePort
```

部署到k8s 集群
```shell
kubectl apply -f sales-order.yml
```

# Ingress
## Nginx-Ingress Deployment
Deploy Ingress,我们这儿使用Nginx-Ingress.
Helm方式(建议)：
```shell
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace ingress-nginx --create-namespace
```
yaml文件方式：
```shell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml
```

发布完成后我们需要更改Ingress的Service Type为NodePort，因为我们没有LoadBalancer可用，具体方法如下：
```shell
# 首选保存nginx-ingress默认vlaues.yml
helm show values ingress-nginx --repo https://kubernetes.github.io/ingress-nginx > nginx-ingress-values.yml

# 编辑这个values.yml文件,修改Type为NodePort（466行），并配置NodePort http和https端口（473行）
vim nginx-ingress-values.yml
458     ipFamilies:
459       - IPv4
460     ports:
461       http: 80
462       https: 443
463     targetPorts:
464       http: http
465       https: https
466     type: NodePort
467     ## type: NodePort
468     ## nodePorts:
469     ##   http: 32080
470     ##   https: 32443
471     ##   tcp:
472     ##     8080: 32808
473     nodePorts:
474       http: "80"
475       https: "443"
476       tcp: {}
477       udp: {}
478     external:
479       enabled: true

# 完成后更新nginx-ingress
helm upgrade --install ingress-nginx ingress-nginx   --repo https://kubernetes.github.io/ingress-nginx   --namespace ingress-nginx --create-namespace --values nginx-ingress-values.yml

```

## 配置Ingress访问sales-order服务
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: sales-ingress
  annotations:
    #nginx.ingress.kubernetes.io/rewrite-target: $1
spec:
  ingressClassName: nginx
  rules:
    - http:
        paths:
          - pathType: Prefix
            path: "/api/v1/order"
            backend:
              service:
                name: sales-order
                port:
                  number: 8080

```


# Postman测试
## 测试curl(sales-order NodePort)
```shell
curl --location --request GET 'http://192.168.56.105:8080/api/v1/order/1234/product/name'
```

## 测试curl(sales-order Ingress)
```shell
curl --location --request GET 'http://192.168.56.105/api/v1/order/1234/product/name'
```

## Postman请求
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695292169727.png)

# 参考链接
1. [k8s Ingress Controllers](https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/)
2. [Nginx Ingress Controller Install Guide](https://kubernetes.github.io/ingress-nginx/deploy/)








