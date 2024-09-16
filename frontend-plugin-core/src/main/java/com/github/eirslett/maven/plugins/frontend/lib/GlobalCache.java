package com.github.eirslett.maven.plugins.frontend.lib;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerCache;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.client.VersionManagerClient;

// TODO add builder pattern
public class GlobalCache {

    private static InstallConfig installConfig;

    private static VersionManagerCache versionManagerCache;


    public static InstallConfig getInstallConfig() {
        assert installConfig != null;
        return installConfig;
    }

    public static void setInstallConfig(InstallConfig installConfig) {
        GlobalCache.installConfig = installConfig;
    }

    public static VersionManagerCache getVersionManagerCache() {
        assert versionManagerCache != null;
        return versionManagerCache;
    }

    public static void setVersionManagerCache(VersionManagerCache versionManagerCache) {
        GlobalCache.versionManagerCache = versionManagerCache;
    }
}
