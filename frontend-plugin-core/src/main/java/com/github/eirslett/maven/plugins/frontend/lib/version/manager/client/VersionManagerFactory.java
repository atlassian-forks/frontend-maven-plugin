package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.CommandExecutor;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerType;

public class VersionManagerFactory {

    final CommandExecutor commandExecutor;

    public VersionManagerFactory(InstallConfig installConfig) {
        this.commandExecutor = new CommandExecutor(installConfig);
    }

    public VersionManagerClient getClient(VersionManagerType type) {
        if (type == VersionManagerType.FNM) {
            return new FnmClient(commandExecutor);
        } else if (type == VersionManagerType.NVM) {
            return new NvmClient(commandExecutor);
        } else if (type == VersionManagerType.NVS) {
            return new NvsClient(commandExecutor);
        } else if (type == VersionManagerType.MISE) {
            return new MiseClient(commandExecutor);
        } else if (type == VersionManagerType.ASDF) {
            return new AsdfClient(commandExecutor);
        }

        throw new RuntimeException(String.format("Version manager (%s) type is not implemented", type));
    }
}
