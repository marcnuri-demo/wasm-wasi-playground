package com.marcnuri.kwasmer.exec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ProcessExecutor {

  default boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("windows");
  }

  default Path findExecutable(String name) {
    final Set<Path> paths = Stream.of(System.getenv("PATH").split(File.pathSeparator))
      .map(Paths::get)
      .collect(Collectors.toSet());
    final Path raw = resolveExecutable(paths, name);
    if (raw != null) {
      return raw;
    } else if (isWindows()) {
      for (String winExtensions : new String[]{".cmd", ".bat", ".exe"}) {
        final Path win = resolveExecutable(paths, name + winExtensions);
        if (win != null) {
          return win;
        }
      }
    }
    return null;
  }

  default ProcessResult execute(Path executable, String... args) {
    return execute(Paths.get(System.getProperty("user.dir")), executable, args);
  }

  default ProcessResult execute(Path workDir, Path executable, String... args) {
    try {
      final Process process = new ProcessBuilder()
        .directory(workDir.toFile())
        .command(Stream.concat(Stream.of(executable.toFile().getAbsolutePath()), Stream.of(args)).collect(Collectors.toList()))
        .redirectErrorStream(true)
        .start();
      return new ProcessResult(
        process.waitFor(),
        new String(process.getInputStream().readAllBytes())
      );
    } catch (InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Process stopped");
    } catch (IOException ex) {
      throw new RuntimeException("Error executing " + executable, ex);
    }
  }

  private static Path resolveExecutable(Set<Path> paths, String name) {
    return paths.stream()
      .map(path -> path.resolve(name))
      .filter(p -> p.toFile().exists())
      .findFirst()
      .orElse(null);
  }

  record ProcessResult(int exitCode, String output) {
  }
}
