# Install k8s
## 1 Containerd runtime config
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

## 2 Create /etc/sysctl.d/99-kubernetes-cri.conf
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

## 3 Enable ipvs
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

## 5 Install Containerd
```shell
wget https://github.com/containerd/containerd/releases/download/v1.7.3/containerd-1.7.3-linux-amd64.tar.gz

```
unzip containerd-1.7.3-linux-amd64.tar.gz
```shell
tar Cxzvf /usr/local containerd-1.7.3-linux-amd64.tar.gz
```

## 6 安装runc:
```shell
wget https://github.com/opencontainers/runc/releases/download/v1.1.9/runc.amd64
install -m 755 runc.amd64 /usr/local/sbin/runc
```

## 7 生成containerd配置
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

## 8 Download containerd.service
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

## 9 Startup Containerd on boot
```shell
systemctl daemon-reload
systemctl enable containerd --now 
#systemctl status containerd
```

## 10 Install Crictl
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


## 11 Update apt Repository
```shell
sudo apt-get update
# apt-transport-https may be a dummy package; if so, you can skip that package
sudo apt-get install -y apt-transport-https ca-certificates curl
```

## 12 Download k8s public key of apt repository
```shell
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

```

## 13 Add k8s apt repository
```shell
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list

```

## 14 Install kubelet, kubeadm, kubectl
```shell
sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl
```

## 15 Disable swap
Disable swap temporarily
```shell
swapoff -a
```

Disable swap permanently
```shell
sed -i '/swap/s/^/#/' /etc/fstab
```
