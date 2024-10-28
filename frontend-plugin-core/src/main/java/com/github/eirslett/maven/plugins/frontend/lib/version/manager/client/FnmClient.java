package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FnmClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final CommandExecutor commandExecutor;

    public FnmClient(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public boolean isInstalled() {
        String fnmDir = getFnmDir();
        logger.debug("Checking if FNM installation directory exists: {}", fnmDir);

        return fnmDir != null;
    }

    @Override
    public void installNode() {
        commandExecutor
            .withPath(getFnmDir())
            .executeOrFail(Arrays.asList(getExecutable(), "use", "--install-if-missing"));
    }

    @Override
    public File getNodeExecutable() {
        String output = commandExecutor
            .withPath(getFnmDir())
            .executeOrFail(Arrays.asList(getExecutable(), "current"));
        String currentNodeVersion = cleanOutput(output);
        String fnmDir = getFnmDir();

        return Paths.get(fnmDir, "node-versions", currentNodeVersion, "installation", "bin", "node").toFile();
    }

    @Override
    public File getNpmExecutable() {
        File nodeExec = getNodeExecutable();
        return new File(nodeExec.getParent(), "npm");
    }

    private String cleanOutput(String output) {
        String[] lines = output.split(System.lineSeparator());

        return Arrays.stream(lines)
            .filter(line -> !line.startsWith("Using Node")) // fnm echos which version is used when using `--use-on-cd`
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private String getExecutable() {
        return Paths.get(getFnmDir(), "fnm").toString();
    }

    private String getFnmDir() {
        String fnmDir = System.getenv("FNM_DIR");
        if (fnmDir != null) {
            Path path = Paths.get(fnmDir);
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        String home = System.getenv("HOME");
        if (home != null) {
            Path path = Paths.get(home, ".fnm");
            if (Files.exists(path)) {
                return path.toString();
            }

            path = Paths.get(home, "Library", "Application Support", "fnm");
            if (Files.exists(path)) {
                return path.toString();
            }

            path = Paths.get(home, ".local", "share", "fnm");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null ) {
            Path path = Paths.get(xdgDataHome, "fnm");
            if (Files.exists(path)) {
                return path.toString();
            }
        }

        return null;
    }
}
