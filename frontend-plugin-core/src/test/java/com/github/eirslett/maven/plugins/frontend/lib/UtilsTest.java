package com.github.eirslett.maven.plugins.frontend.lib;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class UtilsTest {

  @Test
  public void testImplode() {
    final String separator = "Bar";
    final List<String> elements = new ArrayList<>();

    assertEquals("", Utils.implode(separator, elements));

    elements.add("foo");
    elements.add("bar");
    assertEquals("foo bar", Utils.implode(separator, elements));
  }

  @Test
  public void testIsRelative() {
    assertTrue(Utils.isRelative("foo/bar"));

    assertFalse(Utils.isRelative("/foo/bar"));
    assertFalse(Utils.isRelative("file:foo/bar"));
    assertFalse(Utils.isRelative("C:\\SYSTEM"));
  }

  @Test
  public void testMerge() {
    assertEquals(
            Arrays.asList("foo", "bar"),
            Utils.merge(singletonList("foo"), singletonList("bar"))
    );
  }

  @Test
  public void testPrepend() {
    assertEquals(
            Arrays.asList("foo", "bar"),
            Utils.prepend("foo", singletonList("bar"))
    );
  }

  @Test
  public void deleteCorruptedArchiveDeletesWithoutWarning(@TempDir Path temp) throws IOException {
    File archive = Files.createFile(temp.resolve("node.tar.gz")).toFile();
    List<String> warnings = new ArrayList<>();

    assertTrue(Utils.deleteCorruptedArchive(archive, recordingLogger(warnings)));

    assertFalse(archive.exists());
    assertEquals(0, warnings.size());
  }

  @Test
  public void deleteCorruptedArchiveWarnsWhenArchiveCannotBeDeleted(@TempDir Path temp) throws IOException {
    File readOnlyRepository = Files.createDirectory(temp.resolve("tail")).toFile();
    File archive = Files.createFile(readOnlyRepository.toPath().resolve("node.tar.gz")).toFile();
    boolean madeReadOnly = readOnlyRepository.setWritable(false);
    try {
      // Windows and root ignore a read-only directory, so the delete cannot be made to fail there.
      assumeTrue(madeReadOnly && !Files.isWritable(readOnlyRepository.toPath()), "directory is still writable");
      List<String> warnings = new ArrayList<>();

      assertFalse(Utils.deleteCorruptedArchive(archive, recordingLogger(warnings)));

      assertTrue(archive.exists());
      assertEquals(singletonList(archive.getPath()), warnings);
    } finally {
      readOnlyRepository.setWritable(true);
    }
  }

  /** A logger that records the first argument of each warn(String, Object) call. */
  private static Logger recordingLogger(List<String> warnings) {
    return (Logger) Proxy.newProxyInstance(Logger.class.getClassLoader(), new Class<?>[] {Logger.class},
        (proxy, method, args) -> {
          if ("warn".equals(method.getName()) && args != null && args.length == 2) {
            warnings.add(String.valueOf(args[1]));
          }
          return method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        });
  }
}
