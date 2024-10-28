package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class NvsClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final CommandExecutor commandExecutor;

    private static final String EXECUTABLE = "nvs";

    public NvsClient(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public boolean isInstalled() {
        String nvsDir = getNvsDir();
        logger.debug("Checking if NVS installation directory exists: {}", nvsDir);
        return nvsDir != null;
    }

    @Override
    public void installNode() {
        commandExecutor
            .withShell()
            .withSourced(getNvsScript())
            .executeOrFail(Arrays.asList(
                EXECUTABLE, "add"
            ));
    }

    @Override
    public File getNodeExecutable() {
        String nodePath = commandExecutor
            .withShell()
            .withSourced(getNvsScript())
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


    private String getNvsScript() {
        String nvsDir = getNvsDir();
        String nvsScript = Paths.get(nvsDir, "nvs.sh").toString();

        return nvsScript;
    }

    private String getNvsDir() {
        String nvsDir = System.getenv("NVS_DIR");
        if (nvsDir != null) {
            Path path = Paths.get(nvsDir);
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        String home = System.getenv("HOME");
        if (home != null) {
            Path path = Paths.get(home, ".nvs");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        return null;
    }
}
