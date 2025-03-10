package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NvsClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final InstallConfig installConfig;

    public NvsClient(InstallConfig installConfig) {
        this.installConfig = installConfig;
    }

    @Override
    public boolean isInstalled() {
        String nvsDir = getNvsDir();
        logger.debug("Checking if NVS installation directory exists: {}", nvsDir);
        return nvsDir != null;
    }

    @Override
    public File getNodeExecutable(String nodeVersion) {
        String nvsDir = getNvsDir();
        String cleanNodeVersion = nodeVersion.replace("v", "");
        String architecture = installConfig.getPlatform().getArchitectureName();
        return Paths.get(nvsDir, "node", cleanNodeVersion, architecture, "bin", "node").toFile();
    }

    @Override
    public File getNpmExecutable(String nodeVersion) {
        File nodeExec = getNodeExecutable(nodeVersion);
        return new File(nodeExec.getParent(), "npm");
    }

    private String getNvsDir() {
        String nvsDir = System.getenv("NVS_DIR");
        if (nvsDir != null) {
            Path path = Paths.get(nvsDir);
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        String home = System.getenv("HOME");
        if (home != null) {
            Path path = Paths.get(home, ".nvs");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        return null;
    }
}
