package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.Goal.GULP;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.incrementExecutionCount;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionDetector.getNodeVersion;
import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.getDownloadableVersion;
import static com.github.eirslett.maven.plugins.frontend.mojo.MojoUtils.incrementalBuildEnabled;
import static java.util.Objects.isNull;

@Mojo(name="gulp", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class GulpMojo extends AbstractFrontendMojo {

    /**
     * Gulp arguments. Default is empty (runs just the "gulp" command).
     */
    @Parameter(property = "frontend.gulp.arguments")
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
     * Files that should be checked for changes, in addition to the srcdir files.
     * Defaults to gulpfile.js in the {@link #workingDirectory}.
     */
    @Parameter(property = "triggerfiles")
    private List<File> triggerfiles;

    /**
     * The directory containing front end files that will be processed by gulp.
     * If this is set then files in the directory will be checked for
     * modifications before running gulp.
     */
    @Parameter(property = "srcdir")
    private File srcdir;

    /**
     * The directory where front end files will be output by gulp. If this is
     * set then they will be refreshed so they correctly show as modified in
     * Eclipse.
     */
    @Parameter(property = "outputdir")
    private File outputdir;

    /**
     * Skips execution of this mojo.
     */
    @Parameter(property = "skip.gulp", defaultValue = "${skip.gulp}")
    private boolean skip;

    @Component
    private BuildContext buildContext;

    @Override
    protected boolean skipExecution() {
        return this.skip;
    }

    @Override
    public synchronized void execute(FrontendPluginFactory factory) throws Exception {
        boolean incrementalEnabled = incrementalBuildEnabled(buildContext);
        boolean shouldExecute = shouldExecute();

        incrementExecutionCount(project.getArtifactId(), arguments, GULP, getFrontendMavenPluginVersion(), incrementalEnabled, !shouldExecute, () -> {

        if (shouldExecute) {
            String nodeVersion = getNodeVersion(workingDirectory, this.nodeVersion, this.nodeVersionFile, project.getArtifactId(), getFrontendMavenPluginVersion());

            if (isNull(nodeVersion)) {
                throw new LifecycleExecutionException("Node version could not be detected from a file and was not set");
            }

            if (!NodeVersionHelper.validateVersion(nodeVersion)) {
                throw new LifecycleExecutionException("Node version (" + nodeVersion + ") is not valid. If you think it actually is, raise an issue");
            }

            String validNodeVersion = getDownloadableVersion(nodeVersion);
            factory.loadNodeVersionManager(validNodeVersion);

            factory.getGulpRunner().execute(arguments, environmentVariables);

            if (outputdir != null) {
                getLog().info("Refreshing files after gulp: " + outputdir);
                buildContext.refresh(outputdir);
            }
        } else {
            getLog().info("Skipping gulp as no modified files in " + srcdir);
        }

        });
    }

    private boolean shouldExecute() {
        if (triggerfiles == null || triggerfiles.isEmpty()) {
            triggerfiles = Arrays.asList(new File(workingDirectory, "gulpfile.js"));
        }

        return MojoUtils.shouldExecute(buildContext, triggerfiles, srcdir);
    }

}
