package com.github.eirslett.maven.plugins.frontend.lib.version.manager;

import java.io.File;

public class VersionManagerCache {

    private VersionManagerType versionManagerType;

    private File nodeExecutable;
    private File npmExecutable;

    public VersionManagerCache(VersionManagerType versionManagerType) {
        this.versionManagerType = versionManagerType;
    }

    public VersionManagerCache() {

    }

    public VersionManagerType getVersionManagerType() {
        return versionManagerType;
    }

    public boolean isVersionManagerAvailable() {
        return versionManagerType != null;
    }

    public File getNodeExecutable() {
        return nodeExecutable;
    }

    public void setNodeExecutable(File nodeExecutable) {
        this.nodeExecutable = nodeExecutable;
    }

    public File getNpmExecutable() {
        return npmExecutable;
    }

    public void setNpmExecutable(File npmExecutable) {
        this.npmExecutable = npmExecutable;
    }

    public boolean isNodeAvailable() {
        File nodeExecutable = getNodeExecutable();
        return nodeExecutable != null && nodeExecutable.exists();
    }
}
