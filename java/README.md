# Java

Example project written in Java to demonstrate wasi/wasm usage and capabilities.

The project is just a very simple application that displays a message on the console.

## Wasm compilation

In order to generate the wasm bytecode, this project uses a fork of [TeaVM](https://github.com/konsoletyper/teavm).

The [fork](https://github.com/fermyon/teavm-wasi) created by Fermyon, adds support for WASI which is not supported by the original project (see https://github.com/konsoletyper/teavm/issues/575). 

I leverage maven plugin to generate the wasm bytecode.

You can generate the wasm bytecode by running:

```bash
mvn package
```

## Image generation

To generate a `wasi/wasm` compatible image I leverage [Buildah](https://github.com/containers/buildah) which doesn't have any additional requirements.

The Buildah command to generate build the image is the following:

```bash
buildah build --annotation "module.wasm.image/variant=compat" --platform "wasi/wasm" -t marcnuri/hello-world-of-wasm:java .
```

For the CI pipeline (build+publish) I use the Red Hat provided GitHub actions that simplify the process:
- https://github.com/redhat-actions/buildah-build
- https://github.com/redhat-actions/push-to-registry 
