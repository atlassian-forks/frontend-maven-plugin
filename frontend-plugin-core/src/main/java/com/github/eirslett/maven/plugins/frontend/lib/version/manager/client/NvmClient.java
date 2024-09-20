package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.ShellExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

public class NvmClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final ShellExecutor shellExecutor;

    private static final String EXECUTABLE = "nvm";

    public NvmClient(ShellExecutor shellExecutor) {
        this.shellExecutor = shellExecutor;
    }

    @Override
    public boolean isInstalled() {
        String version = shellExecutor.execute(Arrays.asList(
            EXECUTABLE, "--version"
        ));

        return version.matches("\\d+\\.\\d+\\.\\d+");
    }

    @Override
    public void installNode() {
        shellExecutor.execute(Arrays.asList(
            EXECUTABLE, "install"
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
        return new File(nodeExec.getParent(), "npm");
    }
}
