# Overview
## 1. 安装VirtualBox虚拟机
## 2. 下载Ubuntu 23 ISO
## 3. 安装Ubuntu 23 Server系统
## 4. 安装k8s 
## 5. 初始化k8s集群
## 6. 安装Helm
## 7. 安装k8s网络插件
## 8. 测试k8s集群
## 9. FAQ
## 10. 参考链接

# 1. 安装VirtualBox虚拟机
下载地址:
1. [Mac VirtuaBox](https://download.virtualbox.org/virtualbox/7.0.10/VirtualBox-7.0.10a-158379-OSX.dmg)
2. [Windows VirtulaBox](https://download.virtualbox.org/virtualbox/7.0.10/VirtualBox-7.0.10-158379-Win.exe)

# 2. 下载Ubuntu 23 ISO
下载地址：[Ubuntu 23](https://mirror.nju.edu.cn/ubuntu-releases/22.04.3/)

# 3. 创建虚拟机
菜单栏: Machine-> New,如下图所示：
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695271975102.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272006706.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272058852.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272080021.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272099910.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272130354.png)

设置网络：
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272171019.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272193393.png)

# 安装Ubuntu 23操作系统
## 3.1 启动虚拟机开始安装操作系统
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272243485.png)

## 3.2 选择Ubuntu Server
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272361950.png)

## 3.3 两张网卡信息：
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272405872.png)

## 3.4 设置主机及账号信息
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272471302.png)

## 3.5 安装OpenSSH Server
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272517295.png)

## 3.6 开始安装
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272571216.png)

## 3.7 安装完成
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272915227.png)

# 4. 安装K8s
## 4.1 配置containerd运行环境
创建/etc/modules-load.d/containerd.conf配置文件，确保在系统启动时自动加载所需的内核模块，以满足容器运行时的要求:
```shell
cat << EOF > /etc/modules-load.d/containerd.conf
overlay
br_netfilter
EOF
```
配置生效
```shell
modprobe overlay
modprobe br_netfilter
```

## 4.2 创建/etc/sysctl.d/99-kubernetes-cri.conf
```shell
cat << EOF > /etc/sysctl.d/99-kubernetes-cri.conf
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward = 1
user.max_user_namespaces=28633
EOF
```

配置生效
```shell
sysctl -p /etc/sysctl.d/99-kubernetes-cri.conf

```

## 4.3 开启ipvs
```shell
cat > /etc/modules-load.d/ipvs.conf <<EOF
ip_vs
ip_vs_rr
ip_vs_wrr
ip_vs_sh
EOF
```
配置生效
```shell
modprobe ip_vs
modprobe ip_vs_rr
modprobe ip_vs_wrr
modprobe ip_vs_sh
```

安装ipvsadm
```shell
apt install -y ipset ipvsadm

```

## 4.5 安装containerd
```shell
wget https://github.com/containerd/containerd/releases/download/v1.7.3/containerd-1.7.3-linux-amd64.tar.gz

```
解压缩
```shell
tar Cxzvf /usr/local containerd-1.7.3-linux-amd64.tar.gz
```

## 4.6 安装runc:
```shell
wget https://github.com/opencontainers/runc/releases/download/v1.1.9/runc.amd64
install -m 755 runc.amd64 /usr/local/sbin/runc
```

## 4.7 生成containerd配置
```shell
mkdir -p /etc/containerd
containerd config default > /etc/containerd/config.toml
```

配置containerd使用systemd作为容器cgroup driver
```shell
[plugins."io.containerd.grpc.v1.cri".containerd.runtimes.runc]
  ...
  [plugins."io.containerd.grpc.v1.cri".containerd.runtimes.runc.options]
    SystemdCgroup = true
```
国内墙可修改
```shell
[plugins."io.containerd.grpc.v1.cri"]
  ...
  # sandbox_image = "registry.k8s.io/pause:3.8"
  sandbox_image = "registry.aliyuncs.com/google_containers/pause:3.9"
```

## 4.8 下载containerd.service
链接：https://raw.githubusercontent.com/containerd/containerd/main/containerd.service
```shell
cat << EOF > /etc/systemd/system/containerd.service
[Unit]
Description=containerd container runtime
Documentation=https://containerd.io
After=network.target local-fs.target

[Service]
#uncomment to enable the experimental sbservice (sandboxed) version of containerd/cri integration
#Environment="ENABLE_CRI_SANDBOXES=sandboxed"
ExecStartPre=-/sbin/modprobe overlay
ExecStart=/usr/local/bin/containerd

Type=notify
Delegate=yes
KillMode=process
Restart=always
RestartSec=5
# Having non-zero Limit*s causes performance problems due to accounting overhead
# in the kernel. We recommend using cgroups to do container-local accounting.
LimitNPROC=infinity
LimitCORE=infinity
LimitNOFILE=infinity
# Comment TasksMax if your systemd version does not supports it.
# Only systemd 226 and above support this version.
TasksMax=infinity
OOMScoreAdjust=-999

[Install]
WantedBy=multi-user.target
EOF
```

## 4.9 配置Containerd开机启动
```shell
systemctl daemon-reload
systemctl enable containerd --now 
systemctl status containerd
```

## 4.10 安装Crictl
```shell
wget https://github.com/kubernetes-sigs/cri-tools/releases/download/v1.28.0/crictl-v1.28.0-linux-amd64.tar.gz
tar -zxvf crictl-v1.28.0-linux-amd64.tar.gz
install -m 755 crictl /usr/local/bin/crictl
```

测试Crictl
```shell
crictl --runtime-endpoint=unix:///run/containerd/containerd.sock  version

Version:  0.1.0
RuntimeName:  containerd
RuntimeVersion:  v1.7.3
RuntimeApiVersion:  v1
```

## 4.11 更新apt仓库
```shell
sudo apt-get update
# apt-transport-https may be a dummy package; if so, you can skip that package
sudo apt-get install -y apt-transport-https ca-certificates curl
```

## 4.12 下载k8s包仓库公钥
```shell
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

```

## 4.13 添加k8s apt 仓库
```shell
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list

```

## 4.14 安装kubelet, kubeadm和kubelet
```shell
sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl
```

## 4.15 关闭系统swap
```shell
swapoff -a
```

永久关闭
```shell
vim /etc/fstab
```
开机启动kubelet
```shell
systemctl enable kubelet

```



# 5. 初始化K8s集群
```shell
kubeadm init --apiserver-advertise-address=your_host-only-ip --pod-network-cidr=10.244.0.0/16
```

初始化完成后
```shell
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

# 6. 安装Helm
```shell
wget https://get.helm.sh/helm-v3.12.3-linux-amd64.tar.gz
tar -zxvf helm-v3.12.3-linux-amd64.tar.gz
install -m 755 linux-amd64/helm  /usr/local/bin/helm
```

# 7. 安装k8s网络插件
下载tigera-operator
```shell
wget https://github.com/projectcalico/calico/releases/download/v3.26.1/tigera-operator-v3.26.1.tgz

```

查看chart中可定制的配置
```shell
helm show values tigera-operator-v3.26.1.tgz
```
做点简单配置定制,保存为vlaues.yaml
```yaml
apiServer:
  enabled: false
installation:
  kubeletVolumePluginPath: None
```

Heml安装colico
```shell
helm install calico tigera-operator-v3.26.1.tgz -n kube-system  --create-namespace -f values.yaml

```
等待Pod处于Running
```shell
kubectl get pod -n kube-system | grep tigera-operator
```
安装kubectl插件
```shell
cd /usr/local/bin
curl -o kubectl-calico -O -L  "https://github.com/projectcalico/calicoctl/releases/download/v3.21.5/calicoctl-linux-amd64" 
chmod +x kubectl-calico
```

验证是否正常工作
```shell
kubectl calico -h

```

# 8. 测试
## 8.1 验证k8s DNS
```shell
kubectl run curl --image=radial/busyboxplus:curl -it
nslookup kubernetes.default
```

## 8.2 发布一个nginx
命令
```shell
kubectl apply -f nginx.yaml
```
yaml文件如下：
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  annotations:
    change-cause: "Rollout test"
spec:
  strategy:
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 1
  selector:
    matchLabels:
      app: nginx
  replicas: 1
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.14.2
        ports:
        - containerPort: 80
        resources:
          requests:
            cpu: 200m
          limits:
            cpu: 500m
---
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
spec:
  type: NodePort
  selector:
    app: nginx
  ports:
    - protocol: TCP
      port: 40000
      targetPort: 80
      nodePort: 32000

```

# 9. FAQ
1. Nameserver limits exceeded" err="Nameserver limits were exceeded, some nameservers have been omitted, the applied nameserver line is: 10.22.16.2 10.22.16.254 10.245.0.10"
```shell
/run/systemd/resolve/resolv.conf
# 注释掉几个ip
```

2. Master节点作为Node
```shell
kubectl taint node k8s-master node-role.kubernetes.io/master:NoSchedule-
```

3. 忘记join集群命令
```shell
kubeadm token create --print-join-command
```

4. 部署curl测试
```shell
kubectl run curl --image=radial/busyboxplus:curl -it
```

5. kubelet日志查看
```shell
journal -xeu kubelet
journal -xeu kubelet > kubelet.log
```

6. kubeadm初始化有问题可以尝试reset后重新初始化
```shell
kubeadm reset
```

# 10. 参考链接
1. [Installing kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/)

2. [使用kubeadm部署Kubernetes 1.28](https://blog.frognew.com/2023/08/kubeadm-install-kubernetes-1.28.html)

3. [Nameserver Known issues](https://kubernetes.io/docs/tasks/administer-cluster/dns-debugging-resolution/#known-issues)

4. [Windows minikube 安装](https://minikube.sigs.k8s.io/docs/start/)

5. [Install Helm](https://helm.sh/docs/intro/install/)