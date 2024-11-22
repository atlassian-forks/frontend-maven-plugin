package com.github.eirslett.maven.plugins.frontend.lib.version.manager;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import com.github.eirslett.maven.plugins.frontend.lib.Utils;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.client.VersionManagerClient;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.client.VersionManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VersionManagerRunner {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private final InstallConfig installConfig;

    private final VersionManagerCache versionManagerCache;

    private final VersionManagerClient versionManagerClient;

    public VersionManagerRunner(InstallConfig installConfig, VersionManagerCache versionManagerCache) {
        this.installConfig = installConfig;
        this.versionManagerCache = versionManagerCache;

        VersionManagerFactory versionManagerFactory = new VersionManagerFactory(installConfig);
        this.versionManagerClient = versionManagerFactory.getClient(versionManagerCache.getVersionManagerType());
    }

    public void populateCacheForVersion(String nodeVersion) {
        if (!installConfig.isUseNodeVersionManager()) return;
        logger.debug("Populating version manager cache for node: {}", nodeVersion);

        this.versionManagerCache.setNodeExecutable(versionManagerClient.getNodeExecutable(nodeVersion));
        this.versionManagerCache.setNpmExecutable(versionManagerClient.getNpmExecutable(nodeVersion));

        if (versionManagerCache.isNodeAvailable()) {
            logger.info("Using {} version manager", versionManagerCache.getVersionManagerType());
            logger.info("Requested node version {} is already installed", nodeVersion);
        } else {
            logger.warn("Requested node version {} is not installed in version manager", nodeVersion);
        }
    }
}
