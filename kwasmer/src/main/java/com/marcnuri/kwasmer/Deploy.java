package com.marcnuri.kwasmer;


import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static com.marcnuri.kwasmer.Kwasmer.RUNTIME_CLASS_NAME;

@CommandLine.Command(name = "deploy", description = "Deploy the provided (wasi/wasm) image to the current cluster")
public class Deploy implements Callable<Integer> {

  @CommandLine.Mixin
  ContextMixin context;
  @Inject
  KubernetesSerialization kubernetesSerialization;

  @CommandLine.Parameters(index = "0", description = "The image to deploy (for example marcnuri/hello-world-of-wasm:js)")
  private String image;

  @Override
  public Integer call() {
    context.ansi("⚙  Deploying: " + image);
    try (final KubernetesClient kc = new KubernetesClientBuilder()
      .withKubernetesSerialization(kubernetesSerialization)
      .build()) {
      final String name = image.replaceAll("[^a-zA-Z0-9]", "-");
      final Pod pod = new PodBuilder()
        .withMetadata(new ObjectMetaBuilder()
          .withName(name)
          .addToAnnotations("module.wasm.image/variant", "compat-smart")
          .build())
        .withSpec(new PodSpecBuilder()
          .addNewContainer()
          .withName(name)
          .withImage(image)
          .endContainer()
          .withRuntimeClassName(RUNTIME_CLASS_NAME)
          .build())
        .build();
      kc.pods().resource(pod).serverSideApply();
    }
    context.ansiOverwrite("✅  " + image + " deployed\n");
    return null;
  }
}
