package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.ShellExecutor;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerCache;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerType;

public class VersionManagerFactory {

    final ShellExecutor shellExecutor;

    final VersionManagerCache versionManagerCache;

    public VersionManagerFactory(InstallConfig installConfig, VersionManagerCache versionManagerCache) {
        this.shellExecutor = new ShellExecutor(installConfig);
        this.versionManagerCache = versionManagerCache;
    }

    public VersionManagerClient getClient() {
        VersionManagerType type = versionManagerCache.getVersionManagerType();
        if (type == VersionManagerType.FNM) {
            return new FnmClient(shellExecutor);
        } else if (type == VersionManagerType.NVM) {
            return new NvmClient(shellExecutor);
        }

        throw new RuntimeException(String.format("Version manager (%s) type is not implemented", type));
    }
}
