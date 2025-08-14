package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import com.github.eirslett.maven.plugins.frontend.lib.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.util.Optional.empty;

public class FnmClient implements VersionManagerClient {
    final Logger logger = LoggerFactory.getLogger(getClass());
    final InstallConfig installConfig;

    public FnmClient(InstallConfig installConfig) {
        this.installConfig = installConfig;
    }

    @Override
    public boolean isInstalled() {
        String fnmDir = getFnmDir();
        logger.debug("Checking if FNM installation directory exists: {}", fnmDir);

        return fnmDir != null;
    }

    @Override
    public File getNodeExecutable(String nodeVersion) {
        String nodeVersionWithV = nodeVersion.startsWith("v") ? nodeVersion : String.format("v%s", nodeVersion);

        String fnmDir = getFnmDir();
        Platform platform = installConfig.getPlatform();
        String architecture = platform.getArchitectureName();
        String osCodename = platform.getCodename();
        String nodeOnPlatform = String.format("node-%s-%s-%s", nodeVersionWithV, osCodename, architecture);

        if (installConfig.getPlatform().isWindows()) {
            return Paths.get(fnmDir, "node-versions", nodeVersionWithV, "installation", "node.exe").toFile();
        }

        Path path = Paths.get(fnmDir, "node-versions", nodeVersionWithV, nodeOnPlatform, "bin", "node");
        if (Files.exists(path)) {
            return path.toFile();
        }

        return Paths.get(fnmDir, "node-versions", nodeVersionWithV, "installation", "bin", "node").toFile();
    }

    @Override
    public File getNpmExecutable(String nodeVersion) {
        File nodeExec = getNodeExecutable(nodeVersion);

        if (installConfig.getPlatform().isWindows()) {
            return Paths.get(nodeExec.getParent(), "node_modules", "npm", "bin", "npm-cli.js").toFile();
        }

        return new File(nodeExec.getParent(), "npm");
    }

    @Nonnull
    @Override
    public Optional<File> getCorepackModuleDir(String nodeVersion) {
        final Path path = Paths.get(getNodeExecutable(nodeVersion).getParentFile().getParent(), "lib", "node_modules", "corepack");
        final File corepack = path.toFile();
        if (!corepack.exists()) {
            return empty();
        }
        return Optional.of(corepack);
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
            if (installConfig.getPlatform().isWindows()) {
                Path path = Paths.get(home, "AppData", "Roaming", "fnm");
                if (Files.exists(path)) {
                    return path.toString();
                }
            } else {
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
