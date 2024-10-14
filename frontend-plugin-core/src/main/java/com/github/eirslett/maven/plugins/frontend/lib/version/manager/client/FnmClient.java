package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.ShellExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FnmClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final ShellExecutor shellExecutor;

    private static final String EXECUTABLE = "fnm";

    public FnmClient(ShellExecutor shellExecutor) {
        this.shellExecutor = shellExecutor;
    }

    @Override
    public boolean isInstalled() {
        String version = cleanOutput(shellExecutor.executeAndCatchErrors(Arrays.asList(
            EXECUTABLE, "--version"
        )));

        return version.matches("fnm \\d+\\.\\d+\\.\\d+");
    }

    @Override
    public void installNode() {
        shellExecutor.executeOrFail(Arrays.asList(
            EXECUTABLE, "use", "--install-if-missing"
        ));

        // FIXME verify node installed
        shellExecutor.executeOrFail(Arrays.asList(
            "node", "--version"
        ));
    }

    @Override
    public File getNodeExecutable() {
        String currentNodeVersion = cleanOutput(shellExecutor.executeOrFail(Arrays.asList(EXECUTABLE, "current")));
        String fnmDir = cleanOutput(shellExecutor.executeOrFail(Arrays.asList("echo", "$FNM_DIR")));

        return Paths.get(fnmDir, "node-versions", currentNodeVersion, "installation", "bin", "node").toFile();
    }

    @Override
    public File getNpmExecutable() {
        File nodeExec = getNodeExecutable();
        return new File(nodeExec.getParent(), "npm");
    }


    private String cleanOutput(String output) {
        String[] lines = output.split(System.lineSeparator());

        return Arrays.stream(lines)
            .filter(line -> !line.startsWith("Using Node")) // fnm echos which version is used when using `--use-on-cd`
            .collect(Collectors.joining(System.lineSeparator()));
    }
}
