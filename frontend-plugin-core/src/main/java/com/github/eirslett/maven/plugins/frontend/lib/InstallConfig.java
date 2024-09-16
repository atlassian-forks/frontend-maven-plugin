package com.github.eirslett.maven.plugins.frontend.lib;

import java.io.File;

public interface InstallConfig {
  File getInstallDirectory();
  void setInstallDirectory(File installDirectory);
  File getWorkingDirectory();
  CacheResolver getCacheResolver();
  Platform getPlatform();
  boolean isUseNodeVersionManager();
}

final class DefaultInstallConfig implements InstallConfig {

  private File installDirectory;
  private final File workingDirectory;
  private final CacheResolver cacheResolver;
  private final Platform platform;
  private final boolean useNodeVersionManager;
  
  public DefaultInstallConfig(File installDirectory,
                              File workingDirectory,
                              CacheResolver cacheResolver,
                              Platform platform,
                              boolean useNodeVersionManager) {
    this.installDirectory = installDirectory;
    this.workingDirectory = workingDirectory;
    this.cacheResolver = cacheResolver;
    this.platform = platform;
    this.useNodeVersionManager = useNodeVersionManager;
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

  public boolean isUseNodeVersionManager() {
    return useNodeVersionManager;
  }
}