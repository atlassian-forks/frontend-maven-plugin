package com.github.eirslett.maven.plugins.frontend.lib.version.manager;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.client.VersionManagerClient;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.client.VersionManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionManagerRunner {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private final InstallConfig installConfig;

    private final VersionManagerCache versionManagerCache;

    private final VersionManagerClient versionManagerClient;

    public VersionManagerRunner(InstallConfig installConfig, VersionManagerCache versionManagerCache) {
        this.installConfig = installConfig;
        this.versionManagerCache = versionManagerCache;

        VersionManagerFactory versionManagerFactory = new VersionManagerFactory(installConfig, versionManagerCache);
        this.versionManagerClient = versionManagerFactory.getClient();
    }

    /**
     * This method updates global configs as a side effect
     *
     * @param nodeVersion
     */
    public void installNodeAndUpdateCaches(String nodeVersion) {
        if (!installConfig.isUseNodeVersionManager()) return;

        logger.info("Installing node with {}", versionManagerCache.getVersionManagerType());

        versionManagerClient.installNode(nodeVersion);
        populateCache();

        logger.info("Node {} has been installed using {}", nodeVersion, versionManagerCache.getVersionManagerType());
    }

    public void populateCache() {
        if (!installConfig.isUseNodeVersionManager()) return;
        logger.info("Populating version manager cache");

        this.versionManagerCache.setVersionManagerInstalled(true);
        this.versionManagerCache.setNodeExecutable(versionManagerClient.getNodeExecutable());
        this.versionManagerCache.setNpmExecutable(versionManagerClient.getNpmExecutable());
    }
}
