package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.ShellExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

public class AsdfClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final ShellExecutor shellExecutor;

    private static final String EXECUTABLE = "asdf";

    public AsdfClient(ShellExecutor shellExecutor) {
        this.shellExecutor = shellExecutor;
    }

    @Override
    public boolean isInstalled() {
        String version = shellExecutor.execute(Arrays.asList(
            EXECUTABLE, "--version"
        ));

        return version.matches("v\\d+\\.\\d+\\.\\d+-[0-9a-z]+");
    }

    @Override
    public void installNode(String nodeVersion) {
        logger.debug("Installing node {}", nodeVersion);

        shellExecutor.execute(Arrays.asList(
            EXECUTABLE, "plugin", "add", "nodejs"
        ));
        shellExecutor.execute(Arrays.asList(
            EXECUTABLE, "install", "nodejs", nodeVersion
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
        return Paths.get(nodeExec.getParent(), "npm").toFile();
    }
}
