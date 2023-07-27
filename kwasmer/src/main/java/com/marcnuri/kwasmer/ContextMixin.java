package com.marcnuri.kwasmer;

import picocli.CommandLine;

import java.io.PrintWriter;

public class ContextMixin {

  @CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message and exit.")
  boolean helpRequested;

  @CommandLine.Spec(CommandLine.Spec.Target.MIXEE)
  CommandLine.Model.CommandSpec spec;

  private PrintWriter out;
  private PrintWriter err;

  public final void ansiOverwrite(String text) {
    ansi("\u001b[1G\u001b[2K" + text);
  }

  public final void ansi(String text) {
    out().print(CommandLine.Help.Ansi.AUTO.text(text));
    out().flush();
  }

  public final PrintWriter out() {
    if (out == null) {
      out = spec.commandLine().getOut();
    }
    return out;
  }

  public final PrintWriter err() {
    if (err == null) {
      err = spec.commandLine().getErr();
    }
    return err;
  }

  public final boolean isHelpRequested() {
    return helpRequested;
  }

  public final void printUsage() {
    spec.commandLine().usage(out());
  }

}
