package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper;
import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.crypto.SettingsDecrypter;

import java.util.Collections;

import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.Goal.BOWER;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.incrementExecutionCount;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionDetector.getNodeVersion;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.getDownloadableVersion;
import static java.util.Objects.isNull;

@Mojo(name = "bower", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class BowerMojo extends AbstractFrontendMojo {

    /**
     * Bower arguments. Default is "install".
     */
    @Parameter(defaultValue = "install", property = "frontend.bower.arguments", required = false)
    private String arguments;

    /**
     * Skips execution of this mojo.
     */
    @Parameter(property = "skip.bower", defaultValue = "${skip.bower}")
    private boolean skip;

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

    @Parameter(property = "frontend.bower.bowerInheritsProxyConfigFromMaven", required = false, defaultValue = "true")
    private boolean bowerInheritsProxyConfigFromMaven;

    @Component(role = SettingsDecrypter.class)
    private SettingsDecrypter decrypter;

    @Override
    protected boolean skipExecution() {
        return this.skip;
    }

    @Override
    protected synchronized void execute(FrontendPluginFactory factory) throws Exception {
        ProxyConfig proxyConfig = getProxyConfig();
        incrementExecutionCount(project.getArtifactId(), arguments, BOWER, getFrontendMavenPluginVersion(), false, false, () -> {
            String nodeVersion = getNodeVersion(workingDirectory, this.nodeVersion, this.nodeVersionFile, project.getArtifactId(), getFrontendMavenPluginVersion());

            if (isNull(nodeVersion)) {
                throw new LifecycleExecutionException("Node version could not be detected from a file and was not set");
            }

            if (!NodeVersionHelper.validateVersion(nodeVersion)) {
                throw new LifecycleExecutionException("Node version (" + nodeVersion + ") is not valid. If you think it actually is, raise an issue");
            }

            String validNodeVersion = getDownloadableVersion(nodeVersion);
            factory.loadNodeVersionManager(validNodeVersion);
            factory.getBowerRunner(proxyConfig).execute(arguments, environmentVariables);
        });
    }

    private ProxyConfig getProxyConfig() {
        if (bowerInheritsProxyConfigFromMaven) {
            return MojoUtils.getProxyConfig(session, decrypter);
        } else {
            getLog().info("bower not inheriting proxy config from Maven");
            return new ProxyConfig(Collections.<ProxyConfig.Proxy>emptyList());
        }
    }

}
