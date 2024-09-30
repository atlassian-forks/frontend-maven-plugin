package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionDetector;
import com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.eirslett.maven.plugins.frontend.lib.NodeVersionHelper.getDownloadableVersion;
import static java.util.Objects.isNull;

public abstract class AbstractInstallNodeMojo extends AbstractFrontendMojo {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractInstallNodeMojo.class);

    /**
     * Where to download Node.js binary from. Defaults to https://nodejs.org/dist/
     */
    @Parameter(property = "nodeDownloadRoot", required = false)
    protected String nodeDownloadRoot;

    /**
     * The version of Node.js to install. IMPORTANT! Most Node.js version names start with 'v', for example 'v0.10.18'
     * If using with node version manager (enabled by default), nodeVersion parameter will be ignored
     */
    @Parameter(property = "nodeVersion", defaultValue = "", required = false)
    protected String nodeVersion;

    /**
     * The path to the file that contains the Node version to use
     */
    @Parameter(property = "nodeVersionFile", defaultValue = "", required = false)
    protected String nodeVersionFile;

    @Override
    protected void execute(FrontendPluginFactory factory) throws Exception {
        verifyAndResolveNodeVersion(factory);
        executeWithVerifiedNodeVersion(factory);
    }

    protected abstract void executeWithVerifiedNodeVersion(FrontendPluginFactory factory) throws Exception;

    private void verifyAndResolveNodeVersion(FrontendPluginFactory factory) throws Exception {
        if (factory.isVersionManagerAvailable()) {
            if (this.nodeVersion != null && !this.nodeVersion.isEmpty()) {
                logger.warn("`nodeVersion` has been configured to {} but will be ignored when installing with node version manager." +
                    " Version Manager will load the version from their version file (e.g. .nvmrc, .tool-versions)", this.nodeVersion);
            }
        }

        String nodeVersion = NodeVersionDetector.getNodeVersion(this.workingDirectory, this.nodeVersion, this.nodeVersionFile);

        if (isNull(nodeVersion)) {
            throw new LifecycleExecutionException("Node version could not be detected from a file and was not set");
        }

        if (!NodeVersionHelper.validateVersion(nodeVersion)) {
            throw new LifecycleExecutionException("Node version (" + nodeVersion + ") is not valid. If you think it actually is, raise an issue");
        }

        String validNodeVersion = getDownloadableVersion(nodeVersion);
        logger.info("Resolved Node version: {}", validNodeVersion);

        this.nodeVersion = validNodeVersion;
    }
}
