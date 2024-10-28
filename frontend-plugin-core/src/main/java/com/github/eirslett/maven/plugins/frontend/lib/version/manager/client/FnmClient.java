package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.ShellExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class FnmClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final ShellExecutor shellExecutor;

    private static final String EXECUTABLE = "fnm";

    public FnmClient(ShellExecutor shellExecutor) {
        this.shellExecutor = shellExecutor;
    }

    @Override
    public boolean isInstalled() {
        String fnmDir = getFnmDir();
        if (fnmDir == null) return false;

        // FIXME just return true if fnm dir exists
        String version = cleanOutput(shellExecutor.executeAndCatchErrors(Arrays.asList(
            EXECUTABLE, "--version"
        ), Collections.singletonList(fnmDir)));

        return version.matches("fnm \\d+\\.\\d+\\.\\d+");
    }

    @Override
    public void installNode() {
        shellExecutor.executeOrFail(Arrays.asList(
            EXECUTABLE, "use", "--install-if-missing"
        ), Collections.singletonList(getFnmDir()));

        // FIXME verify node installed
        shellExecutor.executeAndCatchErrors(Arrays.asList(
            "node", "--version"
        ), Collections.singletonList(getFnmDir()));
    }

    @Override
    public File getNodeExecutable() {
        String currentNodeVersion = cleanOutput(shellExecutor.executeOrFail(
            Arrays.asList(EXECUTABLE, "current"), Collections.singletonList(getFnmDir())));
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

    private String getFnmDir() {
        String $fnmDir = System.getenv("FNM_DIR");
        Path path = Paths.get($fnmDir);
        if (Files.exists(path)) {
            return path.toString();
        }

        String $home = System.getenv("HOME");
        path = Paths.get($home, ".fnm");
        if (Files.exists(path)) {
            return path.toString();
        }

        String $xdgDataHome = System.getenv("XDG_DATA_HOME");
        path = Paths.get($xdgDataHome, "fnm");
        if (Files.exists(path)) {
            return path.toString();
        }

        path = Paths.get($home, "Library", "Application Support", "fnm");
        if (Files.exists(path)) {
            return path.toString();
        }

        path = Paths.get($home, ".local", "share", "fnm");
        if (Files.exists(path)) {
            return path.toString();
        }

        return null;
    }
}
