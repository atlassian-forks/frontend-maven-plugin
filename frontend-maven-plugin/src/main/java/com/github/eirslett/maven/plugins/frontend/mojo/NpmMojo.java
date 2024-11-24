package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.IncrementalBuildExecutionDigest.ExecutionCoordinates;
import com.github.eirslett.maven.plugins.frontend.lib.IncrementalMojoHelper;
import com.github.eirslett.maven.plugins.frontend.lib.NpmRunner;
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

@Mojo(name="npm",  defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class NpmMojo extends AbstractFrontendMojo {

    private static final String NPM_REGISTRY_URL = "npmRegistryURL";

    /**
     * npm arguments. Default is "install".
     */
    @Parameter(defaultValue = "install", property = "frontend.npm.arguments", required = false)
    private String arguments;

    @Parameter(defaultValue = "", property = "frontend.npm.incremental", required = false)
    private String incremental;

    @Parameter(property = "frontend.npm.npmInheritsProxyConfigFromMaven", required = false, defaultValue = "true")
    private boolean npmInheritsProxyConfigFromMaven;

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
    @Parameter(property = "skip.npm", defaultValue = "${skip.npm}")
    private boolean skip;

    @Override
    protected boolean skipExecution() {
        return this.skip;
    }

    @Override
    public synchronized void execute(FrontendPluginFactory factory) throws TaskRunnerException {
        IncrementalMojoHelper incrementalHelper = new IncrementalMojoHelper(incremental, workingDirectory);

        ProxyConfig proxyConfig = getProxyConfig();
        NpmRunner runner = factory.getNpmRunner(proxyConfig, getRegistryUrl());

        ExecutionCoordinates coordinates = new ExecutionCoordinates(execution.getGoal(), execution.getExecutionId(), execution.getLifecyclePhase());
        if (incrementalHelper.shouldExecute(arguments, coordinates, runner.getRuntime(), environmentVariables)) {
            File packageJson = new File(workingDirectory, "package.json");
            if (buildContext == null || buildContext.hasDelta(packageJson) || !buildContext.isIncremental()) {
                runner.execute(arguments, environmentVariables);

                incrementalHelper.acceptIncrementalBuildDigest();
            } else {
                getLog().info("Skipping npm install as package.json unchanged");
            }
        } else {
            getLog().info("Skipping npm execution as no modified files in" + workingDirectory);
        }
    }

    private ProxyConfig getProxyConfig() {
        if (npmInheritsProxyConfigFromMaven) {
            return MojoUtils.getProxyConfig(session, decrypter);
        } else {
            getLog().info("npm not inheriting proxy config from Maven");
            return new ProxyConfig(Collections.<ProxyConfig.Proxy>emptyList());
        }
    }

    private String getRegistryUrl() {
        // check to see if overridden via `-D`, otherwise fallback to pom value
        return System.getProperty(NPM_REGISTRY_URL, npmRegistryURL);
    }
}
