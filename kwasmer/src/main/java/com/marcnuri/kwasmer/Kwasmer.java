package com.marcnuri.kwasmer;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;


@TopCommand
@CommandLine.Command(
  name = "kwasmer",
  description = "KWasmer provides a set of tools to help you run WebAssembly Containers on Kubernetes",
  subcommands = {
    Start.class,
    Deploy.class
  }
)
public class Kwasmer {
  static final String RUNTIME_CLASS_NAME = "crun";
}
