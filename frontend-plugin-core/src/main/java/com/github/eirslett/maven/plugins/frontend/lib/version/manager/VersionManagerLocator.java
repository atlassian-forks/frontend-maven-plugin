package com.github.eirslett.maven.plugins.frontend.lib.version.manager;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.client.VersionManagerClient;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.client.VersionManagerFactory;

import java.util.Arrays;

public class VersionManagerLocator {

    private final InstallConfig installConfig;

    public VersionManagerLocator(InstallConfig installConfig) {
        this.installConfig = installConfig;
    }

    public VersionManagerType findAvailable() {
        VersionManagerFactory versionManagerFactory = new VersionManagerFactory(installConfig);
        for (VersionManagerType versionManagerType : VersionManagerType.values()) {
            VersionManagerClient versionManagerClient = versionManagerFactory.getClient(versionManagerType);
            if (versionManagerClient.isInstalled()) return versionManagerType;
        }
        return null;
    }
}
