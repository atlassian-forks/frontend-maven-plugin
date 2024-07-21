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

    private static final String TOOLS_VERSION_FILENAME = ".tools-version";

    public static String getNodeVersion(File baseDir, String providedNodeVersion, String genericNodeVersionFile) throws Exception {
        Logger logger = getLogger(NodeVersionDetector.class);

        if (!isNull(providedNodeVersion) && !providedNodeVersion.trim().isEmpty()) {
            logger.debug("Looks like a node version was set so using that: " + providedNodeVersion);
            return providedNodeVersion;
        }

        if (!isNull(genericNodeVersionFile) && !genericNodeVersionFile.trim().isEmpty()) {
            File genericNodeVersionFileFile = new File(genericNodeVersionFile);
            if (!genericNodeVersionFileFile.exists()) {
                throw new Exception("The Node version file doesn't seem to exist: " + genericNodeVersionFileFile);
            }

            if (genericNodeVersionFile.endsWith(TOOLS_VERSION_FILENAME)) {
                return readToolsVersionFile(genericNodeVersionFileFile, genericNodeVersionFileFile.toPath(), logger);
            } else {
                return readNvmrcFile(genericNodeVersionFileFile, genericNodeVersionFileFile.toPath(), logger);
            }
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
            String trimmedLine = readNvmrcFile(nodeVersionFile, nodeVersionFilePath, logger);
            if (trimmedLine != null) return trimmedLine;
        }

        Path nvmrcFilePath = Paths.get(directoryPath, ".nvmrc");
        File nvmrcFile = nvmrcFilePath.toFile();
        if (nvmrcFile.exists()) {
            String trimmedLine = readNvmrcFile(nvmrcFile, nvmrcFilePath, logger);
            if (trimmedLine != null) return trimmedLine;
        }

        Path toolsVersionFilePath = Paths.get(directoryPath, TOOLS_VERSION_FILENAME);
        File toolsVersionFile = toolsVersionFilePath.toFile();
        if (toolsVersionFile.exists()) {
            String trimmedLine = readToolsVersionFile(toolsVersionFile, toolsVersionFilePath, logger);
            if (trimmedLine != null) return trimmedLine;
        }

        File parent = directory.getParentFile();
        if (isNull(parent) || directory.equals(parent)) {
            throw new Exception("Reach root-level without finding a suitable file");
        }

        return recursivelyFindVersion(parent);
    }

    private static String readNvmrcFile(File nvmrcFile, Path nvmrcFilePath, Logger logger) throws Exception {
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
        return null;
    }

    private static String readToolsVersionFile(File toolsVersionFile, Path toolsVersionFilePath, Logger logger) throws Exception {
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
        return null;
    }
}
