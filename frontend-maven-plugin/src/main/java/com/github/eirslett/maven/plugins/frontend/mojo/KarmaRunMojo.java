package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.Goal.KARMA;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.incrementExecutionCount;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionDetector.getNodeVersion;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.getDownloadableVersion;
import static java.util.Objects.isNull;


@Mojo(name="karma",  defaultPhase = LifecyclePhase.TEST, threadSafe = true)
public final class KarmaRunMojo extends AbstractFrontendMojo {

    /**
     * Path to your karma configuration file, relative to the working directory (default is "karma.conf.js")
     */
    @Parameter(defaultValue = "karma.conf.js", property = "karmaConfPath")
    private String karmaConfPath;

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

    /**
     * Skips execution of this mojo.
     */
    @Parameter(property = "skip.karma", defaultValue = "${skip.karma}")
    private boolean skip;

    @Override
    protected boolean skipExecution() {
        return this.skip;
    }

    @Override
    public synchronized void execute(FrontendPluginFactory factory) throws Exception {
        incrementExecutionCount(project.getArtifactId(), karmaConfPath, KARMA, getFrontendMavenPluginVersion(), false, false, () -> {
            String nodeVersion = getNodeVersion(workingDirectory, this.nodeVersion, this.nodeVersionFile, project.getArtifactId(), getFrontendMavenPluginVersion());

            if (isNull(nodeVersion)) {
                throw new LifecycleExecutionException("Node version could not be detected from a file and was not set");
            }

            if (!NodeVersionHelper.validateVersion(nodeVersion)) {
                throw new LifecycleExecutionException("Node version (" + nodeVersion + ") is not valid. If you think it actually is, raise an issue");
            }

            String validNodeVersion = getDownloadableVersion(nodeVersion);
            factory.loadNodeVersionManager(validNodeVersion);
        factory.getKarmaRunner().execute("start " + karmaConfPath, environmentVariables);
        });
    }
}
