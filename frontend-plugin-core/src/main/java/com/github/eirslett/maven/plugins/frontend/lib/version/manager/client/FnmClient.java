package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.ShellExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

public class FnmClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final ShellExecutor shellExecutor;

    private static final String EXECUTABLE = "fnm";

    public FnmClient(ShellExecutor shellExecutor) {
        this.shellExecutor = shellExecutor;
    }

    @Override
    public void installNode(String nodeVersion) {
        logger.debug("Installing node {}", nodeVersion);

        shellExecutor.execute(Arrays.asList(
            EXECUTABLE, "install", nodeVersion
        ));
        shellExecutor.execute(Arrays.asList(
            EXECUTABLE, "use", nodeVersion
        ));
    }

    @Override
    public File getNodeExecutable() {
        // FIXME this produces different result every time it's run
        // this is because of multi shell caching done on fnm side
        return new File(shellExecutor.execute(Arrays.asList("which", "node")));
    }

    @Override
    public File getNpmExecutable() {
        File nodeExec = getNodeExecutable();
        return new File(nodeExec.getParent(), "npm");
    }
}
