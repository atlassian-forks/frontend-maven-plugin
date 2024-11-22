package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import java.io.File;

public interface VersionManagerClient {

    boolean isInstalled();

    File getNodeExecutable(String nodeVersion);

    File getNpmExecutable(String nodeVersion);
}
