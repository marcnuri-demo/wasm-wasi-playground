# WebAssembly (WASM) - WASI playground

## Container images

### JavaScript

See the specific [README.md](./javascript/README.md) for more details about the container image and how it's built.


### Running the images

#### Podman

[Podman](https://github.com/containers/podman) supports running wasi/wasm OCI container images through [crun](https://github.com/containers/crun).

Requirements:
- crun
- crun-wasm

```bash
podman run -it --rm --platform=wasi/wasm marcnuri/hello-world-of-wasm:js
```

#### Docker Desktop
