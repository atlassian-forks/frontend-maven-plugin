package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.ShellExecutor;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerType;

public class VersionManagerFactory {

    final ShellExecutor shellExecutor;

    public VersionManagerFactory(InstallConfig installConfig) {
        this.shellExecutor = new ShellExecutor(installConfig);
    }

    public VersionManagerClient getClient(VersionManagerType type) {
        if (type == VersionManagerType.FNM) {
            return new FnmClient(shellExecutor);
        } else if (type == VersionManagerType.NVM) {
            return new NvmClient(shellExecutor);
        } else if (type == VersionManagerType.NVS) {
            return new NvsClient(shellExecutor);
        } else if (type == VersionManagerType.MISE) {
            return new MiseClient(shellExecutor);
        }

        throw new RuntimeException(String.format("Version manager (%s) type is not implemented", type));
    }
}
