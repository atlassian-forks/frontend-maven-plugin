package com.github.eirslett.maven.plugins.frontend.lib;

import java.io.File;

public interface InstallConfig {
  File getInstallDirectory();
  void setInstallDirectory(File installDirectory);
  File getWorkingDirectory();
  CacheResolver getCacheResolver();
  Platform getPlatform();
  boolean isUseNodeVersionManager();
  File getInstalledNodeDirectory();
}

final class DefaultInstallConfig implements InstallConfig {

  private File installDirectory;
  private final File workingDirectory;
  private final CacheResolver cacheResolver;
  private final Platform platform;
  private final boolean useNodeVersionManager;
  private final File installedNodeDirectory;
  
  public DefaultInstallConfig(File installDirectory,
                              File workingDirectory,
                              CacheResolver cacheResolver,
                              Platform platform,
                              boolean useNodeVersionManager,
                              File installedNodeDirectory) {
    this.installDirectory = installDirectory;
    this.workingDirectory = workingDirectory;
    this.cacheResolver = cacheResolver;
    this.platform = platform;
    this.useNodeVersionManager = useNodeVersionManager;
    this.installedNodeDirectory = installedNodeDirectory;
  }

  @Override
  public File getInstallDirectory() {
    return this.installDirectory;
  }

  @Override
  public void setInstallDirectory(File installDirectory) {
    this.installDirectory = installDirectory;
  }

  @Override
  public File getWorkingDirectory() {
    return this.workingDirectory;
  }
  
  public CacheResolver getCacheResolver() {
    return cacheResolver;
  }

  @Override
  public Platform getPlatform() {
    return this.platform;
  }

  @Override
  public boolean isUseNodeVersionManager() {
    return useNodeVersionManager;
  }

  @Override
  public File getInstalledNodeDirectory() {
    return this.installedNodeDirectory;
  }
}