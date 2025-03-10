package com.github.eirslett.maven.plugins.frontend.lib;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerCache;

import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsInstallationWork.CACHED;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsInstallationWork.DOWNLOADED;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsInstallationWork.INSTALLED;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsInstallationWork.USER_PROVIDED;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsInstallationWork.VERSION_MANAGER_PROVIDED;
import static java.util.Objects.isNull;

public class NodeInstaller {

    public static final String NODEJS_ORG = "nodejs.org";

    public static final String ATLASSIAN_NODE_DOWNLOAD_ROOT = "https://packages.atlassian.com/artifactory/nodejs-dist/";

    public static final String INSTALL_PATH = "/node";

    private static final Object LOCK = new Object();

    private String npmVersion, nodeVersion, nodeDownloadRoot, userName, password;

    private Map<String, String> httpHeaders;

    private final Logger logger;

    private final InstallConfig config;

    private final VersionManagerCache versionManagerCache;

    private final ArchiveExtractor archiveExtractor;

    private final FileDownloader fileDownloader;
    private final NodeExecutorConfig nodeExecutorConfig;

    NodeInstaller(InstallConfig config, VersionManagerCache versionManagerCache, ArchiveExtractor archiveExtractor, FileDownloader fileDownloader) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.config = config;
        this.archiveExtractor = archiveExtractor;
        this.fileDownloader = fileDownloader;
        this.versionManagerCache = versionManagerCache;
        this.nodeExecutorConfig = new InstallNodeExecutorConfig(config, versionManagerCache);
    }

    public NodeInstaller setNodeVersion(String nodeVersion) {
        this.nodeVersion = nodeVersion;
        return this;
    }

    public NodeInstaller setNodeDownloadRoot(String nodeDownloadRoot) {
        this.nodeDownloadRoot = nodeDownloadRoot;
        return this;
    }

    public NodeInstaller setNpmVersion(String npmVersion) {
        this.npmVersion = npmVersion;
        return this;
    }

    public NodeInstaller setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public NodeInstaller setPassword(String password) {
        this.password = password;
        return this;
    }

    public NodeInstaller setHttpHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }

    private boolean npmProvided() throws InstallationException {
        if (this.npmVersion != null) {
            if ("provided".equals(this.npmVersion)) {
                if (Integer.parseInt(this.nodeVersion.replace("v", "").split("[.]")[0]) < 4) {
                    throw new InstallationException("NPM version is '" + this.npmVersion
                        + "' but Node didn't include NPM prior to v4.0.0");
                }
                return true;
            }
            return false;
        }
        return false;
    }

    public AtlassianDevMetricsInstallationWork install() throws InstallationException {
        AtlassianDevMetricsInstallationWork work = INSTALLED;
        // use static lock object for a synchronized block
        synchronized (LOCK) {
            if (this.nodeDownloadRoot == null || this.nodeDownloadRoot.isEmpty()) {
                this.nodeDownloadRoot = this.config.getPlatform().getNodeDownloadRoot();
            }

            // try to install the standard way
            if (!nodeIsAlreadyInstalled()) {
                this.logger.info("Installing node version {}", this.nodeVersion);

                if (!this.nodeVersion.startsWith("v")) {
                    this.logger.warn("Node version does not start with naming convention 'v'.");
                }
                if (this.config.getPlatform().isWindows()) {
                    if (npmProvided()) {
                        work = installNodeWithNpmForWindows();
                    } else {
                        work = installNodeForWindows();
                    }
                } else {
                    work = installNodeDefault();
                }
            } else if (nodeExecutorConfig.hasProvidedNode()) {
                work = USER_PROVIDED;
            } else if (nodeExecutorConfig.hasNodeVersionManagerNode()) {
                work = VERSION_MANAGER_PROVIDED;
            } else {
                work = INSTALLED;
            }
        }
        return work;
    }

    private boolean nodeIsAlreadyInstalled() {
        try {
            File nodeFile = nodeExecutorConfig.getNodePath();
            if (nodeFile.exists()) {
                final String version =
                    new NodeExecutor(nodeExecutorConfig, Arrays.asList("--version"), null).executeAndGetResult(logger);

                if (version.equals(this.nodeVersion)) {
                    this.logger.info("Node {} is already installed.", version);
                    return true;
                } else {
                    if (nodeExecutorConfig.hasProvidedNode()) {
                        this.logger.warn("Provided node executable has version {}, but {} was requested in configuration. Node executable: {}", version, this.nodeVersion, nodeExecutorConfig.getNodePath());
                        return true;
                    }
                    this.logger.info("Node {} was installed, but we need version {}", version,
                        this.nodeVersion);
                    return false;
                }
            } else {
                return false;
            }
        } catch (ProcessExecutionException e) {
            this.logger.warn("Unable to determine current node version: {}", e.getMessage());
            return false;
        }
    }

    private AtlassianDevMetricsInstallationWork installNodeDefault() throws InstallationException {
        try {
            final String longNodeFilename =
                this.config.getPlatform().getLongNodeFilename(this.nodeVersion, false);
            String downloadUrl = this.nodeDownloadRoot
                + this.config.getPlatform().getNodeDownloadFilename(this.nodeVersion, false);
            String classifier = this.config.getPlatform().getNodeClassifier(this.nodeVersion);

            File tmpDirectory = getTempDirectory();

            CacheDescriptor cacheDescriptor = new CacheDescriptor("node", this.nodeVersion, classifier,
                this.config.getPlatform().getArchiveExtension());

            File archive = this.config.getCacheResolver().resolve(cacheDescriptor);

            AtlassianDevMetricsInstallationWork work =
            downloadFileIfMissing(downloadUrl, archive, this.userName, this.password, this.httpHeaders);

            try {
                extractFile(archive, tmpDirectory);
            } catch (ArchiveExtractionException e) {
                if (e.getCause() instanceof EOFException) {
                    // https://github.com/eirslett/frontend-maven-plugin/issues/794
                    // The downloading was probably interrupted and archive file is incomplete:
                    // delete it to retry from scratch
                    this.logger.error("The archive file {} is corrupted and will be deleted. "
                            + "Please try the build again.", archive.getPath());
                    archive.delete();
                    FileUtils.deleteDirectory(tmpDirectory);
                }

                throw e;
            }

            // Search for the node binary
            File nodeBinary =
                new File(tmpDirectory, longNodeFilename + File.separator + "bin" + File.separator + "node");
            if (!nodeBinary.exists()) {
                throw new FileNotFoundException(
                    "Could not find the downloaded Node.js binary in " + nodeBinary);
            } else {
                File destinationDirectory = getInstallDirectory();

                File destination = new File(destinationDirectory, "node");
                this.logger.info("Copying node binary from {} to {}", nodeBinary, destination);
                if (destination.exists() && !destination.delete()) {
                    throw new InstallationException("Could not install Node: Was not allowed to delete " + destination);
                }
                try {
                    Files.move(nodeBinary.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new InstallationException("Could not install Node: Was not allowed to rename "
                        + nodeBinary + " to " + destination);
                }

                if (!destination.setExecutable(true, false)) {
                    throw new InstallationException(
                        "Could not install Node: Was not allowed to make " + destination + " executable.");
                }

                if (npmProvided()) {
                    File tmpNodeModulesDir = new File(tmpDirectory,
                        longNodeFilename + File.separator + "lib" + File.separator + "node_modules");
                    File nodeModulesDirectory = new File(destinationDirectory, "node_modules");
                    File npmDirectory = new File(nodeModulesDirectory, "npm");
                    FileUtils.copyDirectory(tmpNodeModulesDir, nodeModulesDirectory);
                    this.logger.info("Extracting NPM");
                    // create a copy of the npm scripts next to the node executable
                    for (String script : Arrays.asList("npm", "npm.cmd", "npx", "npx.cmd")) {
                        File scriptFile = new File(npmDirectory, "bin" + File.separator + script);
                        if (scriptFile.exists()) {
                            File copy = new File(destinationDirectory, script);
                            if (!copy.exists()) {
                                try
                                {
                                    FileUtils.copyFile(scriptFile, copy);
                                }
                                catch (IOException e)
                                {
                                    throw new InstallationException("Could not copy npm", e);
                                }
                                copy.setExecutable(true);
                            }
                        }
                    }
                }

                deleteTempDirectory(tmpDirectory);

                this.logger.info("Installed node locally.");
                return work;
            }
        } catch (IOException e) {
            throw new InstallationException("Could not install Node", e);
        } catch (DownloadException e) {
            throw new InstallationException("Could not download Node.js", e);
        } catch (ArchiveExtractionException e) {
            throw new InstallationException("Could not extract the Node archive", e);
        }
    }

    private AtlassianDevMetricsInstallationWork installNodeWithNpmForWindows() throws InstallationException {
        try {
            final String longNodeFilename =
                this.config.getPlatform().getLongNodeFilename(this.nodeVersion, true);
            String downloadUrl = this.nodeDownloadRoot
                + this.config.getPlatform().getNodeDownloadFilename(this.nodeVersion, true);
            String classifier = this.config.getPlatform().getNodeClassifier(this.nodeVersion);

            File tmpDirectory = getTempDirectory();

            CacheDescriptor cacheDescriptor = new CacheDescriptor("node", this.nodeVersion, classifier,
                this.config.getPlatform().getArchiveExtension());

            File archive = this.config.getCacheResolver().resolve(cacheDescriptor);

            AtlassianDevMetricsInstallationWork work =
            downloadFileIfMissing(downloadUrl, archive, this.userName, this.password, this.httpHeaders);

            extractFile(archive, tmpDirectory);

            // Search for the node binary
            File nodeBinary = new File(tmpDirectory, longNodeFilename + File.separator + "node.exe");
            if (!nodeBinary.exists()) {
                throw new FileNotFoundException(
                    "Could not find the downloaded Node.js binary in " + nodeBinary);
            } else {
                File destinationDirectory = getInstallDirectory();

                File destination = new File(destinationDirectory, "node.exe");
                this.logger.info("Copying node binary from {} to {}", nodeBinary, destination);
                try {
                    Files.move(nodeBinary.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new InstallationException("Could not install Node: Was not allowed to rename "
                        + nodeBinary + " to " + destination);
                }

                if ("provided".equals(this.npmVersion)) {
                    File tmpNodeModulesDir =
                        new File(tmpDirectory, longNodeFilename + File.separator + "node_modules");
                    File nodeModulesDirectory = new File(destinationDirectory, "node_modules");
                    FileUtils.copyDirectory(tmpNodeModulesDir, nodeModulesDirectory);
                }
                deleteTempDirectory(tmpDirectory);

                this.logger.info("Installed node locally.");
                return work;
            }
        } catch (IOException e) {
            throw new InstallationException("Could not install Node", e);
        } catch (DownloadException e) {
            throw new InstallationException("Could not download Node.js", e);
        } catch (ArchiveExtractionException e) {
            throw new InstallationException("Could not extract the Node archive", e);
        }

    }

    private AtlassianDevMetricsInstallationWork installNodeForWindows() throws InstallationException {
        final String downloadUrl = this.nodeDownloadRoot
            + this.config.getPlatform().getNodeDownloadFilename(this.nodeVersion, false);
        try {
            File destinationDirectory = getInstallDirectory();

            File destination = new File(destinationDirectory, "node.exe");

            String classifier = this.config.getPlatform().getNodeClassifier(this.nodeVersion);

            CacheDescriptor cacheDescriptor =
                new CacheDescriptor("node", this.nodeVersion, classifier, "exe");

            File binary = this.config.getCacheResolver().resolve(cacheDescriptor);

            AtlassianDevMetricsInstallationWork work =
            downloadFileIfMissing(downloadUrl, binary, this.userName, this.password, this.httpHeaders);

            this.logger.info("Copying node binary from {} to {}", binary, destination);
            FileUtils.copyFile(binary, destination);

            this.logger.info("Installed node locally.");
            return work;
        } catch (DownloadException e) {
            throw new InstallationException("Could not download Node.js from: " + downloadUrl, e);
        } catch (IOException e) {
            throw new InstallationException("Could not install Node.js", e);
        }
    }

    private File getTempDirectory() {
        File tmpDirectory = new File(getInstallDirectory(), "tmp");
        if (!tmpDirectory.exists()) {
            this.logger.debug("Creating temporary directory {}", tmpDirectory);
            tmpDirectory.mkdirs();
        }
        return tmpDirectory;
    }

    private File getInstallDirectory() {
        File installDirectory= new File(this.config.getInstallDirectory(), INSTALL_PATH);

        if (!installDirectory.exists()) {
            this.logger.debug("Creating install directory {}", installDirectory);
            installDirectory.mkdirs();
        }
        return installDirectory;
    }

    private void deleteTempDirectory(File tmpDirectory) throws IOException {
        if (tmpDirectory != null && tmpDirectory.exists()) {
            this.logger.debug("Deleting temporary directory {}", tmpDirectory);
            FileUtils.deleteDirectory(tmpDirectory);
        }
    }

    private void extractFile(File archive, File destinationDirectory) throws ArchiveExtractionException {
        this.logger.info("Unpacking {} into {}", archive, destinationDirectory);
        this.archiveExtractor.extract(archive.getPath(), destinationDirectory.getPath());
    }

    private AtlassianDevMetricsInstallationWork downloadFileIfMissing(String downloadUrl, File destination, String userName, String password,
            Map<String, String> httpHeaders) throws DownloadException {
        if (!destination.exists()) {
            downloadFile(downloadUrl, destination, userName, password, httpHeaders);
            return DOWNLOADED;
        }
        return CACHED;
    }

    private void downloadFile(String downloadUrl, File destination, String userName, String password,
            Map<String, String> httpHeaders) throws DownloadException {
        this.logger.info("Downloading {} to {}", downloadUrl, destination);
        this.fileDownloader.download(downloadUrl, destination.getPath(), userName, password, httpHeaders);
    }
}
