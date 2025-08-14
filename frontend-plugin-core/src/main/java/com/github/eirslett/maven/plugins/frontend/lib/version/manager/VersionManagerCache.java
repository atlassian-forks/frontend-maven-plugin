package com.github.eirslett.maven.plugins.frontend.lib.version.manager;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static org.slf4j.LoggerFactory.getLogger;

public class VersionManagerCache {

    private static final Logger log = getLogger(VersionManagerCache.class);

    private VersionManagerType versionManagerType;

    private Optional<File> corepackModuleDir = null;
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

    @Nonnull
    public Optional<File> getCorepackModuleDir() {
        if (isNull(corepackModuleDir)) {
            log.debug("Corepack executable has not been set in VersionManagerCache, defaulting to nothing");
            return empty();
        }
        return corepackModuleDir;
    }

    public void setCorepackModuleDir(@Nullable File corepackExecutable) {
        this.corepackModuleDir = Optional.ofNullable(corepackExecutable);
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
