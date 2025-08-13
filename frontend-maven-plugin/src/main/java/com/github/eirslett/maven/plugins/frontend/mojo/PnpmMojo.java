package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter;
import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper;
import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig;
import com.github.eirslett.maven.plugins.frontend.lib.TaskRunnerException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.util.Collections;

import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.Goal.PNPM;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.incrementExecutionCount;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionDetector.getNodeVersion;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.getDownloadableVersion;
import static com.github.eirslett.maven.plugins.frontend.mojo.MojoUtils.incrementalBuildEnabled;
import static java.util.Objects.isNull;

@Mojo(name="pnpm",  defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class PnpmMojo extends AbstractFrontendMojo {

    private static final String PNPM_REGISTRY_URL = "npmRegistryURL";

    /**
     * pnpm arguments. Default is "install".
     */
    @Parameter(defaultValue = "install", property = "frontend.pnpm.arguments", required = false)
    private String arguments;

    @Parameter(property = "frontend.pnpm.pnpmInheritsProxyConfigFromMaven", required = false, defaultValue = "true")
    private boolean pnpmInheritsProxyConfigFromMaven;

    /**
     * Registry override, passed as the registry option during pnpm install if set.
     */
    @Parameter(property = PNPM_REGISTRY_URL, required = false, defaultValue = "")
    private String pnpmRegistryURL;

    @Parameter(property = "session", defaultValue = "${session}", readonly = true)
    private MavenSession session;

    /**
     * The version of Node.js to install. IMPORTANT! Most Node.js version names start with 'v', for example
     * 'v0.10.18'
     */
    @Parameter(property = "nodeVersion", defaultValue = "", required = false)
    private String nodeVersion;

    /**
     * The path to the file that contains the Node version to use
     */
    @Parameter(property = "nodeVersionFile", defaultValue = "", required = false)
    private String nodeVersionFile;

    @Component
    private BuildContext buildContext;

    @Component(role = SettingsDecrypter.class)
    private SettingsDecrypter decrypter;

    /**
     * Skips execution of this mojo.
     */
    @Parameter(property = "skip.pnpm", defaultValue = "${skip.pnpm}")
    private boolean skip;

    @Override
    protected boolean skipExecution() {
        return this.skip;
    }

    @Override
    public synchronized void execute(FrontendPluginFactory factory) throws Exception {
        File packageJson = new File(workingDirectory, "package.json");

        boolean incrementalEnabled = incrementalBuildEnabled(buildContext);
        boolean willBeIncremental = incrementalEnabled && buildContext.hasDelta(packageJson);

        incrementExecutionCount(project.getArtifactId(), arguments, PNPM, getFrontendMavenPluginVersion(), incrementalEnabled, willBeIncremental, () -> {

        if (!willBeIncremental) {
            String nodeVersion = getNodeVersion(workingDirectory, this.nodeVersion, this.nodeVersionFile, project.getArtifactId(), getFrontendMavenPluginVersion());

            if (isNull(nodeVersion)) {
                throw new LifecycleExecutionException("Node version could not be detected from a file and was not set");
            }

            if (!NodeVersionHelper.validateVersion(nodeVersion)) {
                throw new LifecycleExecutionException("Node version (" + nodeVersion + ") is not valid. If you think it actually is, raise an issue");
            }

            String validNodeVersion = getDownloadableVersion(nodeVersion);
            factory.loadNodeVersionManager(validNodeVersion);

            ProxyConfig proxyConfig = getProxyConfig();
            factory.getPnpmRunner(proxyConfig, getRegistryUrl()).execute(arguments, environmentVariables);
        } else {
            getLog().info("Skipping pnpm install as package.json unchanged");
        }

        });
    }

    private ProxyConfig getProxyConfig() {
        if (pnpmInheritsProxyConfigFromMaven) {
            return MojoUtils.getProxyConfig(session, decrypter);
        } else {
            getLog().info("pnpm not inheriting proxy config from Maven");
            return new ProxyConfig(Collections.<ProxyConfig.Proxy>emptyList());
        }
    }

    private String getRegistryUrl() {
        // check to see if overridden via `-D`, otherwise fallback to pom value
        return System.getProperty(PNPM_REGISTRY_URL, pnpmRegistryURL);
    }
}
