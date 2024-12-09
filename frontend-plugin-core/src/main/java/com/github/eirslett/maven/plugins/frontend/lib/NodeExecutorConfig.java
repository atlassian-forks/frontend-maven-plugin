package com.github.eirslett.maven.plugins.frontend.lib;

import com.github.eirslett.maven.plugins.frontend.lib.version.manager.VersionManagerCache;

import java.io.File;
import java.nio.file.Paths;

import static java.util.Objects.isNull;

public interface NodeExecutorConfig {
  File getNodePath();
  File getNpmPath();
  File getPnpmPath();
  File getPnpmCjsPath();
  File getCorepackPath();

  File getNpxPath();
  File getInstallDirectory();
  File getWorkingDirectory();
  Platform getPlatform();
  boolean hasProvidedNode();
}

final class InstallNodeExecutorConfig implements NodeExecutorConfig {

  private static final String NODE_WINDOWS = NodeInstaller.INSTALL_PATH.replaceAll("/", "\\\\") + "\\node.exe";
  private static final String NODE_DEFAULT = NodeInstaller.INSTALL_PATH + "/node";
  private static final String NPM = NodeInstaller.INSTALL_PATH + "/node_modules/npm/bin/npm-cli.js";
  private static final String PNPM = NodeInstaller.INSTALL_PATH + "/node_modules/pnpm/bin/pnpm.js";
  private static final String PNPM_CJS = NodeInstaller.INSTALL_PATH + "/node_modules/pnpm/bin/pnpm.cjs";
  private static final String COREPACK = NodeInstaller.INSTALL_PATH + "/node_modules/corepack/dist/corepack.js";
  private static final String NPX = NodeInstaller.INSTALL_PATH + "/node_modules/npm/bin/npx-cli.js";

  private final InstallConfig installConfig;

  private final VersionManagerCache versionManagerCache;

  public InstallNodeExecutorConfig(InstallConfig installConfig) {
    this(installConfig, null);
  }

  public InstallNodeExecutorConfig(InstallConfig installConfig, VersionManagerCache versionManagerCache) {
    this.installConfig = installConfig;
    this.versionManagerCache = versionManagerCache;
  }

  @Override
  public File getNodePath() {
    if (hasProvidedNode()) return getInstalledNodeExecutable();
    if (versionManagerCache != null && versionManagerCache.isNodeAvailable()) return versionManagerCache.getNodeExecutable();

    String nodeExecutable = getPlatform().isWindows() ? NODE_WINDOWS : NODE_DEFAULT;
    return new File(installConfig.getInstallDirectory() + nodeExecutable);
  }

  @Override
  public File getNpmPath() {
    if (hasProvidedNode()) return getInstalledNpmExecutable();
    if (versionManagerCache != null && versionManagerCache.isNodeAvailable()) return versionManagerCache.getNpmExecutable();

    return new File(installConfig.getInstallDirectory() + Utils.normalize(NPM));
  }


  @Override
  public File getPnpmPath() {
    return new File(installConfig.getInstallDirectory() + Utils.normalize(PNPM));
  }

  @Override
  public File getPnpmCjsPath() {
    return new File(installConfig.getInstallDirectory() + Utils.normalize(PNPM_CJS));
  }

  @Override
  public File getCorepackPath() {
    return new File(installConfig.getInstallDirectory() + Utils.normalize(COREPACK));
  }

  @Override
  public File getNpxPath() {
    return new File(installConfig.getInstallDirectory() + Utils.normalize(NPX));
  }

  @Override
  public File getInstallDirectory() {
    return installConfig.getInstallDirectory();
  }
  
  @Override
  public File getWorkingDirectory() {
    return installConfig.getWorkingDirectory();
  }

  @Override
  public Platform getPlatform() {
    return installConfig.getPlatform();
  }

  private File getProvidedNodeDirectory() {
    File configuredNodeDirectory = installConfig.getInstalledNodeDirectory();
    if (!isNull(configuredNodeDirectory) && configuredNodeDirectory.exists()) return configuredNodeDirectory;

    String systemNodeDirectoryPath = System.getenv("AFMP_INSTALLED_NODE_DIRECTORY");
    File systemNodeDirectory = new File(systemNodeDirectoryPath);
    if (systemNodeDirectory.exists()) return systemNodeDirectory;

    return null;
  }

  private File getInstalledNodeExecutable() {
    File nodeDirectory = getProvidedNodeDirectory();
    if (getPlatform().isWindows()) {
      return new File(nodeDirectory, "node.exe");
    }
    return new File(nodeDirectory, "node");
  }

  @Override
  public boolean hasProvidedNode() {
    File nodeExecutable = getProvidedNodeDirectory();
    return !isNull(nodeExecutable) && nodeExecutable.exists();
  }

  private File getInstalledNpmExecutable() {
    File nodeDirectory = getProvidedNodeDirectory();
    File npmCli = new File(nodeDirectory, "npm");
    if (npmCli.exists()) return npmCli;

    npmCli = Paths.get(nodeDirectory.getParent(), "lib", "/node_modules/npm/bin/npm-cli.js").toFile();
    if (npmCli.exists()) return npmCli;

    throw new RuntimeException("Npm cli couldn't be found for provided node directory.");
  }
}
