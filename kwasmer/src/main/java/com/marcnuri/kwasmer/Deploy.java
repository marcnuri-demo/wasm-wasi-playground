package com.marcnuri.kwasmer;


import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;
import io.quarkus.runtime.util.StringUtil;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.util.Locale;
import java.util.concurrent.Callable;

import static com.marcnuri.kwasmer.Kwasmer.RUNTIME_CLASS_NAME;

@CommandLine.Command(name = "deploy", description = "Deploy the provided (wasi/wasm) image to the current cluster")
public class Deploy implements Callable<Integer> {

  @CommandLine.Mixin
  ContextMixin context;
  @Inject
  KubernetesSerialization kubernetesSerialization;
  @CommandLine.Parameters(index = "0", description = "The image to deploy (for example marcnuri/hello-world-of-wasm:js)")
  String image;
  @CommandLine.Option(names = {"-n", "--name"}, description = "The name to use for the deployed controller (Pod, Job, etc.)")
  String name;
  @CommandLine.Option(names = {"-c", "--controller"}, description = "The controller to use for the deployment (one of: Pod, Job, Deployment)")
  String controller;

  @Override
  public Integer call() {
    context.ansi("⚙  Deploying: " + image);
    try (final KubernetesClient kc = new KubernetesClientBuilder()
      .withKubernetesSerialization(kubernetesSerialization)
      .build()) {
      final String finalName = StringUtil.isNullOrEmpty(name) ?
        image.replaceAll("[^a-zA-Z0-9]", "-") :
        name;
      final PodTemplateSpecBuilder podTemplate = new PodTemplateSpecBuilder()
        .withMetadata(new ObjectMetaBuilder()
          .withName(finalName)
          .addToAnnotations("module.wasm.image/variant", "compat-smart")
          .addToLabels("app.kubernetes.io/name", finalName)
          .build())
        .withSpec(new PodSpecBuilder()
          .addNewContainer()
          .withName(finalName)
          .withImage(image)
          .endContainer()
          .withRuntimeClassName(RUNTIME_CLASS_NAME)
          .build());
      kc.resource(initController(podTemplate)).serverSideApply();
    }
    context.ansiOverwrite("✅  " + image + " deployed\n");
    return null;
  }

  private HasMetadata initController(PodTemplateSpecBuilder podTemplateSpec) {
    return switch (controller.toLowerCase(Locale.ROOT)) {
      case "deployment" -> new DeploymentBuilder()
        .withMetadata(podTemplateSpec.buildMetadata())
        .withNewSpec()
        .withNewSelector().addToMatchLabels(podTemplateSpec.buildMetadata().getLabels()).endSelector()
        .withTemplate(podTemplateSpec.build()).endSpec()
        .build();
      case "job" -> new JobBuilder()
        .withMetadata(podTemplateSpec.buildMetadata())
        .withNewSpec().withTemplate(podTemplateSpec
          .editSpec().withRestartPolicy("Never").endSpec()
          .build()).endSpec()
        .build();
      default -> new PodBuilder()
        .withMetadata(podTemplateSpec.getMetadata())
        .withSpec(podTemplateSpec.getSpec())
        .build();
    };
  }
}
