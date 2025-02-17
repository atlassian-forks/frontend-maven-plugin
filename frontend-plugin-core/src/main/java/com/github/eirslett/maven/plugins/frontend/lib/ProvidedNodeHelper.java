package com.github.eirslett.maven.plugins.frontend.lib;

import java.io.File;

import static java.util.Objects.isNull;

public abstract class ProvidedNodeHelper {
    public static boolean hasProvidedNode(InstallConfig installConfig) {
        File nodeExecutable = getProvidedNodeDirectory(installConfig);
        return !isNull(nodeExecutable) && nodeExecutable.exists();
    }

    public static File getProvidedNodeDirectory(InstallConfig installConfig) {
        File configuredNodeDirectory = installConfig.getInstalledNodeDirectory();
        if (!isNull(configuredNodeDirectory) && configuredNodeDirectory.exists()) return configuredNodeDirectory;

        String systemNodeDirectoryPath = System.getenv("AFMP_INSTALLED_NODE_DIRECTORY");
        if (!isNull(systemNodeDirectoryPath)) {
            File systemNodeDirectory = new File(systemNodeDirectoryPath);
            if (systemNodeDirectory.exists()) return systemNodeDirectory;
        }

        return null;
    }
}
