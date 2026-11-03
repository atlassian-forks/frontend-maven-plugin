package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.CacheDescriptor;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.internal.impl.DefaultLocalPathComposer;
import org.eclipse.aether.internal.impl.DefaultLocalPathPrefixComposerFactory;
import org.eclipse.aether.internal.impl.DefaultTrackingFileManager;
import org.eclipse.aether.internal.impl.EnhancedLocalRepositoryManagerFactory;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.util.repository.ChainedLocalRepositoryManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepositoryCacheResolverTest {

  private static final CacheDescriptor NODE = new CacheDescriptor("node", "v18.16.0", "darwin-arm64", "tar.gz");
  private static final String NODE_PATH = "com/github/eirslett/node/18.16.0/node-18.16.0-darwin-arm64.tar.gz";

  @TempDir
  Path temp;

  @Test
  public void returnsPrimaryPathWhenArchiveIsMissing() throws Exception {
    Path primary = temp.resolve("primary");

    File resolved = resolverFor(manager(primary)).resolve(NODE);

    assertEquals(primary.resolve(NODE_PATH).toFile(), resolved);
  }

  @Test
  public void returnsPrimaryArchiveWhenPresent() throws Exception {
    Path primary = temp.resolve("primary");
    Path tail = temp.resolve("tail");
    createArchive(primary);
    createArchive(tail);

    File resolved = resolverFor(chained(primary, tail)).resolve(NODE);

    assertEquals(primary.resolve(NODE_PATH).toFile(), resolved);
  }

  @Test
  public void returnsTailArchiveWhenOnlyTailHasIt() throws Exception {
    Path primary = temp.resolve("primary");
    Path tail = temp.resolve("tail");
    createArchive(tail);

    File resolved = resolverFor(chained(primary, tail)).resolve(NODE);

    assertEquals(tail.resolve(NODE_PATH).toFile(), resolved);
  }

  @Test
  public void returnsPrimaryPathWhenArchiveIsInNeitherRepository() throws Exception {
    Path primary = temp.resolve("primary");
    Path tail = temp.resolve("tail");

    File resolved = resolverFor(chained(primary, tail)).resolve(NODE);

    assertEquals(primary.resolve(NODE_PATH).toFile(), resolved);
  }

  @Test
  public void resolvesArtifactWithoutClassifier() throws Exception {
    Path primary = temp.resolve("primary");
    Path tail = temp.resolve("tail");
    String npmPath = "com/github/eirslett/npm/9.5.1/npm-9.5.1.tar.gz";
    Files.createDirectories(tail.resolve(npmPath).getParent());
    Files.createFile(tail.resolve(npmPath));

    File resolved = resolverFor(chained(primary, tail)).resolve(new CacheDescriptor("npm", "9.5.1", "tar.gz"));

    assertEquals(tail.resolve(npmPath).toFile(), resolved);
  }

  @Test
  public void returnsTailArchiveWithMavensDefaultLocalRepositoryManager() throws Exception {
    Path primary = temp.resolve("primary");
    Path tail = temp.resolve("tail");
    createArchive(tail);
    LocalRepositoryManager chained =
        new ChainedLocalRepositoryManager(enhancedManager(primary), singletonList(enhancedManager(tail)), true);

    File resolved = resolverFor(chained).resolve(NODE);

    assertEquals(tail.resolve(NODE_PATH).toFile(), resolved);
  }

  @Test
  public void returnsPrimaryPathWithMavensDefaultLocalRepositoryManagerAndNoTail() throws Exception {
    Path primary = temp.resolve("primary");

    File resolved = resolverFor(enhancedManager(primary)).resolve(NODE);

    assertEquals(primary.resolve(NODE_PATH).toFile(), resolved);
  }

  private static void createArchive(Path repository) throws IOException {
    Path archive = repository.resolve(NODE_PATH);
    Files.createDirectories(archive.getParent());
    Files.createFile(archive);
  }

  private static LocalRepositoryManager manager(Path basedir) throws NoLocalRepositoryManagerException {
    return new SimpleLocalRepositoryManagerFactory()
        .newInstance(new DefaultRepositorySystemSession(), new LocalRepository(basedir.toFile()));
  }

  /** The local repository manager Maven 3.9 uses by default. */
  private static LocalRepositoryManager enhancedManager(Path basedir) throws NoLocalRepositoryManagerException {
    return new EnhancedLocalRepositoryManagerFactory(
        new DefaultLocalPathComposer(), new DefaultTrackingFileManager(), new DefaultLocalPathPrefixComposerFactory())
        .newInstance(new DefaultRepositorySystemSession(), new LocalRepository(basedir.toFile()));
  }

  private static LocalRepositoryManager chained(Path primary, Path tail) throws NoLocalRepositoryManagerException {
    // true is Maven's default for maven.repo.local.tail.ignoreAvailability.
    return new ChainedLocalRepositoryManager(manager(primary), singletonList(manager(tail)), true);
  }

  private static RepositoryCacheResolver resolverFor(LocalRepositoryManager manager) {
    DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
    session.setLocalRepositoryManager(manager);
    return new RepositoryCacheResolver(session);
  }
}
