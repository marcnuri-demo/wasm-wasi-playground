# KWasmer

KWasmer provides a set of tools to help you run WebAssembly Containers on Kubernetes.

## Download

You can download binaries for Linux and Windows from the [releases page](https://github.com/marcnuri-demo/wasm-wasi-playground/releases/latest).

## Usage

### Create a local Kubernetes cluster with wasi/wasm support

You can start a local Kubernetes cluster with wasi/wasm support by executing the following command:

```bash
kwasmer start
```

The command takes care of automatically creating a Kind Kubernetes cluster and adding support for wasi/wasm using KWasm.

It will also set your `.kube/context` to use the newly created cluster.

### Deploy a WebAssembly image

Once you've got your cluster up and running, you can start deploying container images that use WebAssembly as their runtime by executing the following command:

```bash
kwasmer deploy <image>
```

If you want to specify the controller type (Job, Deployment, Pod), you can do so by using the `--controller`/`-c` flag:

```bash
kwasmer deploy <image> --controller <controller>
```

## Leveraged tools and components

### Kind

[Kind](https://kind.sigs.k8s.io/) is a tool for running local Kubernetes clusters using Docker container "nodes".
The provided CLI is straightforward, however, bringing wasi/wasm support to a Kind cluster requires some manual steps.
These steps differ in complexity depending on the approach you want to take.

### KWasm

[KWasm](https://kwasm.sh/) adds WebAssembly support to Kubernetes. It allows you to run WebAssembly modules as containers on Kubernetes.

To get started with KWasm you need to follow certain steps that although are quite simple, can be further simplified and automated. This is one of the goals of KWasmer.


## Building the project

### JVM

To build the project with Java and package it as a Jar you'll need a Java JDK 17+ installed and Maven.
You can build the project by executing the following command:

```bash
mvn package
```

### Linux native

To compile a native Linux executable you'll need GraalVM or a Docker-compatible daemon available.

You can generate the native executable by executing the following command:

```bash
mvn package -Dnative
```

### Windows native

To compile a native Windows executable you need GraalVM installed. You can follow the official [installation guide](https://www.graalvm.org/latest/docs/getting-started/windows/).

Once you have GraalVM installed, you can compile the project by executing the following command:

```bash
mvn package -Dnative
```

If GraalVM doesn't detect your Visual Studio 2022 build tools, you'll need to run the following command:

```bash
"C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat"
```

Then execute the Maven native packaging goal.
