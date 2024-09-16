package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import java.io.File;

public interface VersionManagerClient {

    void installNode(String nodeVersion);

    File getNodeExecutable();

    File getNpmExecutable();
}
