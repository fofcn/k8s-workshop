Kubernetes（简称 k8s）是一个开源的容器编排系统，用于自动部署、扩展和管理容器化应用程序。以下是一些常用的 k8s 命令：

1. 查看集群信息：
   ```
   kubectl cluster-info
   ```

2. 检查节点状态：
   ```
   kubectl get nodes
   ```

3. 查看所有命名空间：
   ```
   kubectl get namespaces
   ```

4. 查看指定命名空间下的所有资源：
   ```
   kubectl get all -n <namespace>
   ```

5. 创建一个资源对象（例如，部署一个应用）：
   ```
   kubectl create -f <configuration_file.yaml>
   ```

6. 删除一个资源对象：
   ```
   kubectl delete -f <configuration_file.yaml>
   ```

7. 查看pods：
   ```
   kubectl get pods
   kubectl get pods -n <namespace>
   ```

8. 查看pod详细信息：
   ```
   kubectl describe pod <pod_name>
   kubectl describe pod <pod_name> -n <namespace>
   ```

9. 查看pod日志：
   ```
   kubectl logs <pod_name>
   kubectl logs <pod_name> -n <namespace>
   ```

10. 查看所有服务：
    ```
    kubectl get services
    kubectl get services -n <namespace>
    ```

11. 查看服务详细信息：
    ```
    kubectl describe service <service_name>
    kubectl describe service <service_name> -n <namespace>
    ```

12. 查看所有deployments：
    ```
    kubectl get deployments
    kubectl get deployments -n <namespace>
    ```

13. 查看deployment详细信息：
    ```
    kubectl describe deployment <deployment_name>
    kubectl describe deployment <deployment_name> -n <namespace>
    ```

14. 查看所有ReplicaSets：
    ```
    kubectl get replicasets
    kubectl get replicasets -n <namespace>
    ```

15. 查看ReplicaSet详细信息：
    ```
    kubectl describe replicaset <replicaset_name>
    kubectl describe replicaset <replicaset_name> -n <namespace>
    ```

16. 查看所有ConfigMaps：
    ```
    kubectl get configmaps
    kubectl get configmaps -n <namespace>
    ```

17. 查看ConfigMap详细信息：
    ```
    kubectl describe configmap <configmap_name>
    kubectl describe configmap <configmap_name> -n <namespace>
    ```

18. 查看所有Secrets：
    ```
    kubectl get secrets
    kubectl get secrets -n <namespace>
    ```

19. 查看Secret详细信息：
    ```
    kubectl describe secret <secret_name>
    kubectl describe secret <secret_name> -n <namespace>
    ```

20. 查看所有StorageClasses：
    ```
    kubectl get storageclasses
    ```

21. 查看StorageClass详细信息：
    ```
    kubectl describe storageclass <storageclass_name>
    ```

22. 查看所有PVCs（Persistent Volume Claims）：
    ```
    kubectl get pvc
    kubectl get pvc -n <namespace>
    ```

23. 查看PVC详细信息：
    ```
    kubectl describe pvc <pvc_name>
    kubectl describe pvc <pvc_name> -n <namespace>
    ```

24. 更新k8s对象：
    ```
    kubectl apply -f <configuration_file.yaml>
    ```

25. 与正在运行的pod建立交互式终端：
    ```
    kubectl exec -it <pod_name> -- /bin/sh
    ```

有关更多 kubectl 命令，请查看 [kubectl 官方文档](https://kubernetes.io/docs/reference/kubectl/overview/)。