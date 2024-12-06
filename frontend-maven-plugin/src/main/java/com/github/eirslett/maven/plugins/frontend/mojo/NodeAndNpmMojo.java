package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.ArchiveExtractionException;
import com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsInstallationWork;
import com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.Timer;
import com.github.eirslett.maven.plugins.frontend.lib.DownloadException;
import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.InstallationException;
import com.github.eirslett.maven.plugins.frontend.lib.NPMInstaller;
import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionDetector;
import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper;
import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.crypto.SettingsDecrypter;

import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsInstallationWork.UNKNOWN;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.formatNodeVersionForMetric;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.getHostForMetric;
import static com.github.eirslett.maven.plugins.frontend.lib.NPMInstaller.ATLASSIAN_NPM_DOWNLOAD_ROOT;
import static com.github.eirslett.maven.plugins.frontend.lib.NPMInstaller.DEFAULT_NPM_DOWNLOAD_ROOT;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeInstaller.ATLASSIAN_NODE_DOWNLOAD_ROOT;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeInstaller.NODEJS_ORG;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.getDownloadableVersion;
import static com.github.eirslett.maven.plugins.frontend.lib.Utils.isBlank;
import static com.github.eirslett.maven.plugins.frontend.mojo.AtlassianUtil.isAtlassianProject;
import static java.util.Objects.isNull;

@Mojo(name="install-node-and-npm", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class NodeAndNpmMojo extends AbstractNodeMojo {

    /**
     * Where to download NPM binary from. Defaults to https://registry.npmjs.org/npm/-/
     */
    @Parameter(property = "npmDownloadRoot", required = false)
    private String npmDownloadRoot;

    /**
     * Where to download Node.js and NPM binaries from.
     *
     * @deprecated use {@link AbstractNodeMojo#nodeDownloadRoot} and {@link #npmDownloadRoot} instead, this configuration will be used only when no {@link AbstractNodeMojo#nodeDownloadRoot} or {@link #npmDownloadRoot} is specified.
     */
    @Parameter(property = "downloadRoot", required = false, defaultValue = "")
    @Deprecated
    private String downloadRoot;

    /**
     * The version of NPM to install.
     */
    @Parameter(property = "npmVersion", required = false, defaultValue = "provided")
    private String npmVersion;

    /**
     * Server Id for download username and password
     */
    @Parameter(property = "serverId", defaultValue = "")
    private String serverId;

    @Parameter(property = "session", defaultValue = "${session}", readonly = true)
    private MavenSession session;

    /**
     * Skips execution of this mojo.
     */
    @Parameter(property = "skip.installnodenpm", defaultValue = "${skip.installnodenpm}")
    private boolean skip;

    @Component(role = SettingsDecrypter.class)
    private SettingsDecrypter decrypter;

    private AtlassianDevMetricsInstallationWork packageManagerWork = UNKNOWN;
    private AtlassianDevMetricsInstallationWork runtimeWork = UNKNOWN;

    @Override
    protected boolean skipExecution() {
        return this.skip;
    }

    @Override
    public void executeWithVerifiedNodeVersion(FrontendPluginFactory factory, String validNodeVersion) throws Exception {
        boolean pacAttemptFailed = false;
        boolean triedToUsePac = false;
        boolean failed = false;
        Timer timer = new Timer();

        final String nodeDownloadRoot = getNodeDownloadRoot();
        final String npmDownloadRoot = getNpmDownloadRoot();

        try {
            if (isAtlassianProject(project) &&
                    isBlank(serverId) &&
                    (isBlank(nodeDownloadRoot) || isBlank(npmDownloadRoot))
            ) { // If they're overridden the settings, they be the boss
                triedToUsePac = true;

                getLog().info("Atlassian project detected, going to use the internal mirrors (requires VPN)");

                serverId = "maven-atlassian-com";
                try {
                    install(factory, validNodeVersion,
                            isBlank(nodeDownloadRoot) ? ATLASSIAN_NODE_DOWNLOAD_ROOT : nodeDownloadRoot,
                            isBlank(npmDownloadRoot) ? ATLASSIAN_NPM_DOWNLOAD_ROOT : npmDownloadRoot);
                    return;
                } catch (InstallationException exception) {
                    // Ignore as many filesystem exceptions unrelated to the mirror easily
                    if (!(exception.getCause() instanceof DownloadException ||
                            exception.getCause() instanceof ArchiveExtractionException)) {
                        throw exception;
                    }
                    pacAttemptFailed = true;
                    getLog().warn("Oh no couldn't use the internal mirrors! Falling back to upstream mirrors");
                    getLog().debug("Using internal mirrors failed because: ", exception);
                } finally {
                    serverId = null;
                }
            }

            install(factory, validNodeVersion, nodeDownloadRoot, npmDownloadRoot);
        } catch (Exception exception) {
            failed = true;
            throw exception;
        } finally {
            // Please the compiler being effectively final
            boolean finalFailed = failed;
            boolean finalPacAttemptFailed = pacAttemptFailed;
            boolean finalTriedToUsePac = triedToUsePac;
            timer.stop(
                    "runtime.download",
                    project.getArtifactId(),
                    getFrontendMavenPluginVersion(),
                    formatNodeVersionForMetric(validNodeVersion),
                    new HashMap<String, String>() {{
                        put("installation", "npm");
                        put("installation-work-runtime", runtimeWork.toString());
                        put("installation-work-package-manager", packageManagerWork.toString());
                        put("runtime-host", getHostForMetric(nodeDownloadRoot, NODEJS_ORG, finalTriedToUsePac, finalPacAttemptFailed));
                        put("package-manager-host", getHostForMetric(npmDownloadRoot, DEFAULT_NPM_DOWNLOAD_ROOT, finalTriedToUsePac, finalPacAttemptFailed));
                        put("failed", Boolean.toString(finalFailed));
                        put("pac-attempted-failed", Boolean.toString(finalPacAttemptFailed));
                        put("tried-to-use-pac", Boolean.toString(finalTriedToUsePac));
                    }});
        }
    }

    private void install(FrontendPluginFactory factory, String validNodeVersion, String nodeDownloadRoot, String npmDownloadRoot) throws InstallationException {
        ProxyConfig proxyConfig = MojoUtils.getProxyConfig(session, decrypter);
        Server server = MojoUtils.decryptServer(serverId, session, decrypter);

        if (null != server) {
            Map<String, String> httpHeaders = getHttpHeaders(server);
            runtimeWork =
            factory.getNodeInstaller(proxyConfig)
                .setNodeVersion(validNodeVersion)
                .setNodeDownloadRoot(nodeDownloadRoot)
                .setNpmVersion(npmVersion)
                .setUserName(server.getUsername())
                .setPassword(server.getPassword())
                .setHttpHeaders(httpHeaders)
                .install();
            packageManagerWork =
            factory.getNPMInstaller(proxyConfig)
                .setNodeVersion(validNodeVersion)
                .setNpmVersion(npmVersion)
                .setNpmDownloadRoot(npmDownloadRoot)
                .setUserName(server.getUsername())
                .setPassword(server.getPassword())
                .setHttpHeaders(httpHeaders)
                .install();
        } else {
            runtimeWork =
            factory.getNodeInstaller(proxyConfig)
                .setNodeVersion(validNodeVersion)
                .setNodeDownloadRoot(nodeDownloadRoot)
                .setNpmVersion(npmVersion)
                .install();
            packageManagerWork =
            factory.getNPMInstaller(proxyConfig)
                .setNodeVersion(validNodeVersion)
                .setNpmVersion(npmVersion)
                .setNpmDownloadRoot(npmDownloadRoot)
                .install();
        }
    }

    private String getNodeDownloadRoot() {
        if (downloadRoot != null && !"".equals(downloadRoot) && nodeDownloadRoot == null) {
            return downloadRoot;
        }
        return nodeDownloadRoot;
    }

    private String getNpmDownloadRoot() {
        if (downloadRoot != null && !"".equals(downloadRoot) && NPMInstaller.DEFAULT_NPM_DOWNLOAD_ROOT.equals(npmDownloadRoot)) {
            return downloadRoot;
        }
        return npmDownloadRoot;
    }
}