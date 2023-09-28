# FAQ

1. Nameserver limits exceeded" err="Nameserver limits were exceeded, some nameservers have been omitted, the applied nameserver line is: 10.22.16.2 10.22.16.254 10.245.0.10"
```shell
/run/systemd/resolve/resolv.conf
# delete some nameserver, for example: you can keep on nameserver in resolv.conf
```

2. Enable master acting as Node
```shell
kubectl taint node k8s-master node-role.kubernetes.io/control-plane:NoSchedule-
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
journalctl -u kubelet -n 100 --no-pager
```

6. Reset k8s cluster if you encountered problems after you initialized k8s cluster
```shell
kubeadm reset
```

7.  The Service "nginx" is invalid: spec.ports[0].nodePort: Invalid value: 80: provided port is not in the valid range. The range of valid ports is 30000-32767
```shell
vim /etc/kubernetes/manifests/kube-apiserver.yaml

# add - --service-node-port-range=1-65535 
```


# Reference Links
1. [Installing kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/)

2. [使用kubeadm部署Kubernetes 1.28](https://blog.frognew.com/2023/08/kubeadm-install-kubernetes-1.28.html)

3. [Nameserver Known issues](https://kubernetes.io/docs/tasks/administer-cluster/dns-debugging-resolution/#known-issues)

4. [Windows minikube 安装](https://minikube.sigs.k8s.io/docs/start/)

5. [Install Helm](https://helm.sh/docs/intro/install/)