package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerType;

public class VersionManagerFactory {

    final InstallConfig installConfig;

    public VersionManagerFactory(InstallConfig installConfig) {
        this.installConfig = installConfig;
    }

    public VersionManagerClient getClient(VersionManagerType type) {
        if (type == VersionManagerType.FNM) {
            return new FnmClient(installConfig);
        } else if (type == VersionManagerType.NVM) {
            return new NvmClient(installConfig);
        } else if (type == VersionManagerType.NVS) {
            return new NvsClient(installConfig);
        } else if (type == VersionManagerType.MISE) {
            return new MiseClient(installConfig);
        } else if (type == VersionManagerType.ASDF) {
            return new AsdfClient(installConfig);
        }

        throw new RuntimeException(String.format("Version manager (%s) type is not implemented", type));
    }
}
