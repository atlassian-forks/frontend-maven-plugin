package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.Goal.EMBER;
import static com.github.eirslett.maven.plugins.frontend.lib.AtlassianDevMetricsReporter.incrementExecutionCount;
import static com.github.eirslett.maven.plugins.frontend.mojo.MojoUtils.incrementalBuildEnabled;

@Mojo(name="ember", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public final class EmberMojo extends AbstractFrontendMojo {

    /**
     * Grunt arguments. Default is empty (runs just the "grunt" command).
     */
    @Parameter(property = "frontend.ember.arguments")
    private String arguments;

    /**
     * Files that should be checked for changes, in addition to the srcdir files.
     * {@link #workingDirectory}.
     */
    @Parameter(property = "triggerfiles")
    private List<File> triggerfiles;

    /**
     * The directory containing front end files that will be processed by grunt.
     * If this is set then files in the directory will be checked for
     * modifications before running grunt.
     */
    @Parameter(property = "srcdir")
    private File srcdir;

    /**
     * The directory where front end files will be output by grunt. If this is
     * set then they will be refreshed so they correctly show as modified in
     * Eclipse.
     */
    @Parameter(property = "outputdir")
    private File outputdir;

    /**
     * Skips execution of this mojo.
     */
    @Parameter(property = "skip.ember", defaultValue = "${skip.ember}")
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

        incrementExecutionCount(project.getArtifactId(), arguments, EMBER, getFrontendMavenPluginVersion(), incrementalEnabled, !shouldExecute, () -> {

        if (shouldExecute) {
            factory.getEmberRunner().execute(arguments, environmentVariables);

            if (outputdir != null) {
                getLog().info("Refreshing files after ember: " + outputdir);
                buildContext.refresh(outputdir);
            }
        } else {
            getLog().info("Skipping ember as no modified files in " + srcdir);
        }

        });
    }

    private boolean shouldExecute() {

        if (triggerfiles == null || triggerfiles.isEmpty()) {
            triggerfiles = Arrays.asList(new File(workingDirectory, "Gruntfile.js"));
        }

        return MojoUtils.shouldExecute(buildContext, triggerfiles, srcdir);
    }

}
