// package com.epam.workshop.sales.product.infrastructure.config;

// public class Config {
    
// }
// apiVersion: apps/v1
// kind: Deployment
// metadata:
//   name: sales-order
// spec:
//   selector:
//     matchLabels:
//       app: sales-order
//   replicas: 1
//   minReadySeconds: 10
//   template:
//     metadata:
//       labels:
//         app: sales-order
//     spec:
//       containers:
//         - name: sales-order
//           image: 192.168.56.105:30002/sales-order:latest
//           imagePullPolicy: Always
//           ports:
//             - containerPort: 8080

// ---
// apiVersion: v1
// kind: Service
// metadata:
//   name: sales-order
//   labels:
//     app: sales-order
// spec:
//   ports:
//     - name: service0
//       port: 8080
//       protocol: TCP
//       targetPort: 8080
//       nodePort: 8080
//   selector:
//     app: sales-order
//   type: NodePort
// ---
// apiVersion: networking.k8s.io/v1
// kind: Ingress
// metadata:
//   name: sales-ingress
//   annotations:
//     #nginx.ingress.kubernetes.io/rewrite-target: $1
// spec:
//   ingressClassName: nginx
//   rules:
//     - http:
//         paths:
//           - pathType: Prefix
//             path: "/api/v1/order"
//             backend:
//               service:
//                 name: sales-order
//                 port:
//                   number: 8080