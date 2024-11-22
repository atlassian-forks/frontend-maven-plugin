package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FnmClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final InstallConfig installConfig;

    public FnmClient(InstallConfig installConfig) {
        this.installConfig = installConfig;
    }

    @Override
    public boolean isInstalled() {
        String fnmDir = getFnmDir();
        logger.debug("Checking if FNM installation directory exists: {}", fnmDir);

        return fnmDir != null;
    }

    @Override
    public File getNodeExecutable(String nodeVersion) {
        String fnmDir = getFnmDir();
        return Paths.get(fnmDir, "node-versions", nodeVersion, "installation", "bin", "node").toFile();
    }

    @Override
    public File getNpmExecutable(String nodeVersion) {
        File nodeExec = getNodeExecutable(nodeVersion);
        return new File(nodeExec.getParent(), "npm");
    }

    private String getFnmDir() {
        String fnmDir = System.getenv("FNM_DIR");
        if (fnmDir != null) {
            Path path = Paths.get(fnmDir);
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        String home = System.getenv("HOME");
        if (home != null) {
            Path path = Paths.get(home, ".fnm");
            if (Files.exists(path)) {
                return path.toString();
            }

            path = Paths.get(home, "Library", "Application Support", "fnm");
            if (Files.exists(path)) {
                return path.toString();
            }

            path = Paths.get(home, ".local", "share", "fnm");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null ) {
            Path path = Paths.get(xdgDataHome, "fnm");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        return null;
    }
}
