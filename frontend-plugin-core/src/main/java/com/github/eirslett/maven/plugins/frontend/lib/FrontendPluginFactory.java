package com.github.eirslett.maven.plugins.frontend.lib;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerCache;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerRunner;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerType;
import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

public final class FrontendPluginFactory {

    private static final Logger logger = LoggerFactory.getLogger(FrontendPluginFactory.class);

    private static final Platform defaultPlatform = Platform.guess();
    private static final String DEFAULT_CACHE_PATH = "cache";

    private final File workingDirectory;
    private final File installDirectory;
    private final CacheResolver cacheResolver;
    private final boolean useNodeVersionManager;

    public FrontendPluginFactory(File workingDirectory, File installDirectory){
        this(workingDirectory, installDirectory, getDefaultCacheResolver(installDirectory));
    }

    public FrontendPluginFactory(File workingDirectory, File installDirectory, CacheResolver cacheResolver){
        this(workingDirectory, installDirectory, cacheResolver, false);
    }

    public FrontendPluginFactory(File workingDirectory, File installDirectory, CacheResolver cacheResolver, boolean useNodeVersionManager){
        this.workingDirectory = workingDirectory;
        this.installDirectory = installDirectory;
        this.cacheResolver = cacheResolver;
        this.useNodeVersionManager = useNodeVersionManager;

        initializeGlobalCache();
    }

    public BunInstaller getBunInstaller(ProxyConfig proxy) {
        return new BunInstaller(getInstallConfig(), new DefaultArchiveExtractor(), new DefaultFileDownloader(proxy));
    }
    public NodeInstaller getNodeInstaller(ProxyConfig proxy) {
        return new NodeInstaller(getInstallConfig(), getVersionManagerCache(), new DefaultArchiveExtractor(), new DefaultFileDownloader(proxy));
    }

    public NPMInstaller getNPMInstaller(ProxyConfig proxy) {
        return new NPMInstaller(getInstallConfig(), getVersionManagerCache(), new DefaultArchiveExtractor(), new DefaultFileDownloader(proxy));
    }

    public CorepackInstaller getCorepackInstaller(ProxyConfig proxy) {
        return new CorepackInstaller(getInstallConfig(), new DefaultArchiveExtractor(), new DefaultFileDownloader(proxy));
    }

    public PnpmInstaller getPnpmInstaller(ProxyConfig proxy) {
        return new PnpmInstaller(getInstallConfig(), new DefaultArchiveExtractor(), new DefaultFileDownloader(proxy));
    }

    public YarnInstaller getYarnInstaller(ProxyConfig proxy) {
        return new YarnInstaller(getInstallConfig(), new DefaultArchiveExtractor(), new DefaultFileDownloader(proxy));
    }

    public BowerRunner getBowerRunner(ProxyConfig proxy) {
        return new DefaultBowerRunner(getExecutorConfig(), proxy);
    }

    public BunRunner getBunRunner(ProxyConfig proxy, String npmRegistryURL) {
        return new DefaultBunRunner(new InstallBunExecutorConfig(getInstallConfig()), proxy, npmRegistryURL);
    }

    public JspmRunner getJspmRunner() {
        return new DefaultJspmRunner(getExecutorConfig());
    }

    public NpmRunner getNpmRunner(ProxyConfig proxy, String npmRegistryURL) {
        return new DefaultNpmRunner(getExecutorConfig(), proxy, npmRegistryURL);
    }

    public CorepackRunner getCorepackRunner() {
        return new DefaultCorepackRunner(getExecutorConfig());
    }

    public PnpmRunner getPnpmRunner(ProxyConfig proxyConfig, String npmRegistryUrl) {
        return new DefaultPnpmRunner(getExecutorConfig(), proxyConfig, npmRegistryUrl);
    }

    public NpxRunner getNpxRunner(ProxyConfig proxy, String npmRegistryURL) {
        return new DefaultNpxRunner(getExecutorConfig(), proxy, npmRegistryURL);
    }

    public YarnRunner getYarnRunner(ProxyConfig proxy, String npmRegistryURL, boolean isYarnBerry) {
        return new DefaultYarnRunner(new InstallYarnExecutorConfig(getInstallConfig(), isYarnBerry), proxy, npmRegistryURL);
    }

    public GruntRunner getGruntRunner(){
        return new DefaultGruntRunner(getExecutorConfig());
    }

    public EmberRunner getEmberRunner() {
        return new DefaultEmberRunner(getExecutorConfig());
    }

    public KarmaRunner getKarmaRunner(){
        return new DefaultKarmaRunner(getExecutorConfig());
    }

    public GulpRunner getGulpRunner(){
        return new DefaultGulpRunner(getExecutorConfig());
    }

    public WebpackRunner getWebpackRunner(){
        return new DefaultWebpackRunner(getExecutorConfig());
    }

    public VersionManagerRunner getVersionManagerRunner() {
        return new VersionManagerRunner(getInstallConfig(), getVersionManagerCache());
    }

    private NodeExecutorConfig getExecutorConfig() {
        return new InstallNodeExecutorConfig(getInstallConfig(), getVersionManagerCache());
    }

    private InstallConfig getInstallConfig() {
        return GlobalCache.getInstallConfig();
    }

    private VersionManagerCache getVersionManagerCache() {
        return GlobalCache.getVersionManagerCache();
    }

    private static final CacheResolver getDefaultCacheResolver(File root) {
        return new DirectoryCacheResolver(new File(root, DEFAULT_CACHE_PATH));
    }

    private void initializeGlobalCache() {
        InstallConfig installConfig = new DefaultInstallConfig(installDirectory, workingDirectory, cacheResolver, defaultPlatform, useNodeVersionManager);
        GlobalCache.setInstallConfig(installConfig);

        if (installConfig.isUseNodeVersionManager()) {
            VersionManagerType versionManagerType = getVersionManagerType(installConfig);
            GlobalCache.setVersionManagerCache(
                new VersionManagerCache(versionManagerType)
            );
        } else {
            GlobalCache.setVersionManagerCache(new VersionManagerCache());
        }
    }

    private static VersionManagerType getVersionManagerType(InstallConfig installConfig) {
        VersionManagerLocator versionManagerLocator = new VersionManagerLocator(installConfig);
        VersionManagerType versionManagerType = versionManagerLocator.findAvailable();
        if (versionManagerType == null) {
            logger.warn("You have configured `useNodeVersionManager=true` but node version manager couldn't be identified. " +
                "If you want to use node version manager, please install a supported manager " + Arrays.toString(VersionManagerType.values()) + " in your environment.");
        }
        return versionManagerType;
    }

    public void loadVersionManager() {
        if (getInstallConfig().isUseNodeVersionManager()) {
            VersionManagerType versionManagerType = getVersionManagerType(getInstallConfig());
            if (versionManagerType != null) {
                getVersionManagerRunner().populateCache();
            }
        }
    }

    public boolean isVersionManagerAvailable() {
        if (!getInstallConfig().isUseNodeVersionManager()) return false;
        return getVersionManagerCache().isVersionManagerAvailable();
    }
}
