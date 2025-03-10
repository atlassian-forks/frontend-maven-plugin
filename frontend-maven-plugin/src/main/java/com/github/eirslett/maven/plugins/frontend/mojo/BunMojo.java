package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig;
import com.github.eirslett.maven.plugins.frontend.lib.TaskRunnerException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.util.Collections;

import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.Goal.BUN;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.incrementExecutionCount;
import static com.github.eirslett.maven.plugins.frontend.mojo.MojoUtils.incrementalBuildEnabled;

@Mojo(name = "bun", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class BunMojo extends AbstractFrontendMojo {

    private static final String NPM_REGISTRY_URL = "npmRegistryURL";

    /**
     * bun arguments. Default is "install".
     */
    @Parameter(defaultValue = "", property = "frontend.bun.arguments", required = false)
    private String arguments;

    @Parameter(property = "frontend.bun.bunInheritsProxyConfigFromMaven", required = false,
            defaultValue = "true")
    private boolean bunInheritsProxyConfigFromMaven;

    /**
     * Registry override, passed as the registry option during npm install if set.
     */
    @Parameter(property = NPM_REGISTRY_URL, required = false, defaultValue = "")
    private String npmRegistryURL;

    @Parameter(property = "session", defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Component
    private BuildContext buildContext;

    @Component(role = SettingsDecrypter.class)
    private SettingsDecrypter decrypter;

    /**
     * Skips execution of this mojo.
     */
    @Parameter(property = "skip.bun", defaultValue = "${skip.bun}")
    private boolean skip;

    /**
     * Node Version Manager usage disabled for Bun execution
     */
    @Parameter(property = "useNodeVersionManager", readonly = true, defaultValue = "false")
    private boolean useNodeVersionManager;

    @Override
    protected boolean skipExecution() {
        return this.skip;
    }

    @Override
    public synchronized void execute(FrontendPluginFactory factory) throws Exception {
        File packageJson = new File(this.workingDirectory, "package.json");

        boolean incrementalEnabled = incrementalBuildEnabled(buildContext);
        boolean willBeIncremental = incrementalEnabled && buildContext.hasDelta(packageJson);

        incrementExecutionCount(project.getArtifactId(), arguments, BUN, getFrontendMavenPluginVersion(), incrementalEnabled, willBeIncremental, () -> {

        if (!willBeIncremental) {
            ProxyConfig proxyConfig = getProxyConfig();
            factory.getBunRunner(proxyConfig, getRegistryUrl()).execute(this.arguments,
                    this.environmentVariables);
        } else {
            getLog().info("Skipping bun install as package.json unchanged");
        }

        });
    }

    private ProxyConfig getProxyConfig() {
        if (this.bunInheritsProxyConfigFromMaven) {
            return MojoUtils.getProxyConfig(this.session, this.decrypter);
        } else {
            getLog().info("bun not inheriting proxy config from Maven");
            return new ProxyConfig(Collections.<ProxyConfig.Proxy>emptyList());
        }
    }

    private String getRegistryUrl() {
        // check to see if overridden via `-D`, otherwise fallback to pom value
        return System.getProperty(NPM_REGISTRY_URL, this.npmRegistryURL);
    }
}
