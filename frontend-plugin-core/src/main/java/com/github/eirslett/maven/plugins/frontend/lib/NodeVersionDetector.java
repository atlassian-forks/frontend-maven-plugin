package com.github.eirslett.maven.plugins.frontend.lib;

import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Objects.isNull;
import static org.slf4j.LoggerFactory.getLogger;

public class NodeVersionDetector {
    public static String getNodeVersion(File baseDir, String providedNodeVersion) {
        Logger logger = getLogger(NodeVersionDetector.class);

        if (!isNull(providedNodeVersion) && !providedNodeVersion.trim().isEmpty()) {
            logger.debug("Looks like a node version was set so using that: " + providedNodeVersion);
            return providedNodeVersion;
        }

        try {
            return recursivelyFindVersion(baseDir);
        } catch (Throwable throwable) {
            logger.debug("Going to use the configuration node version, failed to find a file with the version because",
                    throwable);
            return providedNodeVersion;
        }
    }

    public static String recursivelyFindVersion(File directory) throws Exception {
        Logger logger = getLogger(NodeVersionDetector.class);

        if (!directory.canRead()) {
            throw new Exception("Tried to find a Node version file but giving up because it's not possible to read " +
                    directory.getPath());
        }

        String directoryPath = directory.getPath();

        Path nodeVersionFilePath = Paths.get(directoryPath, ".node-version");
        File nodeVersionFile = nodeVersionFilePath.toFile();
        if (nodeVersionFile.exists()) {
            if (!nodeVersionFile.canRead()) {
                throw new Exception("Tried to read the node version from the file, but giving up because it's possible to read" + nodeVersionFile.getPath());
            }

            List<String> lines = Files.readAllLines(nodeVersionFilePath);
            for (String line: lines) {
                if (!isNull(line)) {
                    String trimmedLine = line.trim();

                    if (trimmedLine.isEmpty()) {
                        continue;
                    }

                    if (trimmedLine.startsWith("#") || trimmedLine.startsWith("/") || trimmedLine.startsWith("!")) {
                        continue;
                    }

                    logger.info("Found the version of Node in: " + nodeVersionFilePath);
                    return trimmedLine;
                }
            }
        }

        Path nvmrcFilePath = Paths.get(directoryPath, ".nvmrc");
        File nvmrcFile = nvmrcFilePath.toFile();
        if (nvmrcFile.exists()) {
            if (!nvmrcFile.canRead()) {
                throw new Exception("Tried to read the node version from the file, but giving up because it's possible to read" + nvmrcFile.getPath());
            }

            List<String> lines = Files.readAllLines(nvmrcFilePath);
            for (String line: lines) {
                if (!isNull(line)) {
                    String trimmedLine = line.trim();

                    if (trimmedLine.isEmpty()) {
                        continue;
                    }

                    if (trimmedLine.startsWith("#") || trimmedLine.startsWith("/") || trimmedLine.startsWith("!")) {
                        continue;
                    }

                    logger.info("Found the version of Node in: " + nvmrcFilePath);
                    return trimmedLine;
                }
            }
        }

        Path toolsVersionFilePath = Paths.get(directoryPath, "tools-version");
        File toolsVersionFile = toolsVersionFilePath.toFile();
        if (toolsVersionFile.exists()) {
            if (!toolsVersionFile.canRead()) {
                throw new Exception("Tried to read the node version from the file, but giving up because it's possible to read" + toolsVersionFile.getPath());
            }

            List<String> lines = Files.readAllLines(toolsVersionFilePath);
            for (String line: lines) {
                if (!isNull(line)) {
                    String trimmedLine = line.trim();

                    if (trimmedLine.isEmpty()) {
                        continue;
                    }

                    if (!trimmedLine.startsWith("node")) {
                        continue;
                    }

                    logger.info("Found the version of Node in: " + toolsVersionFilePath);
                    return trimmedLine.replaceAll("node(js)?\\s*", "");
                }
            }
        }

        File parent = directory.getParentFile();
        if (isNull(parent) || directory.equals(parent)) {
            throw new Exception("Reach root-level without finding a suitable file");
        }

        return recursivelyFindVersion(parent);
    }
}
