# WebAssembly (WASM) - WASI playground

## Container images

### Java

See the specific [README.md](./java/README.md) for more details about the container image and how it's built.

### JavaScript

See the specific [README.md](./javascript/README.md) for more details about the container image and how it's built.


### Running the images

#### Podman

[Podman](https://github.com/containers/podman) supports running wasi/wasm OCI container images through [crun](https://github.com/containers/crun).

Requirements:
- crun
- crun-wasm

```bash
# Run the JavaScript image
podman run -it --rm --platform=wasi/wasm docker.io/marcnuri/hello-world-of-wasm:js
# Run the Java image
podman run -it --rm --platform=wasi/wasm docker.io/marcnuri/hello-world-of-wasm:java
```

#### Docker Desktop
