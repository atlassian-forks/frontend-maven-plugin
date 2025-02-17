package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AsdfClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final InstallConfig installConfig;

    public AsdfClient(InstallConfig installConfig) {
        this.installConfig = installConfig;
    }

    @Override
    public boolean isInstalled() {
        String asdfDir = getAsdfDir();
        logger.debug("Checking if ASDF installation directory exists: {}", asdfDir);
        return asdfDir != null;
    }

    @Override
    public File getNodeExecutable(String nodeVersion) {
        String asdfDir = getAsdfDir();
        return Paths.get(asdfDir, "installs", "nodejs", nodeVersion, "bin", "node").toFile();
    }

    @Override
    public File getNpmExecutable(String nodeVersion) {
        File nodeExec = getNodeExecutable(nodeVersion);
        return Paths.get(nodeExec.getParent(), "npm").toFile();
    }

    private String getAsdfDir() {
        String asdfDir = System.getenv("ASDF_DIR");
        if (asdfDir != null) {
            Path path = Paths.get(asdfDir);
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        String home = System.getenv("HOME");
        if (home != null) {
            Path path = Paths.get(home, ".asdf");
            if (Files.exists(path)) {
                return path.toString();
            }

            path = Paths.get(home, ".local", "share", "asdf");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        return null;
    }
}
