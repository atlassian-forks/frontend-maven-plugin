package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Optional;

public interface VersionManagerClient {

    boolean isInstalled();

    File getNodeExecutable(String nodeVersion);

    File getNpmExecutable(String nodeVersion);

    @Nonnull
    Optional<File> getCorepackModuleDir(String nodeVersion);
}
