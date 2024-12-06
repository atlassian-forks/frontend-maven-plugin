package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.IncrementalBuildExecutionDigest.ExecutionCoordinates;
import com.github.eirslett.maven.plugins.frontend.lib.IncrementalMojoHelper;
import com.github.eirslett.maven.plugins.frontend.lib.NpmRunner;
import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionDetector;
import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.Goal.NPM;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.incrementExecutionCount;
import static com.github.eirslett.maven.plugins.frontend.lib.IncrementalMojoHelper.DEFAULT_EXCLUDED_FILENAMES;

@Mojo(name="npm",  defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class NpmMojo extends AbstractNodeMojo {

    private static final String NPM_REGISTRY_URL = "npmRegistryURL";

    /**
     * npm arguments. Default is "install".
     */
    @Parameter(defaultValue = "install", property = "frontend.npm.arguments", required = false)
    private String arguments;

    /**
     * Enable or disable incremental builds, on by default
     */
    @Parameter(defaultValue = "true", property = "frontend.incremental", required = false)
    private String frontendIncremental;

    @Parameter(property = "frontend.npm.npmInheritsProxyConfigFromMaven", required = false, defaultValue = "true")
    private boolean npmInheritsProxyConfigFromMaven;

    /**
     * Registry override, passed as the registry option during npm install if set.
     */
    @Parameter(property = NPM_REGISTRY_URL, required = false, defaultValue = "")
    private String npmRegistryURL;

    @Parameter(property = "session", defaultValue = "${session}", readonly = true)
    private MavenSession session;

    /**
     * Files that should be checked for changes for incremental builds in addition
     * to the defaults in {@link IncrementalMojoHelper}. Directories will be searched.
     */
    @Parameter(property = "triggerFiles", required = false)
    private Set<File> triggerFiles;

    /**
     * Files that should NOT be checked for changes for incremental builds in addition
     * to the defaults in {@link IncrementalMojoHelper}. Whole directories will be
     * excluded.
     */
    @Parameter(property = "excludedFilenames", defaultValue = DEFAULT_EXCLUDED_FILENAMES, required = false)
    private Set<String> excludedFilenames;

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
    public synchronized void executeWithVerifiedNodeVersion(FrontendPluginFactory factory, String nodeVersion) throws Exception {
        NpmRunner runner = factory.getNpmRunner(getProxyConfig(), getRegistryUrl());

        ExecutionCoordinates coordinates = new ExecutionCoordinates(execution.getGoal(), execution.getExecutionId(), execution.getLifecyclePhase());
        IncrementalMojoHelper incrementalHelper = new IncrementalMojoHelper(frontendIncremental, coordinates, getTargetDir(), workingDirectory, triggerFiles, excludedFilenames);

        boolean incrementalEnabled = incrementalHelper.incrementalEnabled();
        boolean isIncremental = incrementalEnabled && incrementalHelper.canBeSkipped(arguments, runner.getRuntime(), environmentVariables, project.getArtifactId(), getFrontendMavenPluginVersion());

        incrementExecutionCount(project.getArtifactId(), arguments, NPM, getFrontendMavenPluginVersion(), incrementalEnabled, isIncremental, () -> {
            if (isIncremental) {
                getLog().info("Skipping npm execution as no modified files in " + workingDirectory);
            } else {
                runner.execute(arguments, environmentVariables);

                incrementalHelper.acceptIncrementalBuildDigest();
            }
        });
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
