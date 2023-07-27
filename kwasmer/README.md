# KWasmer

KWasmer provides a set of tools to help you run WebAssembly Containers on Kubernetes.

## KWasm

[KWasm](https://kwasm.sh/) adds WebAssembly support to Kubernetes. It allows you to run WebAssembly modules as containers on Kubernetes.

To get started with KWasm you need to follow certain steps that although are quite simple, can be further simplified and automated. This is one of the goals of KWasmer.

You can create a development local [Kind](https://kind.sigs.k8s.io/) cluster with KWasm enabled just by executing the following command:

```bash
kwasm start
```

This will take care of creating a dedicated Kind cluster and deploying the necessary components to run KWasm on it.
