package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MiseClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final InstallConfig installConfig;

    public MiseClient(InstallConfig installConfig) {
        this.installConfig = installConfig;
    }

    @Override
    public boolean isInstalled() {
        String miseDir = getMiseDir();
        logger.debug("Checking if MISE installation directory exists: {}", miseDir);

        return miseDir != null;
    }

    @Override
    public File getNodeExecutable(String nodeVersion) {
        String miseNodeDir = getMiseDir();
        if (miseNodeDir == null) return null;

        String cleanNodeVersion = nodeVersion.replace("v", "");
        return Paths.get(miseNodeDir, cleanNodeVersion, "bin", "node").toFile();
    }

    @Override
    public File getNpmExecutable(String nodeVersion) {
        File nodeExec = getNodeExecutable(nodeVersion);
        return Paths.get(nodeExec.getParentFile().getParent(), "/lib/node_modules/npm/bin/npm-cli.js").toFile();
    }

    private String getMiseDir() {
        String miseDataDir = System.getenv("MISE_DATA_DIR");
        if (miseDataDir != null) {
            Path path = Paths.get(miseDataDir, "installs", "node");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        String xdgDataDir = System.getenv("XDG_DATA_HOME");
        if (miseDataDir != null) {
            Path path = Paths.get(xdgDataDir, "mise", "installs", "node");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        String home = System.getenv("HOME");
        if (home != null) {
            Path path = Paths.get(home, ".local", "share", "mise", "installs", "node");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        return null;
    }
}
