package com.github.eirslett.maven.plugins.frontend.lib.version.manager.client;

import com.github.eirslett.maven.plugins.frontend.lib.InstallConfig;
import com.github.eirslett.maven.plugins.frontend.lib.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        // If `nodeVersion` is loosely defined, find the highest matching version
        if (!nodeVersionWithV.contains(".")) {
            String actualVersion = findHighestMatchingVersion(fnmDir, nodeVersionWithV);

            if (actualVersion == null) {
                logger.debug("No matching version found for: {}", nodeVersionWithV);
                return null;
            }

            nodeVersionWithV = actualVersion;
        }

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

    private String findHighestMatchingVersion(String fnmDir, String versionPrefix) {
        if (fnmDir == null) {
            return null;
        }

        Path nodeVersionsDir = Paths.get(fnmDir, "node-versions");
        if (!Files.exists(nodeVersionsDir)) {
            return null;
        }

        try {
            return Files.list(nodeVersionsDir)
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith(versionPrefix))
                    .sorted((v1, v2) -> compareVersions(v2, v1)) // Sort descending
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.debug("Error finding matching version for {}: {}", versionPrefix, e.getMessage());
            return null;
        }
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.substring(1).split("\\.");
        String[] parts2 = v2.substring(1).split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }
}
