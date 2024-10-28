package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class MiseClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final CommandExecutor commandExecutor;

    public MiseClient(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public boolean isInstalled() {
        String miseBinDir = getMiseBinDir();
        logger.debug("Checking if MISE installation directory exists: {}", miseBinDir);

        return miseBinDir != null;
    }

    @Override
    public void installNode() {
        commandExecutor
            .withPath(getMiseBinDir())
            .executeOrFail(Arrays.asList(getExecutable(), "install", "node"));
    }

    @Override
    public File getNodeExecutable() {
        String nodePath = commandExecutor
            .withPath(getMiseBinDir())
            .executeOrFail(Arrays.asList(getExecutable(), "which", "node"));

        return new File(nodePath);
    }

    @Override
    public File getNpmExecutable() {
        File nodeExec = getNodeExecutable();
        return Paths.get(nodeExec.getParentFile().getParent(), "/lib/node_modules/npm/bin/npm-cli.js").toFile();
    }

    private String getExecutable() {
        return Paths.get(getMiseBinDir(), "mise").toString();
    }

    private String getMiseBinDir() {
        String miseInstallPath = System.getenv("MISE_INSTALL_PATH");
        if (miseInstallPath != null) {
            Path path = Paths.get(miseInstallPath);
            if (Files.exists(path)) {
                return path.getParent().toString();
            }
        }

        String home = System.getenv("HOME");
        if (home != null) {
            Path path = Paths.get(home, ".local", "bin", "mise");
            if (Files.exists(path)) {
                return path.getParent().toString();
            }
        }

        return null;
    }
}
