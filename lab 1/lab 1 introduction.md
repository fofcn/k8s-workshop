# Overview
1. Install VirtualBox
2. Download Ubuntu ISO
3. Install Ubuntu Server System
4. Install k8s
5. Init k8s cluster
6. Install Helm
7. Install k8s network plugin
8. Test k8s cluster
9. FAQ
10. Reference links

# 1. Install VirtualBox
Download links:
1. [Mac VirtuaBox](https://download.virtualbox.org/virtualbox/7.0.10/VirtualBox-7.0.10a-158379-OSX.dmg)
2. [Windows VirtulaBox](https://download.virtualbox.org/virtualbox/7.0.10/VirtualBox-7.0.10-158379-Win.exe)

# 2. Download Ubuntu ISO
Link：[Ubuntu 23](https://mirror.nju.edu.cn/ubuntu-releases/22.04.3/)

# 3. Install Ubuntu Server 
## 3.1 Create a new virtual machine
Menu: Machine-> New：
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695271975102.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272006706.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272058852.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272080021.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272099910.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272130354.png)

设置网络：
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272171019.png)

![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272193393.png)

## 3.1 Start virtual machine
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272243485.png)

## 3.2 Choose Ubuntu Server ISO file
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272361950.png)

## 3.3 Netowrk settings
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272405872.png)

## 3.4 host and account
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272471302.png)

## 3.5 Enable OpenSSH server
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272517295.png)

## 3.6 Start installing the system
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272571216.png)

## 3.7 Install complete
![file](https://fofcn.tech:443/wp-content/uploads/2023/09/image-1695272915227.png)

# 4. Install k8s
## 4.1 Containerd runtime config
Create /etc/modules-load.d/containerd.conf
```shell
cat << EOF > /etc/modules-load.d/containerd.conf
overlay
br_netfilter
EOF
```
Configuration takes effect
```shell
modprobe overlay
modprobe br_netfilter
```

## 4.2 Create /etc/sysctl.d/99-kubernetes-cri.conf
```shell
cat << EOF > /etc/sysctl.d/99-kubernetes-cri.conf
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward = 1
user.max_user_namespaces=28633
EOF
```

Configuration takes effect
```shell
sysctl -p /etc/sysctl.d/99-kubernetes-cri.conf

```

## 4.3 Enable ipvs
```shell
cat > /etc/modules-load.d/ipvs.conf <<EOF
ip_vs
ip_vs_rr
ip_vs_wrr
ip_vs_sh
EOF
```
Configuration takes effect
```shell
modprobe ip_vs
modprobe ip_vs_rr
modprobe ip_vs_wrr
modprobe ip_vs_sh
```

Install ipvsadm
```shell
apt install -y ipset ipvsadm

```

## 4.5 Install Containerd
```shell
wget https://github.com/containerd/containerd/releases/download/v1.7.3/containerd-1.7.3-linux-amd64.tar.gz

```
unzip containerd-1.7.3-linux-amd64.tar.gz
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

Enable Systemd as cgroup driver of Containerd
```shell
[plugins."io.containerd.grpc.v1.cri".containerd.runtimes.runc]
  ...
  [plugins."io.containerd.grpc.v1.cri".containerd.runtimes.runc.options]
    SystemdCgroup = true
```
For some reasons you maybe change the google container registry
```shell
[plugins."io.containerd.grpc.v1.cri"]
  ...
  # sandbox_image = "registry.k8s.io/pause:3.8"
  sandbox_image = "registry.aliyuncs.com/google_containers/pause:3.9"
```

## 4.8 Download containerd.service
Download link：https://raw.githubusercontent.com/containerd/containerd/main/containerd.service
or you can use the command bellowing:
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

## 4.9 Startup Containerd on boot
```shell
systemctl daemon-reload
systemctl enable containerd --now 
#systemctl status containerd
```

## 4.10 Install Crictl
```shell
wget https://github.com/kubernetes-sigs/cri-tools/releases/download/v1.28.0/crictl-v1.28.0-linux-amd64.tar.gz
tar -zxvf crictl-v1.28.0-linux-amd64.tar.gz
install -m 755 crictl /usr/local/bin/crictl
```

Test Crictl
```shell
crictl --runtime-endpoint=unix:///run/containerd/containerd.sock  version


```
Output should be like this:
```shell
Version:  0.1.0
RuntimeName:  containerd
RuntimeVersion:  v1.7.3
RuntimeApiVersion:  v1
```


## 4.11 Update apt Repository
```shell
sudo apt-get update
# apt-transport-https may be a dummy package; if so, you can skip that package
sudo apt-get install -y apt-transport-https ca-certificates curl
```

## 4.12 Download k8s public key of apt repository
```shell
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

```

## 4.13 Add k8s apt repository
```shell
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list

```

## 4.14 Install kubelet, kubeadm, kubectl
```shell
sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl
```

## 4.15 Disable swap
Disable swap temporarily
```shell
swapoff -a
```

Disable swap permanently
```shell
sed -i '/swap/s/^/#/' /etc/fstab
```
Start kubelet on boot
```shell
systemctl enable kubelet
```



# 5. Init k8s cluster
```shell
kubeadm init --apiserver-advertise-address=your_host-only-ip --pod-network-cidr=10.244.0.0/16
```

After cluster initialized
```shell
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

# 6. Install Helm
```shell
wget https://get.helm.sh/helm-v3.12.3-linux-amd64.tar.gz
tar -zxvf helm-v3.12.3-linux-amd64.tar.gz
install -m 755 linux-amd64/helm  /usr/local/bin/helm
```

# 7. Install k8s network plugin
Download tigera-operator
```shell
wget https://github.com/projectcalico/calico/releases/download/v3.26.1/tigera-operator-v3.26.1.tgz

```

Show values of Chart
```shell
helm show values tigera-operator-v3.26.1.tgz
```
Make some simple configuration customizations, and save as values.yaml
```yaml
apiServer:
  enabled: false
installation:
  kubeletVolumePluginPath: None
```

Install colico
```shell
helm install calico tigera-operator-v3.26.1.tgz -n kube-system  --create-namespace -f values.yaml

```
Wait util pod in running state
```shell
kubectl get pod -n kube-system | grep tigera-operator
```

Install kubectl calico plugin
```shell
cd /usr/local/bin
curl -o kubectl-calico -O -L  "https://github.com/projectcalico/calicoctl/releases/download/v3.21.5/calicoctl-linux-amd64" 
chmod +x kubectl-calico
```

Verify if it is working properly
```shell
kubectl calico -h

```

# 8. Test
## 8.1 Verify k8s DNS
```shell
kubectl run curl --image=radial/busyboxplus:curl -it
nslookup kubernetes.default
```

## 8.2 Deploy a nginx in k8s cluster
kubectl command:
```shell
kubectl apply -f nginx.yaml
```
nginx.yaml file content：
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
# delete some nameserver, for example: you can keep on nameserver in resolv.conf
```

2. Enable master acting as Node
```shell
kubectl taint node k8s-master node-role.kubernetes.io/master:NoSchedule-
```

3. Print join cluster command if you forgot 
```shell
kubeadm token create --print-join-command
```

4. Deploy curl for testing
```shell
kubectl run curl --image=radial/busyboxplus:curl -it
```

5. View kubelet log if an error occurred of kubelet
```shell
journal -xeu kubelet
journal -xeu kubelet > kubelet.log
```

6. Reset k8s cluster if you encountered problems after you initialized k8s cluster
```shell
kubeadm reset
```

# 10. Reference links
1. [Installing kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/)

2. [使用kubeadm部署Kubernetes 1.28](https://blog.frognew.com/2023/08/kubeadm-install-kubernetes-1.28.html)

3. [Nameserver Known issues](https://kubernetes.io/docs/tasks/administer-cluster/dns-debugging-resolution/#known-issues)

4. [Windows minikube 安装](https://minikube.sigs.k8s.io/docs/start/)

5. [Install Helm](https://helm.sh/docs/intro/install/)
