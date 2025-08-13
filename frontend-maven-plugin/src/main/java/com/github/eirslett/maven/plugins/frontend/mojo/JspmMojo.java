package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.Goal.JSPM;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.incrementExecutionCount;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionDetector.getNodeVersion;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.getDownloadableVersion;
import static java.util.Objects.isNull;

@Mojo(name="jspm",  defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public class JspmMojo extends AbstractFrontendMojo {

    /**
     * JSPM arguments. Default is "install".
     */
    @Parameter(defaultValue = "install", property = "frontend.bower.arguments", required = false)
    private String arguments;

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
    @Parameter(property = "skip.jspm", defaultValue = "${skip.jspm}")
    private boolean skip;

    @Override
    protected boolean skipExecution() {
        return this.skip;
    }

    @Override
    protected synchronized void execute(FrontendPluginFactory factory) throws Exception {
        incrementExecutionCount(project.getArtifactId(), arguments, JSPM, getFrontendMavenPluginVersion(), false, false, () -> {
            String nodeVersion = getNodeVersion(workingDirectory, this.nodeVersion, this.nodeVersionFile, project.getArtifactId(), getFrontendMavenPluginVersion());

            if (isNull(nodeVersion)) {
                throw new LifecycleExecutionException("Node version could not be detected from a file and was not set");
            }

            if (!NodeVersionHelper.validateVersion(nodeVersion)) {
                throw new LifecycleExecutionException("Node version (" + nodeVersion + ") is not valid. If you think it actually is, raise an issue");
            }

            String validNodeVersion = getDownloadableVersion(nodeVersion);
            factory.loadNodeVersionManager(validNodeVersion);

        factory.getJspmRunner().execute(arguments, environmentVariables);
        });
    }

}
