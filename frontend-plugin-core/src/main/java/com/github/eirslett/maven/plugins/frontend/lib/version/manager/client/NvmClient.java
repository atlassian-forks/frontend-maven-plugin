package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class NvmClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final CommandExecutor commandExecutor;

    public NvmClient(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public boolean isInstalled() {
        String nvmDir = getNvmDir();
        logger.debug("Checking if NVM installation directory exists: {}", nvmDir);
        return nvmDir != null;
    }

    @Override
    public File getNodeExecutable(String nodeVersion) {
        String nodePath = ""; // TODO
        return new File(nodePath);
    }

    @Override
    public File getNpmExecutable(String nodeVersion) {
        File nodeExec = getNodeExecutable(nodeVersion);
        return new File(nodeExec.getParent(), "npm");
    }

    private String getNvmDir() {
        String nvmDir = System.getenv("NVM_DIR");
        if (nvmDir != null) {
            Path path = Paths.get(nvmDir);
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        String home = System.getenv("HOME");
        if (home != null) {
            Path path = Paths.get(home, ".nvm");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
        if (xdgConfigHome != null) {
            Path path = Paths.get(xdgConfigHome, "nvm");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        return null;
    }
}
