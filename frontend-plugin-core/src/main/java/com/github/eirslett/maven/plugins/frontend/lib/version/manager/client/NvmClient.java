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

    private static final String EXECUTABLE = "nvm";

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
    public void installNode() {
        commandExecutor
            .withShell()
            .withSourced(getNvmScript())
            .executeOrFail(Arrays.asList(
                EXECUTABLE, "install"
            ));
    }

    @Override
    public File getNodeExecutable() {
        String nodePath = commandExecutor
            .withShell()
            .withSourced(getNvmScript())
            .executeOrFail(Arrays.asList(
                EXECUTABLE, "which", "node"
            ));
        return new File(nodePath);
    }

    @Override
    public File getNpmExecutable() {
        File nodeExec = getNodeExecutable();
        return new File(nodeExec.getParent(), "npm");
    }

    private String getNvmScript() {
        String nvmDir = getNvmDir();
        String nvmScript = Paths.get(nvmDir, "nvm.sh").toString();

        return nvmScript;
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
