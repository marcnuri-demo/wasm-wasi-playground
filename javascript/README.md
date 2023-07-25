# JavaScript

Example project written in JavaScript to demonstrate wasi/wasm usage and capabilities.

The project is just a very simple application that displays a message on the console.

## Wasm compilation

In order to generate the wasm bytecode, this project uses [Javy](https://github.com/bytecodealliance/javy) from Bytecode Alliance.

I leverage npm to install the javy-cli binary.

The execution of the `javy compile` instruction is also performed through npm.

## Image generation

To generate a `wasi/wasm` compatible image I leverage [Buildah](https://github.com/containers/buildah) which doesn't have any additional requirements.

The Buildah command to generate build the image is the following:

```bash
buildah build --annotation "module.wasm.image/variant=compat" --platform "wasi/wasm" -t marcnuri/hello-world-of-wasm:js .
```

For the CI pipeline (build+publish) I use the Red Hat provided Git Hub actions that simplify the process:
- https://github.com/redhat-actions/buildah-build
- https://github.com/redhat-actions/push-to-registry 
