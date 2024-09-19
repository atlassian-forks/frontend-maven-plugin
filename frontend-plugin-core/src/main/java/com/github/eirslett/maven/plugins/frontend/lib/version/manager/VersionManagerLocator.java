package com.github.eirslett.maven.plugins.frontend.lib.version.manager;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;

import java.util.Arrays;

public class VersionManagerLocator {

    private final ShellExecutor shellExecutor;

    public VersionManagerLocator(InstallConfig installConfig) {
        this.shellExecutor = new ShellExecutor(installConfig);
    }

    public VersionManagerType findAvailable() {
        for (VersionManagerType versionManagerType : VersionManagerType.values()) {
            if(isVersionManagerLoaded(versionManagerType.getExecutable())) return versionManagerType;
        }
        return null;
    }

    private boolean isVersionManagerLoaded(String executable) {
        shellExecutor.execute(Arrays.asList("echo", "$HOME"));
        shellExecutor.execute(Arrays.asList("ls"));

        String result = shellExecutor.execute(Arrays.asList("command", "-v", executable));
        if (!result.isEmpty()) {
            // needed to mock out version managers in tests
            String version = shellExecutor.execute(Arrays.asList(executable, "--version"));
            return !version.isEmpty();
        }
        return false;
    }
}
