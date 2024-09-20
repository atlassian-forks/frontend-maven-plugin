package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.ShellExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MiseClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final ShellExecutor shellExecutor;

    private static final String EXECUTABLE = "mise";

    public MiseClient(ShellExecutor shellExecutor) {
        this.shellExecutor = shellExecutor;
    }

    @Override
    public boolean isInstalled() {
        String version = shellExecutor.execute(Arrays.asList(
            EXECUTABLE, "--version"
        ));

        return version.matches("\\d+\\.\\d+\\.\\d+ .*");
    }

    @Override
    public void installNode(String nodeVersion) {
        logger.debug("Installing node {}", nodeVersion);

        String nodeAsMisePlugin = String.format("node@%s", nodeVersion);
        shellExecutor.execute(Arrays.asList(
            EXECUTABLE, "install", nodeAsMisePlugin
        ));
        shellExecutor.execute(Arrays.asList(
            EXECUTABLE, "use", nodeAsMisePlugin
        ));
    }

    @Override
    public File getNodeExecutable() {
        return new File(shellExecutor.execute(Arrays.asList(
            EXECUTABLE, "which", "node"
        )));
    }

    @Override
    public File getNpmExecutable() {
        File nodeExec = getNodeExecutable();
        return Paths.get(nodeExec.getParentFile().getParent(), "/lib/node_modules/npm/bin/npm-cli.js").toFile();
    }
}
