package com.github.eirslett.maven.plugins.frontend.lib;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsInstallationWork.CACHED;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsInstallationWork.DOWNLOADED;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsInstallationWork.INSTALLED;

public class YarnInstaller {

    public static final String INSTALL_PATH = "/node/yarn";

    public static final String ATLASSIAN_YARN_DOWNLOAD_ROOT =
        "https://packages.atlassian.com/artifactory/yarn-dist/";

    public static final String DEFAULT_YARN_DOWNLOAD_ROOT =
        "https://github.com/yarnpkg/yarn/releases/download/";

    private static final Object LOCK = new Object();

    private static final String YARN_ROOT_DIRECTORY = "dist";

    private String yarnVersion, yarnDownloadRoot, userName, password;

    private Map<String, String> httpHeaders;

    private boolean isYarnBerry;

    private final Logger logger;

    private final InstallConfig config;

    private final ArchiveExtractor archiveExtractor;

    private final FileDownloader fileDownloader;

    YarnInstaller(InstallConfig config, ArchiveExtractor archiveExtractor, FileDownloader fileDownloader) {
        logger = LoggerFactory.getLogger(getClass());
        this.config = config;
        this.archiveExtractor = archiveExtractor;
        this.fileDownloader = fileDownloader;
    }

    public YarnInstaller setYarnVersion(String yarnVersion) {
        this.yarnVersion = yarnVersion;
        return this;
    }

    public YarnInstaller setIsYarnBerry(boolean isYarnBerry) {
        this.isYarnBerry = isYarnBerry;
        return this;
    }

    public YarnInstaller setYarnDownloadRoot(String yarnDownloadRoot) {
        this.yarnDownloadRoot = yarnDownloadRoot;
        return this;
    }

    public YarnInstaller setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public YarnInstaller setPassword(String password) {
        this.password = password;
        return this;
    }

    public YarnInstaller setHttpHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }

    public AtlassianDevMetricsInstallationWork install() throws InstallationException {
        // use static lock object for a synchronized block
        synchronized (LOCK) {
            if (yarnDownloadRoot == null || yarnDownloadRoot.isEmpty()) {
                yarnDownloadRoot = DEFAULT_YARN_DOWNLOAD_ROOT;
            }
            if (!yarnIsAlreadyInstalled()) {
                if (!yarnVersion.startsWith("v")) {
                    throw new InstallationException("Yarn version has to start with prefix 'v'.");
                }
                return installYarn();
            }
        }
        return INSTALLED;
    }

    private boolean yarnIsAlreadyInstalled() {
        try {
            YarnExecutorConfig executorConfig = new InstallYarnExecutorConfig(config, isYarnBerry);
            File nodeFile = executorConfig.getYarnPath();
            if (nodeFile.exists()) {
                final String version =
                    new YarnExecutor(executorConfig, Arrays.asList("--version"), null).executeAndGetResult(logger).trim();

                if (version.equals(yarnVersion.replaceFirst("^v", ""))) {
                    logger.info("Yarn {} is already installed.", version);
                    return true;
                } else {
                    if (isYarnBerry && Integer.parseInt(version.split("\\.")[0]) > 1) {
                        logger.info("Yarn Berry {} is installed.", version);
                        return true;
                    } else{
                        logger.info("Yarn {} was installed, but we need version {}", version, yarnVersion);
                        return false;
                    }
                }
            } else {
                return false;
            }
        } catch (ProcessExecutionException e) {
            return false;
        }
    }

    private AtlassianDevMetricsInstallationWork installYarn() throws InstallationException {
        try {
            logger.info("Installing Yarn version {}", yarnVersion);
            String downloadUrl = yarnDownloadRoot + yarnVersion;
            String extension = "tar.gz";
            String fileending = "/yarn-" + yarnVersion + "." + extension;

            downloadUrl += fileending;

            CacheDescriptor cacheDescriptor = new CacheDescriptor("yarn", yarnVersion, extension);

            File archive = config.getCacheResolver().resolve(cacheDescriptor);

            AtlassianDevMetricsInstallationWork work =
            downloadFileIfMissing(downloadUrl, archive, userName, password, httpHeaders);

            File installDirectory = getInstallDirectory();

            // We need to delete the existing yarn directory first so we clean out any old files, and
            // so we can rename the package directory below.
            try {
                if (installDirectory.isDirectory()) {
                    FileUtils.deleteDirectory(installDirectory);
                }
            } catch (IOException e) {
                logger.warn("Failed to delete existing Yarn installation.");
            }

            try {
                extractFile(archive, installDirectory);
            } catch (ArchiveExtractionException e) {
                if (e.getCause() instanceof EOFException) {
                    // https://github.com/eirslett/frontend-maven-plugin/issues/794
                    // The downloading was probably interrupted and archive file is incomplete:
                    // delete it to retry from scratch
                    this.logger.error("The archive file {} is corrupted and will be deleted. "
                            + "Please try the build again.", archive.getPath());
                    archive.delete();
                    if (installDirectory.exists()) {
                        FileUtils.deleteDirectory(installDirectory);
                    }
                }

                throw e;
            }

            ensureCorrectYarnRootDirectory(installDirectory, yarnVersion);

            logger.info("Installed Yarn locally.");

            return work;
        } catch (DownloadException e) {
            throw new InstallationException("Could not download Yarn", e);
        } catch (ArchiveExtractionException | IOException e) {
            throw new InstallationException("Could not extract the Yarn archive", e);
        }
    }

    private File getInstallDirectory() {
        File installDirectory = new File(config.getInstallDirectory(), INSTALL_PATH);
        if (!installDirectory.exists()) {
            logger.debug("Creating install directory {}", installDirectory);
            installDirectory.mkdirs();
        }
        return installDirectory;
    }

    private void extractFile(File archive, File destinationDirectory) throws ArchiveExtractionException {
        logger.info("Unpacking {} into {}", archive, destinationDirectory);
        archiveExtractor.extract(archive.getPath(), destinationDirectory.getPath());
    }

    private void ensureCorrectYarnRootDirectory(File installDirectory, String yarnVersion) throws IOException {
        File yarnRootDirectory = new File(installDirectory, YARN_ROOT_DIRECTORY);
        if (!yarnRootDirectory.exists()) {
            logger.debug("Yarn root directory not found, checking for yarn-{}", yarnVersion);
            // Handle renaming Yarn 1.X root to YARN_ROOT_DIRECTORY
            File yarnOneXDirectory = new File(installDirectory, "yarn-" + yarnVersion);
            if (yarnOneXDirectory.isDirectory()) {
                if (!yarnOneXDirectory.renameTo(yarnRootDirectory)) {
                    throw new IOException("Could not rename versioned yarn root directory to " + YARN_ROOT_DIRECTORY);
                }
            } else {
                throw new FileNotFoundException("Could not find yarn distribution directory during extract");
            }
        }
    }

    private AtlassianDevMetricsInstallationWork downloadFileIfMissing(String downloadUrl, File destination, String userName, String password, Map<String, String> httpHeaders)
        throws DownloadException {
        if (!destination.exists()) {
            downloadFile(downloadUrl, destination, userName, password, httpHeaders);
            return DOWNLOADED;
        }
        return CACHED;
    }

    private void downloadFile(String downloadUrl, File destination, String userName, String password, Map<String, String> httpHeaders)
        throws DownloadException {
        logger.info("Downloading {} to {}", downloadUrl, destination);
        fileDownloader.download(downloadUrl, destination.getPath(), userName, password, httpHeaders);
    }
}
