package com.marcnuri.kwasmer;

import com.marcnuri.kwasmer.exec.ProcessExecutor;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.apps.DaemonSetBuilder;
import io.fabric8.kubernetes.api.model.apps.DaemonSetSpecBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.utils.KubernetesSerialization;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(name = "start", description = "Start a KWasm enabled Kind cluster")
public class Start implements Callable<Integer> {

  private static final String KIND_CLUSTER_NAME = "kwasmer";
  private static final String KUBE_CONFIG_CONTEXT = "kind-" + KIND_CLUSTER_NAME;
  private static final String KWASM_INSTALLER_IMAGE = "ghcr.io/kwasm/kwasm-node-installer:main";

  @CommandLine.Mixin
  ContextMixin context;
  @Inject
  KubernetesSerialization kubernetesSerialization;

  private final ProcessExecutor processExecutor;

  public Start() {
    this(new ProcessExecutor() {
    });
  }

  public Start(ProcessExecutor processExecutor) {
    this.processExecutor = processExecutor;
  }

  @Override
  public Integer call() {
    if (context.isHelpRequested()) {
      context.printUsage();
      return 0;
    }
    try {
      context.ansi("Checking Requirements\n");
      context.ansi("  ⚙  Docker executable");
      findDocker();
      context.ansiOverwrite("  ✅  Docker executable found\n");
      context.ansi("  ⚙  Kind executable");
      final Path kind = findKind();
      context.ansiOverwrite("  ✅  Kind executable found\n");
      context.ansi("  ⚙  kubectl executable");
      final Path kubectl = findKubectl();
      context.ansiOverwrite("  ✅  kubectl executable found\n");
      startOrGet(kind, kubectl);
      deployKwasm();
      return 0;
    } catch (Exception ex) {
      context.err().println("\n" + ex.getMessage());
      return 1;
    }
  }


  private Path findDocker() {
    final Path kindExecutable = processExecutor.findExecutable("docker");
    if (kindExecutable == null) {
      throw new IllegalStateException("docker executable not found in PATH");
    }
    return kindExecutable;
  }

  private Path findKind() {
    final Path kindExecutable = processExecutor.findExecutable("kind");
    if (kindExecutable == null) {
      throw new IllegalStateException("kind executable not found in PATH");
    }
    return kindExecutable;
  }

  private Path findKubectl() {
    final Path kubectlExecutable = processExecutor.findExecutable("kubectl");
    if (kubectlExecutable == null) {
      throw new IllegalStateException("kubectl executable not found in PATH");
    }
    return kubectlExecutable;
  }

  private void startOrGet(Path kind, Path kubectl) {
    final ProcessExecutor.ProcessResult getClusters = processExecutor.execute(
      kind, "get", "clusters");
    if (getClusters.exitCode() != 0 || !Arrays.asList(getClusters.output().split("\r?\n")).contains(KIND_CLUSTER_NAME)) {
      // Start a new Kind Cluster
      context.ansi("⚙  Starting new Kind cluster (" + KIND_CLUSTER_NAME + ")");
      final ProcessExecutor.ProcessResult createCluster = processExecutor.execute(
        kind, "create", "cluster", "--name=kwasmer");
      if (createCluster.exitCode() != 0) {
        throw new IllegalStateException("Error starting Kind Cluster:\n" + createCluster.output());
      }
      context.ansiOverwrite("✅  Kind cluster (" + KIND_CLUSTER_NAME + ") started\n");
    } else {
      // Reuse existent cluster
      context.ansi("Existing Kind cluster (" + KIND_CLUSTER_NAME + ") found");
    }
    // Set .kube/config context
    final ProcessExecutor.ProcessResult kubeContext = processExecutor.execute(
      kubectl, "config", "use-context", KUBE_CONFIG_CONTEXT);
    if (kubeContext.exitCode() != 0) {
      throw new IllegalStateException("Error setting kube context:\n" + kubeContext.output());
    }
    // Set context's namespace to default
    final ProcessExecutor.ProcessResult kubeNamespace = processExecutor.execute(
      kubectl, "config", "set-context", "--current", "--namespace=default");
    if (kubeNamespace.exitCode() != 0) {
      throw new IllegalStateException("Error setting kube context namespace:\n" + kubeContext.output());
    }
  }

  private void deployKwasm() {
    try (final KubernetesClient kc = new KubernetesClientBuilder()
      .withKubernetesSerialization(kubernetesSerialization)
      .build()) {
      final String currentContext = Optional.ofNullable(kc.getConfiguration())
        .map(Config::getCurrentContext).map(NamedContext::getName).orElse("");
      if (!currentContext.equals(KUBE_CONFIG_CONTEXT)) {
        throw new IllegalStateException("The current .kube/config context (" + currentContext + ") is not OK");
      }
      context.ansi("⚙  Deploying KWasm");
      final String kwasmInitializer = "kwasm-initializer";
      kc.apps().daemonSets().withName(kwasmInitializer).withGracePeriod(0L).delete();
      kc.apps().daemonSets().withName(kwasmInitializer).waitUntilCondition(Objects::isNull, 10L, TimeUnit.SECONDS);
      final ObjectMeta commonMetadata = new ObjectMetaBuilder()
        .withName(kwasmInitializer)
        .addToLabels("app", "default-init")
        .build();
      kc.apps().daemonSets().resource(new DaemonSetBuilder()
          .withMetadata(commonMetadata)
          .withSpec(new DaemonSetSpecBuilder()
            .withNewSelector().addToMatchLabels("app", "default-init").endSelector()
            .withNewUpdateStrategy().withType("RollingUpdate").endUpdateStrategy()
            .withTemplate(new PodTemplateSpecBuilder()
              .withMetadata(commonMetadata)
              .withSpec(new PodSpecBuilder()
                .withHostPID()
                .addNewVolume().withName("node-root").withNewHostPath().withPath("/").endHostPath().endVolume()
                .addNewVolume().withName("entrypoint").withNewConfigMap().withName("entrypoint").withDefaultMode(Integer.parseInt("0744", 8)).endConfigMap().endVolume()
                .addToInitContainers(new ContainerBuilder()
                  .withName(kwasmInitializer)
                  .withImage(KWASM_INSTALLER_IMAGE)
                  .addNewEnv().withName("NODE_ROOT").withValue("/mnt/node-root").endEnv()
                  .withNewSecurityContext().withPrivileged().endSecurityContext()
                  .addNewVolumeMount().withName("node-root").withMountPath("/mnt/node-root").endVolumeMount()
                  .build())
                .addToContainers(new ContainerBuilder()
                  .withName("pause")
                  .withImage("k8s.gcr.io/pause:3.9")
                  .build())
                .build())
              .build())
            .build())
          .build())
        .serverSideApply();
      context.ansiOverwrite("✅  KWasm deployed\n");
    }
  }
}
